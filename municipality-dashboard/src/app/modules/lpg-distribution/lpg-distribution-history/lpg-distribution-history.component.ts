// lpg-distribution-history.component.ts
import { Component, OnInit } from '@angular/core';
import { LpgCardService } from '../../../services/lpg-card.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-lpg-distribution-history',
  templateUrl: './lpg-distribution-history.component.html',
  styleUrls: ['./lpg-distribution-history.component.css']
})
export class LpgDistributionHistoryComponent implements OnInit {

  selectedCycle = new Date().toISOString().slice(0, 7);
  logs: any[]         = [];
  filteredLogs: any[] = [];
  wardList:   string[] = [];
  dealerList: string[] = [];
  isLoading = false;

  filterWard   = '';
  filterDealer = '';

  // Card lookup
  lookupCardNo  = '';
  cardLogs: any[] = [];
  lookupDone    = false;
  lookupLoading = false;

  // Stock
  stockFormOpen = false;
  stockLoading  = false;
  stockSaving   = false;
  stockMsg      = '';
  stockError    = false;
  stockList: any[] = [];

  stockForm = {
    cycleMonth:     new Date().toISOString().slice(0, 7),
    batchLabel:     '',
    ward:           '',
    dealerName:     '',
    dealerCode:     '',
    cylinderSize:   '12kg',
    totalCylinders: null as number | null,
    totalCards:     null as number | null,
  };

  // ── Computed ──────────────────────────────────────────────
  get totalCylinders(): number {
    return this.filteredLogs.reduce((s, l) => s + (l.cylindersQty || 0), 0);
  }
  get totalStockCylinders(): number {
    return this.stockList.reduce((s, r) => s + (r.totalCylinders || 0), 0);
  }
  get totalDistCylinders(): number {
    return this.stockList.reduce((s, r) => s + (r.distributed || 0), 0);
  }

  constructor(public ls: LanguageService, private svc: LpgCardService) {}

  ngOnInit(): void {
    this.loadCycle();
    this.loadStock();
  }

