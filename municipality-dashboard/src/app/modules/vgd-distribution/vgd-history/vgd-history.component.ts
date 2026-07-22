import { Component, OnInit } from '@angular/core';
import { VgdDistributionService } from 'src/app/services/vgd-distribution.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-vgd-history',
  templateUrl: './vgd-history.component.html',
  styleUrls: ['./vgd-history.component.css']
})
export class VgdHistoryComponent implements OnInit {

  tab = 'cycle';

  // Cycle tab
  selectedCycle  = new Date().toISOString().slice(0,7);
  filterCardType = '';
  filterWard     = '';
  wardList: string[] = [];
  logs: any[]         = [];
  filteredLogs: any[] = [];
  isLoading = false;

  // Sessions tab
  sessions: any[]      = [];
  sessionsLoading      = false;
  selectedSessionId: number | null = null;
  sessionDetail: any   = null;

  // Lookup tab
  lookupCardNo  = '';
  cardLogs: any[] = [];
  lookupDone    = false;
  lookupLoading = false;

  get totalRice(): number { return this.filteredLogs.reduce((s,r)=>s+(+r[5]||0),0); }
  get totalCash(): number { return this.filteredLogs.reduce((s,r)=>s+(+r[7]||0),0); }

  constructor(public ls: LanguageService, private svc: VgdDistributionService) {}

  ngOnInit(): void { this.loadCycle(); }

  loadCycle(): void {
    this.isLoading = true;
    this.svc.getCycleSummary(this.selectedCycle, this.filterCardType).subscribe({
      next: (res:any) => {
        this.logs     = res.logs || [];
        this.wardList = [...new Set(this.logs.map((r:any)=>r[4]).filter(Boolean))] as string[];
        this.applyLocalFilter();
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  applyLocalFilter(): void {
    this.filteredLogs = this.filterWard
      ? this.logs.filter((r:any) => r[4] === this.filterWard)
      : this.logs;
  }

  loadSessions(): void {
    this.sessionsLoading = true;
    this.svc.getAllSessions().subscribe({
      next: (res:any[]) => { this.sessions = res; this.sessionsLoading = false; },
      error: ()         => { this.sessionsLoading = false; }
    });
  }

  viewSession(id: number): void {
    if (this.selectedSessionId === id) { this.selectedSessionId=null; this.sessionDetail=null; return; }
    this.selectedSessionId = id;
    this.svc.getSessionDetail(id).subscribe({
      next: (res:any) => { this.sessionDetail = res; }
    });
  }

  lookupCard(): void {
    if (!this.lookupCardNo.trim()) return;
    this.cardLogs=[]; this.lookupDone=false; this.lookupLoading=true;
    this.svc.getCardHistoryByCardNo(this.lookupCardNo.trim()).subscribe({
      next: (res:any[]) => { this.cardLogs=res; this.lookupDone=true; this.lookupLoading=false; },
      error: ()         => { this.lookupDone=true; this.lookupLoading=false; }
    });
  }

  exportCsv(): void {
    const rows = [
      ['#','নাম','কার্ড নং','ধরন','ওয়ার্ড','চাল (kg)','গম (kg)','নগদ (৳)','তারিখ','বিতরণকারী'],
      ...this.filteredLogs.map((r:any,i:number)=>[i+1,r[1],r[2],r[3],r[4],r[5],r[6],r[7],r[8],r[9]])
    ];
    const blob = new Blob([rows.map(r=>r.join(',')).join('\n')],{type:'text/csv'});
    const a = document.createElement('a');
    a.href=URL.createObjectURL(blob);
    a.download=`vgd_${this.selectedCycle}.csv`; a.click();
  }

}
