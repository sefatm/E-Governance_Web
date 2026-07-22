import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { WaterConnection } from '../models/water-connection.model';

@Injectable({ providedIn: 'root' })
export class WaterConnectionService {

  private baseUrl = environment.apiUrl + '/water-connection';

  constructor(private http: HttpClient) {}

  create(data: WaterConnection): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  getAll(): Observable<WaterConnection[]> {
    return this.http.get<WaterConnection[]>(`${this.baseUrl}/getall`);
  }

  getById(id: number): Observable<WaterConnection> {
    return this.http.get<WaterConnection>(`${this.baseUrl}/${id}`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }

  update(id: number, data: WaterConnection): Observable<any> {
    return this.http.put(`${this.baseUrl}/update/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }
}
