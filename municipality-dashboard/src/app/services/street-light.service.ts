import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LightRequest } from '../models/street-light.model';

@Injectable({ providedIn: 'root' })
export class LightService {

  private baseUrl = environment.apiUrl + '/street-light';

  constructor(private http: HttpClient) {}

  create(data: LightRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  getAll(): Observable<LightRequest[]> {
    return this.http.get<LightRequest[]>(`${this.baseUrl}/getall`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }
}
