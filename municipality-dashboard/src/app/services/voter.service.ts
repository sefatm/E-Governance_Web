import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Voter } from '../models/voter.model';
import { environment } from 'src/environments/environment';   // ← FIX

@Injectable({ providedIn: 'root' })
export class VoterService {

  // FIX: hardcoded localhost সরানো হয়েছে
  private baseUrl = `${environment.apiUrl}/voter`;

  constructor(private http: HttpClient) {}

  register(voter: Voter | FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, voter);
  }

  getAll(): Observable<Voter[]> {
    return this.http.get<Voter[]>(`${this.baseUrl}/getall`);
  }

  approve(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/approve/${id}`, {});
  }

  reject(id: number, reason: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/reject/${id}`, { reason });
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }

  verify(nid: string, dob: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/verify`, { nid, dob });
  }

  hasVoted(voterId: number, electionId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/has-voted/${voterId}/${electionId}`);
  }

  // FIX: photo upload — voter-register component ব্যবহার করবে
}
