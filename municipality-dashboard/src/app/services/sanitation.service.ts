import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Sanitation } from '../models/sanitation.model';

@Injectable({
  providedIn: 'root'
})
export class SanitationService {

  private baseUrl = environment.apiUrl + '/sanitation';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Sanitation[]> {
    return this.http.get<Sanitation[]>(`${this.baseUrl}/getall`);
  }

  create(data: Sanitation): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }
}