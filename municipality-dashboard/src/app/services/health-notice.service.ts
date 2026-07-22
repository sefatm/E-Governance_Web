import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notice } from '../models/health-notice.model';


@Injectable({
  providedIn: 'root'
})
export class NoticeService {

  private baseUrl = environment.apiUrl + '/health-notice';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Notice[]> {
    return this.http.get<Notice[]>(`${this.baseUrl}/getall`);
  }

  create(notice: Notice): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, notice);
  }

  update(id: number, notice: Notice): Observable<any> {
    return this.http.put(`${this.baseUrl}/update/${id}`, notice);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
