import { Component, OnInit } from '@angular/core';
import { PaymentService } from 'src/app/services/payment.service';
import { PaymentTransaction, PaymentReceipt, PaymentSummary } from 'src/app/models/payment.model';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-payment-admin',
  templateUrl: './payment-admin.component.html',
  styleUrls: ['./payment-admin.component.css']
})
export class PaymentAdminComponent implements OnInit {

  activeTab: 'transactions' | 'receipts' = 'transactions';

  transactions  : PaymentTransaction[] = [];
  filteredTxns  : PaymentTransaction[] = [];
  receipts      : PaymentReceipt[]     = [];
  summary       : PaymentSummary | null = null;

  isLoading     = false;
  expandedIndex : number | null = null;

  searchText     = '';
  filterStatus   = '';
  filterService  = '';
  filterMethod   = '';

  statuses   = ['Pending', 'Completed', 'Failed', 'Refunded'];
  services   = ['WaterBill', 'TradeLicense', 'HoldingTax', 'ETender', 'Other'];
  methods    = ['Bkash', 'Nagad', 'Rocket', 'Card', 'Bank'];

  constructor(public ls: LanguageService, private paymentService: PaymentService) {}

  ngOnInit(): void { this.loadAll(); }

  loadAll(): void {
    this.isLoading = true;
    this.paymentService.getAll().subscribe({
      next: (res) => {
        this.transactions = res;
        this.filteredTxns = res;
        this.isLoading    = false;
      },
      error: (err) => { console.error(err); this.isLoading = false; }
    });

    this.paymentService.getAllReceipts().subscribe({
      next: (res) => { this.receipts = res; }
    });

    this.paymentService.getSummary().subscribe({
      next: (s) => { this.summary = s; }
    });
  }

  filterData(): void {
    this.filteredTxns = this.transactions.filter(t => {
      const matchStatus  = !this.filterStatus  || t.status      === this.filterStatus;
      const matchService = !this.filterService || t.serviceType === this.filterService;
      const matchMethod  = !this.filterMethod  || t.method      === this.filterMethod;
      const matchSearch  = !this.searchText    ||
        t.citizenName?.toLowerCase().includes(this.searchText.toLowerCase()) ||
        t.citizenNid?.includes(this.searchText) ||
        t.txnRef?.toLowerCase().includes(this.searchText.toLowerCase());
      return matchStatus && matchService && matchMethod && matchSearch;
    });
  }

  clearFilter(): void {
    this.searchText = ''; this.filterStatus = '';
    this.filterService = ''; this.filterMethod = '';
    this.filteredTxns = this.transactions;
  }

  toggleDetails(index: number): void {
    this.expandedIndex = this.expandedIndex === index ? null : index;
  }

  refund(id: number): void {
    if (!confirm('Refund this payment?')) return;
    this.paymentService.refund(id).subscribe({
      next: () => { alert('Refund processed successfully.'); this.loadAll(); },
      error: (err) => alert(err?.error?.message || 'Refund failed.')
    });
  }

  downloadReceiptPdfByTxn(txnId?: number, receiptNo?: string): void {
    if (!txnId) return;
    this.paymentService.downloadReceiptPdfByTxn(txnId, receiptNo);
  }

  downloadReceiptPdfById(receiptId?: number, receiptNo?: string): void {
    if (!receiptId) return;
    this.paymentService.downloadReceiptPdfById(receiptId, receiptNo);
  }

  statusClass(status: string): string {
    const m: any = {
      Completed: 'approved', Pending: 'pending',
      Failed: 'rejected',    Refunded: 'refunded'
    };
    return m[status] || 'pending';
  }

  get totalCollected(): number { 
    return this.summary?.totalCollected ?? 0; }

  get completedCount(): number { 
    return Number(this.summary?.completed ?? 0); }

  get pendingCount(): number { 
    return Number(this.summary?.pending   ?? 0); }

  get failedCount(): number { 
    return Number(this.summary?.failed    ?? 0); }
}
