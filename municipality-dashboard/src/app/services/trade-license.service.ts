import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TradeLicenseService {

  private baseUrl = environment.apiUrl + '/tradeLicense';

  constructor(private http: HttpClient) {}

  submitTradeLicense(formData: FormData): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/create`, formData);
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

  verify(licenseNumber: string, birthDate: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/verify`, { licenseNumber, birthDate });
  }

  downloadCertificate(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/certificate/${id}`, { responseType: 'blob' });
  }
}
