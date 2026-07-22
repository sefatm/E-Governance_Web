import { Component, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ETenderService } from 'src/app/services/etender.service';
import { ETenderNotice } from 'src/app/models/etender.model';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-etender-bid',
  templateUrl: './etender-bid.component.html',
  styleUrls: ['./etender-bid.component.css']
})
export class ETenderBidComponent implements OnInit {

  openNotices    : ETenderNotice[] = [];
  selectedNotice : ETenderNotice | null = null;

  form = {
    tenderId       : 0,
    bidderName     : '',
    companyName    : '',
    nid            : '',
    mobile         : '',
    email          : '',
    bidAmount      : 0,
    completionDays : 0,
    experienceYears: 0,
    previousWorks  : '',
    emdReceiptNo   : ''
  };

  agree        = false;
  isSubmitting = false;

  selectedFile    : File | null = null;
  filePreviewName = '';
  fileError       = '';

  blacklistChecking = false;
  blacklistBlocked  = false;
  blacklistMessage  = '';

  toast         : { type: 'success' | 'error'; msg: string } | null = null;
  submitSuccess  = false;
  submittedBidId = 0;

  constructor(public ls: LanguageService, private svc: ETenderService) {}

  ngOnInit(): void {
    this.svc.getOpenNotices().subscribe({
      next : d => this.openNotices = d,
      error: () => this.showToast('error', 'Open Tenders not loaded.')
    });
  }

  onTenderSelect(): void {
    this.selectedNotice   = this.openNotices.find(n => n.id === +this.form.tenderId) || null;
    this.blacklistBlocked = false;
    this.blacklistMessage = '';
  }

  onFileSelect(event: Event): void {
    this.fileError = '';
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) { this.selectedFile = null; this.filePreviewName = ''; return; }
    const file    = input.files[0];
    const allowed = ['application/pdf', 'image/jpeg', 'image/png'];
    if (!allowed.includes(file.type)) {
      this.fileError = 'Only PDF, JPG, PNG files are allowed.';
      this.selectedFile = null; input.value = ''; return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.fileError = 'File size must be less than 5MB.';
      this.selectedFile = null; input.value = ''; return;
    }
    this.selectedFile    = file;
    this.filePreviewName = file.name;
  }

  removeFile(): void {
    this.selectedFile = null; this.filePreviewName = ''; this.fileError = '';
    const input = document.getElementById('docFile') as HTMLInputElement;
    if (input) input.value = '';
  }

  checkBlacklist(): void {
    if (!this.form.nid && !this.form.email && !this.form.mobile) return;
    this.blacklistChecking = true;
    this.svc.checkBlacklist(this.form.nid, this.form.email, this.form.mobile).subscribe({
      next: (res) => {
        this.blacklistChecking = false;
        this.blacklistBlocked  = res.blacklisted;
        this.blacklistMessage  = res.message;
      },
      error: () => { this.blacklistChecking = false; }
    });
  }

  submitForm(f: NgForm): void {
    if (!f.valid || !this.agree) return;

    this.isSubmitting = true;

    this.svc.checkBlacklist(this.form.nid, this.form.email, this.form.mobile).subscribe({
      next: (res) => {
        if (res.blacklisted) {
          this.isSubmitting     = false;
          this.blacklistBlocked = true;
          this.blacklistMessage = res.message;
          this.showToast('error', res.message);
          return;
        }
        this.doSubmit(f);
      },
      error: () => {
        this.doSubmit(f);
      }
    });
  }

  private doSubmit(f: NgForm): void {
    const fd = new FormData();
    fd.append('tenderId',        String(this.form.tenderId));
    fd.append('bidderName',      this.form.bidderName);
    fd.append('companyName',     this.form.companyName);
    fd.append('nid',             this.form.nid);
    fd.append('mobile',          this.form.mobile);
    fd.append('email',           this.form.email || '');
    fd.append('bidAmount',       String(this.form.bidAmount));
    fd.append('completionDays',  String(this.form.completionDays));
    fd.append('experienceYears', String(this.form.experienceYears || 0));
    fd.append('previousWorks',   this.form.previousWorks || '');
    fd.append('emdReceiptNo',    this.form.emdReceiptNo || '');
    if (this.selectedFile) fd.append('document', this.selectedFile);

    this.svc.submitBidWithDoc(fd).subscribe({
      next: (res) => {
        this.isSubmitting  = false;
        this.submitSuccess = true;
        this.submittedBidId = res.id || 0;
        this.showToast('success', 'Bid submitted successfully!');
        this.resetForm(f);
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = err?.error?.message || 'Bid submission failed.';
        this.showToast('error', msg);
        if (msg.toLowerCase().includes('blacklist')) {
          this.blacklistBlocked = true;
          this.blacklistMessage = msg;
        }
      }
    });
  }

  resetForm(f?: NgForm): void {
    this.form = {
      tenderId: 0, bidderName: '', companyName: '', nid: '',
      mobile: '', email: '', bidAmount: 0, completionDays: 0,
      experienceYears: 0, previousWorks: '', emdReceiptNo: ''
    };
    this.agree = false; this.selectedNotice = null;
    this.blacklistBlocked = false; this.blacklistMessage = '';
    this.removeFile();
    if (f) f.resetForm();
  }

  formatCurrency(v?: number): string {
    return v != null ? '৳ ' + Number(v).toLocaleString('en-IN') : '—';
  }

  daysLeft(endDate?: string): number {
    if (!endDate) return 99;
    return Math.ceil((new Date(endDate).getTime() - new Date().setHours(0,0,0,0)) / 86400000);
  }

  showToast(type: 'success' | 'error', msg: string): void {
    this.toast = { type, msg };
    setTimeout(() => this.toast = null, 5000);
  }
}
