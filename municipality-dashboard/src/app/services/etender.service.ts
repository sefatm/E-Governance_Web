import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';   // FIX 6
import {
  ETenderNotice, ETenderBid, ETenderAward,
  VendorBlacklist, BlacklistCheckResult
} from '../models/etender.model';

@Injectable({ providedIn: 'root' })
export class ETenderService {

  // FIX 6: hardcoded environment.apiUrl + '/etender' সরানো
  private base = `${environment.apiUrl}/etender`;

  constructor(private http: HttpClient) {}

  // ── Notice ────────────────────────────────────────────────────────────────
  createNotice(data: ETenderNotice): Observable<any> {
    return this.http.post<any>(`${this.base}/notice/create`, data);
  }
  getAllNotices(): Observable<ETenderNotice[]> {
    return this.http.get<ETenderNotice[]>(`${this.base}/notice/getall`);
  }
  getOpenNotices(): Observable<ETenderNotice[]> {
    return this.http.get<ETenderNotice[]>(`${this.base}/notice/open`);
  }
  getNoticeById(id: number): Observable<ETenderNotice> {
    return this.http.get<ETenderNotice>(`${this.base}/notice/${id}`);
  }
  updateNotice(id: number, data: ETenderNotice): Observable<any> {
    return this.http.put<any>(`${this.base}/notice/update/${id}`, data);
  }
  updateNoticeStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.base}/notice/status/${id}`, { status });
  }
  deleteNotice(id: number): Observable<any> {
    return this.http.delete(`${this.base}/notice/delete/${id}`);
  }
  closeExpiredTenders(): Observable<{ message: string; closedCount: number }> {
    return this.http.post<any>(`${this.base}/notice/close-expired`, {});
  }

  // ── Bid ───────────────────────────────────────────────────────────────────
  submitBidWithDoc(formData: FormData): Observable<ETenderBid> {
    return this.http.post<ETenderBid>(`${this.base}/bid/submit-with-doc`, formData);
  }
  submitBid(data: ETenderBid): Observable<any> {
    return this.http.post<any>(`${this.base}/bid/submit`, data);
  }
  getAllBids(): Observable<ETenderBid[]> {
    return this.http.get<ETenderBid[]>(`${this.base}/bid/getall`);
  }
  getBidsByTender(tenderId: number): Observable<ETenderBid[]> {
    return this.http.get<ETenderBid[]>(`${this.base}/bid/tender/${tenderId}`);
  }
  getBidById(id: number): Observable<ETenderBid> {
    return this.http.get<ETenderBid>(`${this.base}/bid/${id}`);
  }
  updateBidStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.base}/bid/status/${id}`, { status });
  }
  verifyDocument(bidId: number, verified: boolean, remark: string): Observable<any> {
    return this.http.put(`${this.base}/bid/verify-doc/${bidId}`, { verified, remark });
  }
  getLowestBid(tenderId: number): Observable<ETenderBid> {
    return this.http.get<ETenderBid>(`${this.base}/bid/lowest/${tenderId}`);
  }
  getBidCount(tenderId: number): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.base}/bid/count/${tenderId}`);
  }

  // ── Award ─────────────────────────────────────────────────────────────────
  awardTender(data: ETenderAward): Observable<any> {
    return this.http.post<any>(`${this.base}/award/create`, data);
  }
  getAllAwards(): Observable<ETenderAward[]> {
    return this.http.get<ETenderAward[]>(`${this.base}/award/getall`);
  }
  getAwardByTender(tenderId: number): Observable<ETenderAward> {
    return this.http.get<ETenderAward>(`${this.base}/award/tender/${tenderId}`);
  }

  // ── Blacklist ─────────────────────────────────────────────────────────────
  getAllBlacklisted(): Observable<VendorBlacklist[]> {
    return this.http.get<VendorBlacklist[]>(`${this.base}/blacklist/getall`);
  }
  addBlacklist(v: VendorBlacklist): Observable<VendorBlacklist> {
    return this.http.post<VendorBlacklist>(`${this.base}/blacklist/add`, v);
  }
  unblockVendor(id: number): Observable<any> {
    return this.http.put(`${this.base}/blacklist/unblock/${id}`, {});
  }
  deleteBlacklist(id: number): Observable<any> {
    return this.http.delete(`${this.base}/blacklist/delete/${id}`);
  }
  checkBlacklist(nid?: string, email?: string, mobile?: string): Observable<BlacklistCheckResult> {
    let params = new HttpParams();
    if (nid)    params = params.set('nid', nid);
    if (email)  params = params.set('email', email);
    if (mobile) params = params.set('mobile', mobile);
    return this.http.get<BlacklistCheckResult>(`${this.base}/blacklist/check`, { params });
  }
}
