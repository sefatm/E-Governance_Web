import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Nominee } from '../models/candidate.model';
import { Zone, Center } from '../models/zone-center.model';

@Injectable({
  providedIn: 'root'
})
export class NomineeService {

  private baseUrl = environment.apiUrl + '/nominee';

  constructor(private http: HttpClient) {}

  submit(formData: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, formData);
  }

  getAll(): Observable<Nominee[]> {
    return this.http.get<Nominee[]>(`${this.baseUrl}/getall`);
  }

  getApproved(electionId: number): Observable<Nominee[]> {
    return this.http.get<Nominee[]>(`${this.baseUrl}/approved/${electionId}`);
  }

  approve(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/approve/${id}`, {});
  }

  reject(id: number, reason: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/reject/${id}`, { reason });
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }

  getZones(): Observable<Zone[]> {
    return this.http.get<Zone[]>(environment.apiUrl + '/zone/getall');
  }

  getCenters(): Observable<Center[]> {
    return this.http.get<Center[]>(environment.apiUrl + '/center/getall');
  }
}
