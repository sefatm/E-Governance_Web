import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ETenderService } from 'src/app/services/etender.service';
import { ETenderNotice } from 'src/app/models/etender.model';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-etender-notice',
  templateUrl: './etender-notice.component.html',
  styleUrls: ['./etender-notice.component.css']
})
export class ETenderNoticeComponent implements OnInit {

  notices         : ETenderNotice[] = [];
  filteredNotices : ETenderNotice[] = [];
  isLoading       = false;

  // Filter
  filterOptions = ['All', 'Open', 'Closed', 'Awarded', 'Cancelled'];
  activeFilter  = 'All';

  // Detail modal
  showModal      = false;
  selectedNotice : ETenderNotice | null = null;

  constructor(public ls: LanguageService, private svc: ETenderService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.isLoading = true;
    this.svc.getAllNotices().subscribe({
      next : d => { this.notices = d; this.applyFilter(this.activeFilter); this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
  }

  applyFilter(f: string): void {
    this.activeFilter   = f;
    this.filteredNotices = f === 'All'
      ? [...this.notices]
      : this.notices.filter(n => n.status === f);
  }

  viewDetails(n: ETenderNotice): void { this.selectedNotice = n; this.showModal = true; }
  closeModal(): void { this.showModal = false; this.selectedNotice = null; }

  // ─── Gap Fix #1: Deadline helpers ────────────────────────────────────────
  daysLeft(endDate?: string): number {
    if (!endDate) return 99;
    const diff = new Date(endDate).getTime() - new Date().setHours(0,0,0,0);
    return Math.ceil(diff / 86400000);
  }

  deadlineLabel(endDate?: string): string {
    const d = this.daysLeft(endDate);
    if (d < 0)   return `${Math.abs(d)} Ended days ago`;
    if (d === 0) return 'Today!';
    if (d === 1) return 'Tomorrow!';
    return `${d} days left`;
  }

  deadlineClass(endDate?: string): string {
    const d = this.daysLeft(endDate);
    if (d < 0)  return 'dl-expired';
    if (d <= 1) return 'dl-urgent';
    if (d <= 3) return 'dl-warn';
    if (d <= 7) return 'dl-soon';
    return 'dl-safe';
  }

  // ── Stats ─────────────────────────────────────────────────────────────────
  get openCount()    { return this.notices.filter(n => n.status === 'Open').length; }
  get totalCount()   { return this.notices.length; }
  get awardedCount() { return this.notices.filter(n => n.status === 'Awarded').length; }

  // ── Helpers ───────────────────────────────────────────────────────────────
  statusClass(status: string): string {
    const m: any = { Open: 'badge-open', Closed: 'badge-closed', Awarded: 'badge-awarded', Cancelled: 'badge-cancelled' };
    return m[status] || 'badge-closed';
  }

  formatCurrency(v?: number): string {
    if (v == null) return '—';
    return '৳ ' + Number(v).toLocaleString('en-IN');
  }
}