  // ── Cycle distribution log ─────────────────────────────────
  loadCycle(): void {
    this.isLoading = true;
    this.svc.getCycleSummary(this.selectedCycle).subscribe({
      next: (res: any) => {
        this.logs = res.logs || [];
        this.wardList   = [...new Set(this.logs.map((l: any) => l.ward).filter(Boolean))] as string[];
        this.dealerList = [...new Set(this.logs.map((l: any) => l.dealerName).filter(Boolean))] as string[];
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  applyFilter(): void {
    let list = this.logs;
    if (this.filterWard)   list = list.filter(l => l.ward === this.filterWard);
    if (this.filterDealer) list = list.filter(l => l.dealerName === this.filterDealer);
    this.filteredLogs = list;
  }

  // ── Card lookup ────────────────────────────────────────────
  lookupCard(): void {
    if (!this.lookupCardNo.trim()) return;
    this.cardLogs      = [];
    this.lookupDone    = false;
    this.lookupLoading = true;
    this.svc.getHistoryByCardNo(this.lookupCardNo.trim()).subscribe({
      next: (res: any[]) => {
        this.cardLogs      = res || [];
        this.lookupDone    = true;
        this.lookupLoading = false;
      },
      error: () => { this.lookupDone = true; this.lookupLoading = false; }
    });
  }

  // ── Stock ──────────────────────────────────────────────────
  loadStock(): void {
    this.stockLoading = true;
    this.svc.getStockList(this.selectedCycle).subscribe({
      next: (res: any[]) => { this.stockList = res || []; this.stockLoading = false; },
      error: () => { this.stockLoading = false; }
    });
  }

  saveStock(): void {
    const f = this.stockForm;
    if (!f.cycleMonth || !f.totalCylinders || f.totalCylinders <= 0) {
      this.stockMsg   = 'চক্র ও সিলিন্ডার সংখ্যা আবশ্যক।';
      this.stockError = true;
      return;
    }
    this.stockSaving = true;
    this.stockMsg    = '';
    this.svc.saveStock({
      cycleMonth:     f.cycleMonth,
      batchLabel:     f.batchLabel  || undefined,
      ward:           f.ward        || undefined,
      dealerName:     f.dealerName  || undefined,
      dealerCode:     f.dealerCode  || undefined,
      cylinderSize:   f.cylinderSize,
      totalCylinders: f.totalCylinders,
      totalCards:     f.totalCards  || undefined,
    }).subscribe({
      next: () => {
        this.stockSaving = false;
        this.stockMsg    = 'স্টক সফলভাবে সংরক্ষিত হয়েছে।';
        this.stockError  = false;
        this.loadStock();
        setTimeout(() => this.stockMsg = '', 3000);
      },
      error: () => {
        this.stockSaving = false;
        this.stockMsg    = 'সংরক্ষণে সমস্যা হয়েছে।';
        this.stockError  = true;
      }
    });
  }

  clearStockForm(): void {
    this.stockForm = {
      cycleMonth: this.selectedCycle, batchLabel: '', ward: '',
      dealerName: '', dealerCode: '', cylinderSize: '12kg',
      totalCylinders: null, totalCards: null
    };
    this.stockMsg = ''; this.stockError = false;
  }

  getPct(s: any): number {
    if (!s.totalCylinders || s.totalCylinders <= 0) return 0;
    return Math.min(100, Math.round((s.distributed / s.totalCylinders) * 100));
  }

  // ── PDF Export ─────────────────────────────────────────────
  exportPdf(): void {
    const printWindow = window.open('', '_blank');
    if (!printWindow) return;

    const rows = this.filteredLogs.map((l, i) => `
      <tr>
        <td>${i + 1}</td>
        <td>${l.holderName || ''}</td>
        <td><code>${l.cardNo || ''}</code></td>
        <td>${l.ward || '—'}</td>
        <td style="text-align:center;font-weight:600">${l.cylindersQty || 1}</td>
        <td>${l.cylinderSize || ''}</td>
        <td>${l.dealerName || '—'}</td>
        <td>${l.distDate ? new Date(l.distDate).toLocaleDateString('bn-BD') : ''}</td>
        <td>${l.collectedBy || ''}</td>
      </tr>`).join('');

    printWindow.document.write(`<!DOCTYPE html><html><head>
      <meta charset="utf-8">
      <title>LPG বিতরণ — ${this.selectedCycle}</title>
      <style>
        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 24px; color: #111; }
        h2 { color: #064e3b; margin-bottom: 4px; }
        p  { color: #6b7280; font-size: 13px; margin: 0 0 16px; }
        table { width: 100%; border-collapse: collapse; font-size: 12px; }
        thead tr { background: #064e3b; color: #fff; }
        th { padding: 8px 10px; text-align: left; }
        td { padding: 7px 10px; border-bottom: 1px solid #f3f4f6; }
        tr:nth-child(even) td { background: #f8fafc; }
        code { font-family: monospace; color: #059669; }
        .footer { margin-top: 20px; font-size: 11px; color: #9ca3af; text-align: right; }
        @media print { .footer { position: fixed; bottom: 0; right: 0; } }
      </style>
    </head><body>
      <h2>🔥 LPG বিতরণ প্রতিবেদন — ${this.selectedCycle}</h2>
      <p>মোট কার্ডধারী: ${this.filteredLogs.length} | মোট সিলিন্ডার: ${this.totalCylinders}</p>
      <table>
        <thead>
          <tr>
            <th>#</th><th>নাম</th><th>কার্ড নং</th><th>ওয়ার্ড</th>
            <th>সিলিন্ডার</th><th>সাইজ</th><th>ডিলার</th><th>তারিখ</th><th>সংগ্রহকারী</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
      <div class="footer">মুদ্রণ তারিখ: ${new Date().toLocaleDateString('bn-BD')}</div>
      <script>window.onload = () => { window.print(); window.onafterprint = () => window.close(); }<\/script>
    </body></html>`);
    printWindow.document.close();
  }

  // ── CSV Export ─────────────────────────────────────────────
  exportCsv(): void {
    const rows = [
      ['#','নাম','কার্ড নং','ওয়ার্ড','সিলিন্ডার','সাইজ','ডিলার','তারিখ','সংগ্রহকারী'],
      ...this.filteredLogs.map((l, i) => [
        i+1, l.holderName, l.cardNo, l.ward||'', l.cylindersQty||1,
        l.cylinderSize, l.dealerName||'', l.distDate, l.collectedBy
      ])
    ];
    const blob = new Blob([rows.map(r => r.join(',')).join('\n')], { type: 'text/csv;charset=utf-8;' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `lpg_distribution_${this.selectedCycle}.csv`;
    a.click();
  }
}
