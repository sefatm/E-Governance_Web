import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-tracking',
  templateUrl: './tracking.component.html',
  styleUrls: ['../complaint-shared.css','./tracking.component.css']
})
export class TrackingComponent {

  mobile = '';
  otp    = '';
  generatedOtp = '';
  showOtpPopup = false;

  otpSent   = false;
  verified  = false;
  isSending = false;

  complaints:         any[] = [];
  filteredComplaints: any[] = [];

  successMsg = '';
  errorMsg   = '';

  constructor(public ls: LanguageService, private http: HttpClient) {}

  sendOtp(): void {
    if (!this.mobile || this.mobile.length < 11) {
      this.errorMsg = 'সঠিক মোবাইল নম্বর দিন।';
      setTimeout(() => this.errorMsg = '', 3000);
      return;
    }
    this.isSending = true;
    this.http.post<any>(`${environment.apiUrl}/otp/send`, { mobile: this.mobile.trim() }).subscribe({
      next: (res) => {
        this.isSending = false;
        this.otpSent = true;
        this.generatedOtp = res.devOtp || '';
        this.showOtpPopup = !!this.generatedOtp;
        this.successMsg = 'OTP পাঠানো হয়েছে।';
      },
      error: () => { this.isSending = false; this.errorMsg = 'OTP পাঠানো যায়নি।'; }
    });
  }

  resendOtp(): void { this.otp = ''; this.sendOtp(); }

  verifyOtp(): void {
    if (!this.otp) {
      this.errorMsg = 'OTP দিন।';
      setTimeout(() => this.errorMsg = '', 3000);
      return;
    }
    this.http.post<any>(`${environment.apiUrl}/otp/verify`, {
      mobile: this.mobile.trim(), otp: this.otp.trim()
    }).subscribe({
      next: () => { this.verified = true; this.loadComplaints(); },
      error: () => { this.errorMsg = 'OTP সঠিক নয় বা মেয়াদ শেষ।'; setTimeout(() => this.errorMsg = '', 3000); }
    });
  }

  loadComplaints(): void {
    this.http.get(`${environment.apiUrl}/complaints/mobile/${encodeURIComponent(this.mobile.trim())}`).subscribe({
      next: (res: any) => {
        this.complaints = res.map((c: any) => ({
          id: c.id, category: c.category, ward: c.ward, area: c.area,
          location: c.location, description: c.description,
          status: c.status || 'Pending', remarks: c.remarks,
          mobile: (c.contact || '').trim()
        }));
        this.filteredComplaints = this.complaints.filter(c => c.mobile === this.mobile.trim());
      },
      error: () => { this.errorMsg = 'অভিযোগ লোড করতে সমস্যা।'; setTimeout(() => this.errorMsg = '', 3000); }
    });
  }

  resetTrack(): void {
    this.mobile = ''; this.otp = ''; this.generatedOtp = '';
    this.otpSent = false; this.verified = false;
    this.complaints = []; this.filteredComplaints = [];
  }

  closeOtpPopup(): void { this.showOtpPopup = false; }
}
