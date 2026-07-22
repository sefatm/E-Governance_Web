import { Component } from '@angular/core';
import { LpgCardService } from 'src/app/services/lpg-card.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-lpg-card-apply',
  templateUrl: './lpg-card-apply.component.html',
  styleUrls: ['../family-card-apply/family-card-apply.component.css']
})
export class LpgCardApplyComponent {

  steps = ['Personal Info', 'Location & Dealer', 'Documents'];
  currentStep = 1; submitted = false; loading = false; declared = false;
  toasts: any[] = [];

  form = {
    holderName: '', nid: '', dateOfBirth: '', contact: '',
    address: '', ward: '', upazila: '', district: '',
    membersCount: 1, stoveCount: 1, hasGasLine: false,
    dealerName: '', dealerContact: '', cylinderSize: '12kg'
  };

  photoFile: File | null = null; photoPreview: string | null = null;
  nidFile: File | null   = null; nidFileName: string | null  = null;

  districts = ['ঢাকা', 'গাজীপুর', 'নারায়ণগঞ্জ', 'নরসিংদী', 'মানিকগঞ্জ', 'মুন্সীগঞ্জ', 'টাঙ্গাইল', 'ফরিদপুর', 'গোপালগঞ্জ', 'মাদারীপুর', 'রাজবাড়ী', 'শরীয়তপুর', 'চট্টগ্রাম', 'কক্সবাজার', 'কুমিল্লা', 'ব্রাহ্মণবাড়িয়া', 'চাঁদপুর', 'ফেনী', 'লক্ষ্মীপুর', 'নোয়াখালী', 'খাগড়াছড়ি', 'রাঙ্গামাটি', 'বান্দরবান', 'রাজশাহী', 'নাটোর', 'নওগাঁ', 'চাঁপাইনবাবগঞ্জ', 'পাবনা', 'সিরাজগঞ্জ', 'বগুড়া', 'জয়পুরহাট', 'খুলনা', 'বাগেরহাট', 'সাতক্ষীরা', 'যশোর', 'নড়াইল', 'মাগুরা', 'ঝিনাইদহ', 'কুষ্টিয়া', 'চুয়াডাঙ্গা', 'মেহেরপুর', 'বরিশাল', 'ভোলা', 'পটুয়াখালী', 'পিরোজপুর', 'ঝালকাঠি', 'বরগুনা', 'সিলেট', 'মৌলভীবাজার', 'হবিগঞ্জ', 'সুনামগঞ্জ', 'রংপুর', 'দিনাজপুর', 'ঠাকুরগাঁও', 'পঞ্চগড়', 'নীলফামারী', 'লালমনিরহাট', 'কুড়িগ্রাম', 'গাইবান্ধা', 'ময়মনসিংহ', 'জামালপুর', 'শেরপুর', 'নেত্রকোনা', 'কিশোরগঞ্জ'];

  constructor(public ls: LanguageService, private svc: LpgCardService) {}

  nextStep() {
    this.submitted = true;
    if (this.currentStep === 1 && (!this.form.holderName || !this.form.nid || !this.form.contact || !this.form.address)) return;
    this.submitted = false;
    if (this.currentStep < this.steps.length) this.currentStep++;
  }
  prevStep() { if (this.currentStep > 1) this.currentStep--; }

  onPhoto(e: any) { const f = e.target.files[0]; if (!f) return; this.photoFile = f; const r = new FileReader(); r.onload = (ev: any) => { this.photoPreview = ev.target.result; }; r.readAsDataURL(f); }
  removePhoto() { this.photoFile = null; this.photoPreview = null; }
  onNidFile(e: any) { const f = e.target.files[0]; if (!f) return; this.nidFile = f; this.nidFileName = f.name; }

  submitForm() {
    if (!this.declared) return;
    this.loading = true;
    const fd = new FormData();
    Object.entries(this.form).forEach(([k, v]) => fd.append(k, String(v)));
    if (this.photoFile) fd.append('photo', this.photoFile);
    if (this.nidFile)   fd.append('nidFile', this.nidFile);
    this.svc.apply(fd).subscribe({
      next: (res) => { this.loading = false; this.toast('success', `আবেদন সফল! কার্ড নং: ${res.cardNo}`); },
      error: (err) => { this.loading = false; this.toast('error', err.error?.message || 'সমস্যা হয়েছে।'); }
    });
  }

  toast(type: 'success'|'error', message: string) {
    const t: any = { type, message, removing: false }; this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => { this.toasts = this.toasts.filter(x => x !== t); }, 300); }, 4000);
  }
}
