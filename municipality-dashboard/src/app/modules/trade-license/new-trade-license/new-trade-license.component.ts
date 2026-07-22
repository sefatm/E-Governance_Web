import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TradeLicenseService } from 'src/app/services/trade-license.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success' | 'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-new-trade-license',
  templateUrl: './new-trade-license.component.html',
  styleUrls:  ['./new-trade-license.component.css']
})
export class NewTradeLicenseComponent implements OnInit {

  form!: FormGroup;
  submitted    = false;
  isSubmitting = false;
  toasts: Toast[] = [];
  currentStep  = 1;

  steps = ['Business Info', 'Owner Details', 'Documents & Payment'];
  step1Fields = ['businessName','businessType','licensePeriod','address','wardNo','holdingNo'];
  step2Fields = ['ownerName','fatherName','motherName','dateOfBirth','nid','mobile'];

  nidFile:        File | null = null;
  photoFile:      File | null = null;
  taxReceiptFile: File | null = null;
  photoPreview:   string | null = null;

  constructor(public ls: LanguageService, private fb: FormBuilder, private tradeService: TradeLicenseService) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      businessName:  ['TRS Limited ', [Validators.required, Validators.minLength(2)]],
      businessType:  ['', Validators.required],
      licensePeriod: ['', [Validators.required, Validators.min(1)]],
      address:       ['', Validators.required],
      wardNo:        ['', Validators.required],
      holdingNo:     ['', Validators.required],
      ownerName:     ['', [Validators.required, Validators.minLength(3)]],
      fatherName:    ['', Validators.required],
      motherName:    ['', Validators.required],
      dateOfBirth:   ['', Validators.required],
      nid:           ['', [Validators.required, Validators.pattern(/^\d{10}$|^\d{13}$|^\d{17}$/)]],
      mobile:        ['', [Validators.required, Validators.pattern(/^01[3-9]\d{8}$/)]],
      email:         ['', Validators.email],
      income:        ['', [Validators.required, Validators.min(0)]],
      tax:           ['', [Validators.required, Validators.min(0)]],
      declaration:   [false, Validators.requiredTrue],
    });
  }

  get f(): any { return this.form.controls; }

  nextStep(): void {
    const fields = this.currentStep === 1 ? this.step1Fields : this.step2Fields;
    fields.forEach(n => this.form.get(n)?.markAsTouched());
    this.submitted = true;
    if (fields.some(n => this.form.get(n)?.invalid)) {
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
    if (f.size > 2 * 1024 * 1024) { this.showToast('NID file must be under 2 MB', 'error'); return; }
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

  onTaxReceiptChange(event: Event): void {
    const f = this.getFile(event);
    if (!f) return;
    if (f.size > 5 * 1024 * 1024) { this.showToast('Tax receipt must be under 5 MB', 'error'); return; }
    this.taxReceiptFile = f;
  }

  removeFile(type: 'nid' | 'photo' | 'tax'): void {
    if (type === 'nid')   this.nidFile = null;
    if (type === 'photo') { this.photoFile = null; this.photoPreview = null; }
    if (type === 'tax')   this.taxReceiptFile = null;
  }

  private getFile(event: Event): File | null {
    return (event.target as HTMLInputElement).files?.[0] || null;
  }

  submitForm(): void {
    this.submitted = true;
    if (this.form.invalid) {
      this.showToast('Please fill all required fields', 'error');
      return;
    }

    const v  = this.form.value;
    const fd = new FormData();
    fd.append('businessName',  v.businessName);
    fd.append('businessType',  v.businessType);
    fd.append('licensePeriod', String(v.licensePeriod));
    fd.append('ownerName',     v.ownerName);
    fd.append('fatherName',    v.fatherName);
    fd.append('motherName',    v.motherName);
    fd.append('dateOfBirth',   v.dateOfBirth);
    fd.append('nid',           v.nid);
    fd.append('mobile',        v.mobile);
    if (v.email) fd.append('email', v.email);
    fd.append('address',       v.address);
    fd.append('wardNo',        v.wardNo);
    fd.append('holdingNo',     v.holdingNo);
    fd.append('income',        String(v.income));
    fd.append('tax',           String(v.tax));
    if (this.nidFile)        fd.append('nidFile',        this.nidFile,        this.nidFile.name);
    if (this.photoFile)      fd.append('photo',          this.photoFile,      this.photoFile.name);
    if (this.taxReceiptFile) fd.append('taxReceiptFile', this.taxReceiptFile, this.taxReceiptFile.name);

    this.isSubmitting = true;
    this.tradeService.submitTradeLicense(fd).subscribe({
      next: (res: any) => {
        this.isSubmitting = false;
        this.showToast(`✅ Application submitted! License No: ${res?.licenseNumber || ''}`, 'success');
        setTimeout(() => this.resetForm(), 3500);
      },
      error: (err: any) => {
        this.isSubmitting = false;
        this.showToast(err?.error?.message || 'Submission failed. Please try again.', 'error');
      }
    });
  }

  resetForm(): void {
    this.form.reset();
    this.form.patchValue({ declaration: false });
    this.submitted = false; this.currentStep = 1;
    this.nidFile = null; this.photoFile = null; this.taxReceiptFile = null;
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

  onMobileInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, '');
    if (v.length > 0 && !v.startsWith('01')) v = '01';
    this.form.patchValue({ mobile: v }, { emitEvent: false });
  }
}
