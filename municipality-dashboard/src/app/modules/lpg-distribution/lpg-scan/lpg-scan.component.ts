import { Component, OnInit, OnDestroy } from '@angular/core';
import { LpgCardService } from '../../../services/lpg-card.service';
import { LanguageService } from 'src/app/services/language.service';

declare const Html5Qrcode: any;

@Component({
  selector: 'app-lpg-scan',
  templateUrl: './lpg-scan.component.html',
  styleUrls: ['./lpg-scan.component.css']
})
export class LpgScanComponent implements OnInit, OnDestroy {

  // ── Session setup ──────────────────────────────────────────
  dealerName       = '';
  dealerCode       = '';
  ward             = '';
  cycleMonth       = new Date().toISOString().slice(0, 7);
  cylindersPerCard = 1;

  sessionOpen  = false;
  startLoading = false;
  setupError   = '';

  // ── Scanner state ──────────────────────────────────────────
  scanMode      = 'qr';       // 'qr' | 'manual'
  scannerActive = false;
  manualCardNo  = '';
  scanning      = false;
  lastResult: any = null;
  nowTime       = '';

  // ── Session stats ──────────────────────────────────────────
  sessionLogs: any[]  = [];
  totalDistributed    = 0;
  get totalCylinders(): number {
    return this.sessionLogs.reduce((s, l) => s + (l.cylindersQty || 0), 0);
  }

  // ── Stock info (loaded on session start) ───────────────────
  stockInfo: any = null;

  private html5Qrcode: any = null;

  constructor(public ls: LanguageService, private svc: LpgCardService) {}

  ngOnInit(): void {}
  ngOnDestroy(): void { this.stopScanner(); }

  // ══════════════════════════════════════════════════════════
  // SESSION
  // ══════════════════════════════════════════════════════════

  startSession(): void {
    if (!this.dealerName.trim() || !this.ward.trim() || !this.cycleMonth) {
      this.setupError = 'ডিলারের নাম, ওয়ার্ড ও চক্র আবশ্যক।';
      return;
    }
    this.setupError   = '';
    this.startLoading = true;

    // Load stock info for this cycle
    this.svc.getStockList(this.cycleMonth).subscribe({
      next: (stocks: any[]) => {
        // ward-specific stock অথবা সাম্প্রতিক stock
        const found = stocks.find(s => s.ward === this.ward) || stocks[0] || null;
        this.stockInfo   = found;
        this.sessionOpen = true;
        this.startLoading = false;
      },
      error: () => {
        // stock না থাকলেও session চালু হবে — warning পরে দেখাবে
        this.stockInfo   = null;
        this.sessionOpen = true;
        this.startLoading = false;
      }
    });
  }

  closeSession(): void {
    if (!confirm('Session বন্ধ করবেন?')) return;
    this.stopScanner();
    this.sessionOpen = false;
    this.lastResult  = null;
    this.sessionLogs = [];
  }

  // ══════════════════════════════════════════════════════════
  // SCANNER
  // ══════════════════════════════════════════════════════════

  switchMode(mode: string): void {
    this.scanMode   = mode;
    this.lastResult = null;
    if (mode !== 'qr') this.stopScanner();
  }

