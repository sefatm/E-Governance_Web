import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment';
import { NomineeService } from 'src/app/services/candidate.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-candidate-approval',
  templateUrl: './candidate-approval.component.html',
  styleUrls: ['./candidate-approval.component.css']
})
export class CandidateApprovalComponent implements OnInit {
  candidates: any[] = [];
  expandedIndex: number | null = null;
  isLoading = false;
  errorMessage = '';

  constructor(
    public ls: LanguageService,
    private service: NomineeService
  ) {}

  ngOnInit(): void {
    this.loadCandidates();
  }

  loadCandidates(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.service.getAll().subscribe({
      next: (res) => {
        this.candidates = res.map((c: any) => ({
          ...c,
          showReject: false,
          rejectReason: c.rejectReason || ''
        }));
        this.expandedIndex = null;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message
          || 'There was a problem loading data. Check if the backend is running.';
        console.error('Candidate load error:', err);
      }
    });
  }

  toggleDetails(index: number): void {
    this.expandedIndex = this.expandedIndex === index ? null : index;
  }

  approve(id?: number): void {
    if (!id) return;

    this.service.approve(id).subscribe({
      next: () => {
        alert('Candidate approved successfully.');
        this.loadCandidates();
      },
      error: (err) => alert(err?.error?.message || 'There was a problem approving the candidate.')
    });
  }

  toggleReject(candidate: any): void {
    candidate.showReject = !candidate.showReject;
    if (!candidate.showReject) candidate.rejectReason = '';
  }

  confirmReject(candidate: any): void {
    if (!candidate.id) return;

    if (!candidate.rejectReason?.trim()) {
      alert('Reject reason is required.');
      return;
    }

    this.service.reject(candidate.id, candidate.rejectReason).subscribe({
      next: () => {
        alert('Candidate rejected successfully.');
        this.loadCandidates();
      },
      error: (err) => alert(err?.error?.message || 'There was a problem rejecting the candidate.')
    });
  }

  fileUrl(path: any): string {
    return `${environment.serverUrl}/${encodeURI(String(path ?? '').replace(/^\/+/, ''))}`;
  }

  statusClass(status: string | undefined): string {
    const s = status?.toUpperCase();
    if (s === 'APPROVED') return 'badge-approved';
    if (s === 'REJECTED') return 'badge-rejected';
    return 'badge-pending';
  }
}
