import { Component, OnInit } from '@angular/core';
import { RoadService } from 'src/app/services/road.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success' | 'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-road',
  templateUrl: './road.component.html',
  styleUrls: ['./road.component.css']
})
export class RoadComponent implements OnInit {

  currentStep  = 1;
  submitted    = false;
  isSubmitting = false;
  toasts: Toast[] = [];

  steps       = ['Applicant Info', 'Road Details', 'Declaration'];
  step1Fields = ['name','contact','district','upazila','ward','area'];
  step2Fields = ['roadName','type','roadCondition','description'];

  roadForm: any = {
    name:'', nid:'', contact:'', district:'', upazila:'', ward:'',
    area:'', roadName:'', type:'', roadCondition:'', length:null,
    width:null, description:'', priority:'Low'
  };
  agree = false;

  constructor(public ls: LanguageService, private roadService: RoadService) {}
  ngOnInit(): void {}

  nextStep(): void {
    const fields = this.currentStep === 1 ? this.step1Fields : this.step2Fields;
    this.submitted = true;
    if (fields.some(f => !this.roadForm[f])) {
      this.showToast('Please fill all required fields', 'error'); return;
    }
    this.submitted = false;
    this.currentStep++;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  prevStep(): void { this.submitted = false; this.currentStep--; window.scrollTo({ top: 0, behavior: 'smooth' }); }

  submitForm(): void {
    if (!this.agree) { this.showToast('Please accept the declaration', 'error'); return; }
    this.isSubmitting = true;
    const payload = { ...this.roadForm };
    this.roadService.createRoadRequest(payload).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showToast('Road request submitted successfully!', 'success');
        setTimeout(() => this.resetForm(), 3000);
      },
      error: (err: any) => {
        this.isSubmitting = false;
        this.showToast(err?.error?.message || 'Submission failed. Please try again.', 'error');
      }
    });
  }

  resetForm(): void {
    this.roadForm = { name:'', nid:'', contact:'', district:'', upazila:'', ward:'',
      area:'', roadName:'', type:'', roadCondition:'', length:null, width:null, description:'', priority:'Low' };
    this.agree = false; this.submitted = false; this.currentStep = 1;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  showToast(message: string, type: 'success' | 'error'): void {
    const t: Toast = { type, message };
    this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => { this.toasts = this.toasts.filter(x => x !== t); }, 300); }, 5000);
  }

  setPriority(p: string): void { this.roadForm.priority = p; }

  onContactInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, '');
    if (v.length > 0 && !v.startsWith('01')) v = '01';
    this.roadForm.contact = v;
  }
}
