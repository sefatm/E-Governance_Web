// src/app/services/farmer-distribution.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class FarmerDistributionService {

  private base     = environment.apiUrl + '/farmer';
  private cardBase = environment.apiUrl + '/farmer-card';

  constructor(private http: HttpClient) {}

  // ── Card lookup by card number (for scan) ──────────────
  lookupByCardNo(cardNo: string): Observable<any> {
    return this.http.get(`${this.cardBase}/lookup-by-cardno/${encodeURIComponent(cardNo)}`);
  }

  // ── Physical subsidy distribution ──────────────────────
  distribute(data: {
    cardId: number;
    cycleMonth: string;
    season: string;
    fertilizerKg: number;
    seedKg: number;
    pesticideLitre: number;
    distributedBy: string;
    sessionId?: number;
  }): Observable<any> {
    return this.http.post(`${this.base}/distribute`, data);
  }

  getSubsidyHistory(cardNoOrId: string | number): Observable<any> {
    return typeof cardNoOrId === 'number'
      ? this.http.get(`${this.base}/subsidy-history/${cardNoOrId}`)
      : this.http.get(`${this.base}/cycle-summary/by-card/${encodeURIComponent(cardNoOrId)}`);
  }

  getCycleSummary(cycleMonth: string): Observable<any> {
    return this.http.get(`${this.base}/cycle-summary/${cycleMonth}`);
  }

  // ── Stock management ────────────────────────────────────
  getStockList(cycleMonth?: string): Observable<any> {
    const params = cycleMonth ? `?cycleMonth=${cycleMonth}` : '';
    return this.http.get(`${this.base}/stock${params}`);
  }

  saveStock(data: {
    cycleMonth: string;
    batchNo: string | null;
    fertilizerKg: number;
    seedKg: number;
    pesticideLitre: number;
    note: string | null;
  }): Observable<any> {
    return this.http.post(`${this.base}/stock`, data);
  }

  // ── G2P Bank Transfer ──────────────────────────────────
  getAllBatches(): Observable<any> {
    return this.http.get(`${this.base}/g2p/batches`);
  }

  createBatch(data: {
    cycleMonth: string;
    amountPerFarmer: number;
    gateway: string;
    ward?: string | null;
    district?: string | null;
    submittedBy: string;
  }): Observable<any> {
    return this.http.post(`${this.base}/g2p/batch`, data);
  }

  submitBatch(batchId: number, submittedBy: string): Observable<any> {
    return this.http.put(`${this.base}/g2p/batch/${batchId}/submit`, { submittedBy });
  }

  getBatchDetail(batchId: number): Observable<any> {
    return this.http.get(`${this.base}/g2p/batch/${batchId}`);
  }

  retryFailed(batchId: number): Observable<any> {
    return this.http.put(`${this.base}/g2p/batch/${batchId}/retry`, {});
  }
}
