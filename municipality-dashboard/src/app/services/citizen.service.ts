import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CitizenCertificate } from '../models/citizen.model';

@Injectable({
  providedIn: 'root'
})
export class CitizenService {

  private baseUrl = environment.apiUrl + '/citizen';

  constructor(private http: HttpClient) {}

  create(data: CitizenCertificate): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  createWithFiles(fd: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, fd);
  }

  getAll(): Observable<CitizenCertificate[]> {
    return this.http.get<CitizenCertificate[]>(`${this.baseUrl}/getall`);
  }

  getById(id: number): Observable<CitizenCertificate> {
    return this.http.get<CitizenCertificate>(`${this.baseUrl}/${id}`);
  }

  updateStatus(id: number, status: string): Observable<any> {
  return this.http.put(
    `${this.baseUrl}/status/${id}`,
    { status: status }
  );
}

  downloadCertificate(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/download/${id}`, { responseType: 'blob' });
  }

  /*update(id: number, data: CitizenCertificate): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }*/
}