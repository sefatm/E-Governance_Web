import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


@Injectable({ providedIn: 'root' })
export class ProjectBudgetService {

  private baseUrl = environment.apiUrl + '/project-budget';

  constructor(private http: HttpClient) {}

  create(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  getAll(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/getall`);
  }

  getById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/update/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }
}
