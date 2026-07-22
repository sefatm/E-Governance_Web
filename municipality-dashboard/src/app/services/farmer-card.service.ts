import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { FarmerCard } from '../models/farmer-card.model';

@Injectable({ providedIn: 'root' })
export class FarmerCardService {
  
  private baseUrl = environment.apiUrl + '/farmer-card';

  constructor(private http: HttpClient) {}

  // ================= APPLY =================

  apply(fd: FormData): Observable<any> {

    return this.http.post(
      `${this.baseUrl}/apply`,
      fd
    );
  }

  // ================= GET ALL =================

  getAll(): Observable<FarmerCard[]> {

    return this.http.get<FarmerCard[]>(
      `${this.baseUrl}/getall`
    );
  }

  // ================= CHECK STATUS BY NID =================

  checkByNid(nid: string): Observable<any> {

    return this.http.get(
      `${this.baseUrl}/check/${nid}`
    );
  }

  // ================= GET BY ID =================

  getById(id: number): Observable<FarmerCard> {

    return this.http.get<FarmerCard>(
      `${this.baseUrl}/${id}`
    );
  }

  // ================= UPDATE STATUS =================

  updateStatus(
    id: number,
    status: string,
    approvedBy: string,
    rejectionReason?: string,
    signatureBase64?: string
  ): Observable<any> {

    return this.http.put(
      `${this.baseUrl}/status/${id}`,
      {
        status,
        approvedBy,
        rejectionReason,
        signatureBase64
      }
    );
  }

  // ================= DOWNLOAD CARD =================

  downloadCard(id: number): Observable<Blob> {

    return this.http.get(
      `${this.baseUrl}/download/${id}`,
      {
        responseType: 'blob'
      }
    );
  }

  // ================= LAND VERIFY =================

  verifyLand(id: number, verify: boolean = true, officer: string = 'Admin'): Observable<any> {
    return this.http.put(
      `${this.baseUrl}/verify-land/${id}`,
      { verify: verify ? 'true' : 'false', officer }
    );
  }

  // ================= DELETE =================

  delete(id: number): Observable<any> {

    return this.http.delete(
      `${this.baseUrl}/${id}`
    );
  }

  // ================= UPDATE SUBSIDY =================

  updateSubsidy(
    id: number,
    data: {
      fertilizerQuota: number;
      seedQuota: number;
      lastSubsidyDate: string;
    }
  ): Observable<any> {

    return this.http.put(
      `${this.baseUrl}/subsidy/${id}`,
      data
    );
  }
}
