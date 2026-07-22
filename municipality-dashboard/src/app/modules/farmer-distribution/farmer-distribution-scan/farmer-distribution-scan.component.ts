// farmer-distribution-scan.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FarmerDistributionService } from '../../../services/farmer-distribution.service';
import { LanguageService } from 'src/app/services/language.service';

declare const Html5Qrcode: any;

@Component({
  selector: 'app-farmer-distribution-scan',
  templateUrl: './farmer-distribution-scan.component.html',
  styleUrls: ['./farmer-distribution-scan.component.css']
})
export class FarmerDistributionScanComponent implements OnInit, OnDestroy {

  // Session setup
  officerName  = '';
  ward         = '';
  season       = '';
  cycleMonth   = new Date().toISOString().slice(0, 7);
  fertPerAcre  = 20;
  seedPerAcre  = 5;

  sessionOpen      = false;
  startLoading     = false;
  setupError       = '';

  // Scanner
  scanMode       = 'qr';
  scannerActive  = false;
  manualCardNo   = '';
  scanning       = false;
  lastResult: any = null;

  sessionLogs: any[]  = [];
  totalDistributed    = 0;

  private html5Qrcode: any = null;

  constructor(public ls: LanguageService, private svc: FarmerDistributionService) {}

  ngOnInit(): void {}
  ngOnDestroy(): void { this.stopScanner(); }

  // ── SESSION ──────────────────────────────────────────────
  startSession(): void {
    if (!this.officerName.trim() || !this.ward.trim() || !this.season || !this.cycleMonth) {
      this.setupError = 'সব তথ্য পূরণ করুন।'; return;
    }
    this.setupError   = '';
    this.startLoading = true;

    // No session API for distribution — just open locally
    // (Unlike TCB which needs a backend session, farmer distribution
    //  records are created per-scan via POST /api/farmer/distribute)
    this.sessionOpen    = true;
    this.startLoading   = false;
    this.sessionLogs    = [];
    this.totalDistributed = 0;
  }

  closeSession(): void {
    if (!confirm('বিতরণ session বন্ধ করতে চান?')) return;
    this.stopScanner();
    this.sessionOpen = false;
    this.lastResult  = null;
    this.sessionLogs = [];
  }

  // ── SCANNER ──────────────────────────────────────────────
  switchMode(mode: string): void {
    this.scanMode   = mode;
    this.lastResult = null;
    if (mode !== 'qr') this.stopScanner();
  }

  startScanner(): void {
    if (this.scannerActive) return;
    this.html5Qrcode = new Html5Qrcode('farmer-qr-reader');
    this.html5Qrcode.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 220, height: 220 } },
      (decoded: string) => { this.onScanSuccess(decoded); },
      () => {}
    ).then(() => { this.scannerActive = true; })
     .catch(() => { this.setupError = 'ক্যামেরা চালু করা যায়নি।'; });
  }

  stopScanner(): void {
    if (this.html5Qrcode && this.scannerActive) {
      this.html5Qrcode.stop().then(() => {
        this.scannerActive = false;
        this.html5Qrcode = null;
      }).catch(() => {});
    }
  }

  onScanSuccess(decoded: string): void {
    if (this.scanning) return;
    this.stopScanner();
    this.processCard(decoded);
  }

  submitManual(): void {
    if (!this.manualCardNo.trim() || this.scanning) return;
    this.processCard(this.manualCardNo.trim());
  }

  // ── DISTRIBUTE ───────────────────────────────────────────
  private parseFarmerCardQr(rawValue: string): string {
    const value = (rawValue || '').trim();

    try {
      const json = JSON.parse(value);
      if (json && typeof json === 'object') return String(json.cardNo || json.card_no || '').trim();
    } catch { /* not JSON */ }

    if (value.startsWith('EGOV_CARD|')) {
      const parts: Record<string, string> = {};
      value.split('|').slice(1).forEach(part => {
        const i = part.indexOf('=');
        if (i > 0) parts[part.substring(0, i).trim().toUpperCase()] = part.substring(i + 1).trim();
      });
      if (parts['TYPE'] && parts['TYPE'].toUpperCase() !== 'FARMER') return '';
      return parts['CARD_NO'] || '';
    }

    if (value.startsWith('FARMER_CARD:')) {
      return value.substring('FARMER_CARD:'.length).split('|')[0].trim();
    }

    return value;
  }

  processCard(rawCardNo: string): void {
    this.scanning   = true;
    this.lastResult = null;

    const cardNo = this.parseFarmerCardQr(rawCardNo);
    if (!cardNo) {
      this.scanning = false;
      this.lastResult = { success: false, message: 'অবৈধ Farmer Card QR কোড।' };
      return;
    }

    // Need card_id from card_no — first look up card
    this.svc.lookupByCardNo(cardNo).subscribe({
      next: (card: any) => {
        if (!card || !card.id) {
          this.lastResult = { success: false, message: 'কার্ড নম্বর পাওয়া যায়নি: ' + cardNo };
          this.scanning = false;
          return;
        }

        // Calculate entitlement based on land and per-acre rates
        const acres      = card.landTotal || 0;
        const fertKg     = parseFloat((acres * this.fertPerAcre).toFixed(2));
        const seedKg     = parseFloat((acres * this.seedPerAcre).toFixed(2));

        this.svc.distribute({
          cardId:        card.id,
          cycleMonth:    this.cycleMonth,
          season:        this.season,
          fertilizerKg:  fertKg,
          seedKg:        seedKg,
          pesticideLitre: 0,
          distributedBy: this.officerName
        }).subscribe({
          next: (res: any) => {
            this.lastResult = res;
            this.scanning   = false;
            this.manualCardNo = '';

            if (res.success) {
              this.totalDistributed++;
              this.sessionLogs.unshift({
                farmerName:   res.farmerName,
                cardNo:       res.cardNo,
                fertilizerKg: res.fertilizerKg,
                seedKg:       res.seedKg,
                createdAt:    new Date()
              });
            }
          },
          error: (err: any) => {
            this.scanning = false;
            this.lastResult = {
              success: false,
              message: err?.error?.message || 'সার্ভার সমস্যা।'
            };
          }
        });
      },
      error: () => {
        this.scanning = false;
        this.lastResult = { success: false, message: 'কার্ড নম্বর পাওয়া যায়নি।' };
      }
    });
  }
}
