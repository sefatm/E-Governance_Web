import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';
import { LanguageService } from 'src/app/services/language.service';

// ─────────────────────────────────────────────────────────────────
// Forgot Password — OTP Flow
//
// Step 1: email দাও → backend OTP generate করে email এ পাঠায়
// Step 2: OTP enter করো → backend verify করে
// Step 3: নতুন password দাও → backend update করে
// ─────────────────────────────────────────────────────────────────
@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {

  // 'email' | 'otp' | 'password' | 'success'
  step = 'email';

  email           = '';
  otp             = '';
  password        = '';
  confirmPassword = '';
  showPassword    = false;

  isLoading = false;
  errorMsg  = '';

  // OTP resend cooldown
  resendCooldown  = 0;
  resendInterval: any;

  // private api = environment.apiUrl + '/api/auth';
  private api = environment.apiUrl + '/auth';

  constructor(public ls: LanguageService, private http: HttpClient, private router: Router) {}

  // ── Step 1: Email submit → OTP পাঠাও ─────────────────────────
  sendOtp(): void {
    this.errorMsg = '';
    if (!this.email || !this.email.includes('@')) {
      this.errorMsg = 'Please enter a valid email address.';
      return;
    }
    this.isLoading = true;
    this.http.post<any>(`${this.api}/forgot-password/send-otp`, { email: this.email })
      .subscribe({
        next: () => {
          this.isLoading = false;
          this.step      = 'otp';
          this.startResendCooldown();
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMsg  = err.error?.message || 'Failed to send OTP. Please try again.';
        }
      });
  }

  // ── Step 2: OTP verify ────────────────────────────────────────
  verifyOtp(): void {
    this.errorMsg = '';
    if (!this.otp || this.otp.length !== 6) {
      this.errorMsg = 'Please enter the 6-digit OTP.';
      return;
    }
    this.isLoading = true;
    this.http.post<any>(`${this.api}/forgot-password/verify-otp`, {
      email: this.email,
      otp:   this.otp
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.step      = 'password';
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMsg  = err.error?.message || 'Invalid or expired OTP. Please try again.';
      }
    });
  }

  // ── Step 3: New password submit ───────────────────────────────
  resetPassword(): void {
    this.errorMsg = '';
    if (!this.password || this.password.length < 6) {
      this.errorMsg = 'Password must be at least 6 characters.';
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.errorMsg = 'Passwords do not match.';
      return;
    }
    this.isLoading = true;
    this.http.post<any>(`${this.api}/forgot-password/reset`, {
      email:    this.email,
      otp:      this.otp,
      password: this.password
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.step      = 'success';
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMsg  = err.error?.message || 'Failed to reset password. Please try again.';
      }
    });
  }

  // ── Resend OTP ────────────────────────────────────────────────
  resendOtp(): void {
    if (this.resendCooldown > 0) return;
    this.otp      = '';
    this.errorMsg = '';
    this.sendOtp();
  }

  startResendCooldown(): void {
    this.resendCooldown = 60;
    clearInterval(this.resendInterval);
    this.resendInterval = setInterval(() => {
      this.resendCooldown--;
      if (this.resendCooldown <= 0) clearInterval(this.resendInterval);
    }, 1000);
  }

  // ── OTP input: শুধু digit allow ──────────────────────────────
  onOtpInput(event: any): void {
    this.otp = event.target.value.replace(/\D/g, '').slice(0, 6);
    event.target.value = this.otp;
  }

  get passwordsMatch(): boolean {
    return this.confirmPassword.length > 0 && this.password === this.confirmPassword;
  }
  get passwordsMismatch(): boolean {
    return this.confirmPassword.length > 0 && this.password !== this.confirmPassword;
  }

  goToLogin(): void { this.router.navigate(['/login']); }
}
