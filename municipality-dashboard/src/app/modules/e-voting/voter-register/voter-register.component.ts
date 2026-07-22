import { Component, OnInit } from '@angular/core';
import { Voter } from 'src/app/models/voter.model';
import { Zone, Center } from 'src/app/models/zone-center.model';
import { NomineeService } from 'src/app/services/candidate.service';
import { VoterService } from 'src/app/services/voter.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-voter-register',
  templateUrl: './voter-register.component.html',
  styleUrls: ['./voter-register.component.css']
})
export class VoterRegisterComponent implements OnInit {

  zones:   Zone[]   = [];
  centers: Center[] = [];
  voter: Voter = this.emptyVoter();

  steps = ['Personal Information', 'Identity and Address', 'Voting Information'];
  currentStep = 1;
  submitted   = false;
  agreed      = false;

  selectedPhoto: File | null = null;
  photoPreview:  string | null = null;
  isSubmitting = false;
  toast: { type: 'success' | 'error'; msg: string } | null = null;

  constructor(public ls: LanguageService, private voterService: VoterService, private nomineeService: NomineeService) {}

  ngOnInit(): void {
    this.nomineeService.getZones().subscribe({ next: r => this.zones = r, error: e => console.error(e) });
    this.nomineeService.getCenters().subscribe({ next: r => this.centers = r, error: e => console.error(e) });
    this.voter.registrationDate = new Date().toISOString().split('T')[0];
  }

  nextStep(): void {
    this.submitted = true;
    if (this.currentStep === 1) {
      if (!this.voter.name || !this.voter.dob) return;
    }
    if (this.currentStep === 2) {
      if (!this.voter.nid || !this.voter.mobile) return;
    }
    this.submitted = false;
    this.currentStep++;
  }

  prevStep(): void { this.currentStep--; }

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const allowed = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    if (!allowed.includes(file.type)) { this.showToast('error', 'Only JPG, PNG, or WEBP images can be uploaded.'); return; }
    if (file.size > 2 * 1024 * 1024) { this.showToast('error', 'Image size cannot exceed 2 MB.'); return; }
    this.selectedPhoto = file;
    const reader = new FileReader();
    reader.onload = (e) => this.photoPreview = e.target?.result as string;
    reader.readAsDataURL(file);
  }

  removePhoto(): void { this.selectedPhoto = null; this.photoPreview = null; }

  register(): void {
    this.submitted = true;
    if (!this.voter.name || !this.voter.nid || !this.voter.dob || !this.voter.mobile) {
      this.showToast('error', 'Name, NID, Date of Birth, and Mobile Number are required.'); return;
    }
    if (!this.voter.zoneId || !this.voter.centerId) {
      this.showToast('error', 'Please select a Zone and Voting Center.'); return;
    }
    if (!this.agreed) { this.showToast('error', 'Please agree to the declaration.'); return; }

    this.voter.status = 'PENDING';
    this.isSubmitting = true;

    this.voterService.register(this.buildPayload()).subscribe({
      next: () => this.onSuccess(),
      error: (err) => {
        this.isSubmitting = false;
        this.showToast('error', err.error?.message || 'Registration failed.');
      }
    });
  }

  private onSuccess(msg = 'Voter registration successful! Please wait for approval.'): void {
    this.isSubmitting  = false;
    this.showToast('success', msg);
    this.voter        = this.emptyVoter();
    this.photoPreview  = null;
    this.selectedPhoto = null;
    this.agreed        = false;
    this.currentStep   = 1;
    this.submitted     = false;
  }

  private emptyVoter(): Voter {
    return {
      name: '', dob: '', gender: '', fatherName: '', motherName: '',
      nid: '', mobile: '', district: '', upazila: '', area: '', address: '',
      electionType: '', zoneId: null, centerId: null,
      registrationDate: new Date().toISOString().split('T')[0], status: 'PENDING'
    };
  }

  private buildPayload(): Voter | FormData {
    if (!this.selectedPhoto) return this.voter;

    const fd = new FormData();
    Object.entries(this.voter).forEach(([key, value]) => {
      if (value !== null && value !== undefined) {
        fd.append(key, String(value));
      }
    });
    fd.append('photo', this.selectedPhoto, this.selectedPhoto.name);
    return fd;
  }

  showToast(type: 'success' | 'error', msg: string): void {
    this.toast = { type, msg };
    setTimeout(() => this.toast = null, 5000);
  }
}
