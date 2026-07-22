import { environment } from 'src/environments/environment';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { 
  message: string; 
  type: 'success'|'error'; 
  removing?: boolean; }

@Component({
  selector: 'app-family',
  templateUrl: './family.component.html',
  styleUrls: ['./family.component.css']
})
export class FamilyComponent implements OnInit {

  form!     : FormGroup;
  submitted  = false;
  isSubmitting = false;
  currentStep  = 1;

  steps = ['Family Head', 'Address & Purpose', 'Members & Documents'];

  activeTab: 'form' | 'list' = 'form';
  applications: any[] = [];
  isLoading = false;

  memberFiles: { doc: File|null; preview: string|null }[] = [];

  // Head photo & NID
  headPhotoFile  : File|null = null;
  headPhotoPreview: string|null = null;
  headNidFile    : File|null = null;

  toasts: Toast[] = [];

  private baseUrl = `${environment.apiUrl}/family`;

  relationOptions = [
    'Son', 'Daughter', 'Wife', 'Husband',
    'Father', 'Mother', 'Brother', 'Sister', 'Other'
  ];

  constructor(public ls: LanguageService, private fb: FormBuilder, private http: HttpClient) {}

  ngOnInit(): void {
    this.buildForm();
    this.loadApplications();
  }

  buildForm(): void {
    this.form = this.fb.group({
      // Step 1 — Head
      headName    : ['', [Validators.required, Validators.minLength(3)]],
      headNid     : ['', [Validators.required, Validators.pattern(/^\d{10}$|^\d{13}$|^\d{17}$/)]],
      headDob     : ['', Validators.required],
      headGender  : ['', Validators.required],
      headContact : ['', [Validators.required, Validators.pattern(/^01[3-9]\d{8}$/)]],
      headEmail   : ['', Validators.email],

      // Step 2 — Address & Purpose
      address         : ['', Validators.required],
      permanentAddress: [''],
      division        : ['', Validators.required],
      district        : ['', Validators.required],
      purpose         : ['', [Validators.required, Validators.minLength(3)]],

      // Step 3 — Members (FormArray)
      members: this.fb.array([])
    });

    // Add one initial member row
    this.addMember();
  }

  // FormArray helpers 
  get membersArray(): FormArray {
    return this.form.get('members') as FormArray;
  }

  addMember(): void {
    const memberGroup = this.fb.group({
      name       : ['', Validators.required],
      age        : ['', [Validators.required, Validators.min(0), Validators.max(120)]],
      gender     : ['', Validators.required],
      relation   : ['', Validators.required],
      nidOrBirth : [''],
      docType    : ['nid']
    });
    this.membersArray.push(memberGroup);
    this.memberFiles.push({ doc: null, preview: null });
  }

  removeMember(i: number): void {
    if (this.membersArray.length === 1) return; // কমপক্ষে ১ রাখতে হবে
    this.membersArray.removeAt(i);
    this.memberFiles.splice(i, 1);
  }

  // Step navigation
  nextStep(): void {
    this.submitted = false;
    if (this.currentStep === 1) {
      const step1Fields = ['headName','headNid','headDob','headGender','headContact'];
      const hasError = step1Fields.some(k => this.form.get(k)?.invalid);
      if (hasError) { this.submitted = true; return; }
    }
    if (this.currentStep === 2) {
      const step2Fields = ['address','division','district','purpose'];
      const hasError = step2Fields.some(k => this.form.get(k)?.invalid);
      if (hasError) { this.submitted = true; return; }
    }
    this.currentStep++;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  prevStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  // File uploads
  onHeadPhotoChange(e: Event): void {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.headPhotoFile = file;
    const reader = new FileReader();
    reader.onload = () => this.headPhotoPreview = reader.result as string;
    reader.readAsDataURL(file);
  }

  onHeadNidChange(e: Event): void {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.headNidFile = file;
  }

  onMemberDocChange(e: Event, idx: number): void {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.memberFiles[idx].doc = file;
    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = () => this.memberFiles[idx].preview = reader.result as string;
      reader.readAsDataURL(file);
    } else {
      this.memberFiles[idx].preview = null;
    }
  }

