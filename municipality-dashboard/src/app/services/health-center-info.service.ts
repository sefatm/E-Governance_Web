import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class HealthCenterService {

  private baseUrl = environment.apiUrl + '/health-center';

  constructor(private http: HttpClient) {}

  getAll() {
    return this.http.get<any[]>(`${this.baseUrl}/getall`);
  }

  create(data: any) {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  delete(id: number) {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }
}