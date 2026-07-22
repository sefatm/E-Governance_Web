import { environment } from 'src/environments/environment';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { 
  type: 'success' | 'error'; 
  message: string; 
  removing?: boolean; }

@Component({
  selector: 'app-citizen',
  templateUrl: './citizen.component.html',
  styleUrls: ['./citizen.component.css']
})
export class CitizenComponent implements OnInit {

  form!: FormGroup;
  submitted    = false;
  isSubmitting = false;
  toasts: Toast[] = [];

  photoFile: File | null = null;
  nidFile:   File | null = null;
  photoPreview: string | null = null;

  currentStep = 1;
  steps = ['Personal Info', 'Contact & Address', 'Certificate & Docs'];
  step1Fields = ['name','fatherName','motherName','nid','dateOfBirth','gender'];
  step2Fields = ['contact','address','division','district'];

  private base = `${environment.apiUrl}/citizen`;

  constructor(public ls: LanguageService, private fb: FormBuilder, private http: HttpClient) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name:             ['', [Validators.required, Validators.minLength(3)]],
      fatherName:       ['', Validators.required],
      motherName:       ['', Validators.required],
      nid:              ['', [Validators.required, Validators.pattern(/^(?:\d{10}|\d{13}|\d{17})$/)]],
      dateOfBirth:      ['', Validators.required],
      gender:           ['', Validators.required],
      bloodGroup:       [''],
      religion:         [''],
      maritalStatus:    [''],
      occupation:       [''],
      contact:          ['', [Validators.required, Validators.pattern(/^01[3-9]\d{8}$/)]],
      email:            ['', Validators.email],
      address:          ['', Validators.required],
      permanentAddress: [''],
      division:         ['', Validators.required],
      district:         ['', Validators.required],
      purpose:          ['', [Validators.required, Validators.minLength(3)]],
      certificateType:  ['', Validators.required],
      declaration:      [false, Validators.requiredTrue]
    });
  }

  get f(): any { return this.form.controls; }

  // Step Navigation 
  nextStep(): void {
    const fields = this.currentStep === 1 ? this.step1Fields : this.step2Fields;
    fields.forEach(n => this.form.get(n)?.markAsTouched());
    this.submitted = true;
    if (fields.some(n => this.form.get(n)?.invalid)) {
      this.showToast('Please fill all required fields correctly', 'error');
      this.focusFirstErrorIn(fields);
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

  // Submit — multipart/form-data 
  submitForm(): void {
    this.submitted = true;
    if (this.form.invalid) {
      this.showToast('Please fill all required fields correctly', 'error');
      return;
    }

    this.isSubmitting = true;

    // Build FormData — files + JSON fields
    const fd = new FormData();
    const v  = this.form.value;

    // Append all text fields
    Object.keys(v).forEach(key => {
      if (v[key] !== null && v[key] !== undefined) {
        fd.append(key, String(v[key]));
      }
    });

    // Append files if selected
    if (this.photoFile) fd.append('photo', this.photoFile, this.photoFile.name);
    if (this.nidFile)   fd.append('nidFile', this.nidFile, this.nidFile.name);

    this.http.post(`${this.base}/create`, fd).subscribe({
      next: (res: any) => {
        this.isSubmitting = false;
        const t = res?.trackingNo || '';
        this.showToast('✅ Application submitted! Tracking No: ' + t, 'success');
        setTimeout(() => this.resetForm(), 2800);
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
    this.submitted    = false;
    this.photoFile    = null;
    this.nidFile      = null;
    this.photoPreview = null;
    this.currentStep  = 1;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // File handlers 
  onFileChange(event: any, type: 'photo' | 'nid'): void {
    const file: File = event.target.files[0];
    if (!file) return;

    if (type === 'photo') {
      if (file.size > 300 * 1024) { this.showToast('Photo must be under 300 KB', 'error'); event.target.value = ''; return; }
      this.photoFile = file;
      // Preview
      const reader = new FileReader();
      reader.onload = e => this.photoPreview = e.target?.result as string;
      reader.readAsDataURL(file);
    }

    if (type === 'nid') {
      if (file.size > 1024 * 1024) { this.showToast('NID copy must be under 1 MB', 'error'); event.target.value = ''; return; }
      this.nidFile = file;
    }
  }

  onMobileInput(event: any): void {
    let v = event.target.value.replace(/\D/g, '');
    if (v.length > 0 && !v.startsWith('01')) v = '01';
    this.form.patchValue({ contact: v }, { emitEvent: false });
  }

  onNidInput(event: any): void {
    this.form.patchValue({ nid: event.target.value.replace(/\D/g, '') }, { emitEvent: false });
  }

  // Toast 
  showToast(message: string, type: 'success' | 'error'): void {
    const t: Toast = { type, message };
    this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => { this.toasts = this.toasts.filter(x => x !== t); }, 300); }, 4500);
  }

  private focusFirstErrorIn(fields: string[]): void {
    for (const k of fields) {
      if (this.form.get(k)?.invalid) {
        const el = document.querySelector(`[formControlName="${k}"]`) as HTMLElement;
        if (el) { el.scrollIntoView({ behavior: 'smooth', block: 'center' }); el.focus(); }
        break;
      }
    }
  }
}
