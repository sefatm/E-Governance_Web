import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ComplaintService {

  private apiUrl = environment.apiUrl + '/complaints';

  constructor(private http: HttpClient) {}

  submitComplaint(formData: FormData): Observable<any> {
    return this.http.post(`${this.apiUrl}/create`, formData);
  }

  getAllComplaints(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/getall`);
  }

  getByMobile(mobile: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/mobile/${mobile}`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/status/${id}`, { status });
  }

  updateRemarks(id: number, remarks: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/remarks/${id}`, { remarks });
  }
}
