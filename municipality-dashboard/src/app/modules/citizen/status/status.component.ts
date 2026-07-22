import { environment } from 'src/environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-status',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.css']
})
export class StatusComponent implements OnInit {

  mobile: string = '';
  generatedOtp: string = '';
  userOtp: string = '';

  otpSent: boolean = false;
  otpVerified: boolean = false;
  loading: boolean = false;

  allApplications: any[] = [];
  filteredApplications: any[] = [];
  otpModalVisible = false;
  downloadModalApp: any = null;

  constructor(public ls: LanguageService, private http: HttpClient) {}

  ngOnInit(): void {
    // Pre-fill mobile from logged-in user if available
    const user = JSON.parse(localStorage.getItem('user') || sessionStorage.getItem('user') || '{}');
    if (user?.mobile) this.mobile = user.mobile;
    else if (user?.phone) this.mobile = user.phone;
  }

  // ── Get auth headers ────────────────────────────────────────────────────────
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token') || '';
    return new HttpHeaders({ Authorization: 'Bearer ' + token });
  }

  // ── Send OTP via backend ──────────────────────────────────────────────────
  sendOtp() {
    if (!this.mobile || this.mobile.trim().length < 10) {
      alert(this.ls.current === 'bn' ? 'মোবাইল নম্বর দিন' : 'Enter mobile number');
      return;
    }
    this.http.post<any>(`${environment.apiUrl}/otp/send`, { mobile: this.mobile.trim() }).subscribe({
      next: (res) => {
        this.otpSent = true;
        this.generatedOtp = res.devOtp || '';
        this.otpModalVisible = true;
      },
      error: () => alert(this.ls.current === 'bn' ? 'OTP পাঠানো যায়নি' : 'OTP could not be sent')
    });
  }

  // ── Verify OTP & Load Applications ──────────────────────────────────────────
  verifyOtp() {
    this.http.post<any>(`${environment.apiUrl}/otp/verify`, {
      mobile: this.mobile.trim(), otp: this.userOtp.trim()
    }).subscribe({
      next: () => { this.otpVerified = true; this.loadApplications(); },
      error: () => alert(this.ls.current === 'bn' ? 'ভুল বা মেয়াদোত্তীর্ণ OTP ❌' : 'Invalid or expired OTP ❌')
    });
  }

  // ── Load all applications and filter by mobile ───────────────────────────────
  loadApplications() {
    this.loading = true;
    this.allApplications = [];
    this.filteredApplications = [];

    const headers = this.getHeaders();
    const mobileClean = this.mobile.trim();

    let completed = 0;
    const total = 4;

    const done = () => {
      completed++;
      if (completed === total) {
        this.loading = false;
        this.filteredApplications = this.allApplications.filter(app => {
          const appMobile = (app.mobile ?? '').replace(/\s/g, '');
          const searchMobile = mobileClean.replace(/\s/g, '');
          // Match with or without country code
          return appMobile === searchMobile ||
                 appMobile.endsWith(searchMobile) ||
                 searchMobile.endsWith(appMobile);
        });
        console.log('Filtered:', this.filteredApplications.length, 'of', this.allApplications.length);
      }
    };

    // 1. Birth/Death
    this.http.get<any[]>(`${environment.apiUrl}/birth-death/mobile/${mobileClean}`, { headers }).subscribe({
      next: (res) => {
        const data = (res || []).map((a: any) => ({
          id: a.id,
          name: a.childName ?? a.name ?? a.deceasedName ?? '',
          mobile: (a.mobileNumber ?? a.mobile_number ?? a.emergencyPhone ?? a.contact ?? '').toString().trim(),
          type: a.type ?? 'Birth',
          status: a.status || 'Pending',
          submittedAt: a.createdAt,
          details: a
        }));
        this.allApplications.push(...data);
        done();
      },
      error: (e) => { console.error('birth-death error:', e.status, e.message); done(); }
    });

    // 2. Passport
    this.http.get<any[]>(`${environment.apiUrl}/passport/mobile/${mobileClean}`, { headers }).subscribe({
      next: (res) => {
        const data = (res || []).map((a: any) => ({
          id: a.id,
          name: a.name ?? a.fullName ?? '',
          mobile: (a.contact ?? a.mobile ?? a.phone ?? '').toString().trim(),
          type: 'Passport',
          status: a.status || 'Pending',
          submittedAt: a.createdAt,
          details: a
        }));
        this.allApplications.push(...data);
        done();
      },
      error: (e) => { console.error('passport error:', e.status, e.message); done(); }
    });

    // 3. Family Certificate
    this.http.get<any[]>(`${environment.apiUrl}/family/mobile/${mobileClean}`, { headers }).subscribe({
      next: (res) => {
        const data = (res || []).map((a: any) => ({
          id: a.id,
          name: a.head_name ?? a.headName ?? a.name ?? '',
          mobile: (a.contact ?? a.mobile ?? a.phone ?? '').toString().trim(),
          type: 'Family',
          status: a.status || 'Pending',
          submittedAt: a.createdAt,
          details: a
        }));
        this.allApplications.push(...data);
        done();
      },
      error: (e) => { console.error('family error:', e.status, e.message); done(); }
    });

    // 4. Citizen Certificate — mobile দিয়ে শুধু নিজের data
    this.http.get<any[]>(`${environment.apiUrl}/citizen/by-contact/${mobileClean}`, { headers }).subscribe({
      next: (res) => {
        const data = (res || []).map((a: any) => ({
          id: a.id,
          name: a.name ?? '',
          mobile: (a.contact ?? '').toString().trim(),
          type: 'Citizen',
          status: a.status || 'Pending',
          submittedAt: a.createdAt,
          details: a
        }));
        this.allApplications.push(...data);
        done();
      },
      error: (e) => { console.error('citizen error:', e.status, e.message); done(); }
    });
  }

  isApproved(app: any): boolean {
    return app.status?.replace(/"/g, '').trim().toLowerCase() === 'approved';
  }

  isDownloadSupported(app: any): boolean {
    return ['Birth', 'Death', 'Family', 'Citizen'].includes(app?.type);
  }

  openDownloadPopup(app: any) {
    this.downloadModalApp = app;
  }

  closeDownloadPopup() { this.downloadModalApp = null; }
  closeOtpPopup() { this.otpModalVisible = false; }

  getFamilyMembers(app: any): any[] {
    if (!app || app.type !== 'Family') return [];
    const raw = app.details?.membersJson;
    if (!raw) return [];
    try { return typeof raw === 'string' ? JSON.parse(raw) : raw; } catch { return []; }
  }

  // ── Download Certificate ────────────────────────────────────────────────────
  download(app: any) {
    this.downloadModalApp = null;
    const apiMap: any = {
      'Birth':    'birth-death',
      'Death':    'birth-death',
      'Passport': 'passport',
      'Family':   'family',
      'Citizen':  'citizen'
    };

    const baseUrl = apiMap[app.type];
    if (!baseUrl) { alert('Download not supported'); return; }

    const url = app.type === 'Family'
      ? `${environment.apiUrl}/family/generate-pdf/${app.id}`
      : `${environment.apiUrl}/${baseUrl}/download/${app.id}`;

    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (res: Blob) => {
        const blob = new Blob([res], { type: 'application/pdf' });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = app.type + '_certificate.pdf';
        link.click();
        window.URL.revokeObjectURL(link.href);
      },
      error: (err) => {
        if (err?.status === 409 || err?.status === 400) {
          alert(this.ls.current === 'bn'
            ? 'দুই ধাপের অনুমোদন এখনো সম্পূর্ণ হয়নি। চূড়ান্ত অনুমোদনের পরে PDF ডাউনলোড করা যাবে।'
            : 'Two-step approval is not complete yet. The PDF will be available after final approval.');
          return;
        }
        if (err?.status === 403) {
          alert(this.ls.current === 'bn'
            ? 'ডাউনলোড অনুমতি পাওয়া যায়নি। অনুগ্রহ করে আবার লগইন করে চেষ্টা করুন।'
            : 'Download permission was denied. Please sign in again and retry.');
          return;
        }
        alert(this.ls.current === 'bn' ? 'ডাউনলোড ব্যর্থ হয়েছে' : 'Download failed');
      }
    });
  }
}
