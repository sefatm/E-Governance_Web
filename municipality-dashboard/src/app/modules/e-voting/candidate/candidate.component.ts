import { Component, OnInit } from '@angular/core';
import { Nominee } from 'src/app/models/candidate.model';
import { Election } from 'src/app/models/election.model';
import { Zone, Center } from 'src/app/models/zone-center.model';
import { NomineeService } from 'src/app/services/candidate.service';
import { ElectionService } from 'src/app/services/election.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-candidate',
  templateUrl: './candidate.component.html',
  styleUrls: ['./candidate.component.css']
})
export class CandidateComponent implements OnInit {

  zones:   Zone[]   = [];
  centers: Center[] = [];

  nominee: Nominee = this.emptyNominee();

  steps = ['Personal information', 'Election information', 'Declaration'];
  currentStep = 1;
  submitted   = false;
  agreed      = false;

  symbolFile:    File | null = null;
  symbolPreview: string | null = null;
  isSubmitting = false;
  toast: { type: 'success' | 'error'; msg: string } | null = null;

  loadingElections = false;
  upcomingElections: Election[] = [];
  selectedElectionId: number | null = null;

  get hasUpcomingElection(): boolean {
    return this.upcomingElections.length > 0;
  }

  constructor(
    public ls: LanguageService,
    private service: NomineeService,
    private electionService: ElectionService
  ) {}

  ngOnInit(): void {
    this.loadUpcomingElections();
    this.service.getZones().subscribe({ next: r => this.zones = r, error: e => console.error(e) });
    this.service.getCenters().subscribe({ next: r => this.centers = r, error: e => console.error(e) });
  }

  loadUpcomingElections(): void {
    this.loadingElections = true;
    this.electionService.getAll().subscribe({
      next: (rows: Election[]) => {
        this.upcomingElections = (rows || []).filter(e => (e.status || '').toUpperCase() === 'UPCOMING');
        this.loadingElections = false;
        if (this.upcomingElections.length === 1) {
          const e = this.upcomingElections[0];
          this.selectedElectionId = e.id || null;
          this.applyElection(e);
        }
      },
      error: () => {
        this.upcomingElections = [];
        this.loadingElections = false;
      }
    });
  }

  onElectionChange(): void {
    const e = this.upcomingElections.find(x => x.id === Number(this.selectedElectionId));
    if (e) this.applyElection(e);
  }

  private applyElection(e: Election): void {
    this.nominee.electionType = e.type || '';
    this.nominee.area = e.area || '';
  }

  nextStep(): void {
    this.submitted = true;
    if (this.currentStep === 1) {
      if (!this.nominee.name || !this.nominee.dob || !this.nominee.nid || !this.nominee.mobileNumber) return;
    }
    if (this.currentStep === 2) {
      if (!this.hasUpcomingElection || !this.selectedElectionId || !this.nominee.electionType) return;
    }
    this.submitted = false;
    this.currentStep++;
  }

  prevStep(): void { this.currentStep--; }

  onSymbolFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file  = input.files?.[0];
    if (!file) return;
    const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowed.includes(file.type)) { this.showToast('error', 'Only JPG, PNG, GIF or WEBP allowed.'); input.value = ''; return; }
    if (file.size > 2 * 1024 * 1024) { this.showToast('error', 'Symbol image must be under 2MB.'); input.value = ''; return; }
    this.symbolFile = file;
    const reader = new FileReader();
    reader.onload = (e) => this.symbolPreview = e.target?.result as string;
    reader.readAsDataURL(file);
  }

  removeSymbol(): void { this.symbolFile = null; this.symbolPreview = null; }

  submitNomination(): void {
    this.submitted = true;
    if (!this.hasUpcomingElection || !this.selectedElectionId) {
      this.showToast('error', 'No upcoming election is open for candidate application.'); return;
    }
    if (!this.nominee.name || !this.nominee.nid || !this.nominee.dob || !this.nominee.mobileNumber) {
      this.showToast('error', 'Personal info required.'); return;
    }
    if (!this.agreed) { this.showToast('error', 'Please agree to the declaration.'); return; }

    this.nominee.hasCriminalRecord = this.nominee.hasCriminalRecord ?? false;
    this.isSubmitting = true;

    const fd = new FormData();
    fd.append('name',              this.nominee.name);
    fd.append('fathersName',       this.nominee.fathersName || '');
    fd.append('mothersName',       this.nominee.mothersName || '');
    fd.append('nid',               this.nominee.nid);
    fd.append('mobileNumber',      this.nominee.mobileNumber);
    fd.append('dob',               this.nominee.dob);
    fd.append('electionType',      this.nominee.electionType || '');
    fd.append('area',              this.nominee.area || '');
    fd.append('party',             this.nominee.party || '');
    fd.append('symbol',            this.nominee.symbol || '');
    fd.append('declaration',       this.nominee.declaration || '');
    fd.append('hasCriminalRecord', String(this.nominee.hasCriminalRecord));
    if (this.nominee.zoneId)   fd.append('zoneId',   String(this.nominee.zoneId));
    if (this.nominee.centerId) fd.append('centerId', String(this.nominee.centerId));
    if (this.symbolFile)       fd.append('symbolFile', this.symbolFile, this.symbolFile.name);

    this.service.submit(fd).subscribe({
      next: () => {
        this.isSubmitting  = false;
        this.showToast('success', 'Nomination submitted successfully! Please wait for approval.');
        this.nominee       = this.emptyNominee();
        this.symbolFile    = null;
        this.symbolPreview = null;
        this.agreed        = false;
        this.currentStep   = 1;
        this.submitted     = false;
      },
      error: (err: any) => {
        this.isSubmitting = false;
        this.showToast('error', err?.error?.message || 'Submission failed. Please try again.');
      }
    });
  }

  showToast(type: 'success' | 'error', msg: string): void {
    this.toast = { type, msg };
    setTimeout(() => this.toast = null, 5000);
  }

  private emptyNominee(): Nominee {
    return {
      name: '', fathersName: '', mothersName: '', nid: '', mobileNumber: '', dob: '',
      electionType: '', area: '', party: '', symbol: '',
      zoneId: null, centerId: null, declaration: '',
      hasCriminalRecord: false, status: 'Pending'
    };
  }
}
