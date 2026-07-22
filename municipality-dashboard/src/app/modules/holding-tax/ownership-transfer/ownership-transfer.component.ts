import { Component, OnInit } from '@angular/core';
import { OwnershipTransfer } from 'src/app/models/ownership-transfer.model';
import { OwnershipTransferService } from 'src/app/services/ownership-transfer.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { message: string; type: 'success' | 'error'; removing?: boolean; }

@Component({
  selector: 'app-ownership-transfer',
  templateUrl: './ownership-transfer.component.html',
  styleUrls: ['./ownership-transfer.component.css']
})
export class OwnershipTransferComponent implements OnInit {

  // Multi-step 
  steps       = ['Owner Info', 'Property Details', 'Documents'];
  currentStep = 1;
  touched     = false;

  // Form
  form: OwnershipTransfer & {
    currentOwnerNid?: string;
    newOwnerNid?: string;
    wardNo?: string;
    relationship?: string;
    declaration?: boolean;
  } = this.emptyForm();

  isSubmitting = false;

  // File uploads 
  files: { currentOwnerNid?: File; newOwnerNid?: File; deed?: File } = {};

  // Ward list 
  wards = Array.from({ length: 9 }, (_, i) => i + 1);

  // Tabs
  activeTab: 'form' | 'list' | 'status' = 'form';

  // List
  applications: OwnershipTransfer[] = [];
  isLoading = false;

  // Status check 
  statusSearchText = '';
  statusResult: OwnershipTransfer | null = null;
  statusSearched   = false;
  statusLoading    = false;
  downloadModalApp: OwnershipTransfer | null = null;

  // Toasts
  toasts: Toast[] = [];

  constructor(public ls: LanguageService, private service: OwnershipTransferService) {}

  ngOnInit(): void { this.loadApplications(); }

  // Navigation 
  nextStep(): void {
    this.touched = true;
    if (this.currentStep === 1) {
      if (!this.form.currentOwner || !this.form.currentOwnerNid ||
          !this.form.newOwner     || !this.form.newOwnerNid     || !this.form.contact) {
        this.showToast('Please fill all required fields.', 'error');
        return;
      }
    }
    if (this.currentStep === 2) {
      if (!this.form.holdingNumber || !this.form.address || !this.form.reason) {
        this.showToast('Please fill all required fields.', 'error');
        return;
      }
    }
    this.touched = false;
    if (this.currentStep < this.steps.length) this.currentStep++;
  }

  prevStep(): void {
    if (this.currentStep > 1) this.currentStep--;
  }

  // File upload
  onFile(event: Event, key: 'currentOwnerNid' | 'newOwnerNid' | 'deed'): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) this.files[key] = file;
  }

  removeFile(key: 'currentOwnerNid' | 'newOwnerNid' | 'deed'): void {
    delete this.files[key];
  }

  // Submit 
  submitForm(): void {
    if (!this.form.declaration) {
      this.showToast('Please accept the declaration.', 'error');
      return;
    }

    this.isSubmitting = true;
    this.form.status       = 'Pending';
    this.form.rejectReason = '';

    this.service.createApplication(this.form, this.files).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showToast('Application submitted successfully!', 'success');
        this.form        = this.emptyForm();
        this.files       = {};
        this.currentStep = 1;
        this.loadApplications();
        setTimeout(() => this.switchTab('list'), 1500);
      },
      error: () => {
        this.isSubmitting = false;
        this.showToast('Submission failed. Please try again.', 'error');
      }
    });
  }

  // Load applications
  loadApplications(): void {
    this.isLoading = true;
    this.service.getAll().subscribe({
      next: (res) => { this.applications = res; this.isLoading = false; },
      error: ()    => { this.isLoading = false; }
    });
  }

  switchTab(tab: 'form' | 'list' | 'status'): void {
    this.activeTab = tab;
    if (tab === 'list') this.loadApplications();
  }

  // Status check 
  checkStatus(): void {
    if (!this.statusSearchText.trim()) {
      this.showToast('Enter Holding Number or Owner Name.', 'error');
      return;
    }
    this.statusLoading  = true;
    this.statusResult   = null;
    this.statusSearched = false;

    this.service.getAll().subscribe({
      next: (res) => {
        const txt = this.statusSearchText.toLowerCase().trim();
        this.statusResult   = res.find(a =>
          a.holdingNumber?.toLowerCase() === txt ||
          a.currentOwner?.toLowerCase().includes(txt) ||
          a.newOwner?.toLowerCase().includes(txt)
        ) || null;
        this.statusSearched = true;
        this.statusLoading  = false;
      },
      error: () => { this.statusLoading = false; }
    });
  }

  openDownloadPopup(app: OwnershipTransfer | null): void {
    if (!app?.id) return;
    this.downloadModalApp = app;
  }

  closeDownloadPopup(): void {
    this.downloadModalApp = null;
  }

  confirmDownload(): void {
    const id = this.downloadModalApp?.id;
    if (!id) return;
    this.service.downloadCertificate(id);
    this.downloadModalApp = null;
  }

  isApproved(status?: string): boolean {
    return (status || '').replace(/['"]/g, '').trim().toLowerCase() === 'approved';
  }

  getStatusClass(status?: string): string {
    const s = (status || '').replace(/['"]/g, '').trim().toLowerCase();
    if (s === 'approved') return 'badge-approved';
    if (s === 'rejected') return 'badge-rejected';
    return 'badge-pending';
  }

  // Toast 
  showToast(message: string, type: 'success' | 'error'): void {
    const t: Toast = { message, type };
    this.toasts.push(t);
    setTimeout(() => { t.removing = true; }, 2500);
    setTimeout(() => { this.toasts = this.toasts.filter(x => x !== t); }, 3000);
  }

  // Empty form 
  emptyForm() {
    return {
      currentOwner:    '',
      currentOwnerNid: '',
      newOwner:        '',
      newOwnerNid:     '',
      holdingNumber:   '',
      wardNo:          '',
      address:         '',
      reason:          '',
      relationship:    '',
      contact:         '',
      declaration:     false,
      status:          'Pending',
      rejectReason:    ''
    };
  }
}
