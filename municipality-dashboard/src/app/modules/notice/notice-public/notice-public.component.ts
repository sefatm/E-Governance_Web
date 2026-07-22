import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { Notice } from 'src/app/models/notice.model';
import { NoticeService } from 'src/app/services/notice.service';
import { LanguageService } from 'src/app/services/language.service';


@Component({
  selector: 'app-notice-public',
  templateUrl: './notice-public.component.html',
  styleUrls: ['./notice-public.component.css']
})
export class NoticePublicComponent implements OnInit {

  notices        : Notice[] = [];
  filteredNotices: Notice[] = [];
  isLoading      = false;
  activeFilter   = 'All';
  selectedNotice : Notice | null = null;
  showModal      = false;
  private openedFromDashboard = false;

  filterOptions = ['All', 'Public', 'Emergency', 'Event', 'News'];

  constructor(public ls: LanguageService, 
    private noticeService: NoticeService,
    private route: ActivatedRoute,
    private router: Router,
    private location: Location
  ) {}

  ngOnInit(): void { this.loadNotices(); }

  loadNotices(): void {
    this.isLoading = true;
    this.noticeService.getActive().subscribe({
      next: (res) => {
        this.notices         = res;
        this.filteredNotices = res;
        this.isLoading       = false;
        // Auto-open modal if ?id=X was passed from dashboard notice click
        const idParam = this.route.snapshot.queryParamMap.get('id');
        if (idParam) {
          const target = res.find((n: Notice) => String(n.id) === idParam);
          if (target) {
            this.openedFromDashboard = true;
            this.viewDetails(target);
          }
        }
      },
      error: (err) => { console.error(err); this.isLoading = false; }
    });
  }

  applyFilter(type: string): void {
    this.activeFilter    = type;
    this.filteredNotices = type === 'All'
      ? this.notices
      : this.notices.filter(n => n.type === type);
  }

  viewDetails(notice: Notice): void {
    this.selectedNotice = notice;
    this.showModal      = true;
  }

  closeModal(): void {
    this.showModal      = false;
    this.selectedNotice = null;
    if (this.openedFromDashboard) {
      this.openedFromDashboard = false;
      this.router.navigate(['/dashboard']);
    }
  }

  typeIcon(type: string): string {
    const map: any = {
      Public   : 'fas fa-globe',
      Emergency: 'fas fa-exclamation-triangle',
      Event    : 'fas fa-calendar-alt',
      News     : 'fas fa-newspaper'
    };
    return map[type] || 'fas fa-bell';
  }

  priorityClass(p: string): string {
    const map: any = { High: 'p-high', Medium: 'p-medium', Low: 'p-low' };
    return map[p] || 'p-medium';
  }

  get emergencyNotices(): Notice[] {
    return this.notices.filter(n => n.type === 'Emergency');
  }
}
