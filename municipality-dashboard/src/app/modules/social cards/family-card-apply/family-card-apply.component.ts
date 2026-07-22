import { Component } from '@angular/core';
import { FamilyCardService } from '../../../services/family-card.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-family-card-apply',
  templateUrl: './family-card-apply.component.html',
  styleUrls: ['./family-card-apply.component.css']
})
export class FamilyCardApplyComponent {

  steps = ['Personal Info', 'Location & Household', 'Documents'];
  currentStep = 1;
  submitted   = false;
  loading     = false;
  declared    = false;
  toasts: any[] = [];

  form = {
    holderName: '', nid: '', dateOfBirth: '', contact: '',
    address: '', ward: '', unionName: '', upazila: '', district: '',
    membersCount: 1, incomeMonthly: '', occupation: '', husbandOrFatherName: '', hasOtherCard: false
  };

  photoFile: File | null    = null;
  photoPreview: string | null = null;
  nidFile: File | null      = null;
  nidFileName: string | null = null;

  districts = [
    'ঢাকা','চট্টগ্রাম','রাজশাহী','খুলনা','বরিশাল','সিলেট',
    'রংপুর','ময়মনসিংহ','কুমিল্লা','নারায়ণগঞ্জ','গাজীপুর',
    'টাঙ্গাইল','ফরিদপুর','জামালপুর','কিশোরগঞ্জ','নেত্রকোনা',
    'শেরপুর','নোয়াখালী','ফেনী','লক্ষ্মীপুর','চাঁদপুর'
  ];

  constructor(public ls: LanguageService, private svc: FamilyCardService) {}

  nextStep() {
    this.submitted = true;
    if (this.currentStep === 1 && (!this.form.holderName || !this.form.nid || !this.form.contact || !this.form.address)) return;
    this.submitted = false;
    if (this.currentStep < this.steps.length) this.currentStep++;
  }

  prevStep() {
    if (this.currentStep > 1) this.currentStep--;
  }

  onPhoto(e: any) {
    const file = e.target.files[0];
    if (!file) return;
    this.photoFile = file;
    const reader = new FileReader();
    reader.onload = (ev: any) => { this.photoPreview = ev.target.result; };
    reader.readAsDataURL(file);
  }

  removePhoto() { this.photoFile = null; this.photoPreview = null; }

  onNidFile(e: any) {
    const file = e.target.files[0];
    if (!file) return;
    this.nidFile    = file;
    this.nidFileName = file.name;
  }

  removeNid() { this.nidFile = null; this.nidFileName = null; }

  submitForm() {
    if (!this.declared) return;
    this.loading = true;

    const fd = new FormData();
    Object.entries(this.form).forEach(([k, v]) => fd.append(k, String(v)));
    if (this.photoFile) fd.append('photo',   this.photoFile);
    if (this.nidFile)   fd.append('nidFile', this.nidFile);

    this.svc.apply(fd).subscribe({
      next: (res) => {
        this.loading = false;
        this.toast('success', `আবেদন সফল! কার্ড নং: ${res.cardNo}`);
        setTimeout(() => { this.currentStep = 1; this.resetForm(); }, 2000);
      },
      error: (err) => {
        this.loading = false;
        this.toast('error', err.error?.message || 'আবেদন জমা দিতে সমস্যা হয়েছে।');
      }
    });
  }

  toast(type: 'success' | 'error', message: string) {
    const t: any = { type, message, removing: false };
    this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => { this.toasts = this.toasts.filter(x => x !== t); }, 300); }, 4000);
  }

  resetForm() {
    this.form = { holderName: '', nid: '', dateOfBirth: '', contact: '', address: '', ward: '', unionName: '', upazila: '', district: '', membersCount: 1, incomeMonthly: '', occupation: '', husbandOrFatherName: '', hasOtherCard: false };
    this.photoFile = null; this.photoPreview = null;
    this.nidFile   = null; this.nidFileName  = null;
    this.declared  = false;
  }
}
