import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Election } from 'src/app/models/election.model';
import { environment } from 'src/environments/environment';   // FIX: hardcoded localhost সরানো

@Injectable({ providedIn: 'root' })
export class ElectionService {

  private baseUrl = `${environment.apiUrl}/election`;

  constructor(private http: HttpClient) {}

  create(election: Election): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, election);
  }

  getAll(): Observable<Election[]> {
    return this.http.get<Election[]>(`${this.baseUrl}/getall`);
  }

  getById(id: number): Observable<Election> {
    return this.http.get<Election>(`${this.baseUrl}/${id}`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }
}
