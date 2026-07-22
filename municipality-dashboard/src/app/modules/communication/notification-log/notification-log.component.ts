import { Component, OnInit } from '@angular/core';
import { NotificationMessage } from 'src/app/models/communication.model';
import { CommunicationService } from 'src/app/services/communication.service';
import { LanguageService } from 'src/app/services/language.service';


@Component({
  selector: 'app-notification-log',
  templateUrl: './notification-log.component.html',
  styleUrls: ['../communication-shared.css', './notification-log.component.css']
})
export class NotificationLogComponent implements OnInit {

  notifications : NotificationMessage[] = [];
  filtered      : NotificationMessage[] = [];
  summary       : any = null;
  isLoading     = false;
  successMsg    = '';
  errorMsg      = '';

  searchText = ''; filterType = ''; filterTag = '';

  types = ['SMS','Email','Push'];
  tags  = ['General','WaterBill','TradeLicense','HoldingTax','Election','Health','ETender'];

  constructor(public ls: LanguageService, private commService: CommunicationService) {}

  ngOnInit(): void { this.loadAll(); }

  loadAll(): void {
    this.isLoading = true;
    this.commService.getAllNotifications().subscribe({
      next: (res) => { this.notifications = res; this.filtered = res; this.isLoading = false; this.filterData(); },
      error: (err) => { console.error(err); this.isLoading = false; }
    });
    this.commService.getNotifSummary().subscribe({
      next: (s) => { this.summary = s; }
    });
  }

  filterData(): void {
    this.filtered = this.notifications.filter(n => {
      const matchType   = !this.filterType || n.type === this.filterType;
      const matchTag    = !this.filterTag  || n.serviceTag === this.filterTag;
      const matchSearch = !this.searchText ||
        n.message?.toLowerCase().includes(this.searchText.toLowerCase()) ||
        n.title?.toLowerCase().includes(this.searchText.toLowerCase()) ||
        n.recipientVal?.includes(this.searchText);
      return matchType && matchTag && matchSearch;
    });
  }

  clearFilter(): void {
    this.searchText = ''; this.filterType = ''; this.filterTag = '';
    this.filtered   = this.notifications;
  }

  deleteNotif(id: number): void {
    if (!confirm('Delete this notification record?')) return;
    this.commService.deleteNotif(id).subscribe({
      next: () => {
        this.successMsg = 'Record deleted successfully.';
        this.loadAll();
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: (err) => {
        this.errorMsg = 'Delete failed.';
        console.error(err);
      }
    });
  }

  typeIcon(type: string): string {
    const icons: any = { SMS: 'fa-sms', Email: 'fa-envelope', Push: 'fa-bell' };
    return icons[type] || 'fa-bell';
  }

  typeColor(type: string): string {
    const colors: any = { SMS: '#059669', Email: '#2563eb', Push: '#7c3aed' };
    return colors[type] || '#64748b';
  }
}
