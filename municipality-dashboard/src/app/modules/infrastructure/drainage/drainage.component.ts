import { Component, OnInit } from '@angular/core';
import { DrainageRequest } from 'src/app/models/drainage.model';
import { DrainageService } from 'src/app/services/drainage.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success' | 'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-drainage',
  templateUrl: './drainage.component.html',
  styleUrls: ['./drainage.component.css']
})
export class DrainageComponent implements OnInit {

  currentStep = 1; submitted = false; isSubmitting = false;
  toasts: Toast[] = [];
  steps = ['Applicant Info', 'Drain Details', 'Declaration'];
  step1Fields = ['name','contact','district','upazila','ward','area'];
  step2Fields = ['type','problem','description'];

  drainageForm: DrainageRequest = {
    name:'', nid:'', contact:'', district:'', upazila:'', ward:'',
    area:'', type:'', problem:'', length:0, width:0, description:'', priority:'Low'
  };
  agree = false;

  constructor(public ls: LanguageService, private drainageService: DrainageService) {}
  ngOnInit(): void {}

  nextStep(): void {
    const fields = this.currentStep === 1 ? this.step1Fields : this.step2Fields;
    this.submitted = true;
    if (fields.some((f: string) => !(this.drainageForm as any)[f])) {
      this.showToast('Please fill all required fields', 'error'); return;
    }
    this.submitted = false; this.currentStep++;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  prevStep(): void { this.submitted = false; this.currentStep--; window.scrollTo({ top: 0, behavior: 'smooth' }); }

  submitForm(): void {
    if (!this.agree) { this.showToast('Please accept the declaration', 'error'); return; }
    this.isSubmitting = true;
    this.drainageService.createRequest(this.drainageForm).subscribe({
      next: () => { this.isSubmitting = false; this.showToast('Drainage request submitted successfully!', 'success'); setTimeout(() => this.resetForm(), 3000); },
      error: (err: any) => { this.isSubmitting = false; this.showToast(err?.error?.message || 'Submission failed.', 'error'); }
    });
  }

  resetForm(): void {
    this.drainageForm = { name:'', nid:'', contact:'', district:'', upazila:'', ward:'', area:'', type:'', problem:'', length:0, width:0, description:'', priority:'Low' };
    this.agree = false; this.submitted = false; this.currentStep = 1;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  showToast(message: string, type: 'success' | 'error'): void {
    const t: Toast = { type, message }; this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => { this.toasts = this.toasts.filter(x => x !== t); }, 300); }, 5000);
  }

  setPriority(p: string): void { this.drainageForm.priority = p; }

  onContactInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, '');
    if (v.length > 0 && !v.startsWith('01')) v = '01';
    this.drainageForm.contact = v;
  }
}
