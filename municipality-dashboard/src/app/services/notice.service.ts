import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notice } from '../models/notice.model';

@Injectable({ providedIn: 'root' })
export class NoticeService {

  private baseUrl = environment.apiUrl + '/notice';

  constructor(private http: HttpClient) {}

  // ── Admin

  create(notice: Notice): Observable<Notice> {
    return this.http.post<Notice>(`${this.baseUrl}/create`, notice);
  }

  getAll(): Observable<Notice[]> {
    return this.http.get<Notice[]>(`${this.baseUrl}/getall`);
  }

  getById(id: number): Observable<Notice> {
    return this.http.get<Notice>(`${this.baseUrl}/${id}`);
  }

  update(id: number, notice: Notice): Observable<Notice> {
    return this.http.put<Notice>(`${this.baseUrl}/update/${id}`, notice);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }

  // ── Public

  getActive(): Observable<Notice[]> {
    return this.http.get<Notice[]>(`${this.baseUrl}/active`);
  }

  getByType(type: string): Observable<Notice[]> {
    return this.http.get<Notice[]>(`${this.baseUrl}/type/${type}`);
  }
}
