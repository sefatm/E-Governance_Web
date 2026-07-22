import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Ward {
  id?: number;
  number: number;
  name: string;
  area?: number | null;
  population?: number;
  representative?: string;
  contact?: string;
  status?: string;
  createdAt?: string;
  boundaryGeoJson?: string;   // GeoJSON polygon coords from ward_boundary table
}

@Injectable({ providedIn: 'root' })
export class WardService {

  private baseUrl = environment.apiUrl + '/ward';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Ward[]>                    { 
    return this.http.get<Ward[]>(`${this.baseUrl}/getall`); }

  getAllWithBoundaries(): Observable<Ward[]> {
    return this.http.get<Ward[]>(`${this.baseUrl}/getall-with-boundaries`); }

  getById(id: number): Observable<Ward>           { 
    return this.http.get<Ward>(`${this.baseUrl}/${id}`); }

  create(ward: Ward): Observable<any>             { 
    return this.http.post(`${this.baseUrl}/create`, ward); }

  update(id: number, ward: Ward): Observable<any> { 
    return this.http.put(`${this.baseUrl}/update/${id}`, ward); }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }

  delete(id: number): Observable<any>             { 
    return this.http.delete(`${this.baseUrl}/${id}`); }
}
