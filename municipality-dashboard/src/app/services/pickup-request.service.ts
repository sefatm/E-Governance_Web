import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { WasteRequest } from '../models/pickup-request.model';

@Injectable({ providedIn: 'root' })
export class WasteRequestService {
  private baseUrl = environment.apiUrl + '/waste-request';
  constructor(private http: HttpClient) {}
  getAll(): Observable<WasteRequest[]> { return this.http.get<WasteRequest[]>(`${this.baseUrl}/getall`); }
  getByPhone(phone: string): Observable<WasteRequest[]> { return this.http.get<WasteRequest[]>(`${this.baseUrl}/phone/${encodeURIComponent(phone)}`); }
  create(req: WasteRequest): Observable<any> { return this.http.post(`${this.baseUrl}/create`, req); }
  updateStatus(id: number, status: string): Observable<any> { return this.http.put(`${this.baseUrl}/status/${id}`, { status }); }
  delete(id: number): Observable<any> { return this.http.delete(`${this.baseUrl}/delete/${id}`); }
}
