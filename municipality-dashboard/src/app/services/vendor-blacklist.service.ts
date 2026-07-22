import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

// VendorBlacklist interface — এখানে define করা হয়েছে
// vendor-blacklist.component.ts এ এখান থেকে import করো
export interface VendorBlacklist {
  id?: number;
  nid?: string;
  email?: string;
  mobile?: string;
  vendorName?: string;
  companyName?: string;
  reason?: string;
  blacklistedBy?: string;
  blacklistedAt?: string;
  active?: boolean;
}

export interface BlacklistCheckResult {
  blacklisted: boolean;
  message: string;
}

/**
 * FIX 4: VendorBlacklistComponent এই service import করছিল কিন্তু file ছিল না।
 * এই file টি src/app/services/vendor-blacklist.service.ts এ রাখুন।
 */
@Injectable({ providedIn: 'root' })
export class VendorBlacklistService {

  // FIX 6: hardcoded localhost সরানো — environment variable ব্যবহার
  private baseUrl = `${environment.apiUrl}/etender/blacklist`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<VendorBlacklist[]> {
    return this.http.get<VendorBlacklist[]>(`${this.baseUrl}/getall`);
  }

  add(v: VendorBlacklist): Observable<VendorBlacklist> {
    return this.http.post<VendorBlacklist>(`${this.baseUrl}/add`, v);
  }

  unblock(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/unblock/${id}`, {});
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }

  check(nid?: string, email?: string, mobile?: string): Observable<BlacklistCheckResult> {
    let params = new HttpParams();
    if (nid)    params = params.set('nid', nid);
    if (email)  params = params.set('email', email);
    if (mobile) params = params.set('mobile', mobile);
    return this.http.get<BlacklistCheckResult>(`${this.baseUrl}/check`, { params });
  }
}
