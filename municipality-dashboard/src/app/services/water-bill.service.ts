import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { WaterBill } from '../models/water-bill.model';

@Injectable({ providedIn: 'root' })
export class WaterBillService {

  private baseUrl = environment.apiUrl + '/water-bill';

  constructor(private http: HttpClient) {}

  create(data: WaterBill): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  getAll(): Observable<WaterBill[]> {
    return this.http.get<WaterBill[]>(`${this.baseUrl}/getall`);
  }

  getById(id: number): Observable<WaterBill> {
    return this.http.get<WaterBill>(`${this.baseUrl}/${id}`);
  }

  lookup(meterNo: string, mobile: string): Observable<WaterBill[]> {
    return this.http.get<WaterBill[]>(`${this.baseUrl}/lookup`, { params: { meterNo, mobile } });
  }

  payBill(id: number, payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/pay/${id}`, payload);
  }

  downloadReceipt(receiptId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/receipt/${receiptId}/pdf`, { responseType: 'blob' });
  }

  downloadReceiptByNo(receiptNo: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/receipt/no/${encodeURIComponent(receiptNo)}/pdf`, { responseType: 'blob' });
  }

  updateAuthorityAssets(id: number, signatureBase64: string, sealBase64: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/authority-assets/${id}`, { signatureBase64, sealBase64 });
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }

  pay(id: number, payload: any = {}): Observable<any> {
    return this.http.post(`${this.baseUrl}/pay/${id}`, payload);
  }

  update(id: number, data: WaterBill): Observable<any> {
    return this.http.put(`${this.baseUrl}/update/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }
}
