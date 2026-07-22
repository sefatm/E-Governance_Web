import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ComplaintService } from 'src/app/services/complaint.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success' | 'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-submit-complaint',
  templateUrl: './submit-complaint.component.html',
  styleUrls:  ['./submit-complaint.component.css']
})
export class SubmitComplaintComponent implements OnInit {

  form!: FormGroup;
  submitted    = false;
  isSubmitting = false;
  toasts: Toast[] = [];
  gpsLoading = false;
  latitude: number | null = null;
  longitude: number | null = null;

  selectedFile:  File | null   = null;
  imagePreview:  string | null = null;

  categories = [
    'Road & Infrastructure',
    'Water Supply',
    'Electricity',
    'Garbage & Sanitation',
    'Drainage',
    'Street Light',
    'Noise Pollution',
    'Other'
  ];

  constructor(public ls: LanguageService, private fb: FormBuilder, private complaintService: ComplaintService) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name:        ['', [Validators.required, Validators.minLength(3)]],
      ward:        ['', Validators.required],
      area:        ['', Validators.required],
      location:    ['', Validators.required],
      category:    ['', Validators.required],
      description: ['', [Validators.required, Validators.minLength(20)]],
      contact:     ['', [Validators.required, Validators.pattern(/^01[3-9]\d{8}$/)]],
    });
  }

  get f(): any { return this.form.controls; }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file  = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.showToast('Only image files allowed', 'error');
      input.value = '';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.showToast('Image must be under 5 MB', 'error');
      input.value = '';
      return;
    }
    this.selectedFile = file;
    const reader = new FileReader();
    reader.onload = e => this.imagePreview = e.target?.result as string;
    reader.readAsDataURL(file);
  }

  removeImage(): void {
    this.selectedFile = null;
    this.imagePreview = null;
    this.latitude = null;
    this.longitude = null;
  }

  onContactInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, '');
    if (v.length > 0 && !v.startsWith('01')) v = '01';
    this.form.patchValue({ contact: v }, { emitEvent: false });
  }

  useCurrentLocation(): void {
    if (!navigator.geolocation) {
      this.showToast('Geolocation is not supported by this browser', 'error');
      return;
    }
    this.gpsLoading = true;
    navigator.geolocation.getCurrentPosition(
      pos => {
        this.gpsLoading = false;
        this.latitude = pos.coords.latitude;
        this.longitude = pos.coords.longitude;
        this.showToast('GPS location captured successfully', 'success');
      },
      err => {
        this.gpsLoading = false;
        this.showToast(err.message || 'Unable to capture GPS location', 'error');
      },
      { enableHighAccuracy: true, timeout: 12000, maximumAge: 30000 }
    );
  }

  clearGps(): void {
    this.latitude = null;
    this.longitude = null;
  }

  submitComplaint(): void {
    this.submitted = true;
    if (this.form.invalid) {
      this.showToast('Please fill all required fields correctly', 'error');
      return;
    }

    const v  = this.form.value;
    const fd = new FormData();
    fd.append('name',        v.name);
    fd.append('ward',        v.ward);
    fd.append('area',        v.area);
    fd.append('location',    v.location);
    fd.append('category',    v.category);
    fd.append('description', v.description);
    fd.append('contact',     v.contact);
    if (this.latitude !== null && this.longitude !== null) {
      fd.append('lat', String(this.latitude));
      fd.append('lng', String(this.longitude));
    }

    if (this.selectedFile) {
      fd.append('photo', this.selectedFile, this.selectedFile.name);
    }
    this.isSubmitting = true;
    this.complaintService.submitComplaint(fd).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showToast('✅ Complaint submitted successfully!', 'success');
        setTimeout(() => this.resetForm(), 3000);
      },
      error: (err: any) => {
        this.isSubmitting = false;
        this.showToast(err?.error?.message || 'Submission failed. Please try again.', 'error');
      }
    });
  }

  resetForm(): void {
    this.form.reset();
    this.submitted    = false;
    this.selectedFile = null;
    this.imagePreview = null;
    this.latitude = null;
    this.longitude = null;
  }

  showToast(message: string, type: 'success' | 'error'): void {
    const t: Toast = { type, message };
    this.toasts.push(t);
    setTimeout(() => {
      t.removing = true;
      setTimeout(() => { this.toasts = this.toasts.filter(x => x !== t); }, 300);
    }, 5000);
  }
}
