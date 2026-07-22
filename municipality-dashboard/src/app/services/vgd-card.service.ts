// src/app/services/vgd-card.service.ts
import { Injectable } from '@angular/core';
import { HttpClient }  from '@angular/common/http';
import { Observable }  from 'rxjs';
import { environment } from 'src/environments/environment';
import { VgdCard, VgdStatusResponse } from '../models/vgd-card.model';

@Injectable({ providedIn: 'root' })
export class VgdCardService {

  private base = environment.apiUrl + '/vgd-card';

  constructor(private http: HttpClient) {}

  // ── Core CRUD ─────────────────────────────────────────────
  apply(fd: FormData): Observable<any> {
    return this.http.post(`${this.base}/apply`, fd);
  }

  getAll(): Observable<VgdCard[]> {
    return this.http.get<VgdCard[]>(`${this.base}/getall`);
  }

  getByStatus(status: string): Observable<VgdCard[]> {
    return this.http.get<VgdCard[]>(`${this.base}/status/${status}`);
  }

  getByCardType(cardType: string): Observable<VgdCard[]> {
    return this.http.get<VgdCard[]>(`${this.base}/type/${cardType}`);
  }

  getByWard(ward: string): Observable<VgdCard[]> {
    return this.http.get<VgdCard[]>(`${this.base}/ward/${ward}`);
  }

  getById(id: number): Observable<VgdCard> {
    return this.http.get<VgdCard>(`${this.base}/${id}`);
  }

  // ✅ FIX Bug 7: response now includes id field from backend
  checkByNid(nid: string): Observable<VgdStatusResponse> {
    return this.http.get<VgdStatusResponse>(`${this.base}/check/${nid}`);
  }

  updateStatus(id: number, status: string, approvedBy = 'Admin', rejectionReason?: string | null, signatureBase64?: string): Observable<any> {
    return this.http.put(`${this.base}/status/${id}`, { status, approvedBy, rejectionReason, signatureBase64 });
  }

  // ✅ FIX Bug 6: recordDistribution now accepts distMonth, distributedBy, remarks
  // (backend inserts into vgd_distribution table, not just updates lastReceivedDate)
  recordDistribution(
    id: number,
    distMonth: string,
    distributedBy: string,
    remarks = ''
  ): Observable<any> {
    return this.http.put(`${this.base}/distribute/${id}`, {
      distMonth,
      distributedBy,
      remarks
    });
  }

  // ✅ NEW: distribution history per card
  getDistributionHistory(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/${id}/distribution-history`);
  }

  // ✅ NEW: renewal
  renew(id: number): Observable<any> {
    return this.http.put(`${this.base}/renew/${id}`, {});
  }

  // ✅ NEW: expiring soon list for admin dashboard
  getExpiringSoon(days = 30): Observable<VgdCard[]> {
    return this.http.get<VgdCard[]>(`${this.base}/expiring?days=${days}`);
  }

  // ✅ FIX Bug 8: PDF download works for any status
  downloadCard(id: number): Observable<Blob> {
    return this.http.get(`${this.base}/download/${id}`, { responseType: 'blob' });
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.base}/${id}`);
  }
}
