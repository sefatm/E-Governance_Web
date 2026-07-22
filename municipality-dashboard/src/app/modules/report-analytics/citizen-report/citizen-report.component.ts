import { Component, OnInit } from '@angular/core';
import { ReportAnalyticsService } from 'src/app/services/report-analytics.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-citizen-report',
  templateUrl: './citizen-report.component.html',
  styleUrls: ['./citizen-report.component.css']
})
export class CitizenReportComponent implements OnInit {

  citizens: any[]         = [];
  filteredCitizens: any[] = [];
  searchText   = '';
  selectedWard = '';
  wards: string[] = [];
  loading = true;

  currentPage = 1;
  pageSize    = 10;

  constructor(public ls: LanguageService, private reportSvc: ReportAnalyticsService) {}

  ngOnInit(): void {
    this.reportSvc.getCitizenReport().subscribe({
      next: data => {
        // ✅ FIX #4: Backend field name mismatch — safe fallback mapping
        // Backend থেকে ward, wardNo, wardNumber যেকোনো নামে আসতে পারে
        this.citizens = data.map((c: any) => ({
          id:          c.id,
          name:        c.name         ?? c.fullName      ?? '—',
          age:         c.age          ?? c.yearsOld      ?? '—',
          ward:        c.ward         ?? c.wardNo        ?? c.wardNumber ?? c.wardName ?? c.district ?? c.address ?? '—',
          gender:      c.gender       ?? c.sex           ?? '—',
          status:      c.status       ?? '—',
          createdDate: c.createdDate  ?? c.createdAt     ?? c.appliedDate ?? c.applicationDate ?? '—',
        }));
        this.filteredCitizens = this.citizens;
        this.wards = [...new Set(this.citizens.map(c => c.ward))].filter(w => w !== '—').sort();
        this.loading = false;
      },
      error: () => {
        this.citizens = [];
        this.filteredCitizens = [];
        this.wards = [];
        this.loading          = false;
      }
    });
  }

  applyFilter(): void {
    this.currentPage = 1;
    this.filteredCitizens = this.citizens.filter(c => {
      const matchText = !this.searchText ||
        c.name?.toLowerCase().includes(this.searchText.toLowerCase()) ||
        c.id?.toString().includes(this.searchText);
      const matchWard = !this.selectedWard || c.ward === this.selectedWard;
      return matchText && matchWard;
    });
  }

  // ✅ FIX (Warning #3): filter clear method
  clearFilter(): void {
    this.searchText   = '';
    this.selectedWard = '';
    this.applyFilter();
  }

  get paged(): any[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredCitizens.slice(start, start + this.pageSize);
  }
  get totalPages(): number { return Math.ceil(this.filteredCitizens.length / this.pageSize); }
  get pages(): number[]    { return Array.from({ length: this.totalPages }, (_, i) => i + 1); }
  get isFiltered(): boolean { return !!this.searchText || !!this.selectedWard; }

  exportCSV(): void {
    const headers = ['ID', 'Name', 'Age', 'Ward', 'Gender', 'Status', 'Date'];
    const rows    = this.filteredCitizens.map(c =>
      [c.id, `"${c.name}"`, c.age, c.ward, c.gender, c.status, c.createdDate].join(',')
    );
    // ✅ FIX #5: '\uFEFF' BOM prefix — Excel-এ Bangla নাম ঠিকঠাক দেখাবে
    const csv  = '\uFEFF' + [headers.join(','), ...rows].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const a    = document.createElement('a');
    a.href     = URL.createObjectURL(blob);
    a.download = `citizen_report_${new Date().toISOString().slice(0,10)}.csv`;
    a.click();
    URL.revokeObjectURL(a.href);
  }

  statusClass(status: string): string {
    if (!status) return '';
    switch (status.toLowerCase()) {
      case 'approved': return 'badge-green';
      case 'pending':  return 'badge-orange';
      case 'rejected': return 'badge-red';
      default:         return 'badge-gray';
    }
  }

  private demoData() {
    return [
      { id:1, name:'Adam Khan',       age:32, ward:'Ward 1', gender:'Male',   status:'Approved', createdDate:'2026-03-01' },
      { id:2, name:'Farhana Hossain', age:28, ward:'Ward 2', gender:'Female', status:'Pending',  createdDate:'2026-03-05' },
      { id:3, name:'Karim Ahmed',     age:40, ward:'Ward 3', gender:'Male',   status:'Approved', createdDate:'2026-02-20' },
      { id:4, name:'Sumon Ali',       age:25, ward:'Ward 1', gender:'Male',   status:'Rejected', createdDate:'2026-02-15' },
      { id:5, name:'Ritu Begum',      age:35, ward:'Ward 4', gender:'Female', status:'Approved', createdDate:'2026-01-10' },
      { id:6, name:'Jamal Uddin',     age:44, ward:'Ward 2', gender:'Male',   status:'Pending',  createdDate:'2026-01-22' },
      { id:7, name:'Nasrin Akter',    age:30, ward:'Ward 3', gender:'Female', status:'Approved', createdDate:'2026-02-08' },
    ];
  }
}
