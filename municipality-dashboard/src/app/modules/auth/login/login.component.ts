import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { LanguageService } from 'src/app/services/language.service';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  email:      string  = '';
  password:   string  = 'sefat123';
  showPass:   boolean = false;
  isLoading:  boolean = false;
  errorMsg:   string  = '';
  infoMsg:    string  = '';

  constructor(public ls: LanguageService, 
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Session expired হলে interceptor এই param দিয়ে redirect করে
    this.route.queryParams.subscribe(params => {
      if (params['reason'] === 'session_expired') {
        this.infoMsg = 'আপনার session মেয়াদ শেষ হয়েছে। অনুগ্রহ করে আবার লগইন করুন।';
      }
    });
  }

  login() {
    this.errorMsg = '';
    this.infoMsg  = '';

    if (!this.email || !this.password) {
      this.errorMsg = 'Please enter your email and password.';
      return;
    }

    this.isLoading = true;
    this.authService.login(this.email, this.password).subscribe({
      next: (res) => {
        this.authService.saveSession(res);
        this.isLoading = false;
        this.router.navigateByUrl('/dashboard');
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMsg = err.error?.message || 'Invalid email or password.';
      }
    });
  }
}
