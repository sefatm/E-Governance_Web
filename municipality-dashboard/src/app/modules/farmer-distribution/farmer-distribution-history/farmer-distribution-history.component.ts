// farmer-distribution-history.component.ts
import { Component, OnInit } from '@angular/core';
import { FarmerDistributionService } from '../../../services/farmer-distribution.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-farmer-distribution-history',
  templateUrl: './farmer-distribution-history.component.html',
  styleUrls: ['./farmer-distribution-history.component.css']
})
export class FarmerDistributionHistoryComponent implements OnInit {

  selectedCycle = new Date().toISOString().slice(0, 7);
  logs: any[]         = [];
  filteredLogs: any[] = [];
  wardList: string[]  = [];
  isLoading           = false;

  filterWard   = '';
  filterSeason = '';

  // Card lookup
  lookupCardNo  = '';
  cardLogs: any[] = [];
  lookupDone    = false;
  lookupLoading = false;

  // ── Stock ──────────────────────────────────────────────
  stockFormOpen = false;
  stockLoading  = false;
  stockSaving   = false;
  stockMsg      = '';
  stockError    = false;
  stockList: any[] = [];

  stockForm = {
    cycleMonth:     new Date().toISOString().slice(0, 7),
    batchNo:        '',
    fertilizerKg:   null as number | null,
    seedKg:         null as number | null,
    pesticideLitre: null as number | null,
    note:           ''
  };

  // ── Computed: distribution totals ─────────────────────
  get totalFertilizer(): number {
    return this.filteredLogs.reduce((s, l) => s + (l.fertilizerKg || 0), 0);
  }
  get totalSeed(): number {
    return this.filteredLogs.reduce((s, l) => s + (l.seedKg || 0), 0);
  }

  // ── Computed: stock totals ─────────────────────────────
  get totalStockFertilizer(): number {
    return this.stockList.reduce((s, r) => s + (r.fertilizerKg || 0), 0);
  }
  get totalStockSeed(): number {
    return this.stockList.reduce((s, r) => s + (r.seedKg || 0), 0);
  }
  get totalStockPesticide(): number {
    return this.stockList.reduce((s, r) => s + (r.pesticideLitre || 0), 0);
  }
  get totalDistFertilizer(): number {
    return this.stockList.reduce((s, r) => s + (r.fertilizerDistributed || 0), 0);
  }
  get totalDistSeed(): number {
    return this.stockList.reduce((s, r) => s + (r.seedDistributed || 0), 0);
  }

  constructor(public ls: LanguageService, private svc: FarmerDistributionService) {}

  ngOnInit(): void {
    this.loadCycle();
    this.loadStock();
  }

  // ── Cycle distribution log ─────────────────────────────
  loadCycle(): void {
    this.isLoading = true;
    this.svc.getCycleSummary(this.selectedCycle).subscribe({
      next: (res: any) => {
        this.logs = res.logs || [];
        this.wardList = [...new Set(this.logs.map((l: any) => l.ward).filter(Boolean))] as string[];
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  applyFilter(): void {
    let list = this.logs;
    if (this.filterWard)   list = list.filter(l => l.ward === this.filterWard);
    if (this.filterSeason) list = list.filter(l => l.season === this.filterSeason);
    this.filteredLogs = list;
  }

  // ── Card lookup ────────────────────────────────────────
  lookupCard(): void {
    if (!this.lookupCardNo.trim()) return;
    this.cardLogs      = [];
    this.lookupDone    = false;
    this.lookupLoading = true;
    this.svc.getSubsidyHistory(this.lookupCardNo.trim()).subscribe({
      next: (res: any) => {
        this.cardLogs      = Array.isArray(res) ? res : [];
        this.lookupDone    = true;
        this.lookupLoading = false;
      },
      error: () => {
        this.lookupDone    = true;
        this.lookupLoading = false;
      }
    });
  }

  // ── Stock ──────────────────────────────────────────────
  loadStock(): void {
    this.stockLoading = true;
    this.svc.getStockList(this.selectedCycle).subscribe({
      next: (res: any) => {
        this.stockList    = Array.isArray(res) ? res : (res.stocks || []);
        this.stockLoading = false;
      },
      error: () => { this.stockLoading = false; }
    });
  }

  saveStock(): void {
    const f = this.stockForm;
    if (!f.cycleMonth || f.fertilizerKg == null || f.seedKg == null) {
      this.stockMsg   = 'চক্র, সার ও বীজ পরিমাণ আবশ্যক।';
      this.stockError = true;
      return;
    }
    this.stockSaving = true;
    this.stockMsg    = '';
    this.svc.saveStock({
      cycleMonth:     f.cycleMonth,
      batchNo:        f.batchNo || null,
      fertilizerKg:   f.fertilizerKg,
      seedKg:         f.seedKg,
      pesticideLitre: f.pesticideLitre || 0,
      note:           f.note || null
    }).subscribe({
      next: () => {
        this.stockSaving   = false;
        this.stockMsg      = 'স্টক সফলভাবে সংরক্ষিত হয়েছে।';
        this.stockError    = false;
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
      cycleMonth:     this.selectedCycle,
      batchNo:        '',
      fertilizerKg:   null,
      seedKg:         null,
      pesticideLitre: null,
      note:           ''
    };
    this.stockMsg   = '';
    this.stockError = false;
  }

  getFertPct(s: any): number {
    if (!s.fertilizerKg || s.fertilizerKg <= 0) return 0;
    return Math.min(100, Math.round((s.fertilizerDistributed / s.fertilizerKg) * 100));
  }

  // ── CSV Export ─────────────────────────────────────────
  exportCsv(): void {
    const rows = [
      ['#','নাম','কার্ড নং','ওয়ার্ড','মৌসুম','সার (kg)','বীজ (kg)','তারিখ','বিতরণকারী'],
      ...this.filteredLogs.map((l, i) => [
        i+1, l.farmerName, l.cardNo, l.ward||'', l.season,
        l.fertilizerKg||0, l.seedKg||0, l.distDate, l.distributedBy
      ])
    ];
    const blob = new Blob([rows.map(r=>r.join(',')).join('\n')], { type:'text/csv;charset=utf-8;' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `farmer_subsidy_${this.selectedCycle}.csv`;
    a.click();
  }
}
