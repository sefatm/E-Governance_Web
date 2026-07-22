import { Component, OnInit } from '@angular/core';
import { LightRequest } from 'src/app/models/street-light.model';
import { LightService } from 'src/app/services/street-light.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success' | 'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-street-light',
  templateUrl: './street-light.component.html',
  styleUrls: ['./street-light.component.css']
})
export class StreetLightComponent implements OnInit {

  currentStep = 1; submitted = false; isSubmitting = false;
  toasts: Toast[] = [];
  steps = ['Applicant Info', 'Light Details', 'Declaration'];
  step1Fields = ['name','contact','district','upazila','ward','location'];
  step2Fields = ['problemType','description'];

  lightForm: LightRequest = {
    name:'', nid:'', contact:'', district:'', upazila:'', ward:'',
    location:'', problemType:'', count:0, lightType:'LED', description:'', priority:'Low'
  };
  agree = false;

  constructor(public ls: LanguageService, private lightService: LightService) {}
  ngOnInit(): void {}

  nextStep(): void {
    const fields = this.currentStep === 1 ? this.step1Fields : this.step2Fields;
    this.submitted = true;
    if (fields.some((f: string) => !(this.lightForm as any)[f])) {
      this.showToast('Please fill all required fields', 'error'); return;
    }
    this.submitted = false; this.currentStep++;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  prevStep(): void { this.submitted = false; this.currentStep--; window.scrollTo({ top: 0, behavior: 'smooth' }); }

  submitForm(): void {
    if (!this.agree) { this.showToast('Please accept the declaration', 'error'); return; }
    this.isSubmitting = true;
    this.lightService.create(this.lightForm).subscribe({
      next: () => { this.isSubmitting = false; this.showToast('Street light request submitted successfully!', 'success'); setTimeout(() => this.resetForm(), 3000); },
      error: (err: any) => { this.isSubmitting = false; this.showToast(err?.error?.message || 'Submission failed.', 'error'); }
    });
  }

  resetForm(): void {
    this.lightForm = { name:'', nid:'', contact:'', district:'', upazila:'', ward:'', location:'', problemType:'', count:0, lightType:'LED', description:'', priority:'Low' };
    this.agree = false; this.submitted = false; this.currentStep = 1;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  showToast(message: string, type: 'success' | 'error'): void {
    const t: Toast = { type, message }; this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => { this.toasts = this.toasts.filter(x => x !== t); }, 300); }, 5000);
  }

  setPriority(p: string): void { this.lightForm.priority = p; }

  onContactInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, '');
    if (v.length > 0 && !v.startsWith('01')) v = '01';
    this.lightForm.contact = v;
  }
}
