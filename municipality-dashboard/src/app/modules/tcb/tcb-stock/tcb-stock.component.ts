import { Component, OnInit } from '@angular/core';
import { TcbService } from '../../../services/tcb.service';
import { TcbStock } from '../../../models/tcb.model';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-tcb-stock',
  templateUrl: './tcb-stock.component.html',
  styleUrls: ['./tcb-stock.component.css']
})
export class TcbStockComponent implements OnInit {

  stocks: TcbStock[]         = [];
  filteredStocks: TcbStock[] = [];
  wardList: string[]         = [];
  monthList: string[]        = [];

  isLoading = false;
  saving    = false;
  msg       = '';
  isError   = false;
  formOpen  = true;

  // ── Filter state ─────────────────────────────────────────────
  filterWard    = '';
  filterMonth   = '';
  showLowOnly   = false;

  // ── Form ─────────────────────────────────────────────────────
  form: Partial<TcbStock> = this.defaultForm();

  constructor(public ls: LanguageService, private tcbSvc: TcbService) {}

  ngOnInit(): void {
    this.load();
  }

  // ── LOAD ─────────────────────────────────────────────────────
  load(): void {
    this.isLoading = true;
    this.tcbSvc.getAllStock().subscribe({
      next: (res) => {
        this.stocks = res;

        this.wardList = [...new Set(
          res.map(s => s.ward).filter(Boolean)
        )].sort() as string[];

        this.monthList = [...new Set(
          res.map(s => s.cycleMonth).filter(Boolean)
        )].sort().reverse() as string[];

        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  // ── FILTER ───────────────────────────────────────────────────
  applyFilter(): void {
    let list = this.stocks;

    if (this.filterWard)  list = list.filter(s => s.ward === this.filterWard);
    if (this.filterMonth) list = list.filter(s => s.cycleMonth === this.filterMonth);
    if (this.showLowOnly) list = list.filter(s => this.remaining(s) < 5);

    this.filteredStocks = list;
  }

  resetFilter(): void {
    this.filterWard  = '';
    this.filterMonth = '';
    this.showLowOnly = false;
    this.applyFilter();
  }

  // ── SAVE ─────────────────────────────────────────────────────
  save(): void {
    if (!this.form.batchLabel?.trim()) {
      this.showMsg('Batch label আবশ্যক।', true); return;
    }
    if (!this.form.ward?.trim()) {
      this.showMsg('ওয়ার্ড নম্বর আবশ্যক।', true); return;
    }
    if (!this.form.cycleMonth) {
      this.showMsg('মাস আবশ্যক।', true); return;
    }
    if (!this.form.totalCards || this.form.totalCards < 1) {
      this.showMsg('কার্ড সংখ্যা ন্যূনতম ১ হতে হবে।', true); return;
    }

    this.saving = true;
    this.tcbSvc.createStock(this.form).subscribe({
      next: () => {
        this.showMsg('Stock সফলভাবে যোগ হয়েছে!', false);
        this.saving  = false;
        this.form    = this.defaultForm();
        this.load();
      },
      error: (err) => {
        this.showMsg(err?.error?.message || 'সার্ভার ত্রুটি।', true);
        this.saving = false;
      }
    });
  }

  clearForm(): void {
    this.form    = this.defaultForm();
    this.msg     = '';
    this.isError = false;
  }

  // ── HELPERS ──────────────────────────────────────────────────
  remaining(s: TcbStock): number {
    return s.totalCards - (s.distributed || 0);
  }

  progressPct(s: TcbStock): number {
    if (!s.totalCards) return 0;
    return Math.min(100, Math.round(((s.distributed || 0) / s.totalCards) * 100));
  }

  grandTotal(field: keyof TcbStock): number {
    return this.stocks.reduce((sum, s) => {
      const v = s[field];
      return sum + (typeof v === 'number' ? v : 0);
    }, 0);
  }

  lowStockCount(): number {
    return this.stocks.filter(s => this.remaining(s) < 5).length;
  }

  private showMsg(text: string, error: boolean): void {
    this.msg     = text;
    this.isError = error;
    setTimeout(() => { this.msg = ''; }, 4000);
  }

  private defaultForm(): Partial<TcbStock> {
    return {
      batchLabel : '',
      cycleMonth : new Date().toISOString().slice(0, 7),
      ward       : '',
      dealerName : '',
      oilLitre   : 0,
      riceKg     : 0,
      lentilKg   : 0,
      sugarKg    : 0,
      cashAmount        : 0,
      totalCards        : 0,
      // ✅ NEW: product unit prices
      oilPricePerLitre  : 0,
      ricePricePerKg    : 0,
      lentilPricePerKg  : 0,
      sugarPricePerKg   : 0
    };
  }
}
