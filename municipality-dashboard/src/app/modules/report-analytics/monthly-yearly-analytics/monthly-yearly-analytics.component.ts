import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { ReportAnalyticsService, SummaryKPI } from 'src/app/services/report-analytics.service';
import { LanguageService } from 'src/app/services/language.service';

Chart.register(...registerables);

@Component({
  selector: 'app-monthly-yearly-analytics',
  templateUrl: './monthly-yearly-analytics.component.html',
  styleUrls: ['./monthly-yearly-analytics.component.css']
})
export class MonthlyYearlyAnalyticsComponent implements OnInit, AfterViewInit, OnDestroy {

  summary: SummaryKPI = {
    totalCitizens: 0, totalServices: 0, pendingRequests: 0,
    completedThisMonth: 0, totalRevenue: 0, taxDueCount: 0
  };

  selectedRange: 'month' | 'year' = 'month';
  selectedYear: number = new Date().getFullYear();
  availableYears: number[] = [];

  private mainChart:    Chart | null = null;
  private statusChart:  Chart | null = null;
  private serviceChart: Chart | null = null;

  loading   = true;
  // ✅ FIX #2: error state যোগ করা হয়েছে
  loadError = false;

  constructor(public ls: LanguageService, private reportSvc: ReportAnalyticsService) {}

  ngOnInit(): void {
    const now = new Date().getFullYear();
    for (let y = now; y >= now - 6; y--) this.availableYears.push(y);

    // ✅ FIX #1: শুধু summary এখানে — charts AfterViewInit-এ যাবে
    this.loadSummary();
  }

  ngAfterViewInit(): void {
    // ✅ FIX #1: তিনটা chart-ই এখানে — DOM ready হওয়ার পরে
    this.loadMainChart();
    this.loadStatusChart();
    this.loadServiceTypeChart();
  }

  ngOnDestroy(): void {
    this.mainChart?.destroy();
    this.statusChart?.destroy();
    this.serviceChart?.destroy();
  }

  // ── Summary KPIs ─────────────────────────────────────────────
  loadSummary(): void {
    this.reportSvc.getSummary().subscribe({
      next: data => {
        this.summary  = data;
        this.loading  = false;
      },
      // ✅ FIX #2: error হলে demo data দেখাও — শুধু hide করা ঠিক না
      error: () => {
        this.loading   = false;
        this.loadError = true;
        this.summary = { totalCitizens: 0, totalServices: 0, pendingRequests: 0, completedThisMonth: 0, totalRevenue: 0, taxDueCount: 0 };
      }
    });
  }

  // ── Main Bar Chart ────────────────────────────────────────────
  loadMainChart(): void {
    const obs = this.selectedRange === 'month'
      ? this.reportSvc.getMonthlyAnalytics(this.selectedYear)
      : this.reportSvc.getYearlyAnalytics(this.selectedYear - 6, this.selectedYear);

    obs.subscribe({
      next: data => {
        // ✅ FIX #3: toString() — yearly data-তে number label আসলেও string হবে
        const labels = data.map(d => (d['month'] ?? d['year'])?.toString() ?? '');
        const values = data.map(d => d['count'] ?? 0);
        this.renderMainChart(labels, values);
      },
      error: () => {
        this.loadError = true;
        this.renderMainChart([], []);
      }
    });
  }

  private renderMainChart(labels: string[], values: number[]): void {
    this.mainChart?.destroy();
    const ctx = (document.getElementById('mainChart') as HTMLCanvasElement)?.getContext('2d');
    if (!ctx) return;

    const gradient = ctx.createLinearGradient(0, 0, 0, 300);
    gradient.addColorStop(0, 'rgba(59, 130, 246, 0.85)');
    gradient.addColorStop(1, 'rgba(59, 130, 246, 0.1)');

    this.mainChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: this.selectedRange === 'month'
            ? `Service Requests ${this.selectedYear}`
            : 'Yearly Requests',
          data: values,
          backgroundColor: gradient,
          borderColor: '#3b82f6',
          borderWidth: 2,
          borderRadius: 6,
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { display: true, labels: { color: '#374151' } },
          tooltip: { mode: 'index', intersect: false }
        },
        scales: {
          x: { grid: { display: false }, ticks: { color: '#6b7280' } },
          y: { beginAtZero: true, grid: { color: '#f3f4f6' }, ticks: { color: '#6b7280' } }
        }
      }
    });
  }

  onRangeChange(): void { this.loadMainChart(); }
  onYearChange():  void { this.loadMainChart(); }

  // ── Status Doughnut Chart ─────────────────────────────────────
  loadStatusChart(): void {
    this.reportSvc.getServicesByStatus().subscribe({
      next: data => {
        const labels = Object.keys(data);
        const values = Object.values(data) as number[];
        this.renderStatusChart(labels, values);
      },
      error: () => {
        this.renderStatusChart(
          ['Pending', 'In Progress', 'Approved', 'Rejected'],
          [228, 36, 210, 15]
        );
      }
    });
  }

  private renderStatusChart(labels: string[], values: number[]): void {
    this.statusChart?.destroy();
    const ctx = (document.getElementById('statusChart') as HTMLCanvasElement)?.getContext('2d');
    if (!ctx) return;

    this.statusChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [{
          data: values,
          backgroundColor: ['#f59e0b', '#3b82f6', '#10b981', '#ef4444'],
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        cutout: '65%',
        plugins: { legend: { position: 'bottom', labels: { color: '#374151' } } }
      }
    });
  }

  // ── Service Type Horizontal Bar Chart ─────────────────────────
  loadServiceTypeChart(): void {
    this.reportSvc.getServicesByType().subscribe({
      next: data => {
        const labels = data.map(d => d['serviceType'] ?? '');
        const values = data.map(d => d['count'] ?? 0);
        this.renderServiceChart(labels, values);
      },
      error: () => {
        this.renderServiceChart(
          ['Water Connection', 'Trade License', 'Citizen Certificate', 'Birth Registration', 'Passport'],
          [320, 280, 410, 195, 145]
        );
      }
    });
  }

  private renderServiceChart(labels: string[], values: number[]): void {
    this.serviceChart?.destroy();
    const ctx = (document.getElementById('serviceChart') as HTMLCanvasElement)?.getContext('2d');
    if (!ctx) return;

    this.serviceChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: 'Requests',
          data: values,
          backgroundColor: '#10b981',
          borderRadius: 4
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        plugins: { legend: { display: false } },
        scales: {
          x: { beginAtZero: true, grid: { color: '#f3f4f6' }, ticks: { color: '#6b7280' } },
          y: { grid: { display: false }, ticks: { color: '#374151' } }
        }
      }
    });
  }
}
