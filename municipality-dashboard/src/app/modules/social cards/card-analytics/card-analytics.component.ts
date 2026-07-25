import { Component, OnInit, OnDestroy } from '@angular/core';
import { FamilyCardService } from 'src/app/services/family-card.service';
import { FarmerCardService } from 'src/app/services/farmer-card.service';
import { LpgCardService } from 'src/app/services/lpg-card.service';
import { VgdCardService } from 'src/app/services/vgd-card.service';
import { Chart, registerables } from 'chart.js';
import { LanguageService } from 'src/app/services/language.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

Chart.register(...registerables);

@Component({
  selector: 'app-card-analytics',
  templateUrl: './card-analytics.component.html',
  styleUrls: ['./card-analytics.component.css']
})
export class CardAnalyticsComponent implements OnInit, OnDestroy {

  // ================= STATS =================
  stats = {
    familyTotal: 0,
    familyApproved: 0,
    familyPending: 0,

    farmerTotal: 0,
    farmerApproved: 0,
    farmerPending: 0,

    lpgTotal: 0,
    lpgApproved: 0,
    lpgPending: 0,

    vgdTotal: 0,
    vgdApproved: 0,
    vgdPending: 0,

    vgfTotal: 0,
    vgfApproved: 0
  };

  isLoading = true;

  wardData: any[] = [];

  vgdSummary = {
    vgdCount: 0,
    vgfCount: 0,
    totalRiceKg: 0,
    totalWheatKg: 0,
    totalCash: 0
  };

  farmerSubsidy = {
    totalLand: 0,
    totalFertilizer: 0,
    totalSeed: 0
  };

  private charts: Chart[] = [];

