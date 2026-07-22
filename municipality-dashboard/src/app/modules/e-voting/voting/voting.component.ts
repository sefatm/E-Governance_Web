import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';
import { Nominee } from 'src/app/models/candidate.model';
import { Election } from 'src/app/models/election.model';
import { NomineeService } from 'src/app/services/candidate.service';
import { ElectionService } from 'src/app/services/election.service';
import { VoteService } from 'src/app/services/vote.service';
import { VoterService } from 'src/app/services/voter.service';
import { AuthService } from 'src/app/services/auth.service';  
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-voting',
  templateUrl: './voting.component.html',
  styleUrls: ['./voting.component.css']
})
export class VotingComponent implements OnInit {

  elections:   Election[] = [];
  candidates:  Nominee[]  = [];

  selectedElectionId:  number | null = null;
  selectedCandidateId: number | null = null;

  voterName = '';
  voterId   = 0;

  alreadyVoted        = false;
  isSubmitting        = false;
  isLoadingCandidates = false;

  apiBaseUrl = environment.apiUrl.replace("/api", "");

  constructor(public ls: LanguageService, 
    private router:          Router,
    private electionService: ElectionService,
    private nomineeService:  NomineeService,
    private voterService:    VoterService,
    private voteService:     VoteService,
    private authService:     AuthService     
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

    // Display name: verification page থেকে voter name, নাহলে login user name
    const storedName = sessionStorage.getItem('voter_name');
    this.voterName = storedName || currentUser.name || '';

    this.loadElections();
  }

  loadElections(): void {
    this.electionService.getAll().subscribe({
      next: (res) => {
        this.elections = res.filter(e =>
          e.status?.toUpperCase() === 'ACTIVE'
        );
      },
      error: (err) => console.error(err)
    });
  }

  onElectionChange(): void {
    if (!this.selectedElectionId) return;
    this.candidates          = [];
    this.selectedCandidateId = null;
    this.alreadyVoted        = false;

    const storedVoterId = sessionStorage.getItem('voter_id');
    if (storedVoterId) {
      this.voterService.hasVoted(Number(storedVoterId), this.selectedElectionId).subscribe({
        next: (voted) => {
          this.alreadyVoted = voted;
          if (!voted) this.loadCandidates();
        },
        error: () => this.loadCandidates()
      });
    } else {
      this.loadCandidates();
    }
  }

  loadCandidates(): void {
    if (!this.selectedElectionId) return;
    this.isLoadingCandidates = true;
    this.nomineeService.getApproved(this.selectedElectionId).subscribe({
      next: (res) => { this.candidates = res; this.isLoadingCandidates = false; },
      error: (err) => { console.error(err); this.isLoadingCandidates = false; }
    });
  }

  submitVote(): void {
    if (!this.selectedElectionId || !this.selectedCandidateId) {
      alert('Please select an election and a candidate.');
      return;
    }
    if (this.alreadyVoted) {
      alert('You have already voted in this election.');
      return;
    }
    if (!confirm('Are you sure? Once you cast your vote, you cannot change it.')) return;

    this.isSubmitting = true;

    const storedVoterId = sessionStorage.getItem('voter_id');
    if (!storedVoterId) {
      this.isSubmitting = false;
      alert('Voter verification is required. Please verify your NID first.');
      return;
    }

    this.voteService.cast({
      electionId:  this.selectedElectionId,
      candidateId: this.selectedCandidateId,
      voterId:     Number(storedVoterId)   
    }).subscribe({
      next: () => {
        this.isSubmitting = false;

        // sessionStorage clear
        sessionStorage.removeItem('voter_id');
        sessionStorage.removeItem('voter_name');
        sessionStorage.removeItem('voter_owner_user_id');

        const selectedCandidate = this.candidates.find(c => c.id === this.selectedCandidateId);
        sessionStorage.setItem('vote_done_voter',     this.voterName);
        sessionStorage.setItem('vote_done_candidate', selectedCandidate?.name || '');
        sessionStorage.setItem('vote_done_party',     selectedCandidate?.party || '');

        this.router.navigate(['/vote-complete']);
      },
      error: (err) => {
        this.isSubmitting = false;
        alert(err.error?.message || 'There was an error casting your vote.');
      }
    });
  }
}
