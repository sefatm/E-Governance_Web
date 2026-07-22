import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class EpiService {

  private base = environment.apiUrl + '/epi';

  constructor(private http: HttpClient) {}

  registerChild(child: any): Observable<any> { 
    return this.http.post(`${this.base}/register`, child); }

  getAllChildren(): Observable<any[]>{ 
    return this.http.get<any[]>(`${this.base}/children`); }

  searchChildren(q: string): Observable<any[]>{ 
    return this.http.get<any[]>(`${this.base}/children/search`, { params: { q } }); }
    
  getChildById(id: number): Observable<any> { 
    return this.http.get(`${this.base}/children/${id}`); }

  checkByCardNo(cardNo: string): Observable<any> { 
    return this.http.get(`${this.base}/check/${cardNo}`); }

  getSchedule(childId: number): Observable<any[]>{ 
    return this.http.get<any[]>(`${this.base}/schedule/${childId}`); }

  scanCard(payload: string): Observable<any> {
    return this.http.get(`${this.base}/scan/${encodeURIComponent(payload)}`);
  }

  markGiven(vaccId: number, body: any): Observable<any> { 
    return this.http.put(`${this.base}/vaccinate/${vaccId}`, body); }

  markMissed(vaccId: number): Observable<any> { 
    return this.http.put(`${this.base}/missed/${vaccId}`, {}); }

  getStats(): Observable<any> { 
    return this.http.get(`${this.base}/stats`); }

  getUpcoming(): Observable<any[]>{ 
    return this.http.get<any[]>(`${this.base}/upcoming`); }

  getMissed(): Observable<any[]>{ 
    return this.http.get<any[]>(`${this.base}/missed`); }

  deleteChild(id: number): Observable<any> { 
    return this.http.delete(`${this.base}/children/${id}`); }

  // Pending children list
  getPendingChildren(): Observable<any[]> { 
    return this.http.get<any[]>(`${this.base}/children/pending`); }

  // Approve child registration (admin)
  approveChild(id: number, body: any = {}): Observable<any> { 
    return this.http.put(`${this.base}/approve/${id}`, body); }

  // Download EPI card PDF — HttpClient দিয়ে token সহ
  downloadCard(childId: number, cardNo: string): void {
    const url = `${this.base}/generate-pdf/${childId}`;
    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const objUrl = URL.createObjectURL(blob);
        const a      = document.createElement('a');
        a.href       = objUrl;
        a.download   = `EPI-Card-${cardNo}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(objUrl);
      },
      error: (err) => {
        console.error('PDF download failed:', err);
        alert('PDF download failed. Please try again.');
      }
    });
  }
}
