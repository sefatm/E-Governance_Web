import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-access-denied',
  templateUrl: './access-denied.component.html',
  styleUrls:  ['./access-denied.component.css']
})
export class AccessDeniedComponent implements OnInit {

  currentRole: string = '';
  attemptedUrl: string = '';

  constructor(public ls: LanguageService, 
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentRole  = this.authService.getCurrentRole() || 'Unknown';
    this.attemptedUrl = document.referrer || '';
  }

  goToDashboard(): void { this.router.navigate(['/dashboard']); }
  logout(): void        { this.authService.logout(); }
}
