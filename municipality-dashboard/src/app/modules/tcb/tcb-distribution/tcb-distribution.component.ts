import { Component, OnInit } from '@angular/core';
import { TcbService } from '../../../services/tcb.service';
import { DistributionSession, DistributionLog } from '../../../models/tcb.model';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-tcb-distribution',
  templateUrl: './tcb-distribution.component.html',
  styleUrls: ['./tcb-distribution.component.css']
})
export class TcbDistributionComponent implements OnInit {

  // ── Session list ─────────────────────────────────────────────
  sessions: DistributionSession[]         = [];
  filteredSessions: DistributionSession[] = [];
  wardList: string[]                      = [];
  loadingSessions                         = false;

  // ── Filters ──────────────────────────────────────────────────
  filterDealer = '';
  filterWard   = '';
  filterStatus = '';

  // ── Session detail ───────────────────────────────────────────
  selectedSessionId: number | null = null;
  activeSessionLogs: DistributionLog[] = [];
  detailLoading = false;

  // ── Card lookup ──────────────────────────────────────────────
  lookupCardNo  = '';
  cardLogs: DistributionLog[] = [];
  lookupDone    = false;
  lookupLoading = false;

  constructor(public ls: LanguageService, private tcbSvc: TcbService) {}

  ngOnInit(): void {
    this.loadSessions();
  }

  // ── LOAD SESSIONS ─────────────────────────────────────────────
  loadSessions(): void {
    this.loadingSessions = true;

    this.tcbSvc.getAllSessions().subscribe({
      next: (res) => {
        this.sessions = res;

        // Build ward dropdown list
        this.wardList = [...new Set(
          res.map(s => s.ward).filter(Boolean)
        )].sort() as string[];

        this.applyFilter();
        this.loadingSessions = false;
      },
      error: () => {
        this.loadingSessions = false;
      }
    });
  }

  // ── FILTER ───────────────────────────────────────────────────
  applyFilter(): void {
    let list = this.sessions;

    if (this.filterDealer.trim()) {
      const q = this.filterDealer.toLowerCase();
      list = list.filter(s =>
        s.dealerName?.toLowerCase().includes(q) ||
        s.sessionCode?.toLowerCase().includes(q)
      );
    }

    if (this.filterWard) {
      list = list.filter(s => s.ward === this.filterWard);
    }

    if (this.filterStatus) {
      list = list.filter(s => s.status === this.filterStatus);
    }

    // Most recent sessions first
    this.filteredSessions = list.sort((a, b) => {
      const da = a.openedAt ? new Date(a.openedAt).getTime() : 0;
      const db = b.openedAt ? new Date(b.openedAt).getTime() : 0;
      return db - da;
    });
  }

  resetFilter(): void {
    this.filterDealer = '';
    this.filterWard   = '';
    this.filterStatus = '';
    this.applyFilter();
  }

  // ── SESSION STATUS COUNT ──────────────────────────────────────
  countByStatus(status: string): number {
    return this.sessions.filter(s => s.status === status).length;
  }

  // ── VIEW SESSION DETAIL ──────────────────────────────────────
  viewSession(id: number): void {
    // Toggle: clicking same session closes it
    if (this.selectedSessionId === id) {
      this.selectedSessionId = null;
      this.activeSessionLogs = [];
      return;
    }

    this.selectedSessionId = id;
    this.activeSessionLogs = [];
    this.detailLoading     = true;

    this.tcbSvc.getSessionStatus(id).subscribe({
      next: (res) => {
        this.activeSessionLogs = res.logs || [];
        this.detailLoading     = false;
      },
      error: () => {
        this.detailLoading = false;
      }
    });
  }

  // ── CARD LOOKUP ──────────────────────────────────────────────
  lookupCard(): void {
    const cn = this.lookupCardNo.trim();
    if (!cn) return;

    this.cardLogs     = [];
    this.lookupDone   = false;
    this.lookupLoading = true;

    this.tcbSvc.getCardHistory(cn).subscribe({
      next: (res) => {
        this.cardLogs      = res;
        this.lookupDone    = true;
        this.lookupLoading = false;
      },
      error: () => {
        this.lookupDone    = true;
        this.lookupLoading = false;
      }
    });
  }

  // ── AGGREGATE HELPERS ────────────────────────────────────────
  totalGiven(field: keyof DistributionLog): number {
    return this.activeSessionLogs.reduce((sum, l) => {
      const val = l[field];
      return sum + (typeof val === 'number' ? val : 0);
    }, 0);
  }
}
