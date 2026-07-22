import { Component, OnInit, OnDestroy } from '@angular/core';
import { VgdDistributionService } from 'src/app/services/vgd-distribution.service';
import { LanguageService } from 'src/app/services/language.service';

declare const Html5Qrcode: any;

@Component({
  selector: 'app-vgd-scan',
  templateUrl: './vgd-scan.component.html',
  styleUrls: ['./vgd-scan.component.css']
})
export class VgdScanComponent implements OnInit, OnDestroy {

  cardType    = 'VGD';
  officerName = '';
  ward        = '';
  cycleMonth  = new Date().toISOString().slice(0,7);

  sessionOpen    = false;
  sessionId: number | null = null;
  startLoading   = false;
  setupError     = '';

  scanMode       = 'qr';
  scannerActive  = false;
  manualCardNo   = '';
  scanning       = false;
  lastResult: any = null;

  sessionLogs:    any[] = [];
  totalDistributed = 0;

  private html5Qrcode: any = null;

  constructor(public ls: LanguageService, private svc: VgdDistributionService) {}

  ngOnInit(): void {}
  ngOnDestroy(): void { this.stopScanner(); }

  startSession(): void {
    if (!this.officerName.trim() || !this.ward.trim() || !this.cycleMonth) {
      this.setupError = 'সব তথ্য পূরণ করুন।'; return;
    }
    this.setupError = '';
    this.startLoading = true;

    this.svc.openSession({
      dealerName: this.officerName,
      ward:       this.ward,
      cycleMonth: this.cycleMonth,
      cardType:   this.cardType
    }).subscribe({
      next: (res: any) => {
        this.startLoading = false;
        if (res.error) { this.setupError = res.error; return; }
        this.sessionId   = res.sessionId;
        this.sessionOpen = true;
        this.sessionLogs = [];
        this.totalDistributed = 0;
      },
      error: (err: any) => {
        this.startLoading = false;
        this.setupError = err?.error?.error || 'Session শুরু করা যায়নি।';
      }
    });
  }

  closeSession(): void {
    if (!confirm('বিতরণ session বন্ধ করতে চান?')) return;
    this.stopScanner();
    if (this.sessionId) {
      this.svc.closeSession(this.sessionId).subscribe();
    }
    this.sessionOpen = false;
    this.sessionId   = null;
    this.lastResult  = null;
    this.sessionLogs = [];
  }

  switchMode(mode: string): void {
    this.scanMode = mode;
    this.lastResult = null;
    if (mode !== 'qr') this.stopScanner();
  }

  startScanner(): void {
    if (this.scannerActive) return;
    this.html5Qrcode = new Html5Qrcode('vgd-qr-reader');
    this.html5Qrcode.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 220, height: 220 } },
      (decoded: string) => { this.onScanSuccess(decoded); },
      () => {}
    ).then(() => { this.scannerActive = true; })
     .catch(() => { this.setupError = 'ক্যামেরা চালু হয়নি।'; });
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

  processCard(cardNo: string): void {
    if (!this.sessionId) return;
    this.scanning   = true;
    this.lastResult = null;

    this.svc.scan({
      sessionId:  this.sessionId,
      cardNo:     cardNo,
      scannedBy:  this.officerName
    }).subscribe({
      next: (res: any) => {
        this.lastResult = res;
        this.scanning   = false;
        this.manualCardNo = '';

        if (res.success) {
          this.totalDistributed++;
          this.sessionLogs.unshift({
            holderName:  res.holderName,
            cardNo:      res.cardNo,
            cardType:    res.cardType,
            riceKg:      res.riceKg,
            wheatKg:     res.wheatKg,
            cashAmount:  res.cashAmount,
            time:        new Date()
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
  }
}
