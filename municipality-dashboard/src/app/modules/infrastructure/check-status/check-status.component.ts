import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-check-status',
  templateUrl: './check-status.component.html',
  styleUrls: ['./check-status.component.css']
})
export class CheckStatusComponent implements OnInit {

  mobile       = '01728444584';
  userOtp      = '';
  generatedOtp = '';
  showOtpPopup = false;

  otpSent     = false;
  otpVerified = false;
  loading     = false;

  applications         : any[] = [];
  filteredApplications : any[] = [];
  selectedItem         : any   = null;

  constructor(public ls: LanguageService, private http: HttpClient) {}

  ngOnInit(): void {}

  sendOtp(): void {
    if (!this.mobile || this.mobile.length !== 11) { alert('Enter valid 11-digit mobile number'); return; }
    this.http.post<any>(`${environment.apiUrl}/otp/send`, { mobile: this.mobile.trim() }).subscribe({
      next: (res) => {
        this.generatedOtp = res.devOtp || '';
        this.showOtpPopup = !!this.generatedOtp;
        this.otpSent = true;
        this.otpVerified = false;
        this.selectedItem = null;
      },
      error: () => alert('OTP could not be sent')
    });
  }

  verifyOtp(): void {
    if (!this.userOtp) { alert('Enter OTP'); return; }
    this.http.post<any>(`${environment.apiUrl}/otp/verify`, {
      mobile: this.mobile.trim(), otp: this.userOtp.trim()
    }).subscribe({
      next: () => { this.otpVerified = true; this.loadApplications(); },
      error: () => alert('Invalid or expired OTP. Please try again.')
    });
  }

  loadApplications(): void {
    this.loading      = true;
    this.selectedItem = null;

    const base = `${environment.apiUrl}`;
    const contact = this.mobile;

    const toItem = (type: string) => (a: any) => ({
      id:      a.id,
      name:    a.applicantName || a.name || '—',
      type,
      status:  a.status || 'Pending',
      contact: a.contact || '',
      details: a
    });

    // Use /my-applications?contact= so Citizen JWT is allowed (403 fix)
    forkJoin({
      construction: this.http.get<any[]>(`${base}/construction/my-applications?contact=${contact}`).pipe(catchError(() => of([]))),
      road:         this.http.get<any[]>(`${base}/road/my-applications?contact=${contact}`).pipe(catchError(() => of([]))),
      drainage:     this.http.get<any[]>(`${base}/drainage/my-applications?contact=${contact}`).pipe(catchError(() => of([]))),
      light:        this.http.get<any[]>(`${base}/street-light/my-applications?contact=${contact}`).pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ construction, road, drainage, light }) => {
        this.applications = [
          ...construction.map(toItem('Construction')),
          ...road.map(toItem('Road')),
          ...drainage.map(toItem('Drainage')),
          ...light.map(toItem('Street Light'))
        ];
        this.filteredApplications = [...this.applications];
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  viewItem(item: any): void  { this.selectedItem = item; }

  reset(): void {
    this.mobile = ''; this.userOtp = ''; this.generatedOtp = '';
    this.otpSent = false; this.otpVerified = false;
    this.applications = []; this.filteredApplications = [];
    this.selectedItem = null;
  }

  statusClass(status: string): string {
    const s = (status || '').trim().toLowerCase();
    if (s === 'resolved')    return 'resolved';
    if (s === 'rejected')    return 'rejected';
    if (s === 'in progress') return 'in-progress';
    return 'pending';
  }

  statusIcon(status: string): string {
    const s = (status || '').trim().toLowerCase();
    if (s === 'resolved')    return 'fas fa-check-circle';
    if (s === 'rejected')    return 'fas fa-times-circle';
    if (s === 'in progress') return 'fas fa-spinner';
    return 'fas fa-clock';
  }

  typeIcon(type: string): string {
    const map: any = {
      'Road':         'fas fa-road',
      'Drainage':     'fas fa-water',
      'Street Light': 'fas fa-lightbulb',
      'Construction': 'fas fa-hard-hat',
    };
    return map[type] || 'fas fa-tools';
  }

  closeOtpPopup(): void { this.showOtpPopup = false; }
}