  startScanner(): void {
    if (this.scannerActive) return;
    this.html5Qrcode = new Html5Qrcode('lpg-qr-reader');
    this.html5Qrcode.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 220, height: 220 } },
      (decoded: string) => this.onScanSuccess(decoded),
      () => {}
    ).then(() => { this.scannerActive = true; })
     .catch(() => { this.setupError = 'ক্যামেরা চালু হয়নি। Permission দিন।'; });
  }

  stopScanner(): void {
    if (this.html5Qrcode && this.scannerActive) {
      this.html5Qrcode.stop()
        .then(() => { this.scannerActive = false; this.html5Qrcode = null; })
        .catch(() => {});
    }
  }

  onScanSuccess(decoded: string): void {
    if (this.scanning) return;
    this.stopScanner();
    this.processCard(decoded.trim());
  }

  submitManual(): void {
    const v = this.manualCardNo.trim();
    if (!v || this.scanning) return;
    this.processCard(v);
    this.manualCardNo = '';
  }

  // ══════════════════════════════════════════════════════════
  // DISTRIBUTE — TCB scan এর processCardNo() এর মতো
  // ══════════════════════════════════════════════════════════

  processCard(rawValue: string): void {
    if (this.scanning) return;
    this.scanning   = true;
    this.lastResult = null;

    // Supports new EGOV_CARD payload, legacy LPG_CARD payload, JSON and plain card number.
    const parsedQr = this.parseSocialCardQr(rawValue, 'LPG');
    const cardNo = parsedQr.cardNo;
    const cardId: number | null = parsedQr.cardId;

    if (!cardNo) {
      this.scanning = false;
      this.lastResult = { success: false, message: 'অবৈধ LPG QR কোড।' };
      return;
    }

    // Step 1: card lookup by cardNo (if no id from QR)
    if (!cardId) {
      this.svc.checkByNid(cardNo)  // আপাতত checkByNid fallback — cardNo lookup নেই original service-এ
        .subscribe({
          next: () => {
            // checkByNid NID-based — এটা fallback, আসলে cardNo lookup আলাদা
            // এখানে distribute direct করব cardNo দিয়ে — backend cardNo accept করে
            this.callDistribute(null, cardNo);
          },
          error: () => this.callDistribute(null, cardNo)
        });
    } else {
      this.callDistribute(cardId, cardNo);
    }
  }

  private parseSocialCardQr(rawValue: string, expectedType: string): { cardNo: string; cardId: number | null } {
    const value = (rawValue || '').trim();
    let cardId: number | null = null;

    // JSON backward compatibility: {"cardNo":"LPG-...","id":42}
    try {
      const json = JSON.parse(value);
      if (json && typeof json === 'object') {
        return {
          cardNo: String(json.cardNo || json.card_no || '').trim(),
          cardId: json.id ? Number(json.id) : null
        };
      }
    } catch { /* not JSON */ }

    // New standard: EGOV_CARD|TYPE=LPG|CARD_NO=LPG-2026-0001
    if (value.startsWith('EGOV_CARD|')) {
      const parts: Record<string, string> = {};
      value.split('|').slice(1).forEach(part => {
        const i = part.indexOf('=');
        if (i > 0) parts[part.substring(0, i).trim().toUpperCase()] = part.substring(i + 1).trim();
      });
      if (parts['TYPE'] && parts['TYPE'].toUpperCase() !== expectedType.toUpperCase()) return { cardNo: '', cardId: null };
      return { cardNo: parts['CARD_NO'] || '', cardId: null };
    }

    // Legacy: LPG_CARD:LPG-...|NID:...
    if (value.startsWith('LPG_CARD:')) {
      const rest = value.substring('LPG_CARD:'.length);
      return { cardNo: rest.split('|')[0].trim(), cardId };
    }

    return { cardNo: value, cardId };
  }

  private callDistribute(cardId: number | null, cardNo: string): void {
    // LpgCardService.distribute() accepts cardId
    // যদি id না থাকে, backend-এ /distribute endpoint cardNo দিয়েও কাজ করবে
    // (Controller-এ cardId mandatory — তাই আমরা একটু lookup করব)
    // Simple approach: distribute call করি, backend null id handle করবে
    const payload: any = {
      cycleMonth:    this.cycleMonth,
      cylindersQty:  this.cylindersPerCard,
      collectedBy:   this.dealerName
    };

    if (cardId) {
      payload.cardId = cardId;
    } else {
      // cardNo দিয়ে — backend-এ /distribute-by-cardno endpoint থাকলে ব্যবহার করো
      // এখন আমরা একটা lookup করব
      this.lookupAndDistribute(cardNo);
      return;
    }

    this.svc.distribute(payload).subscribe({
      next: (res: any) => this.handleResult(res),
      error: (err: any) => this.handleError(err)
    });
  }

  /**
   * CardNo দিয়ে প্রথমে card lookup, তারপর distribute।
   * Farmer scan-এর lookupByCardNo() এর মতো।
   */
  private lookupAndDistribute(cardNo: string): void {
    this.svc.lookupByCardNo(cardNo).subscribe({
      next: (card: any) => {
        if (card?.id) {
          this.callDistribute(card.id, card.cardNo || cardNo);
        } else {
          this.handleError({ error: { message: 'কার্ড পাওয়া যায়নি।' } });
        }
      },
      error: () => this.handleError({ error: { message: 'কার্ড নম্বর দিয়ে খোঁজা যায়নি।' } })
    });
  }

  private handleResult(res: any): void {
    this.lastResult = res;
    this.scanning   = false;
    this.nowTime    = new Date().toLocaleTimeString('bn-BD', { hour: '2-digit', minute: '2-digit' });

    if (res.success) {
      this.totalDistributed++;

      // Session log-এ যোগ করি
      this.sessionLogs.unshift({
        holderName:   res.holderName,
        cardNo:       res.cardNo,
        cylindersQty: res.cylindersQty || this.cylindersPerCard,
        time:         this.nowTime
      });

      // Stock info আপডেট (local)
      if (this.stockInfo && res.stockUpdated) {
        this.stockInfo.distributed = (this.stockInfo.distributed || 0) + this.cylindersPerCard;
        this.stockInfo.remaining   = this.stockInfo.totalCylinders - this.stockInfo.distributed;
      }

      // 2 সেকেন্ড পর আবার scan শুরু (TCB scan এর মতো)
      if (this.scanMode === 'qr') {
        setTimeout(() => this.startScanner(), 2000);
      }
    }
  }

  private handleError(err: any): void {
    this.scanning   = false;
    this.lastResult = {
      success: false,
      message: err?.error?.message || 'সার্ভার সমস্যা হয়েছে।'
    };
  }
}
