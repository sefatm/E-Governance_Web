import { Component, OnInit } from '@angular/core';
import { PaymentService } from 'src/app/services/payment.service';
import { PaymentTransaction, PaymentReceipt } from 'src/app/models/payment.model';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-payment-history',
  templateUrl: './payment-history.component.html',
  styleUrls: ['./payment-history.component.css']
})
export class PaymentHistoryComponent implements OnInit {

  activeTab : 'history' | 'verify' = 'history';

  transactions      : PaymentTransaction[] = [];
  expandedIndex     : number | null = null;
  searchNid         = '';
  isLoading         = false;
  notFound          = false;

  receiptNo         = '';
  verifiedReceipt   : PaymentReceipt | null = null;
  verifyNotFound    = false;
  isVerifying       = false;

  constructor(public ls: LanguageService, private paymentService: PaymentService) {}

  ngOnInit(): void {}

  searchByNid(): void {
    if (!this.searchNid.trim()) { alert('Please enter NID number.'); return; }
    this.isLoading = true;
    this.notFound  = false;

    this.paymentService.getByNid(this.searchNid.trim()).subscribe({
      next: (res) => {
        this.transactions = res;
        this.notFound     = res.length === 0;
        this.isLoading    = false;
      },
      error: (err) => { console.error(err); this.isLoading = false; }
    });
  }

  verifyReceipt(): void {
    if (!this.receiptNo.trim()) { alert('Please enter receipt number.'); return; }
    this.isVerifying    = true;
    this.verifiedReceipt = null;
    this.verifyNotFound  = false;

    this.paymentService.verifyReceipt(this.receiptNo.trim()).subscribe({
      next: (r) => {
        this.verifiedReceipt = r;
        this.isVerifying     = false;
      },
      error: () => {
        this.verifyNotFound = true;
        this.isVerifying    = false;
      }
    });
  }

  selectedReceipt: PaymentReceipt | null = null;

  viewReceipt(txnId: number): void {
    this.paymentService.getReceiptByTxn(txnId).subscribe({
      next: r => this.selectedReceipt = r,
      error: () => alert('Receipt not found')
    });
  }

  downloadReceiptPdf(txnId?: number, receiptNo?: string): void {
    if (!txnId) return;
    this.paymentService.downloadReceiptPdfByTxn(txnId, receiptNo);
  }

  toggleDetails(index: number): void {
    this.expandedIndex = this.expandedIndex === index ? null : index;
  }

  statusClass(status: string): string {
    const m: any = {
      Completed: 'approved', Pending: 'pending',
      Failed: 'rejected',    Refunded: 'refunded'
    };
    return m[status] || 'pending';
  }
}
