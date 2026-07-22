import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

export interface DashboardKPI {
  citizenCount:     number;
  applicationCount: number;
  complaintCount:   number;
  tenderOpenCount:  number;
  noticeCount:      number;
  taxCollected:     number;
}

export interface ComplaintStats {
  pending:    number;
  inProgress: number;
  resolved:   number;
  total:      number;
}

export interface SocialCardStats {
  familyCard:  number;
  farmerCard:  number;
  lpgCard:     number;
  vgdCard:     number;
}

export interface MonthlyTax {
  label: string;
  value: number;
}

export interface ServiceRequestPoint {
  month:        string;
  citizenCert:  number;
  tradeLicense: number;
  holdingTax:   number;
  eTender:      number;
}

const BASE = environment.apiUrl;

@Injectable({ providedIn: 'root' })
export class DashboardService {

  constructor(private http: HttpClient) {}

  // ── KPI: /api/report/analytics/summary ব্যবহার করি
  // এটা সব role-এর জন্য allowed — citizen/getall (admin-only) আর call করব না
  loadAllKPIs(): Observable<DashboardKPI> {
    return forkJoin({
      summary:   this.http.get<any>(`${BASE}/report/analytics/summary`).pipe(catchError(() => of(null))),
      complaints: this.http.get<any[]>(`${BASE}/complaints/getall`).pipe(catchError(() => of([]))),
      tenders:   this.http.get<any[]>(`${BASE}/etender/notice/open`).pipe(catchError(() => of([]))),
      notices:   this.http.get<any[]>(`${BASE}/notice/active`).pipe(catchError(() => of([]))),
    }).pipe(
      map(res => ({
        citizenCount:     res.summary?.totalCitizens     ?? 0,
        applicationCount: res.summary?.totalServices     ?? 0,
        complaintCount:   res.complaints?.length         ?? 0,
        tenderOpenCount:  res.tenders?.length            ?? 0,
        noticeCount:      res.notices?.length            ?? 0,
        taxCollected:     res.summary?.totalRevenue      ?? 0,
      }))
    );
  }

  // ── Complaint status breakdown
  getComplaintStats(): Observable<ComplaintStats> {
    return this.http.get<any[]>(`${BASE}/complaints/getall`).pipe(
      map(list => {
        const pending    = list.filter(c => c.status?.toLowerCase() === 'pending').length;
        const inProgress = list.filter(c => c.status?.toLowerCase() === 'in progress' || c.status?.toLowerCase() === 'inprogress').length;
        const resolved   = list.filter(c => c.status?.toLowerCase() === 'resolved').length;
        return { pending, inProgress, resolved, total: list.length };
      }),
      catchError(() => of({ pending: 0, inProgress: 0, resolved: 0, total: 0 }))
    );
  }

  // ── Recent applications — tradeLicense & holding (no citizen/getall)
  getRecentApplications(): Observable<any[]> {
    return forkJoin({
      trade:   this.http.get<any[]>(`${BASE}/tradeLicense/getall`).pipe(catchError(() => of([]))),
      holding: this.http.get<any[]>(`${BASE}/tax-assessment/getall`).pipe(catchError(() => of([]))),
      notices: this.http.get<any[]>(`${BASE}/notice/active`).pipe(catchError(() => of([]))),
    }).pipe(
      map(res => {
        const all = [
          ...res.trade.map(a => ({ name: a.ownerName || a.applicantName || 'N/A',  service: 'Trade License', time: a.createdAt || a.applicationDate || '' })),
          ...res.holding.map(a => ({ name: a.ownerName || 'N/A',                   service: 'Holding Tax',   time: a.assessmentDate || '' })),
          ...res.notices.slice(0, 3).map(n => ({ name: n.title || 'Notice',        service: 'Notice',        time: n.createdAt || n.publishDate || '' })),
        ];
        return all
          .filter(a => a.name !== 'N/A')
          .sort((x, y) => new Date(y.time).getTime() - new Date(x.time).getTime())
          .slice(0, 5)
          .map(a => ({ ...a, time: this.timeAgo(a.time) }));
      })
    );
  }

