import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { LanguageService } from 'src/app/services/language.service';


@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {

  name:       string  = '';
  email:      string  = '';
  password:   string  = '';
  repassword: string  = '';
  role:       string  = 'Citizen';
  showPass:   boolean = false;
  showRePass: boolean = false;
  isLoading:  boolean = false;
  errorMsg:   string  = '';

  roles: string[] = [
    'Citizen',
    // 'Admin / Municipal Officer',
    // 'Department Officer',
    // 'Project Officer',
    // 'Auditor / Accountant',
    // 'Health / Sanitation Officer',
    // 'Super Admin',
  ];

  constructor(public ls: LanguageService, private authService: AuthService, private router: Router) {}

  get passwordStrength(): number {
    const p = this.password;
    let score = 0;
    if (p.length >= 6)  score++;
    if (p.length >= 10) score++;
    if (/[A-Z]/.test(p)) score++;
    if (/[0-9]/.test(p)) score++;
    if (/[^A-Za-z0-9]/.test(p)) score++;
    return score;
  }

  get strengthLabel(): string {
    const s = this.passwordStrength;
    if (s <= 1) return 'Weak';
    if (s <= 3) return 'Medium';
    return 'Strong';
  }

  get strengthColor(): string {
    const s = this.passwordStrength;
    if (s <= 1) return '#dc2626';
    if (s <= 3) return '#f59e0b';
    return '#16a34a';
  }

  register() {
    this.errorMsg = '';

    if (!this.name || !this.email || !this.password || !this.repassword || !this.role) {
      this.errorMsg = 'All fields are required.';
      return;
    }

    if (this.password !== this.repassword) {
      this.errorMsg = 'Passwords do not match.';
      return;
    }

    if (this.password.length < 6) {
      this.errorMsg = 'Password must be at least 6 characters.';
      return;
    }

    this.isLoading = true;

    this.authService.register(this.name, this.email, this.password, this.role).subscribe({
      next: () => {
        this.isLoading = false;
        alert('Registration Successful! Please login.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMsg = err.error?.message || 'Registration failed. Please try again.';
      }
    });
  }
}
