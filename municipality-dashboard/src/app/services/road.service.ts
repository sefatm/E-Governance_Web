import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RoadRequest } from '../models/road.model';

@Injectable({
  providedIn: 'root'
})
export class RoadService {

  private apiUrl = environment.apiUrl + '/road';

  constructor(private http: HttpClient) {}

  createRoadRequest(data: RoadRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/create`, data);
  }

  getAll(): Observable<RoadRequest[]> {
    return this.http.get<RoadRequest[]>(`${this.apiUrl}/getall`);
  }

  getById(id: number): Observable<RoadRequest> {
    return this.http.get<RoadRequest>(`${this.apiUrl}/${id}`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(
      `${this.apiUrl}/status/${id}`,
      status, 
      {
        headers: { 'Content-Type': 'text/plain' }
      }
    );
  }
}