  constructor(public ls: LanguageService, 
    private familySvc: FamilyCardService,
    private farmerSvc: FarmerCardService,
    private lpgSvc: LpgCardService,
    private vgdSvc: VgdCardService
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  ngOnDestroy(): void {
    this.charts.forEach(c => c.destroy());
  }

  // ================= LOAD =================
  loadAll(): void {
    this.isLoading = true;
    this.wardData = [];

    const isApproved = (s: any) => (s || '').toLowerCase() === 'approved';
    const isPending  = (s: any) => (s || '').toLowerCase() === 'pending';

    // Ward accumulators — keyed by ward string
    const wardFamily:  Map<string, number> = new Map();
    const wardFarmer:  Map<string, number> = new Map();
    const wardLpg:     Map<string, number> = new Map();
    const wardVgd:     Map<string, number> = new Map();

    const addToWard = (map: Map<string, number>, data: any[]) => {
      data.filter(x => isApproved(x.status)).forEach(x => {
        const w = x.ward || 'Unknown';
        map.set(w, (map.get(w) || 0) + 1);
      });
    };

    forkJoin({
      family: this.familySvc.getAll().pipe(catchError(() => of([]))),
      farmer: this.farmerSvc.getAll().pipe(catchError(() => of([]))),
      lpg: this.lpgSvc.getAll().pipe(catchError(() => of([]))),
      vgd: this.vgdSvc.getAll().pipe(catchError(() => of([])))
    }).subscribe(({ family, farmer, lpg, vgd }: any) => {
      this.stats.familyTotal    = family.length;
      this.stats.familyApproved = family.filter((x: any) => isApproved(x.status)).length;
      this.stats.familyPending  = family.filter((x: any) => isPending(x.status)).length;
      addToWard(wardFamily, family);

      this.stats.farmerTotal    = farmer.length;
      this.stats.farmerApproved = farmer.filter((x: any) => isApproved(x.status)).length;
      this.stats.farmerPending  = farmer.filter((x: any) => isPending(x.status)).length;
      this.farmerSubsidy.totalLand       = farmer.filter((x: any) => isApproved(x.status)).reduce((s: number, c: any) => s + Number(c.landTotal || 0), 0);
      this.farmerSubsidy.totalFertilizer = farmer.filter((x: any) => isApproved(x.status)).reduce((s: number, c: any) => s + Number(c.fertilizerQuota || 0), 0);
      this.farmerSubsidy.totalSeed       = farmer.filter((x: any) => isApproved(x.status)).reduce((s: number, c: any) => s + Number(c.seedQuota || 0), 0);
      addToWard(wardFarmer, farmer);

      this.stats.lpgTotal    = lpg.length;
      this.stats.lpgApproved = lpg.filter((x: any) => isApproved(x.status)).length;
      this.stats.lpgPending  = lpg.filter((x: any) => isPending(x.status)).length;
      addToWard(wardLpg, lpg);

      this.stats.vgdTotal    = vgd.filter((x: any) => x.cardType === 'VGD').length;
      this.stats.vgfTotal    = vgd.filter((x: any) => x.cardType === 'VGF').length;
      this.stats.vgdApproved = vgd.filter((x: any) => x.cardType === 'VGD' && isApproved(x.status)).length;
      this.stats.vgfApproved = vgd.filter((x: any) => x.cardType === 'VGF' && isApproved(x.status)).length;
      this.stats.vgdPending  = vgd.filter((x: any) => isPending(x.status)).length;
      this.vgdSummary = {
        vgdCount:      this.stats.vgdTotal,
        vgfCount:      this.stats.vgfTotal,
        totalRiceKg:   this.stats.vgdTotal * 25,
        totalWheatKg:  this.stats.vgfTotal * 20,
        totalCash:     (this.stats.vgdTotal + this.stats.vgfTotal) * 500
      };
      addToWard(wardVgd, vgd);

      const allWards = new Set([
        ...wardFamily.keys(), ...wardFarmer.keys(),
        ...wardLpg.keys(),    ...wardVgd.keys()
      ]);
      this.wardData = Array.from(allWards).sort().map(ward => ({
        ward,
        family:  wardFamily.get(ward)  || 0,
        farmer:  wardFarmer.get(ward)  || 0,
        lpg:     wardLpg.get(ward)     || 0,
        vgd:     wardVgd.get(ward)     || 0
      }));
      this.isLoading = false;
      setTimeout(() => this.buildCharts(), 100);
    });
  }

  // ================= CHART =================
  buildCharts(): void {
    this.charts.forEach(c => c.destroy());
    this.charts = [];

    const statusCanvas = document.getElementById('statusChart') as HTMLCanvasElement;
    if (statusCanvas) {
      const ctx = statusCanvas.getContext('2d');
      if (ctx) {
        this.charts.push(new Chart(ctx, {
          type: 'bar',
          data: {
            labels: ['Family', 'Farmer', 'LPG', 'VGD', 'VGF'],
            datasets: [
              {
                label: 'Approved',
                data: [
                  this.stats.familyApproved,
                  this.stats.farmerApproved,
                  this.stats.lpgApproved,
                  this.stats.vgdApproved,
                  this.stats.vgfApproved
                ],
                backgroundColor: '#1e40af'
              },
              {
                label: 'Pending',
                data: [
                  this.stats.familyPending,
                  this.stats.farmerPending,
                  this.stats.lpgPending,
                  this.stats.vgdPending,
                  0
                ],
                backgroundColor: '#d97706'
              }
            ]
          },
          options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'top' } } }
        }));
      }
    }

    // Ward distribution chart — all 4 card types
    const wardCanvas = document.getElementById('wardChart') as HTMLCanvasElement;
    if (wardCanvas && this.wardData.length) {
      const ctx = wardCanvas.getContext('2d');
      if (ctx) {
        this.charts.push(new Chart(ctx, {
          type: 'bar',
          data: {
            labels: this.wardData.map(w => 'Ward ' + w.ward),
            datasets: [
              { label: 'Family',  data: this.wardData.map(w => w.family),  backgroundColor: '#1e40af' },
              { label: 'Farmer',  data: this.wardData.map(w => w.farmer),  backgroundColor: '#065f46' },
              { label: 'LPG',     data: this.wardData.map(w => w.lpg),     backgroundColor: '#92400e' },
              { label: 'VGD/VGF', data: this.wardData.map(w => w.vgd),     backgroundColor: '#831843' }
            ]
          },
          options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'top' } }, scales: { x: { stacked: true }, y: { stacked: true } } }
        }));
      }
    }
  }

  // ================= GETTERS =================
  get totalFamily() { return this.stats.familyTotal; }
  get approvedFamily() { return this.stats.familyApproved; }

  get totalFarmer() { return this.stats.farmerTotal; }
  get approvedFarmer() { return this.stats.farmerApproved; }

  get totalLpg() { return this.stats.lpgTotal; }
  get approvedLpg() { return this.stats.lpgApproved; }

  get totalVgd() { return this.stats.vgdTotal; }
  get approvedVgd() { return this.stats.vgdApproved; }
}
