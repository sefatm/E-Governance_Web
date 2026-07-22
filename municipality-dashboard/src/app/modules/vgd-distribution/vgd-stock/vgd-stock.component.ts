import { Component, OnInit } from '@angular/core';
import { VgdDistributionService } from '../../../services/vgd-distribution.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-vgd-stock',
  templateUrl: './vgd-stock.component.html',
  styleUrls: ['./vgd-stock.component.css']
})
export class VgdStockComponent implements OnInit {

  stocks: any[]         = [];
  filteredStocks: any[] = [];
  monthList: string[]   = [];

  isLoading   = false;
  saving      = false;
  msg         = '';
  isError     = false;
  formOpen    = true;

  filterType  = '';
  filterMonth = '';
  showLowOnly = false;

  form: any = this.defaultForm();

  constructor(public ls: LanguageService, private svc: VgdDistributionService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.isLoading = true;
    this.svc.getAllStock().subscribe({
      next: (res: any[]) => {
        this.stocks    = res;
        this.monthList = [...new Set(res.map((s:any)=>s.cycleMonth))].sort().reverse() as string[];
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  applyFilter(): void {
    let list = this.stocks;
    if (this.filterType)  list = list.filter((s:any) => s.cardType === this.filterType);
    if (this.filterMonth) list = list.filter((s:any) => s.cycleMonth === this.filterMonth);
    if (this.showLowOnly) list = list.filter((s:any) => this.remaining(s) < 5);
    this.filteredStocks = list;
  }

  save(): void {
    if (!this.form.batchLabel?.trim() || !this.form.ward?.trim() ||
        !this.form.cycleMonth || this.form.totalCards < 1) {
      this.showMsg('সব আবশ্যক তথ্য পূরণ করুন।', true); return;
    }
    this.saving = true;
    this.svc.createStock(this.form).subscribe({
      next: () => {
        this.saving = false;
        this.showMsg('Stock সফলভাবে যোগ হয়েছে!', false);
        this.form = this.defaultForm();
        this.load();
      },
      error: (err:any) => {
        this.saving = false;
        this.showMsg(err?.error?.message || 'সার্ভার ত্রুটি।', true);
      }
    });
  }

  clearForm(): void { this.form = this.defaultForm(); this.msg = ''; }

  remaining(s: any): number { return s.totalCards - (s.distributed || 0); }
  pct(s: any): number { return s.totalCards ? Math.min(100, Math.round(((s.distributed||0)/s.totalCards)*100)) : 0; }
  lowStockCount(): number { return this.stocks.filter(s => this.remaining(s) < 5).length; }
  countType(t: string): number { return this.stocks.filter(s => s.cardType === t).length; }

  private showMsg(txt: string, err: boolean): void {
    this.msg = txt; this.isError = err;
    setTimeout(() => this.msg = '', 4000);
  }

  private defaultForm(): any {
    return {
      cardType:   'VGD',
      batchLabel: '',
      cycleMonth: new Date().toISOString().slice(0,7),
      ward:       '',
      dealerName: '',
      riceKg:     0,
      wheatKg:    0,
      cashAmount: 0,
      totalCards: 0
    };
  }
}
