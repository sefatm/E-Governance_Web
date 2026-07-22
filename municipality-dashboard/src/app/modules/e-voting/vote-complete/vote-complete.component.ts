import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-vote-complete',
  templateUrl: './vote-complete.component.html',
  styleUrls: ['./vote-complete.component.css']
})
export class VoteCompleteComponent implements OnInit {

  voterName     = '';
  candidateName = '';
  party         = '';

  constructor(public ls: LanguageService, private router: Router) {}

  ngOnInit(): void {
    this.voterName     = sessionStorage.getItem('vote_done_voter')     || '';
    this.candidateName = sessionStorage.getItem('vote_done_candidate') || '';
    this.party         = sessionStorage.getItem('vote_done_party')     || '';

    // যদি vote_done data না থাকে → home-এ redirect
    if (!this.voterName) {
      this.router.navigate(['/']);
      return;
    }

    // এই page-এর data clear করো — refresh করলে আবার আসতে পারবে না
    sessionStorage.removeItem('vote_done_voter');
    sessionStorage.removeItem('vote_done_candidate');
    sessionStorage.removeItem('vote_done_party');
  }

  goHome(): void {
    this.router.navigate(['/']);
  }

  goResult(): void {
    this.router.navigate(['/vote-result']);
  }
}
