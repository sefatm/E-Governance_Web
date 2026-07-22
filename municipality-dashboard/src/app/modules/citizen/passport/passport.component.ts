import { environment } from 'src/environments/environment';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { PassportApplication } from 'src/app/models/passport.model';
import { PassportService } from 'src/app/services/passport.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success'|'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-passport',
  templateUrl: './passport.component.html',
  styleUrls: ['./passport.component.css']
})
export class PassportComponent implements OnInit {

  passportForm: FormGroup;
  applications: PassportApplication[] = [];
  isLoading    = false;
  isSubmitting = false;
  agreed       = false;
  activeTab: 'form'|'list'|'status' = 'form';
  currentStep  = 1;
  steps        = ['Personal Info', 'Passport & Address', 'Documents', 'Payment'];
  toasts: Toast[] = [];

  photoFile:    File|null = null;
  nidFile:      File|null = null;
  birthFile:    File|null = null;
  photoPreview: string|null = null;
  fileError     = '';

  statusSearch  = '';
  statusResult: PassportApplication|null = null;
  statusSearched = false;
  statusLoading  = false;

  readonly amountMap: Record<string,number> = {
    Ordinary: 3450, Official: 6900, Diplomatic: 6900
  };

  paymentMethods = [
    { val: 'Bank Draft', label: 'Bank Draft', icon: 'fa-building-columns' },
    { val: 'Bkash',     label: 'bKash',       icon: 'fa-mobile-screen'    },
    { val: 'Nagad',     label: 'Nagad',        icon: 'fa-mobile-screen'    },
    { val: 'Card',      label: 'Card',         icon: 'fa-credit-card'      },
  ];

  private readonly BASE = `${environment.apiUrl}`;

  constructor(public ls: LanguageService, 
    private fb: FormBuilder,
    private http: HttpClient,
    private passportService: PassportService
  ) {
    this.passportForm = this.fb.group({
      fullName:               ['', Validators.required],
      dob:                    ['', Validators.required],
      placeOfBirth:           ['', Validators.required],
      gender:                 ['', Validators.required],
      nationality:            ['Bangladesh', Validators.required],
      maritalStatus:          ['', Validators.required],
      religion:               ['', Validators.required],
      bloodGroup:             [''],
      fatherName:             ['', Validators.required],
      motherName:             ['', Validators.required],
      passportType:           ['', Validators.required],
      nidNumber:              ['', Validators.required],
      passportNoPrevious:     [''],
      currentAddress:         ['', Validators.required],
      permanentAddress:       [''],
      contact:                ['', Validators.required],
      email:                  ['', [Validators.required, Validators.email]],
      emergencyContactName:   ['', Validators.required],
      emergencyContactPhone:  ['', Validators.required],
      previousTravelCountries:[''],
      previousVisaNumbers:    [''],
      paymentMethod:          ['', Validators.required],
      amount:                 [{ value: 0, disabled: true }]
    });
  }

  ngOnInit(): void { this.loadApplications(); }

  loadApplications(): void {
    this.isLoading = true;
    this.passportService.getAllApplications().subscribe({
      next: r => { this.applications = r; this.isLoading = false; },
      error: () => this.isLoading = false
    });
  }

  switchTab(tab: 'form'|'list'|'status'): void {
    this.activeTab = tab;
    if (tab === 'list') this.loadApplications();
  }

  onPassportTypeChange(): void {
    const type = this.passportForm.get('passportType')?.value;
    this.passportForm.get('amount')?.setValue(this.amountMap[type] || 0);
  }

