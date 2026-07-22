import { Component, OnInit } from '@angular/core';
import { ReportAnalyticsService } from 'src/app/services/report-analytics.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-service-report',
  templateUrl: './service-report.component.html',
  styleUrls: ['./service-report.component.css']
})
export class ServiceReportComponent implements OnInit {

  services: any[]         = [];
  filteredServices: any[] = [];
  searchText     = '';
  selectedType   = '';
  selectedStatus = '';
  serviceTypes: string[] = [];
  statusOptions = ['Pending', 'In Progress', 'Approved', 'Rejected'];
  loading = true;

  currentPage = 1;
  pageSize    = 10;

  constructor(public ls: LanguageService, private reportSvc: ReportAnalyticsService) {}

  ngOnInit(): void {
    this.reportSvc.getServiceReport().subscribe({
      next: data => {
        this.services = data.map((s: any) => ({
          citizenName: s.citizenName ?? s.name ?? '-',
          serviceType: s.serviceType ?? '-',
          status: this.normalizeStatus(s.status),
          appliedDate: s.appliedDate ?? s.createdDate ?? s.applicationDate ?? '-',
        }));
        this.filteredServices = this.services;
        this.serviceTypes     = [...new Set(this.services.map((s: any) => s.serviceType))].filter(Boolean).sort();
        this.loading          = false;
      },
      error: () => {
        this.services = [];
        this.filteredServices = [];
        this.serviceTypes = [];
        this.loading          = false;
      }
    });
  }

  applyFilter(): void {
    this.currentPage = 1;
    this.filteredServices = this.services.filter(s => {
      const needle = this.searchText.toLowerCase();
      const matchText   = !this.searchText   || s.citizenName?.toLowerCase().includes(needle) || s.serviceType?.toLowerCase().includes(needle);
      const matchType   = !this.selectedType   || s.serviceType  === this.selectedType;
      const matchStatus = !this.selectedStatus || s.status        === this.selectedStatus;
      return matchText && matchType && matchStatus;
    });
  }

  // ✅ FIX Warning #3: clear filter — page reset সহ
  clearFilter(): void {
    this.searchText     = '';
    this.selectedType   = '';
    this.selectedStatus = '';
    this.applyFilter();
  }

  get paged(): any[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredServices.slice(start, start + this.pageSize);
  }
  get totalPages(): number { return Math.ceil(this.filteredServices.length / this.pageSize); }
  get pages(): number[]    { return Array.from({ length: this.totalPages }, (_, i) => i + 1); }
  get isFiltered(): boolean { return !!this.searchText || !!this.selectedType || !!this.selectedStatus; }

  exportCSV(): void {
    const headers = ['#', 'Citizen Name', 'Service Type', 'Status', 'Applied Date'];
    const rows    = this.filteredServices.map((s, i) =>
      [i + 1, `"${s.citizenName}"`, s.serviceType, s.status, s.appliedDate].join(',')
    );
    // ✅ FIX #5: BOM prefix — Excel-এ Bangla নাম ঠিকঠাক দেখাবে
    const csv  = '\uFEFF' + [headers.join(','), ...rows].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const a    = document.createElement('a');
    a.href     = URL.createObjectURL(blob);
    a.download = `service_report_${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    URL.revokeObjectURL(a.href);
  }

  statusClass(s: string): string {
    if (!s) return '';
    switch (s.toLowerCase()) {
      case 'approved':    return 'badge-green';
      case 'pending':     return 'badge-orange';
      case 'rejected':    return 'badge-red';
      case 'in progress': return 'badge-blue';
      default:            return 'badge-gray';
    }
  }

  private normalizeStatus(status: any): string {
    if (!status) return '-';
    let value = String(status).trim();

    if (value.startsWith('{')) {
      try {
        value = JSON.parse(value).status ?? value;
      } catch {
        return value;
      }
    }

    const normalized = value.toLowerCase().replace(/[_-]+/g, ' ');
    if (normalized === 'in progress') return 'In Progress';
    return normalized.charAt(0).toUpperCase() + normalized.slice(1);
  }

  private demoData() {
    return [
      { citizenName: 'Adam Khan',       serviceType: 'Water Connection',    status: 'Approved',    appliedDate: '2026-03-01' },
      { citizenName: 'Farhana Hossain', serviceType: 'Trade License',        status: 'In Progress', appliedDate: '2026-03-02' },
      { citizenName: 'Karim Ahmed',     serviceType: 'Citizen Certificate',  status: 'Pending',     appliedDate: '2026-03-03' },
      { citizenName: 'Sumon Ali',       serviceType: 'Birth Registration',   status: 'Approved',    appliedDate: '2026-02-20' },
      { citizenName: 'Ritu Begum',      serviceType: 'Passport Application', status: 'Rejected',    appliedDate: '2026-02-18' },
      { citizenName: 'Jamal Uddin',     serviceType: 'Trade License',        status: 'Approved',    appliedDate: '2026-02-10' },
      { citizenName: 'Nasrin Akter',    serviceType: 'Water Connection',     status: 'Pending',     appliedDate: '2026-01-30' },
    ];
  }
}
