import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { WardService, Ward } from 'src/app/services/ward.service';
import { LanguageService } from 'src/app/services/language.service';

declare var Chart: any;

@Component({
  selector: 'app-population',
  templateUrl: './population.component.html',
  styleUrls: ['./population.component.css']
})
export class PopulationComponent implements OnInit, AfterViewInit, OnDestroy {

  chart: any     = null;
  isLoading      = false;
  selectedYear   = '2026';
  wards: Ward[]  = [];

  private historicalData: Record<string, any[]> = {
    '2025': [],
    '2024': [],
  };

  constructor(public ls: LanguageService, private wardService: WardService) {}

  ngOnInit(): void { this.loadData(); }

  ngAfterViewInit(): void { setTimeout(() => this.buildChart(), 150); }

  ngOnDestroy(): void {
    if (this.chart) { this.chart.destroy(); this.chart = null; }
  }

  loadData(): void {
    this.isLoading = true;
    this.wardService.getAll().subscribe({
      next: (res) => {
        this.wards = res;
        this.isLoading = false;
        this.historicalData['2025'] = res.map(w => ({ ...w, population: Math.round((w.population || 0) * 0.92) }));
        this.historicalData['2024'] = res.map(w => ({ ...w, population: Math.round((w.population || 0) * 0.84) }));
        this.updateChart();
      },
      error: () => { this.isLoading = false; }
    });
  }

  get current(): any[] {
    if (this.selectedYear === '2026') return this.wards;
    return this.historicalData[this.selectedYear] || [];
  }

  get totalPop(): number { 
    return this.current.reduce((s, w) => s + (w.population || 0), 0); }

  get highestWard(): any { 
    return this.current.reduce((a, b) => (b.population || 0) > (a.population || 0) ? b : a, this.current[0] || {}); }

  get lowestWard(): any { 
    return this.current.reduce((a, b) => (b.population || 0) < (a.population || 0) ? b : a, this.current[0] || {}); }

  get avgPop(): number { 
    return this.current.length ? Math.round(this.totalPop / this.current.length) : 0; }

  growthPct(ward: any): number {
    const prevYear = String(Number(this.selectedYear) - 1);
    const prev = prevYear === '2025'
      ? this.historicalData['2025']?.find((w: any) => w.number === ward.number)
      : this.historicalData['2024']?.find((w: any) => w.number === ward.number);
    if (!prev || !prev.population) return 0;
    return Math.round((ward.population - prev.population) / prev.population * 1000) / 10;
  }

  updateChart(): void {
    if (!this.chart) { this.buildChart(); return; }
    const ds = this.current;
    this.chart.data.labels           = ds.map((d: any) => d.name || `Ward ${d.number}`);
    this.chart.data.datasets[0].data = ds.map((d: any) => d.population || 0);
    this.chart.data.datasets[0].label = `Population (${this.selectedYear})`;
    this.chart.update('active');
  }

  buildChart(): void {
    if (typeof Chart === 'undefined') return;
    if (this.chart) { this.chart.destroy(); }
    const canvas = document.getElementById('populationChart') as HTMLCanvasElement;
    if (!canvas) return;
    const ds     = this.current;
    const colors = ds.map((_: any, i: number) => `hsl(${140 + i * 8}, 60%, ${38 + i * 2}%)`);
    this.chart = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: ds.map((d: any) => d.name || `Ward ${d.number}`),
        datasets: [{ label: `Population (${this.selectedYear})`, data: ds.map((d: any) => d.population || 0),
          backgroundColor: colors, borderRadius: 6, barPercentage: 0.65 }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false },
          tooltip: { backgroundColor: '#0a3d1f', titleColor: '#fff', bodyColor: '#a5c8b0',
            padding: 12, cornerRadius: 8,
            callbacks: { label: (ctx: any) => ' Population: ' + ctx.parsed.y.toLocaleString() } } },
        scales: {
          x: { grid: { display: false }, ticks: { color: '#5a7a5a', font: { size: 12 } } },
          y: { grid: { color: 'rgba(10,87,52,0.07)' },
            ticks: { color: '#5a7a5a', font: { size: 12 }, callback: (v: any) => Number(v).toLocaleString() },
            beginAtZero: true }
        }
      }
    });
  }
}
