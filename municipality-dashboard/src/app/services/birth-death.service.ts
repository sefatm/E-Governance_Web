import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BirthDeathService {

  private baseUrl = environment.apiUrl + '/birth-death';

  constructor(private http: HttpClient) {}

  submitBirth(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/create-birth`, data);
  }

  submitDeath(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/create-death`, data);
  }

  getAll(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/getall`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }

  downloadCertificate(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/download/${id}`, { responseType: 'blob' });
  }
}
