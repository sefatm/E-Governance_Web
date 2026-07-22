import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DrainageRequest } from '../models/drainage.model';

@Injectable({ providedIn: 'root' })
export class DrainageService {

  private baseUrl = environment.apiUrl + '/drainage';

  constructor(private http: HttpClient) {}

  createRequest(data: DrainageRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  getAll(): Observable<DrainageRequest[]> {
    return this.http.get<DrainageRequest[]>(`${this.baseUrl}/getall`);
  }

  getById(id: number): Observable<DrainageRequest> {
    return this.http.get<DrainageRequest>(`${this.baseUrl}/${id}`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }
}
