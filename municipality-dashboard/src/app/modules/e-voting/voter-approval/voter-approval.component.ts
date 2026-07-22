import { Component, OnInit, OnDestroy, Renderer2 } from '@angular/core';
import { Voter } from 'src/app/models/voter.model';
import { VoterService } from 'src/app/services/voter.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-voter-approval',
  templateUrl: './voter-approval.component.html',
  styleUrls: ['./voter-approval.component.css']
})
export class VoterApprovalComponent implements OnInit, OnDestroy {

  voters:       Voter[]       = [];
  selectedVoter: Voter | null = null;
  isLoading     = false;
  errorMessage  = '';

  private modalEl: HTMLElement | null = null;

  constructor(public ls: LanguageService, 
    private voterService: VoterService,
    private renderer:     Renderer2
  ) {}

  ngOnInit(): void    { this.loadVoters(); }
  ngOnDestroy(): void { this.destroyModal(); }

  loadVoters(): void {
    this.isLoading    = true;
    this.errorMessage = '';

    this.voterService.getAll().subscribe({
      next: (res) => {
        this.voters    = res;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading    = false;
        this.errorMessage = err?.error?.message
            || 'There was a problem loading voter data. Check if the backend is running.';
        console.error('Voter load error:', err);
      }
    });
  }

  approve(id?: number): void {
    if (!id) return;
    this.voterService.approve(id).subscribe({
      next: () => { alert('Voter approved successfully.'); this.loadVoters(); },
      error: (err) => alert(err?.error?.message || 'Error occurred while approving voter.')
    });
  }

  toggleReject(voter: Voter): void {
    voter.showReject = !voter.showReject;
    if (!voter.showReject) voter.rejectReason = '';
  }

  confirmReject(voter: Voter): void {
    if (!voter.rejectReason?.trim()) { alert('Reason for rejection is required.'); return; }
    if (!voter.id) return;
    this.voterService.reject(voter.id, voter.rejectReason!).subscribe({
      next: () => { alert('Voter rejected successfully.'); this.loadVoters(); },
      error: (err) => alert(err?.error?.message || 'Error occurred while rejecting voter.')
    });
  }

  deleteVoter(id?: number): void {
    if (!id || !confirm('Are you sure you want to delete this voter?')) return;
    this.voterService.delete(id).subscribe({
      next: () => { alert('Voter delete successfully.'); this.loadVoters(); },
      error: (err) => alert(err?.error?.message || 'Error occurred while deleting voter.')
    });
  }

  viewVoter(voter: Voter): void {
    this.selectedVoter = { ...voter };
    this.renderModal(voter);
  }

  closeView(): void {
    this.selectedVoter = null;
    this.destroyModal();
  }

  private renderModal(v: any): void {
    this.destroyModal();

    const statusColor = v.status?.toUpperCase() === 'APPROVED' ? '#1b7f4b'
                      : v.status?.toUpperCase() === 'REJECTED'  ? '#c62828'
                      : '#e65100';

    const rejectRow = v.rejectReason
      ? `<div class="va-detail-row">
           <span class="va-label">Reject Reason</span>
           <span class="va-value" style="color:#c62828;">${v.rejectReason}</span>
         </div>` : '';

    const html = `
      <div id="va-modal-overlay" style="
          position:fixed;top:0;left:0;right:0;bottom:0;
          background:rgba(0,0,0,0.55);
          display:flex;align-items:center;justify-content:center;
          z-index:99999;">
        <div style="
            background:#fff;width:480px;max-width:95vw;
            border-radius:12px;box-shadow:0 8px 40px rgba(0,0,0,0.25);
            overflow:hidden;font-family:sans-serif;">

          <div style="background:#1b7f4b;padding:16px 20px;
                      display:flex;align-items:center;justify-content:space-between;">
            <span style="color:#fff;font-size:17px;font-weight:600;">Voter Details</span>
            <button id="va-modal-close" style="
                background:transparent;border:none;color:#fff;
                font-size:22px;cursor:pointer;line-height:1;">✕</button>
          </div>

          <div style="padding:16px 20px;max-height:72vh;overflow-y:auto;">
            <div class="va-detail-row"><span class="va-label">Name</span>             <span class="va-value">${v.name || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">NID</span>             <span class="va-value">${v.nid || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">Date of Birth</span>     <span class="va-value">${v.dob || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">Gender</span>           <span class="va-value">${v.gender || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">Father's Name</span>       <span class="va-value">${v.fatherName || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">Mother's Name</span>       <span class="va-value">${v.motherName || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">Mobile</span>          <span class="va-value">${v.mobile || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">District</span>            <span class="va-value">${v.district || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">Upazila</span>          <span class="va-value">${v.upazila || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">Area</span>           <span class="va-value">${v.area || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">Address</span>          <span class="va-value">${v.address || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">Election Type</span>  <span class="va-value">${v.electionType || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">Zone</span>             <span class="va-value">${v.zoneId || '—'}</span></div>
            <div class="va-detail-row"><span class="va-label">Center</span>          <span class="va-value">${v.centerId || '—'}</span></div>
            <div class="va-detail-row">
              <span class="va-label">Status</span>
              <span class="va-value" style="color:${statusColor};font-weight:600;">${v.status || '—'}</span>
            </div>
            ${rejectRow}
          </div>

        </div>
      </div>`;

    // Inject styles once
    if (!document.getElementById('va-modal-styles')) {
      const style = this.renderer.createElement('style');
      style.id = 'va-modal-styles';
      style.textContent = `
        .va-detail-row {
          display:flex; padding:8px 0;
          border-bottom:1px solid #f0f0f0; font-size:14px;
        }
        .va-detail-row:last-child { border-bottom:none; }
        .va-label { color:#666; width:145px; flex-shrink:0; font-weight:500; }
        .va-value { color:#1a1a1a; flex:1; }
      `;
      this.renderer.appendChild(document.head, style);
    }

    const wrapper = this.renderer.createElement('div');
    wrapper.innerHTML = html;
    this.modalEl = wrapper.firstElementChild as HTMLElement;
    this.renderer.appendChild(document.body, this.modalEl);

    // Overlay click → close
    this.modalEl!.addEventListener('click', (e) => {
      if ((e.target as HTMLElement).id === 'va-modal-overlay') this.closeView();
    });

    // Close button
    document.getElementById('va-modal-close')
      ?.addEventListener('click', () => this.closeView());
  }

  private destroyModal(): void {
    if (this.modalEl) {
      this.renderer.removeChild(document.body, this.modalEl);
      this.modalEl = null;
    }
  }
}
