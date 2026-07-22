import { Component } from '@angular/core';
import { WaterBill } from 'src/app/models/water-bill.model';
import { WaterBillService } from 'src/app/services/water-bill.service';

@Component({
  selector: 'app-bill-status-pay',
  templateUrl: './bill-status-pay.component.html',
  styleUrls: ['./bill-status-pay.component.css']
})
export class BillStatusPayComponent {
  meterNo = '';
  mobile = '';
  bills: WaterBill[] = [];
  loading = false;
  searched = false;

  payBillItem: WaterBill | null = null;
  receipt: any = null;
  payment = { method: 'Mobile Banking', nid: '', mobile: '', email: '', providerTxnId: '' };
  paying = false;
  message = '';
  error = '';

  constructor(private waterBillService: WaterBillService) {}

  checkStatus(): void {
    this.error = '';
    this.message = '';
    if (!this.meterNo.trim() || !this.mobile.trim()) {
      this.error = 'Meter number and mobile number are required.';
      return;
    }
    this.loading = true;
    this.searched = true;
    this.waterBillService.lookup(this.meterNo.trim(), this.mobile.trim()).subscribe({
      next: data => {
        this.bills = data || [];
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.error = err?.error?.message || 'Unable to check bill status.';
      }
    });
  }

  openPayment(bill: WaterBill): void {
    this.payBillItem = bill;
    this.receipt = null;
    this.error = '';
    this.payment = {
      method: 'Mobile Banking',
      nid: bill.nid || '',
      mobile: bill.mobile || this.mobile,
      email: bill.email || '',
      providerTxnId: ''
    };
  }

  closePayment(): void {
    if (!this.paying) {
      this.payBillItem = null;
      this.receipt = null;
    }
  }

  confirmPayment(): void {
    if (!this.payBillItem?.id) return;
    if (!this.payment.nid.trim()) {
      this.error = 'NID is required for the official receipt.';
      return;
    }
    this.paying = true;
    this.error = '';
    this.waterBillService.payBill(this.payBillItem.id, this.payment).subscribe({
      next: res => {
        this.paying = false;
        this.receipt = res?.receipt || null;
        const updated = res?.bill as WaterBill;
        this.bills = this.bills.map(b => b.id === updated?.id ? updated : b);
        this.message = 'Payment completed successfully. Receipt email has been queued.';
        if (this.meterNo.trim() && this.mobile.trim()) this.refreshLookupSilently();
      },
      error: err => {
        this.paying = false;
        this.error = err?.error?.message || 'Payment failed.';
      }
    });
  }

  private refreshLookupSilently(): void {
    this.waterBillService.lookup(this.meterNo.trim(), this.mobile.trim()).subscribe({
      next: data => this.bills = data || [],
      error: () => {}
    });
  }

  downloadPaidBillReceipt(bill: WaterBill): void {
    if (!bill.receiptNo) { this.error = 'Receipt number is not available for this bill.'; return; }
    this.waterBillService.downloadReceiptByNo(bill.receiptNo).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `water-bill-receipt-${bill.receiptNo}.pdf`;
        document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url);
      },
      error: () => this.error = 'Receipt PDF download failed.'
    });
  }

  downloadReceipt(): void {
    if (!this.receipt?.id) return;
    const receipt = this.receipt;
    this.waterBillService.downloadReceipt(receipt.id).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `water-bill-receipt-${receipt.receiptNo || receipt.id}.pdf`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
        this.closePayment();
      },
      error: () => this.error = 'Receipt PDF download failed.'
    });
  }

  statusClass(status?: string): string {
    return (status || '').toLowerCase() === 'paid' ? 'paid' : 'unpaid';
  }
}
