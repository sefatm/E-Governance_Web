import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FamilyCertificate } from '../models/family.model';

@Injectable({
  providedIn: 'root'
})
export class FamilyService {

  private baseUrl = environment.apiUrl + '/family';

  constructor(private http: HttpClient) {}

  create(data: FamilyCertificate): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  getAll(): Observable<FamilyCertificate[]> {
    return this.http.get<FamilyCertificate[]>(`${this.baseUrl}/getall`);
  }

  getById(id: number): Observable<FamilyCertificate> {
    return this.http.get<FamilyCertificate>(`${this.baseUrl}/${id}`);
  }

  update(id: number, data: FamilyCertificate): Observable<any> {
    return this.http.put(`${this.baseUrl}/update/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }
  
  downloadCertificate(id: number): void {
    window.open(`${this.baseUrl}/generate-pdf/${id}`, '_blank');
  }
}
