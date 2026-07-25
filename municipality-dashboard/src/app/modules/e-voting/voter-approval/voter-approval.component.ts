import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Voter } from 'src/app/models/voter.model';
import { VoterService } from 'src/app/services/voter.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-voter-approval',
  templateUrl: './voter-approval.component.html',
  styleUrls: ['./voter-approval.component.css']
})
export class VoterApprovalComponent implements OnInit {

  voters: Voter[] = [];
  expandedIndex: number | null = null;
  isLoading = false;
  errorMessage = '';

  constructor(
    public ls: LanguageService,
    private voterService: VoterService
  ) {}

  ngOnInit(): void {
    this.loadVoters();
  }

  loadVoters(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.voterService.getAll().subscribe({
      next: (res) => {
        this.voters = res.map(v => ({ ...v, showReject: false, rejectReason: v.rejectReason || '' }));
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message
          || 'There was a problem loading voter data. Check if the backend is running.';
        console.error('Voter load error:', err);
      }
    });
  }

  toggleDetails(index: number): void {
    this.expandedIndex = this.expandedIndex === index ? null : index;
  }

  approve(id?: number): void {
    if (!id) return;
    this.voterService.approve(id).subscribe({
      next: () => {
        alert('Voter approved successfully.');
        this.loadVoters();
      },
      error: (err) => alert(err?.error?.message || 'Error occurred while approving voter.')
    });
  }

  toggleReject(voter: Voter): void {
    voter.showReject = !voter.showReject;
    if (!voter.showReject) voter.rejectReason = '';
  }

  confirmReject(voter: Voter): void {
    if (!voter.rejectReason?.trim()) {
      alert('Reason for rejection is required.');
      return;
    }
    if (!voter.id) return;

    this.voterService.reject(voter.id, voter.rejectReason).subscribe({
      next: () => {
        alert('Voter rejected successfully.');
        this.loadVoters();
      },
      error: (err) => alert(err?.error?.message || 'Error occurred while rejecting voter.')
    });
  }

  deleteVoter(id?: number): void {
    if (!id || !confirm('Are you sure you want to delete this voter?')) return;
    this.voterService.delete(id).subscribe({
      next: () => {
        alert('Voter deleted successfully.');
        this.loadVoters();
      },
      error: (err) => alert(err?.error?.message || 'Error occurred while deleting voter.')
    });
  }

  statusClass(status?: string): string {
    const s = (status || '').toUpperCase();
    if (s === 'APPROVED') return 'badge-approved';
    if (s === 'REJECTED') return 'badge-rejected';
    return 'badge-pending';
  }

  fileUrl(path?: string): string {
    if (!path) return '';
    if (/^https?:\/\//i.test(path)) return path;
    const apiRoot = environment.apiUrl.replace(/\/api\/?$/, '');
    return `${apiRoot}/${path.replace(/^\/+/, '')}`;
  }
}
