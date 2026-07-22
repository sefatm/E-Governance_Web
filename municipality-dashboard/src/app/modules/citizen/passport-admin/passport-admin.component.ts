import { Component, OnInit } from '@angular/core';
import { PassportApplication } from 'src/app/models/passport.model';
import { PassportService } from 'src/app/services/passport.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-passport-admin',
  templateUrl: './passport-admin.component.html',
  styleUrls: ['./passport-admin.component.css']
})
export class PassportAdminComponent implements OnInit {

  applications: any[] = [];   
  filtered: any[] = [];
  selected: PassportApplication | null = null;

  isLoading = false;
  searchText = '';
  filterStatus = '';

  constructor(public ls: LanguageService, private passportService: PassportService) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.isLoading = true;
    this.passportService.getAllApplications().subscribe({
      next: (res) => {
        this.applications = res.map(a => ({ ...a, showReject: false, rejectReason: '' }));
        this.applyFilter();
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  applyFilter() {
    this.filtered = this.applications.filter(a => {
      const matchSearch = !this.searchText ||
        a.fullName?.toLowerCase().includes(this.searchText.toLowerCase()) ||
        a.nidNumber?.toLowerCase().includes(this.searchText.toLowerCase());
      const matchStatus = !this.filterStatus || a.status === this.filterStatus;
      return matchSearch && matchStatus;
    });
  }

  approve(id?: number) {
    if (!id) return;
    this.passportService.approveApplication(id).subscribe({
      next: () => { alert('Application Approved.'); this.load(); },
      error: (err) => console.error(err)
    });
  }

  toggleReject(app: any) {
    app.showReject = !app.showReject;
    if (!app.showReject) app.rejectReason = '';
  }

  confirmReject(app: any) {
    if (!app.rejectReason?.trim()) {
      alert('Please enter a rejection reason.');
      return;
    }
    if (!app.id) return;
    this.passportService.rejectApplication(app.id, app.rejectReason).subscribe({
      next: () => { alert('Application Rejected.'); this.load(); },
      error: (err) => console.error(err)
    });
  }

  view(app: PassportApplication) {
    this.selected = { ...app };
  }

  closeView() {
    this.selected = null;
  }

  deleteApp(id?: number) {
    if (!id) return;
    if (!confirm('Delete this application?')) return;
    this.passportService.deleteApplication(id).subscribe({
      next: () => { alert('Deleted.'); this.load(); },
      error: (err) => console.error(err)
    });
  }

  getStatusClass(status?: string): string {
    switch (status?.toUpperCase()) {
      case 'APPROVED': return 'badge-approved';
      case 'REJECTED': return 'badge-rejected';
      default:         return 'badge-pending';
    }
  }
}
