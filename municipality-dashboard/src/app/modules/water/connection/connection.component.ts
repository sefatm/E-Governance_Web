import { Component, OnInit } from '@angular/core';
import { WaterConnection } from 'src/app/models/water-connection.model';
import { WaterConnectionService } from 'src/app/services/water-connection.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success'|'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-connection',
  templateUrl: './connection.component.html',
  styleUrls: ['./connection.component.css']
})
export class ConnectionComponent implements OnInit {

  applications: WaterConnection[] = [];
  isLoading    = false;
  isSubmitting = false;
  activeTab: 'form'|'list'|'status' = 'form';
  currentStep  = 1;
  steps        = ['Applicant Info', 'Address Details', 'Declaration'];
  toasts: Toast[] = [];

  statusSearch   = '';
  statusResult: WaterConnection|null = null;
  statusSearched = false;

  application: WaterConnection = this.emptyForm();

  constructor(public ls: LanguageService, private service: WaterConnectionService) {}

  ngOnInit(): void { this.loadApplications(); }

  loadApplications(): void {
    this.isLoading = true;
    this.service.getAll().subscribe({
      next: r => { this.applications = r; this.isLoading = false; },
      error: () => this.isLoading = false
    });
  }

  switchTab(tab: 'form'|'list'|'status'): void {
    this.activeTab = tab;
    if (tab === 'list') this.loadApplications();
  }

  nextStep(): void {
    if (this.currentStep === 1) {
      if (!this.application.name || !this.application.phone) {
        this.showToast('Please fill Name and Phone number', 'error'); return;
      }
      // email থাকলে format validate করো
      if (this.application.email && !this.isValidEmail(this.application.email)) {
        this.showToast('Please enter a valid email address', 'error'); return;
      }
    }
    if (this.currentStep === 2) {
      if (!this.application.district || !this.application.upazila ||
          !this.application.ward || !this.application.address) {
        this.showToast('Please fill all required address fields', 'error'); return;
      }
    }
    if (this.currentStep < 3) { this.currentStep++; window.scrollTo({ top: 0, behavior: 'smooth' }); }
  }

  prevStep(): void {
    if (this.currentStep > 1) { this.currentStep--; window.scrollTo({ top: 0, behavior: 'smooth' }); }
  }

  submit(): void {
    if (!this.application.agree) { this.showToast('Please accept the declaration', 'error'); return; }
    this.isSubmitting = true;
    this.application.status = 'Pending';

    this.service.create(this.application).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showToast('Application submitted successfully!', 'success');
        this.application = this.emptyForm();
        this.currentStep = 1;
        this.loadApplications();
        setTimeout(() => this.activeTab = 'list', 2000);
      },
      error: () => {
        this.isSubmitting = false;
        this.showToast('Submission failed. Please try again.', 'error');
      }
    });
  }

  checkStatus(): void {
    if (!this.statusSearch.trim()) { this.showToast('Enter NID or phone', 'error'); return; }
    const txt = this.statusSearch.trim().toLowerCase();
    this.statusResult = this.applications.find(a =>
      a.nid?.toLowerCase() === txt || a.phone?.toLowerCase() === txt
    ) || null;
    this.statusSearched = true;
  }

  getStatusClass(status?: string): string {
    const s = (status || '').toLowerCase();
    if (s === 'approved') return 'badge-approved';
    if (s === 'rejected') return 'badge-rejected';
    return 'badge-pending';
  }

  isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  showToast(message: string, type: 'success'|'error'): void {
    const t: Toast = { type, message };
    this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => this.toasts = this.toasts.filter(x => x !== t), 300); }, 4000);
  }

  emptyForm(): WaterConnection {
    return {
      name: '', fatherName: '', nid: '', phone: '', email: '',
      district: '', upazila: '', ward: '', address: '',
      connectionType: 'Residential', members: undefined,
      usage: '', startDate: '', description: '',
      agree: false, status: 'Pending'
    };
  }
}
