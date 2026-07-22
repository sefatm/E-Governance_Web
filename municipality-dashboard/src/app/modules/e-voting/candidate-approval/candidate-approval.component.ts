import { environment } from 'src/environments/environment';
import { Component, OnInit, OnDestroy, Renderer2 } from '@angular/core';
import { Nominee } from 'src/app/models/candidate.model';
import { NomineeService } from 'src/app/services/candidate.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-candidate-approval',
  templateUrl: './candidate-approval.component.html',
  styleUrls: ['./candidate-approval.component.css']
})
export class CandidateApprovalComponent implements OnInit, OnDestroy {
  readonly serverUrl = environment.serverUrl;

  candidates:   any[]          = [];
  selected:     Nominee | null = null;
  isLoading     = false;
  errorMessage  = '';

  private modalEl: HTMLElement | null = null;

  constructor(public ls: LanguageService, 
    private service:   NomineeService,
    private renderer:  Renderer2
  ) {}

  ngOnInit(): void  { this.loadCandidates(); }
  ngOnDestroy(): void { this.destroyModal(); }

  loadCandidates(): void {
    this.isLoading    = true;
    this.errorMessage = '';
    this.service.getAll().subscribe({
      next: (res) => {
        this.candidates = res.map((c: any) => ({
          ...c,
          showReject:   false,
          rejectReason: ''
        }));
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading    = false;
        this.errorMessage = err?.error?.message
            || 'There was a problem loading data. Check if the backend is running.';
        console.error('Candidate load error:', err);
      }
    });
  }

  approve(id?: number): void {
    if (!id) return;
    this.service.approve(id).subscribe({
      next: () => { alert('Approved'); this.loadCandidates(); },
      error: (err) => alert(err?.error?.message || 'There was a problem approving the candidate.')
    });
  }

  toggleReject(candidate: any): void {
    candidate.showReject = !candidate.showReject;
    if (!candidate.showReject) candidate.rejectReason = '';
  }

  confirmReject(candidate: any): void {
    if (!candidate.id) return;
    if (!candidate.rejectReason?.trim()) { alert('Reject reason is required.'); return; }
    this.service.reject(candidate.id, candidate.rejectReason).subscribe({
      next: () => { alert('Rejected'); this.loadCandidates(); },
      error: (err) => alert(err?.error?.message || 'There was a problem rejecting the candidate.')
    });
  }

  // MODAL—document.body

  view(candidate: any): void {
    this.selected = { ...candidate };
    this.renderModal(candidate);
  }

  closeView(): void {
    this.selected = null;
    this.destroyModal();
  }

  private esc(value: any): string {
    return String(value ?? '—')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  private fileUrl(path: any): string {
    return `${environment.serverUrl}/${encodeURI(String(path ?? '').replace(/^\/+/, ''))}`;
  }

  private renderModal(c: any): void {
    this.destroyModal();

    const symbolHtml = c.symbolFileUrl ? `<img src="${this.fileUrl(c.symbolFileUrl)}" alt="symbol" style="height:44px;border-radius:4px;border:1px solid #eee;" />` : `<span>${this.esc(c.symbol)}</span>`;

    const rejectRow = c.rejectReason ? `<div class="ca-detail-row"> <span class="ca-label">Reason for rejection</span> <span class="ca-value" style="color:#c62828;">${this.esc(c.rejectReason)}</span></div>` : '';

    const statusColor = c.status?.toUpperCase() === 'APPROVED' ? '#1b7f4b' : c.status?.toUpperCase() === 'REJECTED' ? '#c62828' : '#e65100';

    const html = `
      <div id="ca-modal-overlay" style="
          position:fixed;top:0;left:0;right:0;bottom:0;
          background:rgba(0,0,0,0.55);
          display:flex;align-items:center;justify-content:center;
          z-index:99999;">
        <div id="ca-modal-box" style="
            background:#fff;width:480px;max-width:95vw;
            border-radius:12px;box-shadow:0 8px 40px rgba(0,0,0,0.25);
            overflow:hidden;font-family:sans-serif;">

          <div style="background:#1b7f4b;padding:16px 20px;
                      display:flex;align-items:center;justify-content:space-between;">
            <span style="color:#fff;font-size:17px;font-weight:600;">Candidate Details</span>
            <button id="ca-modal-close" style="
                background:transparent;border:none;color:#fff;
                font-size:22px;cursor:pointer;line-height:1;">✕</button>
          </div>

          <div style="padding:16px 20px;max-height:72vh;overflow-y:auto;">
            <div class="ca-detail-row"><span class="ca-label">Name</span>           <span class="ca-value">${this.esc(c.name)}</span></div>
            <div class="ca-detail-row"><span class="ca-label">Father's Name</span>     <span class="ca-value">${this.esc(c.fathersName)}</span></div>
            <div class="ca-detail-row"><span class="ca-label">Mother's Name</span>     <span class="ca-value">${this.esc(c.mothersName)}</span></div>
            <div class="ca-detail-row"><span class="ca-label">Date of Birth</span>   <span class="ca-value">${this.esc(c.dob)}</span></div>
            <div class="ca-detail-row"><span class="ca-label">NID</span>           <span class="ca-value">${this.esc(c.nid)}</span></div>
            <div class="ca-detail-row"><span class="ca-label">Mobile</span>        <span class="ca-value">${this.esc(c.mobileNumber)}</span></div>
            <div class="ca-detail-row"><span class="ca-label">Party</span>            <span class="ca-value">${this.esc(c.party)}</span></div>
            <div class="ca-detail-row"><span class="ca-label">Symbol</span>        <span class="ca-value">${symbolHtml}</span></div>
            <div class="ca-detail-row"><span class="ca-label">Area</span>         <span class="ca-value">${this.esc(c.area)}</span></div>
            <div class="ca-detail-row"><span class="ca-label">Election Type</span><span class="ca-value">${this.esc(c.electionType)}</span></div>
            <div class="ca-detail-row"><span class="ca-label">Zone</span>           <span class="ca-value">${this.esc(c.zoneId)}</span></div>
            <div class="ca-detail-row">
              <span class="ca-label">Status</span>
              <span class="ca-value" style="color:${statusColor};font-weight:600;">${this.esc(c.status)}</span>
            </div>
            ${rejectRow}
          </div>

        </div>
      </div>`;

    // Inline styles inject (one-time)
    if (!document.getElementById('ca-modal-styles')) {
      const style = this.renderer.createElement('style');
      style.id = 'ca-modal-styles';
      style.textContent = `
        .ca-detail-row {
          display:flex; padding:8px 0;
          border-bottom:1px solid #f0f0f0; font-size:14px;
        }
        .ca-detail-row:last-child { border-bottom:none; }
        .ca-label { color:#666; width:145px; flex-shrink:0; font-weight:500; }
        .ca-value { color:#1a1a1a; flex:1; }
      `;
      this.renderer.appendChild(document.head, style);
    }

    // body to append
    const wrapper = this.renderer.createElement('div');
    wrapper.innerHTML = html;
    this.modalEl = wrapper.firstElementChild as HTMLElement;
    this.renderer.appendChild(document.body, this.modalEl);

    // Close on overlay click
    this.modalEl!.addEventListener('click', (e) => {
      if ((e.target as HTMLElement).id === 'ca-modal-overlay') this.closeView();
    });

    // Close button
    document.getElementById('ca-modal-close')
      ?.addEventListener('click', () => this.closeView());
  }

  private destroyModal(): void {
    if (this.modalEl) {
      this.renderer.removeChild(document.body, this.modalEl);
      this.modalEl = null;
    }
  }
}
