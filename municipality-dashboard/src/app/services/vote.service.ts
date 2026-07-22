import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditLog } from '../models/zone-center.model';
import { environment } from 'src/environments/environment';   // ← FIX: env variable

// FIX: voterId backend-এ JWT থেকে নেওয়া হচ্ছে, তাই interface থেকে সরানো হয়েছে
export interface VoteCast {
  electionId:  number;
  candidateId: number;
  voterId:     number;   // FIX: voter_registration.id — NID verify করার পর sessionStorage থেকে আসে
}

export interface VoteResult {
  candidateId:    number;
  name:           string;
  party:          string;
  votes:          number;
  percent?:       number;
  symbol?:        string;
  symbolFileUrl?: string;
}

export interface AnalyticsResponse {
  totalVotes:          number;
  totalCandidates:     number;
  totalApprovedVoters: number;
  turnoutPercent:      number;
  results:             VoteResult[];
}

@Injectable({ providedIn: 'root' })
export class VoteService {

  // FIX: hardcoded localhost:8080 সরানো — environment variable ব্যবহার করছে
  private baseUrl = `${environment.apiUrl}/vote`;

  constructor(private http: HttpClient) {}

  cast(vote: VoteCast): Observable<any> {
    return this.http.post(`${this.baseUrl}/cast`, vote);
  }

  getResult(electionId: number): Observable<VoteResult[]> {
    return this.http.get<VoteResult[]>(`${this.baseUrl}/result/${electionId}`);
  }

  getAnalytics(electionId: number): Observable<AnalyticsResponse> {
    return this.http.get<AnalyticsResponse>(`${this.baseUrl}/analytics/${electionId}`);
  }

  getAuditLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${environment.apiUrl}/auditlog/getall`);
  }
}
