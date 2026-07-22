import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { FamilyCard, CardStatusResponse } from '../models/family-card.model';

@Injectable({ providedIn: 'root' })
export class FamilyCardService {

  private baseUrl = environment.apiUrl + '/family-card';

  constructor(private http: HttpClient) {}

  apply(fd: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/apply`, fd);
  }

  getAll(): Observable<FamilyCard[]> {
    return this.http.get<FamilyCard[]>(`${this.baseUrl}/getall`);
  }

  getByStatus(status: string): Observable<FamilyCard[]> {
    return this.http.get<FamilyCard[]>(`${this.baseUrl}/status/${status}`);
  }

  getById(id: number): Observable<FamilyCard> {
    return this.http.get<FamilyCard>(`${this.baseUrl}/${id}`);
  }

  checkByNid(nid: string): Observable<CardStatusResponse> {
    return this.http.get<CardStatusResponse>(`${this.baseUrl}/check/${nid}`);
  }

  updateStatus(id: number, status: string, approvedBy?: string, rejectionReason?: string, signatureBase64?: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status, approvedBy, rejectionReason, signatureBase64 });
  }

  downloadCard(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/download/${id}`, { responseType: 'blob' });
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
