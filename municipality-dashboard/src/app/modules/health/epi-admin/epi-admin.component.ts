import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { EpiService } from 'src/app/services/epi.service';
import { LanguageService } from 'src/app/services/language.service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-epi-admin',
  templateUrl: './epi-admin.component.html',
  styleUrls: ['./epi-shared.css']
})
export class EpiAdminComponent implements OnInit, OnDestroy {
  private pendingEpiApproval: { child: any; signatureBase64: string } | null = null;


  children:    any[] = [];
  filtered:    any[] = [];
  pendingList: any[] = [];
  stats:       any   = {};
  upcoming:    any[] = [];
  missed:      any[] = [];

  selectedChild:  any | null = null;
  selectedDetailChild: any | null = null;
  vaccinations:   any[]      = [];
  expandedChildId: number | null = null;
  activeTab = 'pending'; // starts on pending tab

  searchText   = '';
  selectedWard = '';
  isLoading    = true;
  errorMsg     = '';

  // Give vaccine modal
  showGiveModal = false;
  selectedVacc: any | null = null;
  givenBy      = '';
  healthCenter = '';
  batchNo      = '';
  remarks      = '';

  approvalChild: any | null = null;
  approvalSignatureBase64 = '';
  approvalSealBase64 = '';
  approvalSignatureName = '';
  approvalSealName = '';
  approvalSubmitting = false;

  wards = ['','Ward 1','Ward 2','Ward 3','Ward 4','Ward 5',
           'Ward 6','Ward 7','Ward 8','Ward 9','Ward 10','Ward 11','Ward 12'];

  private modalEl: HTMLElement | null = null;

  // QR scanner / dose entry
  @ViewChild('qrVideo') qrVideo?: ElementRef<HTMLVideoElement>;
  showScannerModal = false;
  scannerActive = false;
  scannerMessage = '';
  manualCardNo = '';
  scannedChild: any | null = null;
  scannedSchedule: any[] = [];
  private scannerStream: MediaStream | null = null;
  private scanTimer: any = null;
  private barcodeDetector: any = null;

  constructor(public ls: LanguageService, private epi: EpiService) {}

  readonly serverBase = (environment.serverUrl || environment.apiUrl.replace(/\/api$/, '')).replace(/\/$/, '');

  ngOnInit() { this.loadAll(); }

  loadAll() {
    this.isLoading = true;
    this.epi.getStats().subscribe(s => this.stats = s);
    this.epi.getUpcoming().subscribe(u => this.upcoming = u);
    this.epi.getMissed().subscribe(m => this.missed = m);
    this.epi.getPendingChildren().subscribe(p => this.pendingList = p);
    this.epi.getAllChildren().subscribe({
      next: (res) => {
        this.children = res;
        this.filtered = res.filter(c => c.status === 'Approved');
        this.isLoading = false;
        // auto-switch to children if no pending
        if (this.pendingList.length === 0) this.activeTab = 'children';
      },
      error: () => { this.errorMsg = 'There was a problem loading the data.'; this.isLoading = false; }
    });
  }

  applyFilter() {
    const src = this.children.filter(c => c.status === 'Approved');
    this.filtered = src.filter(c => {
      const matchText = !this.searchText ||
        c.childName?.toLowerCase().includes(this.searchText.toLowerCase()) ||
        c.cardNo?.includes(this.searchText) ||
        c.guardianNid?.includes(this.searchText);
      const matchWard = !this.selectedWard || c.ward === this.selectedWard;
      return matchText && matchWard;
    });
  }

  // ── QR Scanner: EPI card → child + schedule → dose entry ──
  openScanner() {
    this.showScannerModal = true;
    this.scannerMessage = '';
    this.scannedChild = null;
    this.scannedSchedule = [];
    setTimeout(() => this.startScanner(), 150);
  }

  closeScanner() {
    this.stopScanner();
    this.showScannerModal = false;
    this.scannedChild = null;
    this.scannedSchedule = [];
  }

