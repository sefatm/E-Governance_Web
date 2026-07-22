import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConstructionRequest } from '../models/construction.model';

@Injectable({ providedIn: 'root' })
export class ConstructionService {

  private baseUrl = environment.apiUrl + '/construction';

  constructor(private http: HttpClient) {}

  create(data: ConstructionRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  getAll(): Observable<ConstructionRequest[]> {
    return this.http.get<ConstructionRequest[]>(`${this.baseUrl}/getall`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }
}
