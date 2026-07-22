import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class VgdDistributionService {
  private base = environment.apiUrl + '/vgd';
  constructor(private http: HttpClient) {}

  // Stock
  createStock(data: any): Observable<any>     { return this.http.post(`${this.base}/stock`, data); }
  getAllStock(): Observable<any[]>             { return this.http.get<any[]>(`${this.base}/stock`); }

  // Session
  openSession(data: any): Observable<any>     { return this.http.post(`${this.base}/session/open`, data); }
  closeSession(id: number): Observable<any>   { return this.http.put(`${this.base}/session/${id}/close`, {}); }
  getAllSessions(): Observable<any[]>          { return this.http.get<any[]>(`${this.base}/sessions`); }
  getSessionDetail(id: number): Observable<any>{ return this.http.get(`${this.base}/session/${id}`); }

  // Scan
  scan(data: { sessionId:number; cardNo:string; scannedBy:string }): Observable<any> {
    return this.http.post(`${this.base}/scan`, data);
  }

  // History
  getCycleSummary(cycle: string, cardType?: string): Observable<any> {
    const params = cardType ? `?cardType=${cardType}` : '';
    return this.http.get(`${this.base}/cycle-summary/${cycle}${params}`);
  }
  getCardHistory(cardId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/history/card/${cardId}`);
  }
  getCardHistoryByCardNo(cardNo: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/history/cardno/${encodeURIComponent(cardNo)}`);
  }
}