  async startScanner() {
    this.stopScanner();
    const BarcodeDetectorCtor = (window as any).BarcodeDetector;
    if (!BarcodeDetectorCtor) {
      this.scannerMessage = 'Camera QR detection is not supported in this browser. Use Chrome/Edge or enter the card number manually.';
      return;
    }
    try {
      this.barcodeDetector = new BarcodeDetectorCtor({ formats: ['qr_code'] });
      this.scannerStream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: { ideal: 'environment' } }, audio: false
      });
      const video = this.qrVideo?.nativeElement;
      if (!video) throw new Error('Scanner video is not ready');
      video.srcObject = this.scannerStream;
      await video.play();
      this.scannerActive = true;
      this.scannerMessage = 'Point the camera at the QR code on the EPI card.';
      this.scanTimer = setInterval(() => this.detectQrFrame(), 650);
    } catch (e: any) {
      this.scannerMessage = e?.message || 'Camera permission denied or camera unavailable.';
      this.stopScanner();
    }
  }

  private async detectQrFrame() {
    if (!this.scannerActive || !this.barcodeDetector || !this.qrVideo?.nativeElement) return;
    const video = this.qrVideo.nativeElement;
    if (video.readyState < 2) return;
    try {
      const codes = await this.barcodeDetector.detect(video);
      if (codes && codes.length > 0) {
        const raw = String(codes[0].rawValue || '').trim();
        if (raw) {
          this.stopScanner();
          this.lookupScannedCard(raw);
        }
      }
    } catch {
      // Ignore transient detector errors and keep scanning.
    }
  }

  lookupManualCard() {
    const value = this.manualCardNo.trim();
    if (!value) {
      this.scannerMessage = 'Enter an EPI card number.';
      return;
    }
    this.stopScanner();
    this.lookupScannedCard(value);
  }

  private lookupScannedCard(rawValue: string) {
    const payload = rawValue.startsWith('EPI:') ? rawValue.substring(4) : rawValue;
    this.scannerMessage = 'Card detected. Loading vaccination schedule...';
    this.epi.scanCard(payload).subscribe({
      next: (res: any) => {
        this.scannedChild = res.child;
        this.scannedSchedule = res.schedule || [];
        this.manualCardNo = res.child?.cardNo || '';
        this.scannerMessage = 'Card verified. Select a pending dose to enter vaccination details.';
      },
      error: (err: any) => {
        this.scannerMessage = err?.error?.message || 'EPI card not found.';
        this.scannedChild = null;
        this.scannedSchedule = [];
      }
    });
  }

  openScannedDose(vacc: any) {
    this.selectedVacc = { ...vacc, childName: this.scannedChild?.childName };
    this.showGiveModal = true;
    this.givenBy = '';
    this.healthCenter = '';
    this.batchNo = '';
    this.remarks = '';
  }

  stopScanner() {
    this.scannerActive = false;
    if (this.scanTimer) { clearInterval(this.scanTimer); this.scanTimer = null; }
    if (this.scannerStream) {
      this.scannerStream.getTracks().forEach(t => t.stop());
      this.scannerStream = null;
    }
    if (this.qrVideo?.nativeElement) this.qrVideo.nativeElement.srcObject = null;
  }

  // ── Approve child ──────────────────────────────────────────
  approveChild(child: any) {
    this.openApproval(child);
  }

  openApproval(child: any) {
    this.approvalChild = child;
    this.approvalSignatureBase64 = '';
    this.approvalSealBase64 = '';
    this.approvalSignatureName = '';
    this.approvalSealName = '';
    this.approvalSubmitting = false;
  }

  closeApproval() {
    if (this.approvalSubmitting) return;
    this.approvalChild = null;
    this.approvalSignatureBase64 = '';
    this.approvalSealBase64 = '';
    this.approvalSignatureName = '';
    this.approvalSealName = '';
  }

  onApprovalUpload(event: Event, type: 'signature' | 'seal') {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      alert(type === 'signature' ? 'Please select a signature image.' : 'Please select a seal image.');
      input.value = '';
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      if (type === 'signature') {
        this.approvalSignatureBase64 = String(reader.result || '');
        this.approvalSignatureName = file.name;
      } else {
        this.approvalSealBase64 = String(reader.result || '');
        this.approvalSealName = file.name;
      }
    };
    reader.readAsDataURL(file);
  }

  submitApproval() {
    const child = this.approvalChild;
    if (!child) return;
    if (!this.approvalSignatureBase64) { alert('Signature image is required.'); return; }
    if (!this.approvalSealBase64) { alert('Seal image is required.'); return; }
    this.approvalSubmitting = true;
    this.epi.approveChild(child.id, {
      signatureBase64: this.approvalSignatureBase64,
      sealBase64: this.approvalSealBase64
    }).subscribe({
      next: () => {
        this.showToast(`✅ ${this.esc(child.childName)} ${this.approvalLabel(child).toLowerCase()} completed.`);
        this.approvalSubmitting = false;
        this.approvalChild = null;
        this.approvalSignatureBase64 = '';
        this.approvalSealBase64 = '';
        this.approvalSignatureName = '';
        this.approvalSealName = '';
        this.loadAll();
      },
      error: (err) => {
        this.approvalSubmitting = false;
        alert(err?.error?.message || 'Approval failed.');
      }
    });
  }

  onEpiSignatureSelected(event: Event, child: any, sealInput: HTMLInputElement) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) { alert('Please select a signature image.'); input.value = ''; return; }
    const reader = new FileReader();
    reader.onload = () => {
      this.pendingEpiApproval = { child, signatureBase64: String(reader.result || '') };
      input.value = '';
      sealInput.click();
    };
    reader.readAsDataURL(file);
  }

  onEpiSealSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !this.pendingEpiApproval) return;
    if (!file.type.startsWith('image/')) { alert('Please select a seal image.'); input.value = ''; return; }
    const reader = new FileReader();
    reader.onload = () => {
      const { child, signatureBase64 } = this.pendingEpiApproval!;
      const sealBase64 = String(reader.result || '');
      const stepLabel = this.approvalLabel(child);
      if (!confirm(`${stepLabel} EPI registration for "${this.esc(child.childName)}"?
Signature and seal will be saved for this approval step and printed on the EPI card.`)) { this.pendingEpiApproval = null; input.value = ''; return; }
      this.epi.approveChild(child.id, { signatureBase64, sealBase64 }).subscribe({
        next: () => { this.showToast(`✅ ${this.esc(child.childName)} approved — email sent`); this.loadAll(); },
        error: (err) => alert(err?.error?.message || 'Approval failed.')
      });
      this.pendingEpiApproval = null;
      input.value = '';
    };
    reader.readAsDataURL(file);
  }

  // ── Download EPI card PDF ──────────────────────────────────
  downloadCard(child: any) {
    if (child.status !== 'Approved') {
      alert('Two-step approval must be completed before downloading the card.');
      return;
    }
    this.epi.downloadCard(child.id, child.cardNo);
  }

  approvalLabel(child: any): string {
    return (child?.approvalStage || 0) >= 1 || child?.status === 'First Approved'
      ? 'Final Approve'
      : 'First Approve';
  }

  // ── View schedule ──────────────────────────────────────────
  viewSchedule(child: any) {
    this.selectedChild = child;
    this.epi.getSchedule(child.id).subscribe(v => {
      this.vaccinations = v;
      this.renderScheduleModal(child, v);
    });
  }

  openGiveModal(vacc: any) {
    this.selectedVacc = vacc;
    this.showGiveModal = true;
    this.givenBy = ''; this.healthCenter = ''; this.batchNo = ''; this.remarks = '';
  }

  confirmGive() {
    if (!this.selectedVacc) return;
    this.epi.markGiven(this.selectedVacc.vaccinationId ?? this.selectedVacc.id, {
      givenBy:      this.givenBy,
      healthCenter: this.healthCenter,
      batchNo:      this.batchNo,
      remarks:      this.remarks
    }).subscribe({
      next: () => {
        this.showToast('✅ Vaccine record saved successfully.');
        this.showGiveModal = false;
        this.loadAll();
        if (this.showScannerModal && this.scannedChild?.cardNo) {
          this.lookupScannedCard(this.scannedChild.cardNo);
        }
      },
      error: (err) => alert(err?.error?.message || 'Failed. Please try again.')
    });
  }

  markMissed(vaccId: number) {
    this.epi.markMissed(vaccId).subscribe(() => {
      this.showToast('Marked as missed.');
      this.loadAll();
    });
  }

  deleteChild(id: number) {
    if (!confirm('Delete this child record?')) return;
    this.epi.deleteChild(id).subscribe(() => {
      this.showToast('Deleted.');
      this.loadAll();
    });
  }

  setTab(t: string) { this.activeTab = t; }

  toggleChildDetails(child: any) {
    this.expandedChildId = this.expandedChildId === child.id ? null : child.id;
  }

  detailFields(child: any): { key: string; value: any }[] {
    return [
      { key: 'Card No', value: child.cardNo },
      { key: 'Child Name', value: child.childName },
      { key: 'Date of Birth', value: child.dateOfBirth },
      { key: 'Birth Place', value: child.birthPlace },
      { key: 'Gender', value: child.gender },
      { key: 'Father Name', value: child.fatherName },
      { key: 'Father NID', value: child.fatherNid },
      { key: 'Mother Name', value: child.motherName },
      { key: 'Mother NID', value: child.motherNid },
      { key: 'Guardian NID', value: child.guardianNid },
      { key: 'Mobile', value: child.guardianPhone },
      { key: 'Email', value: child.guardianEmail },
      { key: 'Ward', value: child.ward },
      { key: 'Union / Municipality', value: child.unionName },
      { key: 'Upazila', value: child.upazila },
      { key: 'District', value: child.district },
      { key: 'Present Address', value: child.presentAddress },
      { key: 'Permanent Address', value: child.permanentAddress },
      { key: 'Additional Address', value: child.address },
      { key: 'Status', value: child.status },
      { key: 'Registered', value: child.createdAt }
    ].filter(f => f.value !== null && f.value !== undefined && String(f.value).trim() !== '');
  }

  fileEntries(child: any): { label: string; url: string; isImage: boolean }[] {
    return [
      { label: 'Child Photo', url: child.childPhotoUrl },
      { label: 'Father NID File', url: child.fatherNidFileUrl },
      { label: 'Mother NID File', url: child.motherNidFileUrl }
    ]
      .filter(f => !!f.url)
      .map(f => ({
        ...f,
        isImage: /\.(png|jpe?g|webp|gif|bmp)$/i.test(f.url || '')
      }));
  }

  fileUrl(path: string): string {
    if (!path) return '';
    if (/^(https?:|data:|blob:)/i.test(path)) return path;
    return `${this.serverBase}/${path.replace(/^\/+/, '')}`;
  }

  renderChildDetails(child: any) {
    this.selectedDetailChild = child;
    /*
    if (this.modalEl) { document.body.removeChild(this.modalEl); this.modalEl = null; }

    const fields = this.detailFields(child).map(f => `
      <div class="epi-detail-item">
        <span>${this.esc(f.key)}</span>
        <strong>${this.esc(f.value)}</strong>
      </div>
    `).join('');

    const files = this.fileEntries(child).map(f => {
      const url = this.fileUrl(f.url);
      const preview = f.isImage
        ? `<img src="${this.esc(url)}" alt="${this.esc(f.label)}">`
        : `<div class="epi-pdf-icon"><i class="fas fa-file-pdf"></i><span>Document</span></div>`;
      return `
        <div class="epi-file-card">
          <div class="epi-file-label">${this.esc(f.label)}</div>
          <div class="epi-file-preview">${preview}</div>
          <a href="${this.esc(url)}" target="_blank" class="epi-file-link">
            <i class="fas fa-download"></i> View / Download
          </a>
        </div>
      `;
    }).join('');

    const html = `
      <div class="modal-overlay" id="epi-child-detail-overlay">
        <div class="modal-box epi-child-detail-modal">
          <div class="modal-head">
            <span>Child Registration Details</span>
            <button id="epi-child-detail-close">✕</button>
          </div>
          <div class="modal-body">
            <div class="child-summary">
              <div class="child-avatar">👶</div>
              <div>
                <h3>${this.esc(child.childName)}</h3>
                <p>${this.esc(child.cardNo)} · ${this.esc(child.gender)} · ${this.esc(this.ageLabel(child.dateOfBirth))}</p>
                <p>${this.esc(child.fatherName)} / ${this.esc(child.motherName)}</p>
              </div>
            </div>
            <div class="epi-detail-grid">${fields}</div>
            ${files ? `
              <div class="epi-file-section">
                <div class="epi-detail-title"><i class="fas fa-paperclip"></i> Uploaded Files</div>
                <div class="epi-file-cards">${files}</div>
              </div>
            ` : ''}
          </div>
        </div>
      </div>
    `;

    const wrapper = document.createElement('div');
    wrapper.innerHTML = html;
    this.modalEl = wrapper.firstElementChild as HTMLElement;
    document.body.appendChild(this.modalEl);
    const close = () => {
      if (this.modalEl) { document.body.removeChild(this.modalEl); this.modalEl = null; }
    };
    document.getElementById('epi-child-detail-close')?.addEventListener('click', close);
    this.modalEl.addEventListener('click', (e) => {
      if ((e.target as HTMLElement).id === 'epi-child-detail-overlay') close();
    });
    */
  }

  closeDetailModal() {
    this.selectedDetailChild = null;
  }

  statusClass(s: string) {
    if (!s) return '';
    return s === 'Given'     ? 'badge-given'
         : s === 'Scheduled' ? 'badge-scheduled'
         : s === 'Due'       ? 'badge-due'
         : 'badge-missed';
  }

  ageLabel(dob: string): string {
    if (!dob) return '';
    const d = new Date(dob);
    const now = new Date();
    const months = (now.getFullYear() - d.getFullYear()) * 12 + (now.getMonth() - d.getMonth());
    if (months < 1)  return `${Math.floor((now.getTime()-d.getTime())/(1000*60*60*24))} Day`;
    if (months < 12) return `${months} Month`;
    return `${Math.floor(months/12)} Year ${months%12} Month`;
  }

  private esc(value: any): string {
    return String(value ?? '—')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  // ── Schedule modal (unchanged logic, just kept intact) ─────
  private renderScheduleModal(child: any, vaccinations: any[]) {
    if (this.modalEl) { document.body.removeChild(this.modalEl); this.modalEl = null; }

    const rows = vaccinations.map((v, i) => {
      const sc = v.status === 'Given'  ? '#065f46' : v.status === 'Due'    ? '#92400e'
               : v.status === 'Missed' ? '#991b1b' : '#1e40af';
      const bg = v.status === 'Given'  ? '#d1fae5' : v.status === 'Due'    ? '#fef3c7'
               : v.status === 'Missed' ? '#fee2e2' : '#eff6ff';
      const actionBtn = v.status !== 'Given'
        ? `<button data-vacc-id="${this.esc(v.id)}" data-vacc-name="${this.esc(v.vaccineName)}" data-dose="${this.esc(v.doseNo)}" data-action="give"
             style="background:#d1fae5;color:#065f46;border:none;padding:4px 10px;border-radius:6px;font-size:11px;font-weight:600;cursor:pointer;margin-right:4px;">
             ✅ Give
           </button>
           <button data-vacc-id="${this.esc(v.id)}" data-action="missed"
             style="background:#fee2e2;color:#991b1b;border:none;padding:4px 10px;border-radius:6px;font-size:11px;font-weight:600;cursor:pointer;">
             ✗ Missed
           </button>`
        : `<span style="font-size:11px;color:#065f46;">✓ Completed</span>`;

      return `<tr style="border-bottom:1px solid #f3f4f6;">
        <td style="padding:10px 8px;">${i+1}</td>
        <td style="padding:10px 8px;"><strong>${this.esc(v.vaccineName)}</strong> Dose ${this.esc(v.doseNo)}</td>
        <td style="padding:10px 8px;">${this.esc(v.scheduledDate)}</td>
        <td style="padding:10px 8px;">${this.esc(v.givenDate)}</td>
        <td style="padding:10px 8px;">
          <span style="background:${bg};color:${sc};padding:2px 10px;border-radius:12px;font-size:11px;font-weight:700;">${this.esc(v.status)}</span>
        </td>
        <td style="padding:10px 8px;">${this.esc(v.givenBy)}</td>
        <td style="padding:10px 8px;">${actionBtn}</td>
      </tr>`;
    }).join('');

    const giveForm = `
      <div id="epi-give-panel" style="display:none;background:#f0fdf4;border:1px solid #bbf7d0;border-radius:10px;padding:16px;margin:12px 0 0;">
        <div style="font-size:13px;font-weight:600;color:#065f46;margin-bottom:12px;" id="epi-give-title">Vaccinate</div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;">
          <div><label style="font-size:11px;font-weight:600;color:#6b7280;display:block;margin-bottom:4px;">Vaccinator</label>
            <input id="epi-given-by" placeholder="Name of vaccinator"
              style="width:100%;padding:7px 10px;border:1.5px solid #e5e7eb;border-radius:6px;font-size:13px;box-sizing:border-box;"/></div>
          <div><label style="font-size:11px;font-weight:600;color:#6b7280;display:block;margin-bottom:4px;">Health Center</label>
            <input id="epi-health-center" placeholder="Center Name"
              style="width:100%;padding:7px 10px;border:1.5px solid #e5e7eb;border-radius:6px;font-size:13px;box-sizing:border-box;"/></div>
          <div><label style="font-size:11px;font-weight:600;color:#6b7280;display:block;margin-bottom:4px;">Batch Number</label>
            <input id="epi-batch-no" placeholder="Batch Number"
              style="width:100%;padding:7px 10px;border:1.5px solid #e5e7eb;border-radius:6px;font-size:13px;box-sizing:border-box;"/></div>
          <div><label style="font-size:11px;font-weight:600;color:#6b7280;display:block;margin-bottom:4px;">Remarks</label>
            <input id="epi-remarks" placeholder="Optional"
              style="width:100%;padding:7px 10px;border:1.5px solid #e5e7eb;border-radius:6px;font-size:13px;box-sizing:border-box;"/></div>
        </div>
        <div style="display:flex;gap:8px;margin-top:12px;">
          <button id="epi-give-confirm"
            style="background:#064e3b;color:#fff;border:none;padding:8px 20px;border-radius:8px;font-size:13px;font-weight:600;cursor:pointer;">
            ✅ Confirm
          </button>
          <button id="epi-give-cancel"
            style="background:#f3f4f6;color:#374151;border:none;padding:8px 16px;border-radius:8px;font-size:13px;cursor:pointer;">
            Cancel
          </button>
        </div>
      </div>`;

    const html = `
    <div id="epi-modal-overlay" style="position:fixed;inset:0;background:rgba(0,0,0,.5);z-index:99999;display:flex;align-items:center;justify-content:center;padding:16px;">
      <div style="background:#fff;width:860px;max-width:96vw;border-radius:16px;overflow:hidden;box-shadow:0 20px 60px rgba(0,0,0,.25);max-height:90vh;display:flex;flex-direction:column;">
        <div style="background:#064e3b;padding:18px 24px;display:flex;align-items:center;justify-content:space-between;flex-shrink:0;">
          <div>
            <div style="color:#fff;font-size:16px;font-weight:700;">💉 ${this.esc(child.childName)} — Vaccination Schedule</div>
            <div style="color:#a7f3d0;font-size:12px;margin-top:3px;">Card No: ${this.esc(child.cardNo)} | Father: ${this.esc(child.fatherName)} | Mother: ${this.esc(child.motherName)}</div>
          </div>
          <div style="display:flex;gap:8px;align-items:center;">
            <button id="epi-dl-pdf"
              style="background:#f59e0b;color:#fff;border:none;padding:7px 14px;border-radius:8px;font-size:12px;font-weight:700;cursor:pointer;">
              📄 Download Card PDF
            </button>
            <button id="epi-modal-close" style="background:rgba(255,255,255,.15);border:none;color:#fff;width:32px;height:32px;border-radius:8px;cursor:pointer;font-size:16px;">✕</button>
          </div>
        </div>
        <div style="overflow-y:auto;padding:20px;flex:1;">
          <table style="width:100%;border-collapse:collapse;font-size:13px;">
            <thead>
              <tr style="background:#f1f5f9;">
                <th style="padding:10px 8px;text-align:left;font-size:11px;text-transform:uppercase;letter-spacing:.04em;color:#6b7280;">#</th>
                <th style="padding:10px 8px;text-align:left;font-size:11px;text-transform:uppercase;letter-spacing:.04em;color:#6b7280;">Vaccine</th>
                <th style="padding:10px 8px;text-align:left;font-size:11px;text-transform:uppercase;letter-spacing:.04em;color:#6b7280;">Scheduled</th>
                <th style="padding:10px 8px;text-align:left;font-size:11px;text-transform:uppercase;letter-spacing:.04em;color:#6b7280;">Given</th>
                <th style="padding:10px 8px;text-align:left;font-size:11px;text-transform:uppercase;letter-spacing:.04em;color:#6b7280;">Status</th>
                <th style="padding:10px 8px;text-align:left;font-size:11px;text-transform:uppercase;letter-spacing:.04em;color:#6b7280;">Given By</th>
                <th style="padding:10px 8px;text-align:left;font-size:11px;text-transform:uppercase;letter-spacing:.04em;color:#6b7280;">Action</th>
              </tr>
            </thead>
            <tbody>${rows}</tbody>
          </table>
          ${giveForm}
        </div>
      </div>
    </div>`;

    const w = document.createElement('div');
    w.innerHTML = html;
    this.modalEl = w.firstElementChild as HTMLElement;
    document.body.appendChild(this.modalEl);

    let activeVaccId: number | null = null;
    const closeModal = () => {
      if (this.modalEl) { document.body.removeChild(this.modalEl); this.modalEl = null; }
    };

    this.modalEl.addEventListener('click', (e) => {
      if ((e.target as HTMLElement).id === 'epi-modal-overlay') closeModal();
    });
    document.getElementById('epi-modal-close')?.addEventListener('click', closeModal);

    // Download PDF from modal
    document.getElementById('epi-dl-pdf')?.addEventListener('click', () => {
      this.epi.downloadCard(child.id, child.cardNo);
    });

    this.modalEl.querySelectorAll('[data-action="give"]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const el = e.currentTarget as HTMLElement;
        activeVaccId = parseInt(el.dataset['vaccId']!);
        const panel = document.getElementById('epi-give-panel')!;
        document.getElementById('epi-give-title')!.textContent =
          `${el.dataset['vaccName']} — Dose ${el.dataset['dose']} Vaccinate`;
        panel.style.display = 'block';
        panel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      });
    });

    this.modalEl.querySelectorAll('[data-action="missed"]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const vaccId = parseInt((e.currentTarget as HTMLElement).dataset['vaccId']!);
        this.epi.markMissed(vaccId).subscribe({
          next: () => { this.showToast('Marked as missed.'); closeModal(); this.loadAll(); },
          error: () => alert('Failed to mark as missed.')
        });
      });
    });

    document.getElementById('epi-give-confirm')?.addEventListener('click', () => {
      if (!activeVaccId) return;
      const body = {
        givenBy:      (document.getElementById('epi-given-by')      as HTMLInputElement).value,
        healthCenter: (document.getElementById('epi-health-center')  as HTMLInputElement).value,
        batchNo:      (document.getElementById('epi-batch-no')       as HTMLInputElement).value,
        remarks:      (document.getElementById('epi-remarks')        as HTMLInputElement).value,
      };
      this.epi.markGiven(activeVaccId, body).subscribe({
        next: () => { this.showToast('✅ Vaccination record saved.'); closeModal(); this.loadAll(); },
        error: (err: any) => alert(err?.error?.message || 'An error occurred.')
      });
    });

    document.getElementById('epi-give-cancel')?.addEventListener('click', () => {
      document.getElementById('epi-give-panel')!.style.display = 'none';
      activeVaccId = null;
    });
  }

  // ── Toast ──────────────────────────────────────────────────
  showToast(msg: string) {
    const el = document.createElement('div');
    el.style.cssText = 'position:fixed;bottom:24px;right:24px;background:#064e3b;color:#d1fae5;'
      + 'padding:12px 20px;border-radius:10px;font-size:13px;font-weight:600;z-index:99999;'
      + 'box-shadow:0 8px 24px rgba(0,0,0,.18);transition:opacity .3s;';
    el.textContent = msg;
    document.body.appendChild(el);
    setTimeout(() => { el.style.opacity = '0'; setTimeout(() => el.remove(), 300); }, 2800);
  }

  ngOnDestroy() {
    this.stopScanner();
    if (this.modalEl) { document.body.removeChild(this.modalEl); this.modalEl = null; }
  }
}
