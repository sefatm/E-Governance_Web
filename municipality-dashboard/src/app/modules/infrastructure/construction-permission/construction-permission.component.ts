import { Component, OnInit } from '@angular/core';
import { ConstructionService } from 'src/app/services/construction.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success' | 'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-construction-permission',
  templateUrl: './construction-permission.component.html',
  styleUrls: ['./construction-permission.component.css']
})
export class ConstructionPermissionComponent implements OnInit {

  currentStep = 1; submitted = false; isSubmitting = false;
  toasts: Toast[] = [];
  steps = ['Personal Info', 'Building Details', 'Declaration'];
  step1Fields = ['applicantName','contact','district','upazila','ward','plotNo','location'];
  step2Fields = ['buildingType','floors','area','startDate'];

  form: any = {
    applicantName:'', guardianName:'', nid:'', contact:'', email:'',
    district:'', upazila:'', ward:'', plotNo:'', location:'',
    buildingType:'', floors:null, area:null, landSize:'',
    startDate:'', engineerName:'', licenseNo:'', description:''
  };
  agree = false;

  constructor(public ls: LanguageService, private constructionService: ConstructionService) {}
  ngOnInit(): void {}

  nextStep(): void {
    const fields = this.currentStep === 1 ? this.step1Fields : this.step2Fields;
    this.submitted = true;
    if (fields.some((f: string) => !this.form[f])) {
      this.showToast('Please fill all required fields', 'error'); return;
    }
    this.submitted = false; this.currentStep++;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  prevStep(): void { this.submitted = false; this.currentStep--; window.scrollTo({ top: 0, behavior: 'smooth' }); }

  submitForm(): void {
    if (!this.agree) { this.showToast('Please accept the declaration', 'error'); return; }
    this.isSubmitting = true;
    const { ...payload } = this.form;
    this.constructionService.create(payload).subscribe({
      next: () => { this.isSubmitting = false; this.showToast('Construction permission submitted successfully!', 'success'); setTimeout(() => this.resetForm(), 3000); },
      error: (err: any) => { this.isSubmitting = false; this.showToast(err?.error?.message || 'Submission failed.', 'error'); }
    });
  }

  resetForm(): void {
    this.form = { applicantName:'', guardianName:'', nid:'', contact:'', email:'', district:'', upazila:'', ward:'', plotNo:'', location:'', buildingType:'', floors:null, area:null, landSize:'', startDate:'', engineerName:'', licenseNo:'', description:'' };
    this.agree = false; this.submitted = false; this.currentStep = 1;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  showToast(message: string, type: 'success' | 'error'): void {
    const t: Toast = { type, message }; this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => { this.toasts = this.toasts.filter(x => x !== t); }, 300); }, 5000);
  }

  onContactInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, '');
    if (v.length > 0 && !v.startsWith('01')) v = '01';
    this.form.contact = v;
  }
}