  // ── Active notices
  getActiveNotices(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/notice/active`).pipe(
      map(list => list.slice(0, 6).map(n => ({
        id:          n.id,
        text:        n.title || n.subject || n.content,
        description: n.description || n.content || '',
        type:        n.type || 'Public',
        priority:    n.priority || 'Medium',
        publishDate: n.publishDate || n.createdAt || '',
        expiryDate:  n.expiryDate || '',
        attachmentUrl: n.attachmentUrl || null,
        route:       '/notice/public',
        queryParams: n.id ? { id: n.id } : {}
      }))),
      catchError(() => of([
        { id: null, text: 'License Renewal Notice',          description: 'Trade license renewal notice for all business holders.',  type: 'Public',    priority: 'High',   publishDate: '', expiryDate: '', attachmentUrl: null, route: '/notice/public', queryParams: {} },
        { id: null, text: 'Municipal Budget Meeting',        description: 'Annual budget meeting scheduled for next week.',           type: 'Event',     priority: 'Medium', publishDate: '', expiryDate: '', attachmentUrl: null, route: '/notice/public', queryParams: {} },
        { id: null, text: 'Road Maintenance Notice',         description: 'Road maintenance work will be conducted on main road.',    type: 'Public',    priority: 'Medium', publishDate: '', expiryDate: '', attachmentUrl: null, route: '/notice/public', queryParams: {} },
        { id: null, text: 'E-Tender: Road Construction Open',description: 'Open tender for road construction project.',               type: 'News',      priority: 'High',   publishDate: '', expiryDate: '', attachmentUrl: null, route: '/etender-notices', queryParams: {} },
        { id: null, text: 'Eid Holiday Notice',              description: 'Office will remain closed during Eid holidays.',           type: 'Event',     priority: 'Low',    publishDate: '', expiryDate: '', attachmentUrl: null, route: '/notice/public', queryParams: {} },
        { id: null, text: 'Emergency Health Advisory',       description: 'Health advisory issued for residents of the municipality.',type: 'Emergency', priority: 'High',   publishDate: '', expiryDate: '', attachmentUrl: null, route: '/notice/public', queryParams: {} },
      ]))
    );
  }

  // ── Monthly tax collection chart data
  getMonthlyTax(year: number): Observable<MonthlyTax[]> {
    return this.http.get<any[]>(`${BASE}/report/tax/monthly?year=${year}`).pipe(
      map(list => list.map(item => ({ label: item.month || item.label, value: item.amount || item.total || item.value || 0 }))),
      catchError(() => of([
        { label: 'Jan', value: 0 }, { label: 'Feb', value: 0 },
        { label: 'Mar', value: 0 }, { label: 'Apr', value: 0 },
        { label: 'May', value: 0 }, { label: 'Jun', value: 0 },
      ]))
    );
  }

  // ── Social Cards stats — ইতিমধ্যে admin endpoints, catchError দিয়ে protect
  getSocialCardStats(): Observable<SocialCardStats> {
    return forkJoin({
      family:  this.http.get<any[]>(`${BASE}/family-card/getall`).pipe(catchError(() => of([]))),
      farmer:  this.http.get<any[]>(`${BASE}/farmer-card/getall`).pipe(catchError(() => of([]))),
      lpg:     this.http.get<any[]>(`${BASE}/lpg-card/getall`).pipe(catchError(() => of([]))),
      vgd:     this.http.get<any[]>(`${BASE}/vgd-card/getall`).pipe(catchError(() => of([]))),
    }).pipe(
      map(res => ({
        familyCard:  res.family.length,
        farmerCard:  res.farmer.length,
        lpgCard:     res.lpg.length,
        vgdCard:     res.vgd.length,
      })),
      catchError(() => of({ familyCard: 0, farmerCard: 0, lpgCard: 0, vgdCard: 0 }))
    );
  }

  // ── Service requests monthly — tradeLicense & holding (no citizen/getall)
  getServiceRequests(): Observable<ServiceRequestPoint[]> {
    return forkJoin({
      trade:   this.http.get<any[]>(`${BASE}/tradeLicense/getall`).pipe(catchError(() => of([]))),
      holding: this.http.get<any[]>(`${BASE}/tax-assessment/getall`).pipe(catchError(() => of([]))),
      tender:  this.http.get<any[]>(`${BASE}/etender/bid/getall`).pipe(catchError(() => of([]))),
      complaints: this.http.get<any[]>(`${BASE}/complaints/getall`).pipe(catchError(() => of([]))),
    }).pipe(
      map(res => {
        const months = this.lastFiveMonths();
        return months.map(m => ({
          month:        m.label,
          citizenCert:  this.countByMonth(res.complaints, m.year, m.month),
          tradeLicense: this.countByMonth(res.trade,      m.year, m.month),
          holdingTax:   this.countByMonth(res.holding,    m.year, m.month),
          eTender:      this.countByMonth(res.tender,     m.year, m.month),
        }));
      }),
      catchError(() => of([
        { month:'Feb', citizenCert:0, tradeLicense:0, holdingTax:0, eTender:0 },
        { month:'Mar', citizenCert:0, tradeLicense:0, holdingTax:0, eTender:0 },
        { month:'Apr', citizenCert:0, tradeLicense:0, holdingTax:0, eTender:0 },
        { month:'May', citizenCert:0, tradeLicense:0, holdingTax:0, eTender:0 },
        { month:'Jun', citizenCert:0, tradeLicense:0, holdingTax:0, eTender:0 },
      ]))
    );
  }

  // ── Social Cards monthly
  getSocialCardMonthly(): Observable<any> {
    return forkJoin({
      family:  this.http.get<any[]>(`${BASE}/family-card/getall`).pipe(catchError(() => of([]))),
      farmer:  this.http.get<any[]>(`${BASE}/farmer-card/getall`).pipe(catchError(() => of([]))),
      lpg:     this.http.get<any[]>(`${BASE}/lpg-card/getall`).pipe(catchError(() => of([]))),
      vgd:     this.http.get<any[]>(`${BASE}/vgd-card/getall`).pipe(catchError(() => of([]))),
    }).pipe(
      map(res => {
        const months = this.lastSixMonths();
        return {
          labels:  months.map(m => m.label),
          family:  months.map(m => this.countByMonth(res.family, m.year, m.month)),
          farmer:  months.map(m => this.countByMonth(res.farmer, m.year, m.month)),
          lpg:     months.map(m => this.countByMonth(res.lpg,    m.year, m.month)),
          vgd:     months.map(m => this.countByMonth(res.vgd,    m.year, m.month)),
        };
      }),
      catchError(() => of({
        labels: ['Jan','Feb','Mar','Apr','May','Jun'],
        family: [0,0,0,0,0,0], farmer: [0,0,0,0,0,0],
        lpg:    [0,0,0,0,0,0], vgd:    [0,0,0,0,0,0],
      }))
    );
  }

  // ─── Helpers ─────────────────────────────────────────────
  private timeAgo(dateStr: string): string {
    if (!dateStr) return '';
    const diff = Date.now() - new Date(dateStr).getTime();
    const min  = Math.floor(diff / 60000);
    const hr   = Math.floor(diff / 3600000);
    const day  = Math.floor(diff / 86400000);
    if (min < 2)   return 'Just now';
    if (min < 60)  return `${min} min ago`;
    if (hr < 24)   return `${hr} hr ago`;
    if (day === 1) return 'Yesterday';
    return `${day} days ago`;
  }

  private lastFiveMonths(): { label: string; year: number; month: number }[] {
    const months: any[] = [];
    const now = new Date();
    for (let i = 4; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      months.push({ label: d.toLocaleString('en-US', { month: 'short' }), year: d.getFullYear(), month: d.getMonth() + 1 });
    }
    return months;
  }

  private lastSixMonths(): { label: string; year: number; month: number }[] {
    const months: any[] = [];
    const now = new Date();
    for (let i = 5; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      months.push({ label: d.toLocaleString('en-US', { month: 'short' }), year: d.getFullYear(), month: d.getMonth() + 1 });
    }
    return months;
  }

  private countByMonth(list: any[], year: number, month: number): number {
    return list.filter(item => {
      const fields = ['createdAt', 'applicationDate', 'assessmentDate', 'submissionDate', 'issueDate'];
      for (const f of fields) {
        if (item[f]) {
          const d = new Date(item[f]);
          if (!isNaN(d.getTime()) && d.getFullYear() === year && d.getMonth() + 1 === month) return true;
        }
      }
      return false;
    }).length;
  }
}
