import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {

  // State machine: 'validating' | 'invalid' | 'form' | 'loading' | 'success'
  state: string = 'validating';

  token:           string = '';
  password:        string = '';
  confirmPassword: string = '';
  errorMsg:        string = '';
  showPassword:    boolean = false;
  tokenErrorMsg:   string = '';

  private apiUrl = environment.apiUrl + '/auth';

  constructor(public ls: LanguageService, 
    private route:  ActivatedRoute,
    private router: Router,
    private http:   HttpClient
  ) {}

  ngOnInit(): void {
    // URL থেকে token নাও
    this.token = this.route.snapshot.queryParamMap.get('token') || '';

    if (!this.token) {
      this.tokenErrorMsg = 'No reset token found. Please request a new password reset link.';
      this.state = 'invalid';
      return;
    }

    // Backend এ token validate করো
    this.http.get<any>(`${this.apiUrl}/reset-password/validate?token=${this.token}`)
      .subscribe({
        next:  ()    => { this.state = 'form'; },
        error: (err) => {
          this.tokenErrorMsg = err.error?.message || 'This reset link is invalid or has expired.';
          this.state = 'invalid';
        }
      });
  }

  submit(): void {
    this.errorMsg = '';

    // Client-side validation
    if (!this.password || this.password.length < 6) {
      this.errorMsg = 'Password must be at least 6 characters.';
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.errorMsg = 'Passwords do not match.';
      return;
    }

    this.state = 'loading';

    this.http.post<any>(`${this.apiUrl}/reset-password`, {
      token:    this.token,
      password: this.password
    }).subscribe({
      next: () => {
        this.state = 'success';
      },
      error: (err) => {
        this.state    = 'form';
        this.errorMsg = err.error?.message || 'Something went wrong. Please try again.';
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  requestNewLink(): void {
    this.router.navigate(['/auth/forgot-password']);
  }
}
