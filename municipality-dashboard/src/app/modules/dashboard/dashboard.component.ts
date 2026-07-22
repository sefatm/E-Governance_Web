import { Component, OnInit, AfterViewInit, OnDestroy, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { DashboardService, DashboardKPI, ComplaintStats, SocialCardStats, } from '../../services/dashboard.service';
import { forkJoin } from 'rxjs';
import { LanguageService } from 'src/app/services/language.service';

declare var Chart: any;

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {

  // ── KPI ──────────────────────────────────────────────
  kpi: DashboardKPI = {
    citizenCount: 0, applicationCount: 0, complaintCount: 0,
    tenderOpenCount: 0, noticeCount: 0, taxCollected: 0,
  };

  // ── Complaint ─────────────────────────────────────────
  complaintStats: ComplaintStats = { pending: 0, inProgress: 0, resolved: 0, total: 0 };

  // ── Social Cards ──────────────────────────────────────
  socialStats: SocialCardStats = { familyCard: 0, farmerCard: 0, lpgCard: 0, vgdCard: 0 };

  // ── Recent applications & notices ────────────────────
  applications: { name: string; service: string; time: string }[] = [];
  notices:      { text: string; route: string; queryParams?: any }[] = [];

  // ── Loading states ───────────────────────────────────
  loading = true;
  kpiLoading = true;

  // ── Chart instances ──────────────────────────────────
  private taxChart:     any = null;
  private svcChart:     any = null;
  private cmpChart:     any = null;
  private socialChart:  any = null;

  // ── Pending chart data (stored if chart not yet built) ─
  private pendingSvcData:    any = null;
  private pendingSocialData: any = null;

  activeRange = 'year'; 

  // ── Tax chart range data (filled from API) ───────────
  private taxRangeData: any = {
    week:  { labels: ['Mon','Tue','Wed','Thu','Fri','Sat','Sun'], data: [0,0,0,0,0,0,0] },
    month: { labels: ['Week 1','Week 2','Week 3','Week 4'],       data: [0,0,0,0] },
    year:  { labels: [],                                          data: [] },
  };

  private tip = {
    backgroundColor: '#0a3d1f', titleColor: '#fff',
    bodyColor: '#a5c8b0', padding: 12, cornerRadius: 8,
  };

  // ── Role ─────────────────────────────────────────────
  role: string = '';

  get isCitizen():    boolean { return this.role === 'Citizen'; }
  get isStaff():      boolean { return !this.isCitizen; }
  get isAdminLevel(): boolean { return this.role === 'Super Admin' || this.role === 'Admin / Municipal Officer'; }
  get isFinance():    boolean { return this.isAdminLevel || this.role === 'Auditor / Accountant'; }
  get isInfra():      boolean { return this.isAdminLevel || this.role === 'Department Officer' || this.role === 'Project Officer'; }
  get isHealth():     boolean { return this.isAdminLevel || this.role === 'Health / Sanitation Officer'; }
  get isSocial():     boolean { return this.isAdminLevel || this.role === 'Department Officer'; }
  get isElection():   boolean { return this.isAdminLevel || this.role === 'ElectionOfficer'; }

  getRoleIcon(): string {
    const icons: Record<string, string> = {
      'Super Admin':                 'fa-crown',
      'Admin / Municipal Officer':   'fa-user-shield',
      'Department Officer':          'fa-sitemap',
      'Project Officer':             'fa-project-diagram',
      'Health / Sanitation Officer': 'fa-heartbeat',
      'Auditor / Accountant':        'fa-calculator',
      'ElectionOfficer':             'fa-vote-yea',
      'Citizen':                     'fa-user',
    };
    return icons[this.role] || 'fa-user-circle';
  }

  getRoleBadgeClass(): string {
    const classes: Record<string, string> = {
      'Super Admin':                 'rb-super',
      'Admin / Municipal Officer':   'rb-admin',
      'Department Officer':          'rb-dept',
      'Project Officer':             'rb-project',
      'Health / Sanitation Officer': 'rb-health',
      'Auditor / Accountant':        'rb-accountant',
      'ElectionOfficer':             'rb-election',
      'Citizen':                     'rb-citizen',
    };
    return classes[this.role] || '';
  }

  constructor(public ls: LanguageService, 
    private router: Router,
    private dashSvc: DashboardService,
    private authService: AuthService,
  ) {}

  // ─────────────────────────────────────────────────────
  ngOnInit(): void {
    this.role = this.authService.getCurrentRole();
    if (!this.isCitizen) {
      this.loadAllData();
    } else {
      // Citizen — শুধু notices load করো
      this.dashSvc.getActiveNotices().subscribe({
        next: (notices) => {
          this.notices  = notices;
          this.kpi.noticeCount = notices.length;
          this.kpiLoading = false;
          this.loading    = false;
        },
        error: () => { this.kpiLoading = false; this.loading = false; }
      });
    }
  }

  ngAfterViewInit(): void {
    // Chart draw হবে data আসার পরে, তবু 300ms দিই
    setTimeout(() => this.buildPlaceholderCharts(), 300);
  }

  ngOnDestroy(): void {
    [this.taxChart, this.svcChart, this.cmpChart, this.socialChart]
      .forEach(c => c?.destroy());
  }

  // ── Notice Modal ─────────────────────────────────────
  selectedNotice: any = null;
  showNoticeModal = false;

  openNotice(n: any): void {
    if (n.id) {
      this.selectedNotice = n;
      this.showNoticeModal = true;
    } else {
      this.router.navigate([n.route], { queryParams: n.queryParams });
    }
  }

  closeNoticeModal(): void {
    this.showNoticeModal = false;
    this.selectedNotice = null;
  }

  navigate(path: string) { this.router.navigate([path]); }

  // Window resize হলে Chart.js কে force resize করা — maximize/restore fix
  @HostListener('window:resize')
  onWindowResize(): void {
    // একটু delay দিই যাতে DOM settle করে
    setTimeout(() => {
      [this.taxChart, this.svcChart, this.socialChart, this.cmpChart].forEach(c => {
        if (c) { c.resize(); }
      });
    }, 150);
  }

  // ─────────────────────────────────────────────────────
  loadAllData(): void {
    this.loading = true;

    // KPI + complaint stats + apps + notices — parallel
    forkJoin({
      kpi:       this.dashSvc.loadAllKPIs(),
      complaint: this.dashSvc.getComplaintStats(),
      apps:      this.dashSvc.getRecentApplications(),
      notices:   this.dashSvc.getActiveNotices(),
      social:    this.dashSvc.getSocialCardStats(),
    }).subscribe({
      next: res => {
        this.kpi           = res.kpi;
        this.complaintStats = res.complaint;
        this.applications  = res.apps;
        this.notices       = res.notices;
        this.socialStats   = res.social;
        this.kpiLoading    = false;
        this.loading       = false; 

        // চার্ট update
        setTimeout(() => {
          this.updateComplaintChart();
        }, 100);
      },
      error: () => { this.kpiLoading = false; this.loading = false; }
    });

    // Tax monthly chart
    const year = new Date().getFullYear();
    this.dashSvc.getMonthlyTax(year).subscribe(data => {
      this.taxRangeData.year.labels = data.map(d => d.label);
      this.taxRangeData.year.data   = data.map(d => d.value);
      if (data.length) {
        if (this.taxChart) {
          this.taxChart.data.labels           = this.taxRangeData.year.labels;
          this.taxChart.data.datasets[0].data = this.taxRangeData.year.data;
          this.taxChart.update('active');
        }
        // Mark year as the active default once data arrives
        this.activeRange = 'year';
      }
    });

    // Service requests bar chart — store data, update chart when ready
    this.dashSvc.getServiceRequests().subscribe(points => {
      this.pendingSvcData = points;
      this.updateSvcChart();
    });

    // Social cards monthly chart — store data, update chart when ready
    this.dashSvc.getSocialCardMonthly().subscribe(data => {
      this.pendingSocialData = data;
      this.updateSocialChart();
    });
  }

  // ── Tax range toggle ──────────────────────────────────
  setRange(range: string, event: Event) {
    document.querySelectorAll('.rbtn').forEach(b => b.classList.remove('active'));
    (event.target as HTMLElement).classList.add('active');
    if (!this.taxChart) return;
    const d = this.taxRangeData[range];
    this.taxChart.data.labels           = d.labels;
    this.taxChart.data.datasets[0].data = d.data;
    this.taxChart.update('active');
  }

  // ─────────────────────────────────────────────────────
  // Build charts with placeholder data first,
  // real data আসলে update হবে
  // ─────────────────────────────────────────────────────
  private buildPlaceholderCharts(): void {
    this.buildTaxChart();
    this.buildSvcChart();
    this.buildCmpChart();
    this.buildSocialChart();
  }

  private buildTaxChart(): void {
    const canvas = document.getElementById('taxChart') as HTMLCanvasElement;
    if (!canvas || typeof Chart === 'undefined') return;
    // FIX: use year data if already loaded, otherwise use week placeholder
    const rangeKey = this.taxRangeData.year.labels.length ? 'year' : 'week';
    const d = this.taxRangeData[rangeKey];
    this.taxChart = new Chart(canvas, {
      type: 'line',
      data: { labels: d.labels, datasets: [{
        label: 'Tax (Tk.)', data: d.data,
        borderColor: '#0f7a3f', backgroundColor: 'rgba(15,122,63,0.08)',
        tension: 0.4, fill: true,
        pointBackgroundColor: '#0f7a3f', pointBorderColor: '#fff',
        pointBorderWidth: 2, pointRadius: 5, pointHoverRadius: 7,
      }]},
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: { ...this.tip, callbacks: { label: (c: any) => ' Tk. ' + c.parsed.y.toLocaleString('en-IN') } },
        },
        scales: {
          x: { grid: { display: false }, ticks: { color: '#9ab09a', font: { size: 11 } } },
          y: { grid: { color: 'rgba(10,87,52,0.06)' }, ticks: { color: '#9ab09a', font: { size: 11 }, callback: (v: any) => 'Tk. ' + Number(v).toLocaleString('en-IN') } },
        },
      },
    });
  }

  private buildSvcChart(): void {
    const canvas = document.getElementById('serviceChart') as HTMLCanvasElement;
    if (!canvas || typeof Chart === 'undefined') return;
    this.svcChart = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: ['...','...','...','...','...'],
        datasets: [
          { label: 'Citizen Cert.',  data: [0,0,0,0,0], backgroundColor: 'rgba(15,122,63,0.85)',  borderRadius: 5, barPercentage: 0.6 },
          { label: 'Trade License',  data: [0,0,0,0,0], backgroundColor: 'rgba(21,101,192,0.85)', borderRadius: 5, barPercentage: 0.6 },
          { label: 'Holding Tax',    data: [0,0,0,0,0], backgroundColor: 'rgba(230,81,0,0.75)',   borderRadius: 5, barPercentage: 0.6 },
          { label: 'E-Tender Bids',  data: [0,0,0,0,0], backgroundColor: 'rgba(106,27,154,0.75)', borderRadius: 5, barPercentage: 0.6 },
        ],
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip: this.tip },
        scales: {
          x: { grid: { display: false }, ticks: { color: '#9ab09a', font: { size: 11 } } },
          y: { grid: { color: 'rgba(10,87,52,0.06)' }, ticks: { color: '#9ab09a', font: { size: 11 } }, beginAtZero: true },
        },
      },
    });
    this.updateSvcChart();
  }

  private buildCmpChart(): void {
    const canvas = document.getElementById('complaintChart') as HTMLCanvasElement;
    if (!canvas || typeof Chart === 'undefined') return;
    this.cmpChart = new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels: ['Pending','In Progress','Resolved'],
        datasets: [{ data: [0,0,0], backgroundColor: ['#EF9F27','#1565C0','#0f7a3f'], borderWidth: 0, hoverOffset: 6 }],
      },
      options: {
        responsive: false,
        maintainAspectRatio: false,
        cutout: '70%',
        plugins: { legend: { display: false }, tooltip: this.tip },
        layout: { padding: 4 },
      },
    });
    this.updateComplaintChart();
  }

  private buildSocialChart(): void {
    const canvas = document.getElementById('socialChart') as HTMLCanvasElement;
    if (!canvas || typeof Chart === 'undefined') return;
    this.socialChart = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: ['...','...','...','...','...','...'],
        datasets: [
          { label: 'Family Card (TCB)', data: [0,0,0,0,0,0], backgroundColor: 'rgba(15,122,63,0.82)',  borderRadius: 6, barPercentage: 0.65 },
          { label: 'Farmer Card',       data: [0,0,0,0,0,0], backgroundColor: 'rgba(85,139,47,0.82)',  borderRadius: 6, barPercentage: 0.65 },
          { label: 'LPG Card',          data: [0,0,0,0,0,0], backgroundColor: 'rgba(230,81,0,0.75)',   borderRadius: 6, barPercentage: 0.65 },
          { label: 'VGD / VGF',         data: [0,0,0,0,0,0], backgroundColor: 'rgba(21,101,192,0.75)', borderRadius: 6, barPercentage: 0.65 },
        ],
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: { ...this.tip, callbacks: { label: (c: any) => ' ' + c.dataset.label + ': ' + c.parsed.y + ' cards' } },
        },
        scales: {
          x: { grid: { display: false }, ticks: { color: '#9ab09a', font: { size: 11 } } },
          y: { grid: { color: 'rgba(10,87,52,0.06)' }, ticks: { color: '#9ab09a', font: { size: 11 } }, beginAtZero: true,
            title: { display: true, text: 'Cards Issued', color: '#9ab09a', font: { size: 10 } } },
        },
      },
    });
    // chart তৈরির পর pending data থাকলে সঙ্গে সঙ্গে update করো
    this.updateSocialChart();
  }

  // Complaint chart কে real data দিয়ে update করা হয়
  private updateComplaintChart(): void {
    if (!this.cmpChart) return;
    this.cmpChart.data.datasets[0].data = [
      this.complaintStats.pending,
      this.complaintStats.inProgress,
      this.complaintStats.resolved,
    ];
    this.cmpChart.update('active');
  }

  // Service Request chart update — data আগে আসলে pending রাখে, chart ready হলে update করে
  private updateSvcChart(): void {
    if (!this.svcChart || !this.pendingSvcData) return;
    const points = this.pendingSvcData;
    this.svcChart.data.labels               = points.map((p: any) => p.month);
    this.svcChart.data.datasets[0].data     = points.map((p: any) => p.citizenCert);
    this.svcChart.data.datasets[1].data     = points.map((p: any) => p.tradeLicense);
    this.svcChart.data.datasets[2].data     = points.map((p: any) => p.holdingTax);
    this.svcChart.data.datasets[3].data     = points.map((p: any) => p.eTender);
    this.svcChart.update('active');
  }

  // Social Card chart update — data আগে আসলে pending রাখে, chart ready হলে update করে
  private updateSocialChart(): void {
    if (!this.socialChart || !this.pendingSocialData) return;
    const data = this.pendingSocialData;
    this.socialChart.data.labels               = data.labels;
    this.socialChart.data.datasets[0].data     = data.family;
    this.socialChart.data.datasets[1].data     = data.farmer;
    this.socialChart.data.datasets[2].data     = data.lpg;
    this.socialChart.data.datasets[3].data     = data.vgd;
    this.socialChart.update('active');
  }
}
