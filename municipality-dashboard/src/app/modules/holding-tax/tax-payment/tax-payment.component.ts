import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-tax-payment',
  templateUrl: './tax-payment.component.html',
  styleUrls: ['./tax-payment.component.css']
})
export class TaxPaymentComponent implements OnInit {

  searchHoldingNo = '';
  isSearching     = false;
  searchError     = '';

  // API থেকে আসা due info
  dueInfo: any = null;

  // যত টাকা দিতে চায় (default = full due)
  customAmount: number | null = null;

  private baseUrl = `${environment.apiUrl}`;

  constructor(public ls: LanguageService, private http: HttpClient, private router: Router) {}

  ngOnInit(): void {}

  // Step 1 — Holding No দিয়ে বকেয়া আনো
  searchHolding(): void {
    const h = this.searchHoldingNo.trim();
    if (!h) { this.searchError = 'Enter the holding number.'; return; }

    this.isSearching = true;
    this.searchError = '';
    this.dueInfo     = null;

    this.http.get<any>(`${this.baseUrl}/tax-payment/due/${h}`).subscribe({
      next: (res) => {
        this.isSearching = false;
        if (res.due <= 0) {
          this.searchError = `Holding No ${h} - No due amount. All taxes paid.`;
        } else {
          this.dueInfo      = res;
          this.customAmount = Math.round(res.due); // default: full due
        }
      },
      error: (err) => {
        this.isSearching = false;
        this.searchError = err.status === 404
          ? `Holding number "${h}" not found.`
          : 'There is an issue with the server. Please try again.';
      }
    });
  }

  // Step 2 — Payment Gateway-তে যাও
  proceedToPayment(): void {
    if (!this.dueInfo || !this.customAmount || this.customAmount <= 0) return;

    if (this.customAmount > this.dueInfo.due) {
      alert(`Maximum ৳${this.dueInfo.due} can be paid.`);
      return;
    }

    this.router.navigate(['/payment'], {
      queryParams: {
        serviceType: 'HoldingTax',
        amount:      this.customAmount,
        holdingNo:   this.dueInfo.holdingNo,
        description: `Holding No: ${this.dueInfo.holdingNo} | Owner: ${this.dueInfo.ownerName}`,
        name:        this.dueInfo.ownerName
      }
    });
  }

  reset(): void {
    this.dueInfo          = null;
    this.searchHoldingNo  = '';
    this.customAmount     = null;
    this.searchError      = '';
  }
}
