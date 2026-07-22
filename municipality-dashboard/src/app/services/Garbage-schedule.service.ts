import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GarbageSchedule } from '../models/garbage-schedule.model';

@Injectable({ providedIn: 'root' })
export class ScheduleService {

  baseUrl = environment.apiUrl + '/garbage-schedule';

  constructor(private http: HttpClient) {}

  getAll(): Observable<GarbageSchedule[]> {
    return this.http.get<GarbageSchedule[]>(`${this.baseUrl}/getall`);
  }

  create(data: GarbageSchedule): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  update(id: number, data: GarbageSchedule): Observable<any> {
    return this.http.put(`${this.baseUrl}/update/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }
}
