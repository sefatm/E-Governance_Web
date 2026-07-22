// lpg-card.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http'; // এখানে HttpParams যোগ করা হয়েছে
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { LpgCard, LpgStatusResponse } from '../models/lpg-card.model';

@Injectable({ providedIn: 'root' })
export class LpgCardService {

  private base = environment.apiUrl + '/lpg-card';

  constructor(private http: HttpClient) {}

  // ── Card management (আগের মতো) ────────────────────────────
  apply(fd: FormData): Observable<any> {
    return this.http.post(`${this.base}/apply`, fd);
  }
  getAll(): Observable<LpgCard[]> {
    return this.http.get<LpgCard[]>(`${this.base}/getall`);
  }
  getByStatus(status: string): Observable<LpgCard[]> {
    return this.http.get<LpgCard[]>(`${this.base}/status/${status}`);
  }
  getByDealer(dealerCode: string): Observable<LpgCard[]> {
    return this.http.get<LpgCard[]>(`${this.base}/dealer/${dealerCode}`);
  }
  getById(id: number): Observable<LpgCard> {
    return this.http.get<LpgCard>(`${this.base}/${id}`);
  }
  checkByNid(nid: string): Observable<LpgStatusResponse> {
    return this.http.get<LpgStatusResponse>(`${this.base}/check/${nid}`);
  }
  lookupByCardNo(cardNo: string): Observable<LpgCard> {
    return this.http.get<LpgCard>(`${this.base}/by-cardno/${encodeURIComponent(cardNo)}`);
  }
  updateStatus(id: number, status: string, approvedBy?: string, rejectionReason?: string, signatureBase64?: string): Observable<any> {
    return this.http.put(`${this.base}/status/${id}`, { status, approvedBy, rejectionReason, signatureBase64 });
  }
  recordCollection(id: number): Observable<any> {
    return this.http.put(`${this.base}/collect/${id}`, {});
  }
  downloadCard(id: number): Observable<Blob> {
    return this.http.get(`${this.base}/download/${id}`, { responseType: 'blob' });
  }
  delete(id: number): Observable<any> {
    return this.http.delete(`${this.base}/${id}`);
  }

  // ── Distribution (নতুন) ────────────────────────────────────
  /**
   * POST /api/lpg-card/distribute
   * Dealer সিলিন্ডার বিতরণ রেকর্ড করেন।
   */
  distribute(data: {
    cardId: number;
    cycleMonth: string;
    cylindersQty?: number;
    collectedBy?: string;
  }): Observable<any> {
    return this.http.post(`${this.base}/distribute`, data);
  }

  // ── History (নতুন) ─────────────────────────────────────────
  getHistoryByCardId(cardId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/history/${cardId}`);
  }
  getHistoryByCardNo(cardNo: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/history/by-cardno/${encodeURIComponent(cardNo)}`);
  }
  getCycleSummary(cycleMonth: string): Observable<any> {
    return this.http.get<any>(`${this.base}/cycle-summary/${cycleMonth}`);
  }
  getDealerHistory(cycleMonth: string, dealerCode: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/dealer-history`, {
      params: { cycleMonth, dealerCode }
    });
  }

  // ── Stock (নতুন) ────────────────────────────────────────────
  getStockList(cycleMonth?: string): Observable<any[]> {
    // HttpParams ব্যবহার করে undefined বা টাইপ মিসম্যাচ এরর দূর করা হয়েছে
    let params = new HttpParams();
    if (cycleMonth) {
      params = params.set('cycleMonth', cycleMonth);
    }
    return this.http.get<any[]>(`${this.base}/stock`, { params });
  }

  saveStock(data: {
    cycleMonth: string;
    batchLabel?: string;
    ward?: string;
    dealerName?: string;
    dealerCode?: string;
    cylinderSize?: string;
    totalCylinders: number;
    totalCards?: number;
  }): Observable<any> {
    return this.http.post(`${this.base}/stock`, data);
  }
}