  // Submit 
  submitForm(): void {
    this.submitted = true;
    if (this.form.invalid) {
      this.showToast('Please fill all required fields.', 'error');
      return;
    }

    this.isSubmitting = true;
    const fv = this.form.value;

    // Build FormData for multipart upload
    const fd = new FormData();

    // Head fields
    fd.append('headName',     fv.headName);
    fd.append('nid',          fv.headNid);
    fd.append('contact',      fv.headContact);
    fd.append('address',      fv.address);
    fd.append('purpose',      fv.purpose);
    fd.append('memberCount',      String(fv.members.length + 1)); 
    if (fv.permanentAddress) fd.append('permanentAddress', fv.permanentAddress);
    fd.append('division',         fv.division);
    fd.append('district',         fv.district);

    // Head photo & NID
    if (this.headPhotoFile) fd.append('headPhoto',   this.headPhotoFile);
    if (this.headNidFile)   fd.append('headNidFile', this.headNidFile);

    // Members JSON
    fd.append('membersJson', JSON.stringify(fv.members.map((m: any, i: number) => ({
      name    : m.name,
      age     : m.age,
      gender  : m.gender,
      relation: m.relation,
      nidOrBirth: m.nidOrBirth,
      docType : m.docType,
    }))));

    // Per-member document files
    fv.members.forEach((_: any, i: number) => {
      if (this.memberFiles[i]?.doc) {
        fd.append(`memberDoc_${i}`, this.memberFiles[i].doc!);
      }
    });

    this.http.post(`${this.baseUrl}/create-multipart`, fd).subscribe({
      next: (res: any) => {
        this.isSubmitting = false;
        this.showToast(
          'Application submitted! Tracking No: ' + (res.trackingNo || ''),
          'success'
        );
        this.form.reset();
        this.membersArray.clear();
        this.memberFiles = [];
        this.headPhotoFile = null;
        this.headPhotoPreview = null;
        this.headNidFile = null;
        this.submitted = false;
        this.currentStep = 1;
        this.buildForm();
        this.loadApplications();
        setTimeout(() => this.activeTab = 'list', 1200);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.showToast(err.error?.message || 'Submission failed. Try again.', 'error');
      }
    });
  }

  // Load list 
  loadApplications(): void {
    this.isLoading = true;
    this.http.get<any[]>(`${this.baseUrl}/getall`).subscribe({
      next: (res) => { this.applications = res; this.isLoading = false; },
      error: ()  => { this.isLoading = false; }
    });
  }

  switchTab(tab: 'form' | 'list'): void {
    this.activeTab = tab;
    if (tab === 'list') this.loadApplications();
  }

  download(id: number): void {
    window.open(`${this.baseUrl}/generate-pdf/${id}`, '_blank');
  }

  isApproved(status?: string): boolean {
    return (status || '').replace(/['"]/g,'').trim().toLowerCase() === 'approved';
  }

  getStatusClass(status?: string): string {
    const s = (status || '').replace(/['"]/g,'').trim().toLowerCase();
    if (s === 'approved') return 'badge-approved';
    if (s === 'rejected') return 'badge-rejected';
    return 'badge-pending';
  }

  getStatusText(status?: string): string {
    return (status || 'Pending').replace(/['"]/g,'').trim();
  }

  // Toast 
  showToast(message: string, type: 'success'|'error'): void {
    const t: Toast = { message, type };
    this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => this.toasts = this.toasts.filter(x => x !== t), 400); }, 3500);
  }

  // Shortcut 
  get f() { return this.form.controls; }
  memberCtrl(i: number, key: string) { return this.membersArray.at(i).get(key); }
}
