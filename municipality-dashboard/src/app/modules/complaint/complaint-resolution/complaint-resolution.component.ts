import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-complaint-resolution',
  templateUrl: './complaint-resolution.component.html',
  styleUrls: ['../complaint-shared.css','./complaint-resolution.component.css']
})
export class ComplaintResolutionComponent implements OnInit {

  complaints:         any[] = [];
  filteredComplaints: any[] = [];
  searchText     = '';
  selectedStatus = '';
  expandedIndex: number | null = null;
  savingId: number | null = null;
  successMsg = '';
  errorMsg   = '';

  constructor(public ls: LanguageService, private http: HttpClient) {}

  ngOnInit(): void { this.loadComplaints(); }

  loadComplaints(): void {
    this.http.get(`${environment.apiUrl}/complaints/getall`).subscribe({
      next: (res: any) => {
        this.complaints = res.map((c: any) => ({
          id: c.id, name: c.name ?? '—', ward: c.ward ?? '', area: c.area ?? '',
          status: c.status ?? 'Pending', remarks: c.remarks ?? '', details: c
        }));
        this.filteredComplaints = [...this.complaints];
      },
      error: () => { this.errorMsg = 'অভিযোগ লোড করতে সমস্যা।'; setTimeout(() => this.errorMsg = '', 3000); }
    });
  }

  filterData(): void {
    const s = this.searchText.toLowerCase();
    this.filteredComplaints = this.complaints.filter(c =>
      (!s || c.name.toLowerCase().includes(s) || c.area.toLowerCase().includes(s)) &&
      (!this.selectedStatus || c.status === this.selectedStatus)
    );
  }

  clearFilter(): void { this.searchText = ''; this.selectedStatus = ''; this.filterData(); }

  toggleDetails(i: number): void { this.expandedIndex = this.expandedIndex === i ? null : i; }

  updateStatus(c: any, status: string): void {
    this.http.put(`${environment.apiUrl}/complaints/status/${c.id}`, { status }).subscribe({
      next: () => {
        c.status = status;
        this.successMsg = `স্ট্যাটাস "${status}" করা হয়েছে।`;
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: () => { this.errorMsg = 'স্ট্যাটাস আপডেট করা যায়নি।'; setTimeout(() => this.errorMsg = '', 3000); }
    });
  }

  saveRemarks(c: any): void {
    this.savingId = c.id;
    this.http.put(`${environment.apiUrl}/complaints/remarks/${c.id}`, { remarks: c.remarks || '' }).subscribe({
      next: () => {
        this.savingId = null;
        this.successMsg = 'মন্তব্য সংরক্ষিত হয়েছে।';
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: () => { this.savingId = null; this.errorMsg = 'মন্তব্য সংরক্ষণ করা যায়নি।'; setTimeout(() => this.errorMsg = '', 3000); }
    });
  }

  countByStatus(status: string): number { return this.complaints.filter(c => c.status === status).length; }
}
