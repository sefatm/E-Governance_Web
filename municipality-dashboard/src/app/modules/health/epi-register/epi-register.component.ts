import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EpiService } from 'src/app/services/epi.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-epi-register',
  templateUrl: './epi-register.component.html',
  styleUrls: ['./epi-register.component.css']
})
export class EpiRegisterComponent {

  form: FormGroup;
  submitting = false;
  success    = '';
  error      = '';
  childPhoto: File | null = null;
  fatherNidFile: File | null = null;
  motherNidFile: File | null = null;

  today = new Date().toISOString().split('T')[0];

  wards = ['Ward 1','Ward 2','Ward 3','Ward 4','Ward 5',
           'Ward 6','Ward 7','Ward 8','Ward 9','Ward 10',
           'Ward 11','Ward 12'];

  constructor(public ls: LanguageService, private fb: FormBuilder, private epi: EpiService) {
    this.form = this.fb.group({
      childName:    ['', Validators.required],
      dateOfBirth:  ['', Validators.required],
      gender:       ['', Validators.required],
      fatherName:   ['', Validators.required],
      motherName:   ['', Validators.required],
      guardianNid:  [''],
      fatherNid:    [''],
      motherNid:    [''],
      guardianPhone:['', [Validators.required, Validators.pattern(/^01[3-9]\d{8}$/)]],
      guardianEmail:['', [Validators.email]],
      ward:         ['', Validators.required],
      unionName:    [''],
      upazila:      [''],
      district:     [''],
      address:      [''],
      presentAddress: [''],
      permanentAddress: [''],
      birthPlace: [''],
    });
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting = true;
    this.success = ''; this.error = '';

    this.epi.registerChild(this.buildPayload()).subscribe({
      next: (res) => {
        this.success = `Child registration successful! Card No.: ${res.cardNo}`;
        this.form.reset();
        this.childPhoto = null;
        this.fatherNidFile = null;
        this.motherNidFile = null;
        this.submitting = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Registration failed.';
        this.submitting = false;
      }
    });
  }

  onFileSelected(event: Event, field: 'childPhoto' | 'fatherNidFile' | 'motherNidFile'): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] || null;
    this[field] = file;
  }

  fileName(file: File | null): string {
    return file ? file.name : 'No file selected';
  }

  f(name: string) { return this.form.get(name)!; }

  private buildPayload(): FormData | any {
    if (!this.childPhoto && !this.fatherNidFile && !this.motherNidFile) {
      return this.form.value;
    }

    const fd = new FormData();
    Object.entries(this.form.value).forEach(([key, value]) => {
      if (value !== null && value !== undefined) fd.append(key, String(value));
    });
    if (this.childPhoto) fd.append('childPhoto', this.childPhoto, this.childPhoto.name);
    if (this.fatherNidFile) fd.append('fatherNidFile', this.fatherNidFile, this.fatherNidFile.name);
    if (this.motherNidFile) fd.append('motherNidFile', this.motherNidFile, this.motherNidFile.name);
    return fd;
  }
}
