import { Component, OnInit } from '@angular/core';
import { VoteService } from 'src/app/services/vote.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-e-voting-audit-logs',
  templateUrl: './e-voting-audit-logs.component.html',
  styleUrls: ['./e-voting-audit-logs.component.css']
})
export class EVotingAuditLogsComponent implements OnInit {

  logs: any[]  = [];
    filtered: any[] = [];
    isLoading    = false;
    searchText   = '';
    filterAction = '';
  
    actions = ['VOTE_CAST', 'VOTER_REGISTERED', 'VOTER_APPROVED', 'VOTER_REJECTED',
               'NOMINATION_SUBMITTED', 'CANDIDATE_APPROVED', 'CANDIDATE_REJECTED'];
  
    constructor(public ls: LanguageService, private voteService: VoteService) {}
  
    ngOnInit(): void { this.loadLogs(); }
  
    loadLogs() {
      this.isLoading = true;
      this.voteService.getAuditLogs().subscribe({
        next: (res: any[]) => {
          this.isLoading = false;
          this.logs      = res;
          this.applyFilter();
        },
        error: (err) => { this.isLoading = false; console.error(err); }
      });
    }
  
    applyFilter() {
      this.filtered = this.logs.filter(l => {
        const matchSearch = !this.searchText ||
          (l.nid || '').toLowerCase().includes(this.searchText.toLowerCase()) ||
          (l.details || '').toLowerCase().includes(this.searchText.toLowerCase());
        const matchAction = !this.filterAction || l.action === this.filterAction;
        return matchSearch && matchAction;
      });
    }
  
    formatTime(dt: string): string {
      if (!dt) return '—';
      const d = new Date(dt);
      return d.toLocaleString('en-BD', { dateStyle: 'medium', timeStyle: 'short' });
    }
  
    badgeClass(action: string): string {
      if (action?.includes('VOTE_CAST'))    return 'badge-green';
      if (action?.includes('APPROVED'))     return 'badge-blue';
      if (action?.includes('REJECTED'))     return 'badge-red';
      if (action?.includes('REGISTERED'))   return 'badge-orange';
      if (action?.includes('SUBMITTED'))    return 'badge-purple';
      return 'badge-gray';
    }
}
