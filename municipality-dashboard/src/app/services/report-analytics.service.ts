import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

const BASE = environment.apiUrl + '/report';

export interface SummaryKPI {
  totalCitizens:      number;
  totalServices:      number;
  pendingRequests:    number;
  completedThisMonth: number;
  totalRevenue:       number;
  taxDueCount:        number;
}

export interface ChartPoint {
  label: string;  
  value: number;
}

@Injectable({ providedIn: 'root' })
export class ReportAnalyticsService {

  constructor(private http: HttpClient) {}

  // ── Summary KPIs 
  getSummary(): Observable<SummaryKPI> {
    return this.http.get<SummaryKPI>(`${BASE}/analytics/summary`);
  }

  // ── Citizen Report
  getCitizenReport(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/citizens`);
  }
  getCitizensByWard(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/citizens/by-ward`);
  }
  getCitizenGenderDistribution(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${BASE}/citizens/gender-distribution`);
  }

  // ── Service Report
  getServiceReport(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/services`);
  }
  getServicesByType(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/services/by-type`);
  }
  getServicesByStatus(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${BASE}/services/by-status`);
  }

  // ── Analytics 
  getMonthlyAnalytics(year: number): Observable<any[]> {
    const params = new HttpParams().set('year', year);
    return this.http.get<any[]>(`${BASE}/analytics/monthly`, { params });
  }
  getYearlyAnalytics(fromYear: number, toYear: number): Observable<any[]> {
    const params = new HttpParams().set('fromYear', fromYear).set('toYear', toYear);
    return this.http.get<any[]>(`${BASE}/analytics/yearly`, { params });
  }

  // ── Tax Collection 
  getTaxCollectionReport(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/tax`);
  }
  getTaxByWard(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE}/tax/by-ward`);
  }
  getMonthlyTaxCollection(year: number): Observable<any[]> {
    const params = new HttpParams().set('year', year);
    return this.http.get<any[]>(`${BASE}/tax/monthly`, { params });
  }
}
