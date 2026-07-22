import { Component, OnInit, OnDestroy } from '@angular/core';
import { TcbService } from '../../../services/tcb.service';
import { ScanResult, DistributionLog } from '../../../models/tcb.model';
import { LanguageService } from 'src/app/services/language.service';

declare const Html5Qrcode: any;

@Component({
  selector: 'app-tcb-scan',
  templateUrl: './tcb-scan.component.html',
  styleUrls: ['./tcb-scan.component.css']
})
export class TcbScanComponent implements OnInit, OnDestroy {

  // Session state
  sessionId: number | null = null;
  sessionCode = '';
  dealerName  = 'Sefat Mahmud';
  ward        = '03';
  cycleMonth  = new Date().toISOString().slice(0, 7); // YYYY-MM
  sessionOpen = false;

  // Scanner state
  scannerActive  = false;
  manualCardNo   = '';
  lastResult: ScanResult | null = null;
  isScanning     = false;
  sessionLogs: DistributionLog[] = [];
  totalScanned   = 0;

  // Stock summary
  stockSummary: any = null;

  private html5Qrcode: any = null;

  constructor(public ls: LanguageService, private tcbSvc: TcbService) {}

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.stopScanner();
  }

  // ── SESSION ────────────────────────────────────────────────

  openSession(): void {
    if (!this.dealerName || !this.ward || !this.cycleMonth) return;
    this.tcbSvc.openSession(this.dealerName, this.ward, this.cycleMonth).subscribe({
      next: (res) => {
        if (res.error) { alert(res.error); return; }
        this.sessionId   = res.sessionId;
        this.sessionCode = res.sessionCode;
        this.stockSummary = res.stock || null;
        this.sessionOpen = true;
        this.refreshLogs();
      },
      error: (err) => alert(err?.error?.message || 'Session খোলা যায়নি।')
    });
  }

  closeSession(): void {
    if (!this.sessionId || !confirm('Session বন্ধ করবেন?')) return;
    this.stopScanner();
    this.tcbSvc.closeSession(this.sessionId).subscribe({
      next: () => {
        this.sessionOpen = false;
        this.sessionId   = null;
        this.lastResult  = null;
      }
    });
  }

  // ── SCAN ───────────────────────────────────────────────────

  startScanner(): void {
    if (this.scannerActive || !this.sessionId) return;
    this.scannerActive = true;
    this.html5Qrcode   = new Html5Qrcode('qr-reader');
    this.html5Qrcode.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 250, height: 250 } },
      (decodedText: string) => this.processCardNo(decodedText),
      () => {}
    ).catch(() => { this.scannerActive = false; });
  }

  stopScanner(): void {
    if (this.html5Qrcode && this.scannerActive) {
      this.html5Qrcode.stop().catch(() => {});
      this.html5Qrcode = null;
    }
    this.scannerActive = false;
  }

  submitManual(): void {
    const cardNo = this.manualCardNo.trim();
    if (!cardNo || !this.sessionId) return;
    this.processCardNo(cardNo);
    this.manualCardNo = '';
  }

  private processCardNo(cardNo: string): void {
    if (this.isScanning || !this.sessionId) return;
    this.isScanning = true;
    this.lastResult = null;

    this.tcbSvc.scan(this.sessionId, cardNo, this.dealerName).subscribe({
      next: (res) => {
        this.lastResult  = res;
        this.isScanning  = false;
        if (res.success) {
          this.totalScanned++;
          this.refreshLogs();
          // Brief pause before next scan (prevent double-scan)
          if (this.scannerActive) {
            this.stopScanner();
            setTimeout(() => this.startScanner(), 2000);
          }
        }
      },
      error: (err) => {
        this.lastResult  = { success: false, message: err?.error?.message || 'সার্ভার ত্রুটি।' };
        this.isScanning  = false;
      }
    });
  }

  private refreshLogs(): void {
    if (!this.sessionId) return;
    this.tcbSvc.getSessionStatus(this.sessionId).subscribe({
      next: (res) => {
        this.sessionLogs  = res.logs        || [];
        this.totalScanned = res.session?.totalScanned || this.totalScanned;
        this.stockSummary = res.stock       || this.stockSummary;
      }
    });
  }
}
