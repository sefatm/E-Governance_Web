import { Component } from '@angular/core';
import { FarmerCardService } from 'src/app/services/farmer-card.service';
import { LanguageService } from 'src/app/services/language.service';

// ── FIX 2: landTotal auto-calculate — submitForm() এ landOwn + landLease যোগ করে send ──
// ── FIX 4: farmer-card-admin এ status filter case-insensitive করা হয়েছে ──────────────

@Component({
  selector: 'app-farmer-card-apply',
  templateUrl: './farmer-card-apply.component.html',
  styleUrls: ['../family-card-apply/family-card-apply.component.css']
})
export class FarmerCardApplyComponent {

  steps     = ['Personal Info', 'Land & Bank', 'Documents'];
  currentStep = 1;
  submitted = false;
  loading   = false;
  declared  = false;
  toasts: any[] = [];

  form = {
    farmerName: '', fatherName: '', nid: '', dateOfBirth: '',
    contact: '', address: '', ward: '', unionName: '',
    upazila: '', district: '',
    landOwn: 0, landLease: 0,
    cropTypes: '', farmingSeason: '',
    bankName: '', bankAccount: ''
  };

  photoFile: File | null    = null; photoPreview: string | null = null;
  nidFile: File | null      = null; nidFileName: string | null  = null;
  landDocFile: File | null  = null; landDocName: string | null  = null;

  districts = ['ঢাকা','গাজীপুর','নারায়ণগঞ্জ','নরসিংদী','মানিকগঞ্জ','মুন্সীগঞ্জ','টাঙ্গাইল','ফরিদপুর','গোপালগঞ্জ','মাদারীপুর','রাজবাড়ী','শরীয়তপুর','চট্টগ্রাম','কক্সবাজার','কুমিল্লা','ব্রাহ্মণবাড়িয়া','চাঁদপুর','ফেনী','লক্ষ্মীপুর','নোয়াখালী','খাগড়াছড়ি','রাঙ্গামাটি','বান্দরবান','রাজশাহী','নাটোর','নওগাঁ','চাঁপাইনবাবগঞ্জ','পাবনা','সিরাজগঞ্জ','বগুড়া','জয়পুরহাট','খুলনা','বাগেরহাট','সাতক্ষীরা','যশোর','নড়াইল','মাগুরা','ঝিনাইদহ','কুষ্টিয়া','চুয়াডাঙ্গা','মেহেরপুর','বরিশাল','ভোলা','পটুয়াখালী','পিরোজপুর','ঝালকাঠি','বরগুনা','সিলেট','মৌলভীবাজার','হবিগঞ্জ','সুনামগঞ্জ','রংপুর','দিনাজপুর','ঠাকুরগাঁও','পঞ্চগড়','নীলফামারী','লালমনিরহাট','কুড়িগ্রাম','গাইবান্ধা','ময়মনসিংহ','জামালপুর','শেরপুর','নেত্রকোনা','কিশোরগঞ্জ'];

  constructor(public ls: LanguageService, private svc: FarmerCardService) {}

  // FIX 2: landTotal getter — HTML-এ live দেখাবে
  get landTotal(): number {
    return (Number(this.form.landOwn) || 0) + (Number(this.form.landLease) || 0);
  }

  nextStep() {
    this.submitted = true;
    if (this.currentStep === 1 && (!this.form.farmerName || !this.form.nid || !this.form.contact || !this.form.address)) return;
    this.submitted = false;
    if (this.currentStep < this.steps.length) this.currentStep++;
  }

  prevStep() { if (this.currentStep > 1) this.currentStep--; }

  onPhoto(e: any) {
    const file = e.target.files[0]; if (!file) return; this.photoFile = file;
    const r = new FileReader(); r.onload = (ev: any) => { this.photoPreview = ev.target.result; }; r.readAsDataURL(file);
  }
  removePhoto() { this.photoFile = null; this.photoPreview = null; }
  onNidFile(e: any) { const f = e.target.files[0]; if (!f) return; this.nidFile = f; this.nidFileName = f.name; }
  removeNid() { this.nidFile = null; this.nidFileName = null; }
  onLandDoc(e: any) { const f = e.target.files[0]; if (!f) return; this.landDocFile = f; this.landDocName = f.name; }

  submitForm() {
    if (!this.declared) return;
    this.loading = true;
    const fd = new FormData();

    // FIX 2: landTotal auto-calculate করে send করা হচ্ছে
    // আগে Object.entries() দিয়ে সরাসরি form পাঠানো হতো
    // landTotal ছিল না form-এ, backend-এ 0 যাচ্ছিল
    const payload = {
      ...this.form,
      landOwn:   Number(this.form.landOwn)   || 0,
      landLease: Number(this.form.landLease) || 0,
      landTotal: this.landTotal   // ← FIX: calculated value
    };

    Object.entries(payload).forEach(([k, v]) => fd.append(k, String(v)));
    if (this.photoFile)   fd.append('photo',   this.photoFile);
    if (this.nidFile)     fd.append('nidFile', this.nidFile);
    if (this.landDocFile) fd.append('landDoc', this.landDocFile);

    this.svc.apply(fd).subscribe({
      next: (res) => {
        this.loading = false;
        this.toast('success', `আবেদন সফল! কার্ড নং: ${res.cardNo}`);
        setTimeout(() => { this.currentStep = 1; this.resetForm(); }, 2000);
      },
      error: (err) => { this.loading = false; this.toast('error', err.error?.message || 'সমস্যা হয়েছে।'); }
    });
  }

  resetForm() {
    this.form = { farmerName:'',fatherName:'',nid:'',dateOfBirth:'',contact:'',address:'',ward:'',unionName:'',upazila:'',district:'',landOwn:0,landLease:0,cropTypes:'',farmingSeason:'',bankName:'',bankAccount:'' };
    this.photoFile=null; this.photoPreview=null;
    this.nidFile=null; this.nidFileName=null;
    this.landDocFile=null; this.landDocName=null;
    this.declared=false;
  }

  toast(type: 'success' | 'error', message: string) {
    const t: any = { type, message, removing: false }; this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => { this.toasts = this.toasts.filter(x => x !== t); }, 300); }, 4000);
  }
}
