import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PassportApplication } from '../models/passport.model';

@Injectable({
  providedIn: 'root'
})
export class PassportService {

  private baseUrl = environment.apiUrl + '/passport';

  constructor(private http: HttpClient) { }

  createApplication(data: PassportApplication): Observable<PassportApplication> {
    return this.http.post<PassportApplication>(`${this.baseUrl}/create`, data);
  }

  getAllApplications(): Observable<PassportApplication[]> {
    return this.http.get<PassportApplication[]>(`${this.baseUrl}/getall`);
  }

  getApplicationById(id: number): Observable<PassportApplication> {
    return this.http.get<PassportApplication>(`${this.baseUrl}/${id}`);
  }

  updateApplication(id: number, data: PassportApplication): Observable<PassportApplication> {
    return this.http.put<PassportApplication>(`${this.baseUrl}/update/${id}`, data);
  }

  deleteApplication(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }

  approveApplication(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/approve/${id}`, {});
  }

  rejectApplication(id: number, reason: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/reject/${id}`, { reason });
  }
}