  nextStep(): void {
    if (this.currentStep === 1) {
      const f = this.passportForm;
      if (f.get('fullName')?.invalid || f.get('dob')?.invalid ||
          f.get('placeOfBirth')?.invalid || f.get('gender')?.invalid ||
          f.get('maritalStatus')?.invalid || f.get('religion')?.invalid ||
          f.get('fatherName')?.invalid || f.get('motherName')?.invalid) {
        this.passportForm.markAllAsTouched();
        this.showToast('Please fill all required fields in Step 1', 'error'); return;
      }
    }
    if (this.currentStep === 2) {
      const f = this.passportForm;
      if (f.get('passportType')?.invalid || f.get('nidNumber')?.invalid ||
          f.get('currentAddress')?.invalid || f.get('contact')?.invalid ||
          f.get('email')?.invalid || f.get('emergencyContactName')?.invalid ||
          f.get('emergencyContactPhone')?.invalid) {
        this.passportForm.markAllAsTouched();
        this.showToast('Please fill all required fields in Step 2', 'error'); return;
      }
    }
    if (this.currentStep < 4) {
      this.currentStep++;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  prevStep(): void {
    if (this.currentStep > 1) { this.currentStep--; window.scrollTo({ top: 0, behavior: 'smooth' }); }
  }

  onFile(event: Event, type: 'photo'|'nid'|'birth'): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.fileError = '';
    const maxMB = type === 'photo' ? 1 : 2;
    if (file.size > maxMB * 1024 * 1024) {
      this.fileError = `${type.toUpperCase()} must be under ${maxMB}MB`; return;
    }
    if (type === 'photo') {
      if (!file.type.startsWith('image/')) { this.fileError = 'Photo must be an image'; return; }
      this.photoFile = file;
      const r = new FileReader(); r.onload = e => this.photoPreview = e.target?.result as string;
      r.readAsDataURL(file);
    } else if (type === 'nid')   this.nidFile   = file;
    else if (type === 'birth')   this.birthFile  = file;
  }

  removeFile(type: 'photo'|'nid'|'birth'): void {
    if (type === 'photo')  { this.photoFile = null; this.photoPreview = null; }
    if (type === 'nid')    this.nidFile   = null;
    if (type === 'birth')  this.birthFile  = null;
    this.fileError = '';
  }

  onSubmit(): void {
    if (this.passportForm.invalid) {
      this.passportForm.markAllAsTouched();
      this.showToast('Please fill all required fields', 'error'); return;
    }
    if (!this.agreed) { this.showToast('Please accept the declaration', 'error'); return; }

    this.isSubmitting = true;
    const payload: PassportApplication = {
      ...this.passportForm.getRawValue(),
      status: 'PENDING',
      applicationDate: new Date().toISOString().split('T')[0]
    };

    this.passportService.createApplication(payload).subscribe({
      next: (res: any) => {
        const newId = res?.id;
        if (newId && (this.photoFile || this.nidFile || this.birthFile)) {
          this.uploadFiles(newId);
        } else {
          this.onSuccess();
        }
      },
      error: () => { this.showToast('Submission failed. Try again.', 'error'); this.isSubmitting = false; }
    });
  }

  private uploadFiles(id: number): void {
    const fd = new FormData();
    if (this.photoFile)  fd.append('photo',     this.photoFile,  this.photoFile.name);
    if (this.nidFile)    fd.append('nidFile',   this.nidFile,    this.nidFile.name);
    if (this.birthFile)  fd.append('birthFile', this.birthFile,  this.birthFile.name);

    this.http.post(`${this.BASE}/passport/upload/${id}`, fd).subscribe({
      next: () => this.onSuccess(),
      error: () => this.onSuccess()
    });
  }

  private onSuccess(): void {
    this.isSubmitting = false;
    this.showToast('Application submitted successfully!', 'success');
    this.passportForm.reset();
    this.passportForm.get('nationality')?.setValue('Bangladesh');
    this.passportForm.get('amount')?.setValue(0);
    this.photoFile = null; this.nidFile = null; this.birthFile = null;
    this.photoPreview = null; this.agreed = false; this.currentStep = 1;
    this.loadApplications();
    setTimeout(() => this.activeTab = 'list', 2000);
  }

  checkStatus(): void {
    if (!this.statusSearch.trim()) { this.showToast('Enter NID number', 'error'); return; }
    this.statusLoading = true; this.statusResult = null; this.statusSearched = false;
    this.passportService.getAllApplications().subscribe({
      next: res => {
        const txt = this.statusSearch.trim().toLowerCase();
        this.statusResult = res.find(a => a.nidNumber?.trim().toLowerCase() === txt) || null;
        this.statusSearched = true; this.statusLoading = false;
      },
      error: () => this.statusLoading = false
    });
  }

  isInvalid(name: string): boolean {
    const c = this.passportForm.get(name);
    return !!(c?.invalid && c?.touched);
  }

  getStatusClass(status?: string): string {
    const s = (status || '').toUpperCase();
    if (s === 'APPROVED') return 'badge-approved';
    if (s === 'REJECTED') return 'badge-rejected';
    return 'badge-pending';
  }

  showToast(message: string, type: 'success'|'error'): void {
    const t: Toast = { type, message };
    this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => this.toasts = this.toasts.filter(x => x !== t), 300); }, 4000);
  }
}
