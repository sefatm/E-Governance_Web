// src/app/services/tcb.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { TcbStock, DistributionSession, ScanResult, DistributionLog } from '../models/tcb.model';

@Injectable({ providedIn: 'root' })
export class TcbService {

  private base = environment.apiUrl + '/tcb';

  constructor(private http: HttpClient) {}

  // Stock
  createStock(stock: Partial<TcbStock>): Observable<TcbStock> {
    return this.http.post<TcbStock>(`${this.base}/stock`, stock);
  }
  getAllStock(): Observable<TcbStock[]> {
    return this.http.get<TcbStock[]>(`${this.base}/stock`);
  }

  // Session
  openSession(dealerName: string, ward: string, cycleMonth: string): Observable<any> {
    return this.http.post(`${this.base}/session/open`, { dealerName, ward, cycleMonth });
  }
  closeSession(sessionId: number): Observable<any> {
    return this.http.put(`${this.base}/session/${sessionId}/close`, {});
  }
  getSessionStatus(sessionId: number): Observable<any> {
    return this.http.get(`${this.base}/session/${sessionId}/status`);
  }
  getAllSessions(): Observable<DistributionSession[]> {
    return this.http.get<DistributionSession[]>(`${this.base}/sessions`);
  }

  // Scan
  scan(sessionId: number, cardNo: string, scannedBy = 'dealer'): Observable<ScanResult> {
    return this.http.post<ScanResult>(`${this.base}/scan`, { sessionId, cardNo, scannedBy });
  }

  // History
  getCardHistory(cardNo: string): Observable<DistributionLog[]> {
    return this.http.get<DistributionLog[]>(`${this.base}/history/${cardNo}`);
  }
}
