import { environment } from 'src/environments/environment';
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success'|'error'|'info'; message: string; removing?: boolean; }

@Component({
  selector: 'app-license-status',
  templateUrl: './license-status.component.html',
  styleUrls: ['./license-status.component.css']
})
export class LicenseStatusComponent implements OnInit {

  toasts: Toast[] = [];
  mobile        = '';
  generatedOtp  = '';
  showOtpPopup = false;
  userOtp       = '';
  otpSent       = false;
  otpVerified   = false;
  isLoadingOtp  = false;
  isLoadingData = false;

  allApplications:     any[] = [];
  filteredApplications: any[] = [];

  readonly BASE = `${environment.serverUrl}`;

  constructor(public ls: LanguageService, private http: HttpClient) {}

  ngOnInit(): void {}

  sendOtp(): void {
    if (!this.mobile || this.mobile.length < 11) {
      this.showToast('Enter the correct mobile number (11 digits)', 'error'); return;
    }
    if (!/^01[3-9]\d{8}$/.test(this.mobile)) {
      this.showToast('Enter the correct Bangladeshi mobile number', 'error'); return;
    }
    this.isLoadingOtp = true;
    this.http.post<any>(`${this.BASE}/api/otp/send`, { mobile: this.mobile.trim() }).subscribe({
      next: (res) => {
        this.generatedOtp = res.devOtp || '';
        this.showOtpPopup = !!this.generatedOtp;
        this.otpSent = true;
        this.isLoadingOtp = false;
        this.showToast('OTP sent successfully', 'info');
      },
      error: () => { this.isLoadingOtp = false; this.showToast('OTP could not be sent', 'error'); }
    });
  }

  verifyOtp(): void {
    if (!/^\d{6}$/.test(this.userOtp.trim())) { this.showToast('Enter the 6-digit OTP', 'error'); return; }
    this.http.post<any>(`${this.BASE}/api/otp/verify`, { mobile: this.mobile.trim(), otp: this.userOtp.trim() }).subscribe({
      next: () => {
        this.otpVerified = true;
        this.showToast('✔ Successfully verified', 'success');
        this.loadApplications();
      },
      error: () => this.showToast('Incorrect or expired OTP. Please try again.', 'error')
    });
  }

  resendOtp(): void {
    this.userOtp  = '';
    this.otpSent  = false;
    setTimeout(() => this.sendOtp(), 100);
  }

  loadApplications(): void {
    this.isLoadingData = true;
    forkJoin({
      licenses: this.http.get<any[]>(`${this.BASE}/api/tradeLicense/getall`),
      renewals: this.http.get<any[]>(`${this.BASE}/api/trade-renewal/getall`)
    }).subscribe({
      next: ({ licenses, renewals }) => {
        const licenseData = licenses.map(a => ({
          id: a.id,
          name: a.ownerName || 'No Name',
          mobile: (a.mobile || a.mobileNumber || '').trim(),
          type: 'Trade License',
          // status normalize করা হচ্ছে — DB তে 'Approved', 'Pending', 'Rejected'
          status: this.normalizeStatus(a.status),
          licenseNumber: a.licenseNumber || '—'
        }));
        const renewalData = renewals.map(a => ({
          id: a.id,
          name: a.applicantName || 'No Name',
          mobile: (a.contact || '').trim(),
          type: 'Trade Renewal',
          status: this.normalizeStatus(a.status),
          licenseNumber: a.originalLicense?.licenseNumber || a.licenseNumber || '—'
        }));
        this.allApplications      = [...licenseData, ...renewalData];
        this.filteredApplications = this.allApplications.filter(
          app => app.mobile === this.mobile.trim()
        );
        this.isLoadingData = false;

        if (this.filteredApplications.length === 0) {
          this.showToast('No applications found for this mobile number', 'info');
        }
      },
      error: () => {
        this.isLoadingData = false;
        this.showToast('Failed to load data. Please try again.', 'error');
      }
    });
  }

  // Status normalize — DB/API যেভাবেই আসুক, consistent করা
  normalizeStatus(status: string): string {
    if (!status) return 'Pending';
    const s = status.trim().toLowerCase();
    if (s === 'approved') return 'Approved';
    if (s === 'rejected') return 'Rejected';
    return 'Pending';
  }

  // PDF Download — with blob fallback for proper download
  downloadLicense(id: number): void {
    this.showToast('Downloading PDF...', 'info');
    this.http.get(`${this.BASE}/api/tradeLicense/certificate/${id}`, { responseType: 'blob' })
      .subscribe({
        next: (blob) => {
          const url  = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href  = url;
          link.download = `TradeLicense_${id}.pdf`;
          link.click();
          window.URL.revokeObjectURL(url);
          this.showToast('PDF downloaded successfully', 'success');
        },
        error: () => this.showToast('Failed to download PDF', 'error')
      });
  }

  downloadRenewal(id: number): void {
    this.showToast('Downloading PDF...', 'info');
    this.http.get(`${this.BASE}/api/trade-renewal/certificate/${id}`, { responseType: 'blob' })
      .subscribe({
        next: (blob) => {
          const url  = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href  = url;
          link.download = `TradeRenewal_${id}.pdf`;
          link.click();
          window.URL.revokeObjectURL(url);
          this.showToast('PDF downloaded successfully', 'success');
        },
        error: () => this.showToast('Failed to download PDF', 'error')
      });
  }

  showToast(message: string, type: 'success'|'error'|'info'): void {
    const t: Toast = { type, message };
    this.toasts.push(t);
    setTimeout(() => {
      t.removing = true;
      setTimeout(() => this.toasts = this.toasts.filter(x => x !== t), 350);
    }, 4000);
  }

  badgeClass(status: string): string {
    if (status === 'Approved') return 'badge-approved';
    if (status === 'Rejected') return 'badge-rejected';
    return 'badge-pending';
  }

  closeOtpPopup(): void { this.showOtpPopup = false; }
}
