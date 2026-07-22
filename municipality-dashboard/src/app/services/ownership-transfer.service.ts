import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OwnershipTransfer } from '../models/ownership-transfer.model';

@Injectable({ providedIn: 'root' })
export class OwnershipTransferService {

  private baseUrl = environment.apiUrl + '/ownership-transfer';

  constructor(private http: HttpClient) {}

  // Create 
  createApplication(
    data: OwnershipTransfer,
    files?: {
      currentOwnerNid?: File;
      newOwnerNid?:     File;
      deed?:            File;
    }
  ): Observable<any> {
    const fd = new FormData();

    fd.append('currentOwner',    data.currentOwner    ?? '');
    fd.append('currentOwnerNid', data.currentOwnerNid ?? '');
    fd.append('newOwner',        data.newOwner        ?? '');
    fd.append('newOwnerNid',     data.newOwnerNid     ?? '');
    fd.append('contact',         data.contact         ?? '');
    fd.append('relationship',    data.relationship    ?? '');
    fd.append('holdingNumber',   data.holdingNumber   ?? '');
    fd.append('wardNo',          data.wardNo          ?? '');
    fd.append('address',         data.address         ?? '');
    fd.append('reason',          data.reason          ?? '');
    fd.append('status',          'Pending');

    if (files?.currentOwnerNid) fd.append('currentOwnerNidFile', files.currentOwnerNid);
    if (files?.newOwnerNid)     fd.append('newOwnerNidFile',     files.newOwnerNid);
    if (files?.deed)            fd.append('deedFile',            files.deed);

    return this.http.post(`${this.baseUrl}/create`, fd);
  }

  getAll(): Observable<OwnershipTransfer[]> {
    return this.http.get<OwnershipTransfer[]>(`${this.baseUrl}/getall`);
  }

  getByUser(userId: number): Observable<OwnershipTransfer[]> {
    return this.http.get<OwnershipTransfer[]>(`${this.baseUrl}/user/${userId}`);
  }

  getById(id: number): Observable<OwnershipTransfer> {
    return this.http.get<OwnershipTransfer>(`${this.baseUrl}/${id}`);
  }

  updateStatus(id: number, status: string, rejectReason?: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status, rejectReason });
  }

  downloadCertificate(id: number): void {
    const a = document.createElement('a');
    a.href     = `${this.baseUrl}/generate-pdf/${id}`;
    a.target   = '_blank';
    a.download = `ownership-transfer-certificate-${id}.pdf`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }
}
