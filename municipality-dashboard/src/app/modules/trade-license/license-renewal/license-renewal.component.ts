import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TradeRenewalService } from 'src/app/services/trade-renewal.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success' | 'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-license-renewal',
  templateUrl: './license-renewal.component.html',
  styleUrls:  ['./license-renewal.component.css']
})
export class LicenseRenewalComponent implements OnInit {

  renewForm!: FormGroup;
  submitted    = false;
  isSubmitting = false;
  toasts: Toast[] = [];
  currentStep  = 1;

  steps = ['License & Business', 'Applicant Details', 'Documents & Payment'];
  step1Fields = ['licenseNumber','licenseExpiry','issuingAuthority','businessName','businessType','address','wardNo','holdingNo'];
  step2Fields = ['applicantName','fatherName','motherName','dateOfBirth','nid','contact'];

  nidFile:        File | null = null;
  photoFile:      File | null = null;
  licenseFile:    File | null = null;
  photoPreview:   string | null = null;

  constructor(public ls: LanguageService, private fb: FormBuilder, private tradeService: TradeRenewalService) {}

  ngOnInit(): void {
    this.renewForm = this.fb.group({
      licenseNumber:    ['', Validators.required],
      licenseExpiry:    ['', Validators.required],
      issuingAuthority: ['', Validators.required],
      businessName:     ['', Validators.required],
      businessType:     ['', Validators.required],
      address:          ['', Validators.required],
      wardNo:           ['', Validators.required],
      holdingNo:        ['', Validators.required],

      applicantName:    ['', [Validators.required, Validators.minLength(3)]],
      fatherName:       ['', Validators.required],
      motherName:       ['', Validators.required],
      dateOfBirth:      ['', Validators.required],
      nid:              ['', [Validators.required, Validators.pattern(/^\d{10}$|^\d{13}$|^\d{17}$/)]],
      contact:          ['', [Validators.required, Validators.pattern(/^01[3-9]\d{8}$/)]],
      email:            ['', Validators.email],

      renewalPeriod:    ['', [Validators.required, Validators.min(1)]],
      annualIncome:     ['', [Validators.required, Validators.min(0)]],
      taxPaid:          ['', [Validators.required, Validators.min(0)]],
      purpose:          ['', Validators.required],
      declaration:      [false, Validators.requiredTrue],
    });
  }

  get f(): any { return this.renewForm.controls; }

  nextStep(): void {
    const fields = this.currentStep === 1 ? this.step1Fields : this.step2Fields;
    fields.forEach(n => this.renewForm.get(n)?.markAsTouched());
    this.submitted = true;
    if (fields.some(n => this.renewForm.get(n)?.invalid)) {
      this.showToast('Please fill all required fields correctly', 'error');
      return;
    }
    this.submitted = false;
    this.currentStep++;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  prevStep(): void {
    this.submitted = false;
    this.currentStep--;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  onNidFileChange(event: Event): void {
    const f = this.getFile(event);
    if (!f) return;
    if (f.size > 2 * 1024 * 1024) { this.showToast('NID file must be under 2 MB', 'error'); 
      return; }
    this.nidFile = f;
  }

  onPhotoChange(event: Event): void {
    const f = this.getFile(event);
    if (!f) return;
    if (!f.type.startsWith('image/')) { this.showToast('Photo must be an image file', 'error'); return; }
    if (f.size > 1 * 1024 * 1024) { this.showToast('Photo must be under 1 MB', 'error'); return; }
    this.photoFile = f;
    const reader = new FileReader();
    reader.onload = e => this.photoPreview = e.target?.result as string;
    reader.readAsDataURL(f);
  }

  onLicenseFileChange(event: Event): void {
    const f = this.getFile(event);
    if (!f) return;
    if (f.size > 5 * 1024 * 1024) { this.showToast('License copy must be under 5 MB', 'error'); 
      return; }
    this.licenseFile = f;
  }

  removeFile(type: 'nid' | 'photo' | 'license'): void {
    if (type === 'nid')     this.nidFile = null;
    if (type === 'photo')   { this.photoFile = null; this.photoPreview = null; }
    if (type === 'license') this.licenseFile = null;
  }

  private getFile(event: Event): File | null {
    return (event.target as HTMLInputElement).files?.[0] || null;
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.renewForm.invalid) {
      this.showToast('Please fill all required fields', 'error');
      return;
    }

    const v  = this.renewForm.value;
    const fd = new FormData();

    fd.append('licenseNumber',    v.licenseNumber);
    fd.append('licenseExpiry',    v.licenseExpiry);
    fd.append('issuingAuthority', v.issuingAuthority);
    fd.append('businessName',     v.businessName);
    fd.append('businessType',     v.businessType);
    fd.append('address',          v.address);
    fd.append('wardNo',           v.wardNo);
    fd.append('holdingNo',        v.holdingNo);

    fd.append('applicantName',    v.applicantName);
    fd.append('fatherName',       v.fatherName);
    fd.append('motherName',       v.motherName);
    fd.append('dateOfBirth',      v.dateOfBirth);
    fd.append('nid',              v.nid);
    fd.append('contact',          v.contact);
    if (v.email) fd.append('email', v.email);

    fd.append('renewalPeriod',    String(v.renewalPeriod));
    fd.append('annualIncome',     String(v.annualIncome));
    fd.append('taxPaid',          String(v.taxPaid));
    fd.append('purpose',          v.purpose);
    fd.append('declaration',      String(v.declaration));

    if (this.nidFile)     fd.append('nidFile',     this.nidFile,     this.nidFile.name);
    if (this.photoFile)   fd.append('photo',        this.photoFile,   this.photoFile.name);
    if (this.licenseFile) fd.append('licenseFile',  this.licenseFile, this.licenseFile.name);

    this.isSubmitting = true;
    this.tradeService.submitTradeRenewal(fd).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showToast('✅ Renewal application submitted successfully!', 'success');
        setTimeout(() => this.resetForm(), 3500);
      },
      error: (err: any) => {
        this.isSubmitting = false;
        this.showToast(err?.error?.message || 'Submission failed. Please try again.', 'error');
      }
    });
  }

  resetForm(): void {
    this.renewForm.reset();
    this.renewForm.patchValue({ declaration: false });
    this.submitted = false; this.currentStep = 1;
    this.nidFile = null; this.photoFile = null; this.licenseFile = null;
    this.photoPreview = null;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  showToast(message: string, type: 'success' | 'error'): void {
    const t: Toast = { type, message };
    this.toasts.push(t);
    setTimeout(() => {
      t.removing = true;
      setTimeout(() => { this.toasts = this.toasts.filter(x => x !== t); }, 300);
    }, 5000);
  }

  onContactInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, '');
    if (v.length > 0 && !v.startsWith('01')) v = '01';
    this.renewForm.patchValue({ contact: v }, { emitEvent: false });
  }
}
