import { Component, OnInit } from '@angular/core';
import { WaterBillService } from 'src/app/services/water-bill.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-water-usage',
  templateUrl: './water-usage.component.html',
  styleUrls: ['./water-usage.component.css']
})
export class WaterUsageComponent implements OnInit {

  isLoading    = false;
  errorMsg     = '';

  chartLabels: string[] = [];
  chartData:   number[] = [];

  totalUnits   = 0;
  totalBilled  = 0;
  totalPaid    = 0;
  avgMonthly   = 0;

  constructor(public ls: LanguageService, private billService: WaterBillService) {}

  ngOnInit(): void { this.loadUsageData(); }

  loadUsageData(): void {
    this.isLoading = true;
    this.errorMsg  = '';

    this.billService.getAll().subscribe({
      next: (bills) => {
        this.isLoading = false;

        if (!bills || bills.length === 0) {
          this.chartLabels = [];
          this.chartData   = [];
          return;
        }

        this.totalUnits  = bills.reduce((s, b) => s + (b.units || 0), 0);
        this.totalBilled = bills.reduce((s, b) => s + (b.amount || 0), 0);
        this.totalPaid   = bills
          .filter(b => (b.status || '').toLowerCase() === 'paid')
          .reduce((s, b) => s + (b.amount || 0), 0);
        this.avgMonthly  = bills.length > 0
          ? Math.round(this.totalUnits / bills.length) : 0;

        const monthMap: { [key: string]: number } = {};
        bills.forEach(b => {
          const key = b.month || 'Unknown';
          monthMap[key] = (monthMap[key] || 0) + (b.units || 0);
        });

        const monthOrder = [
          'January','February','March','April','May','June',
          'July','August','September','October','November','December'
        ];
        const sorted = Object.entries(monthMap).sort(([a], [b]) => {
          const ma = monthOrder.findIndex(m => a.includes(m));
          const mb = monthOrder.findIndex(m => b.includes(m));
          return ma - mb;
        });

        this.chartLabels = sorted.map(([k]) => k);
        this.chartData   = sorted.map(([, v]) => v);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMsg  = 'Failed to load usage data.';
        console.error('Water usage load error:', err);
      }
    });
  }

  formatNumber(value: number | null | undefined): string {
    return new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }).format(value || 0);
  }
}
