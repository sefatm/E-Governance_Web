import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { VoterService } from 'src/app/services/voter.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-cast-voting',
  templateUrl: './cast-voting.component.html',
  styleUrls: ['./cast-voting.component.css']
})
export class CastVotingComponent implements OnInit {

  nid: string = '';
  dob: string = '';

  isLoading = false;
  error: string = '';

  constructor(public ls: LanguageService, 
    private voterService: VoterService,
    private authService:  AuthService,
    private router:       Router
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    const ownerUserId = sessionStorage.getItem('voter_owner_user_id');
    if (ownerUserId && Number(ownerUserId) !== currentUser.id) {
      sessionStorage.removeItem('voter_id');
      sessionStorage.removeItem('voter_name');
      sessionStorage.removeItem('voter_owner_user_id');
    }

    // যদি voter আগেই verify করা থাকে (same user), সরাসরি voting page-এ নিয়ে যাও
    if (sessionStorage.getItem('voter_id')) {
      this.router.navigate(['/cast-vote']);
    }
  }

  verifyVoter() {
    this.error = '';

    if (!this.nid || !this.dob) {
      this.error = 'NID number and date of birth are required.';
      return;
    }
    const nidClean = this.nid.trim().replace(/\s/g, '');
    if (!/^\d{10}$|^\d{17}$/.test(nidClean)) {
      this.error = 'NID number must be 10 or 17 digits.';
      return;
    }

    this.isLoading = true;

    this.voterService.verify(nidClean, this.dob).subscribe({
      next: (res: any) => {
        this.isLoading = false;

        const currentUser = this.authService.getCurrentUser();

        sessionStorage.setItem('voter_id',   String(res.voterId));
        sessionStorage.setItem('voter_name', res.name);
        sessionStorage.setItem('voter_owner_user_id', String(currentUser?.id ?? ''));

        this.router.navigate(['/cast-vote']);
      },
      error: (err) => {
        this.isLoading = false;
        this.error = err.error?.message
          || 'NID and date of birth do not match. Please ensure you are an authorized voter.';
      }
    });
  }
}
