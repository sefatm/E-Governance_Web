import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentTransaction, PaymentReceipt, PaymentSummary } from '../models/payment.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {

  private base = environment.apiUrl + '/payment';

  constructor(private http: HttpClient) {}

  // ── Transactions ──────────────────────────────────────────

  /** Initiate একটি নতুন transaction। Returns { txn, gatewayUrl } */
  initiate(data: PaymentTransaction): Observable<{ txn: PaymentTransaction; gatewayUrl: string }> {
    return this.http.post<any>(`${this.base}/initiate`, data);
  }

  /** Payment confirm করো। HoldingTax হলে backend TaxPayment table-এও save করে। */
  confirm(id: number, providerTxnId: string): Observable<{ txn: PaymentTransaction; receipt: PaymentReceipt }> {
    return this.http.put<any>(`${this.base}/confirm/${id}`, { providerTxnId });
  }

  fail(id: number, reason: string): Observable<void> {
    return this.http.put<void>(`${this.base}/fail/${id}`, { reason });
  }

  refund(id: number): Observable<void> {
    return this.http.put<void>(`${this.base}/refund/${id}`, {});
  }

  getAll(): Observable<PaymentTransaction[]> {
    return this.http.get<PaymentTransaction[]>(`${this.base}/transactions`);
  }

  getById(id: number): Observable<PaymentTransaction> {
    return this.http.get<PaymentTransaction>(`${this.base}/transactions/${id}`);
  }

  getByNid(nid: string): Observable<PaymentTransaction[]> {
    return this.http.get<PaymentTransaction[]>(`${this.base}/transactions/citizen/${nid}`);
  }

  getByStatus(status: string): Observable<PaymentTransaction[]> {
    return this.http.get<PaymentTransaction[]>(`${this.base}/transactions/status/${status}`);
  }

  // ── Receipts ──────────────────────────────────────────────

  getAllReceipts(): Observable<PaymentReceipt[]> {
    return this.http.get<PaymentReceipt[]>(`${this.base}/receipts`);
  }

  getReceiptByTxn(txnId: number): Observable<PaymentReceipt> {
    return this.http.get<PaymentReceipt>(`${this.base}/receipts/txn/${txnId}`);
  }

  downloadReceiptPdfByTxn(txnId: number, receiptNo?: string): void {
    this.downloadPdf(`${this.base}/receipts/pdf/txn/${txnId}`, `receipt-${receiptNo || txnId}.pdf`);
  }

  downloadReceiptPdfById(receiptId: number, receiptNo?: string): void {
    this.downloadPdf(`${this.base}/receipts/pdf/${receiptId}`, `receipt-${receiptNo || receiptId}.pdf`);
  }

  getReceiptsByNid(nid: string): Observable<PaymentReceipt[]> {
    return this.http.get<PaymentReceipt[]>(`${this.base}/receipts/citizen/${nid}`);
  }

  verifyReceipt(receiptNo: string): Observable<PaymentReceipt> {
    return this.http.get<PaymentReceipt>(`${this.base}/receipts/verify/${receiptNo}`);
  }

  // ── Summary ───────────────────────────────────────────────

  getSummary(): Observable<PaymentSummary> {
    return this.http.get<PaymentSummary>(`${this.base}/summary`);
  }

  private downloadPdf(url: string, fileName: string): void {
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.target = '_blank';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }
}
