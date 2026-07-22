import { Component, OnInit } from '@angular/core';
import { AuditLog } from 'src/app/models/settings.model';
import { SettingsService } from 'src/app/services/settings.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-audit-logs',
  templateUrl: './audit-logs.component.html',
  styleUrls: ['./audit-logs.component.css']
})
export class AuditLogsComponent implements OnInit {

  logs          : AuditLog[] = [];
  filteredLogs  : AuditLog[] = [];
  isLoading     = false;
  expandedIndex : number | null = null;
  searchText    = '';
  filterModule  = '';

  modules = ['Auth','Payment','Trade License','Citizen','Complaint','Water Bill','E-Tender','Notification','Settings'];

  constructor(public ls: LanguageService, private settingsService: SettingsService) {}

  ngOnInit(): void { this.loadLogs(); }

  loadLogs(): void {
    this.isLoading = true;
    this.settingsService.getAllLogs().subscribe({
      next: (res) => { this.logs = res; this.filteredLogs = res; this.isLoading = false; },
      error: (err) => { console.error(err); this.isLoading = false; }
    });
  }

  filterData(): void {
    this.filteredLogs = this.logs.filter(l => {
      const matchMod    = !this.filterModule || l.module === this.filterModule;
      const matchSearch = !this.searchText   ||
        l.username?.toLowerCase().includes(this.searchText.toLowerCase()) ||
        l.action?.toLowerCase().includes(this.searchText.toLowerCase());
      return matchMod && matchSearch;
    });
  }

  clearFilter(): void {
    this.searchText = ''; this.filterModule = '';
    this.filteredLogs = this.logs;
  }

  toggleDetails(index: number): void {
    this.expandedIndex = this.expandedIndex === index ? null : index;
  }

  clearAllLogs(): void {
    if (!confirm('Clear all audit logs? This cannot be undone.')) return;
    this.settingsService.clearLogs().subscribe({
      next: () => { alert('All logs cleared.'); this.loadLogs(); },
      error: (err) => console.error(err)
    });
  }

  statusClass(status: string): string {
    return status === 'Success' ? 'approved' : 'rejected';
  }
}
