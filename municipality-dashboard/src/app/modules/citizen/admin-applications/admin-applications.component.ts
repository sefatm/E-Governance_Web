import { environment } from 'src/environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-admin-applications',
  templateUrl: './admin-applications.component.html',
  styleUrls: ['./admin-applications.component.css']
})
export class AdminApplicationsComponent implements OnInit {

  applications: any[] = [];
  filteredApps: any[] = [];
  isLoading = false;

  searchText     = '';
  selectedType   = '';
  selectedStatus = '';
  expandedIndex: number | null = null;

  toasts: {type:'success'|'error'; message:string; removing?:boolean}[] = [];
  approvalApp: any = null;
  signaturePreview = '';
  signatureBase64 = '';
  sealPreview = '';
  sealBase64 = '';
  approvalBusy = false;
  sealBusyId: string | null = null;

  showToast(message: string, type: 'success'|'error'): void {
    const t = { type, message };
    this.toasts.push(t);
    setTimeout(() => { (t as any).removing = true; setTimeout(() => this.toasts = this.toasts.filter(x => x !== t), 300); }, 4000);
  }

  private readonly FILE_FIELDS = [
    'photoUrl', 'photo_url', 'applicantPhotoUrl',
    'nidFileUrl', 'nid_file_url', 'nidCopyUrl',
    'fatherNidFileUrl', 'father_nid_file_url',
    'motherNidFileUrl', 'mother_nid_file_url',
    'vaccineFileUrl', 'vaccine_file_url',
    'deathNidFileUrl', 'death_nid_file_url',
    'medicalFileUrl', 'medical_file_url',
    'birthFileUrl', 'birth_file_url',
    'headPhotoUrl', 'head_photo_url',
    'headNidUrl', 'head_nid_url',
    'memberDocUrls', 'member_doc_urls', 'membersJson', 'members_json'
  ];

  private readonly FILE_LABELS: Record<string, string> = {
    photoUrl:          'Applicant Photo',
    photo_url:         'Applicant Photo',
    applicantPhotoUrl: 'Applicant Photo',
    nidFileUrl:        'NID Copy',
    nid_file_url:      'NID Copy',
    nidCopyUrl:        'NID Copy',
    fatherNidFileUrl:  "Father's NID",
    father_nid_file_url: "Father's NID",
    motherNidFileUrl:  "Mother's NID",
    mother_nid_file_url: "Mother's NID",
    vaccineFileUrl:    'Vaccine Card',
    vaccine_file_url:  'Vaccine Card',
    deathNidFileUrl:   "Deceased NID",
    death_nid_file_url: "Deceased NID",
    medicalFileUrl:    'Medical / Death Doc',
    medical_file_url:  'Medical / Death Doc',
    birthFileUrl:      'Birth Document',
    birth_file_url:    'Birth Document',
    headPhotoUrl:      'Head of Family Photo',
    head_photo_url:    'Head of Family Photo',
    headNidUrl:        'Head of Family NID',
    head_nid_url:      'Head of Family NID',
    memberDocUrls:     'Family Member Document',
    member_doc_urls:   'Family Member Document',
    membersJson:       'Family Member Document',
    members_json:      'Family Member Document',
  };

  readonly BASE = `${environment.serverUrl}`;

  get pendingCount():  number { return this.applications.filter(a => a.status === 'Pending').length; }
  get approvedCount(): number { return this.applications.filter(a => a.status === 'Approved').length; }

  constructor(public ls: LanguageService, private http: HttpClient) {}

  // ── Auth header ─────────────────────────────────────────────────────────────
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token') || '';
    return new HttpHeaders({ Authorization: 'Bearer ' + token });
  }

  ngOnInit(): void { this.loadData(); }

  get currentRole(): string {
    return localStorage.getItem('role') || '';
  }

  get isDepartmentOfficer(): boolean {
    return this.currentRole === 'Department Officer';
  }

  get isFinalApprover(): boolean {
    return this.currentRole === 'Admin / Municipal Officer' || this.currentRole === 'Super Admin';
  }

  canApprove(app: any): boolean {
    if (!this.isCertificateType(app) || app?.status === 'Approved' || app?.status === 'Rejected') return false;
    if (app?.status === 'Pending') return this.isDepartmentOfficer;
    if (app?.status === 'First Approved') return this.isFinalApprover;
    return false;
  }

  canReject(app: any): boolean {
    if (app?.status === 'Rejected' || app?.status === 'Approved') return false;
    if (!this.isCertificateType(app)) return true;
    if (app?.status === 'Pending') return this.isDepartmentOfficer || this.isFinalApprover;
    if (app?.status === 'First Approved') return this.isFinalApprover;
    return false;
  }

  canUpdateSeal(app: any): boolean {
    return this.isCertificateType(app)
      && app?.status === 'Approved'
      && (this.isDepartmentOfficer || this.isFinalApprover);
  }

  // ── Load all applications with auth header ───────────────────────────────────
  loadData(): void {
    this.isLoading = true;
    const headers = this.getHeaders();

    forkJoin({
      birthDeath: this.http.get<any[]>(`${this.BASE}/api/birth-death/getall`, { headers }).pipe(catchError(() => of([]))),
      passport:   this.http.get<any[]>(`${this.BASE}/api/passport/getall`,   { headers }).pipe(catchError(() => of([]))),
      family:     this.http.get<any[]>(`${this.BASE}/api/family/getall`,     { headers }).pipe(catchError(() => of([]))),
      citizen:    this.http.get<any[]>(`${this.BASE}/api/citizen/getall`,    { headers }).pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ birthDeath, passport, family, citizen }) => {
        const bd  = birthDeath.map(a => ({ id:a.id, name:a.name, mobile:a.mobileNumber??a.contact??'', type:a.type||'Birth', status:a.status||'Pending', details:a }));
        const pp  = passport.map(a   => ({ id:a.id, name:a.name??a.fullName, mobile:a.contact??'', type:'Passport', status:a.status||'Pending', details:a }));
        const fam = family.map(a     => ({ id:a.id, name:a.head_name??a.headName, mobile:a.contact??'', type:'Family', status:a.status||'Pending', details:a }));
        const cit = citizen.map(a    => ({ id:a.id, name:a.name, mobile:a.contact??'', type:'Citizen', status:a.status||'Pending', details:a }));

        this.applications = [...bd, ...pp, ...fam, ...cit]
          .map(app => this.prepareApp(app))
          .sort((a, b) => new Date(b.details?.createdAt||0).getTime() - new Date(a.details?.createdAt||0).getTime());
        this.filteredApps = [...this.applications];
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  filterData(): void {
    const s = this.searchText.toLowerCase();
    this.filteredApps = this.applications.filter(a =>
      (!s || a.name?.toLowerCase().includes(s) || a.mobile?.includes(s)) &&
      (!this.selectedType   || a.type   === this.selectedType) &&
      (!this.selectedStatus || a.status === this.selectedStatus)
    );
  }

  resetFilter(): void {
    this.searchText = ''; this.selectedType = ''; this.selectedStatus = '';
    this.filteredApps = [...this.applications];
  }

  toggleDetails(index: number): void {
    this.expandedIndex = this.expandedIndex === index ? null : index;
  }

  private prepareApp(app: any): any {
    return {
      ...app,
      detailFields: this.getDetailFields(app.details),
      fileEntries: this.getFileEntries(app.details)
    };
  }


  openApproval(app: any): void {
    this.approvalApp = app;
    this.signaturePreview = '';
    this.signatureBase64 = '';
    this.sealPreview = '';
    this.sealBase64 = '';
  }

  closeApproval(): void {
    if (this.approvalBusy) return;
    this.approvalApp = null;
    this.signaturePreview = '';
    this.signatureBase64 = '';
    this.sealPreview = '';
    this.sealBase64 = '';
  }

  onSignatureSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.showToast('Please select a signature image.', 'error');
      input.value = '';
      return;
    }
    this.readImageAsPng(file)
      .then(dataUrl => {
        this.signatureBase64 = dataUrl;
        this.signaturePreview = dataUrl;
      })
      .catch(() => {
        this.showToast('Signature image could not be processed.', 'error');
        input.value = '';
      });
  }

  onSealSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.showToast('Please select a seal image.', 'error');
      input.value = '';
      return;
    }
    this.readImageAsPng(file)
      .then(dataUrl => {
        this.sealBase64 = dataUrl;
        this.sealPreview = dataUrl;
      })
      .catch(() => {
        this.showToast('Seal image could not be processed.', 'error');
        input.value = '';
      });
  }

  private readImageAsPng(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onerror = () => reject();
      reader.onload = () => {
        const img = new Image();
        img.onerror = () => reject();
        img.onload = () => {
          const canvas = document.createElement('canvas');
          canvas.width = img.naturalWidth || img.width;
          canvas.height = img.naturalHeight || img.height;
          const ctx = canvas.getContext('2d');
          if (!ctx || !canvas.width || !canvas.height) {
            reject();
            return;
          }
          ctx.drawImage(img, 0, 0);
          resolve(canvas.toDataURL('image/png'));
        };
        img.src = String(reader.result || '');
      };
      reader.readAsDataURL(file);
    });
  }

  submitApproval(): void {
    const app = this.approvalApp;
    if (!app || !this.signatureBase64) {
      this.showToast('Signature image is required.', 'error');
      return;
    }
    if (!this.sealBase64) {
      this.showToast('Seal image is required.', 'error');
      return;
    }
    const urlMap: any = {
      'Birth': `${this.BASE}/api/birth-death/approve/${app.id}`,
      'Death': `${this.BASE}/api/birth-death/approve/${app.id}`,
      'Family': `${this.BASE}/api/family/approve/${app.id}`,
      'Citizen': `${this.BASE}/api/citizen/approve/${app.id}`
    };
    const url = urlMap[app.type];
    if (!url) { this.showToast('Two-step approval is not configured for this application type.', 'error'); return; }

    this.approvalBusy = true;
    this.http.put<any>(url, { signatureBase64: this.signatureBase64, sealBase64: this.sealBase64 }, { headers: this.getHeaders() }).subscribe({
      next: (res) => {
        app.status = res.status;
        app.details.status = res.status;
        app.details.approvalStage = res.approvalStage;
        this.approvalBusy = false;
        this.closeApproval();
        this.filterData();
        this.showToast(res.message || 'Approval completed successfully.', 'success');
        this.loadData();
      },
      error: (err) => {
        this.approvalBusy = false;
        this.showToast(err?.error?.message || 'Approval failed.', 'error');
      }
    });
  }

  updateSeal(app: any, event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.showToast('Please select a seal image.', 'error');
      return;
    }
    const key = `${app.type}-${app.id}`;
    const urlMap: any = {
      'Birth': `${this.BASE}/api/birth-death/seal/${app.id}`,
      'Death': `${this.BASE}/api/birth-death/seal/${app.id}`,
      'Family': `${this.BASE}/api/family/seal/${app.id}`,
      'Citizen': `${this.BASE}/api/citizen/seal/${app.id}`
    };
    const url = urlMap[app.type];
    if (!url) { this.showToast('Seal update is not configured for this application type.', 'error'); return; }

    this.sealBusyId = key;
    this.readImageAsPng(file)
      .then(sealBase64 => {
        this.http.put<any>(url, { sealBase64 }, { headers: this.getHeaders() }).subscribe({
          next: (res) => {
            app.status = res.status;
            app.details.status = res.status;
            app.details.approvalStage = res.approvalStage;
            this.sealBusyId = null;
            this.showToast(res.message || 'Seal updated successfully.', 'success');
            this.loadData();
          },
          error: (err) => {
            this.sealBusyId = null;
            this.showToast(err?.error?.message || 'Seal update failed.', 'error');
          }
        });
      })
      .catch(() => {
        this.sealBusyId = null;
        this.showToast('Seal image could not be processed.', 'error');
      });
  }

  approvalButtonLabel(app: any): string {
    return app?.status === 'First Approved' ? 'Final Admin Approval' : 'Department Verification';
  }

  isCertificateType(app: any): boolean {
    return ['Birth','Death','Family','Citizen'].includes(app?.type);
  }

    // ── Update status with auth header ───────────────────────────────────────────
  updateStatus(app: any, status: string): void {
    const urlMap: any = {
      'Birth':    `${this.BASE}/api/birth-death/status/${app.id}`,
      'Death':    `${this.BASE}/api/birth-death/status/${app.id}`,
      'Passport': `${this.BASE}/api/passport/status/${app.id}`,
      'Family':   `${this.BASE}/api/family/status/${app.id}`,
      'Citizen':  `${this.BASE}/api/citizen/status/${app.id}`,
    };
    const url = urlMap[app.type];
    if (!url) return;

    const headers = this.getHeaders();
    this.http.put(url, { status }, { headers }).subscribe({
      next: () => {
        app.status = status;
        this.filterData();
        this.showToast('Status updated successfully!', 'success');
      },
      error: () => this.showToast('Failed to update status. Please try again.', 'error')
    });
  }

  // ── Detail helpers ───────────────────────────────────────────────────────────
  getDetailFields(details: any): { key: string; value: any }[] {
    if (!details) return [];
    const skip = new Set([
      'id', 'status', 'certificateNo', 'certificate_no', 'declaration',
      'firstSignature', 'secondSignature', 'firstSeal', 'secondSeal',
      'first_signature', 'second_signature', 'first_seal', 'second_seal',
      ...this.FILE_FIELDS
    ]);
    return Object.keys(details)
      .filter(k => !skip.has(k) && !this.looksLikeFileField(k) && details[k] != null && details[k] !== '')
      .map(k => ({ key: k, value: this.formatDetailValue(details[k]) }));
  }

  isFileField(key: string): boolean { return this.FILE_FIELDS.includes(key) || this.looksLikeFileField(key); }

  getFileEntries(details: any): { key: string; label: string; url: string }[] {
    if (!details) return [];
    const out: { key: string; label: string; url: string }[] = [];
    const seen = new Set<string>();

    const add = (key: string, label: string, value: any, index?: number) => {
      for (const url of this.extractUrls(value)) {
        if (!url || seen.has(url)) continue;
        seen.add(url);
        out.push({ key: index != null ? `${key}_${index}` : key, label: index != null ? `${label} ${index + 1}` : label, url });
      }
    };

    Object.keys(details).forEach(key => {
      const label = this.FILE_LABELS[key] || this.formatKey(key);
      if (this.FILE_FIELDS.includes(key) || this.looksLikeFileField(key)) {
        add(key, label, details[key]);
      }
    });

    // Family member documents may be inside membersJson as docUrl / documentUrl / fileUrl.
    for (const key of ['membersJson', 'members_json']) {
      const raw = details[key];
      if (!raw) continue;
      try {
        const members = typeof raw === 'string' ? JSON.parse(raw) : raw;
        if (Array.isArray(members)) {
          members.forEach((m: any, idx: number) => {
            const name = m?.name || m?.memberName || `Member ${idx + 1}`;
            add(key, `${name} Document`, m?.docUrl || m?.documentUrl || m?.fileUrl || m?.nidUrl || m?.birthDocUrl, idx);
          });
        }
      } catch { /* ignore raw JSON display */ }
    }

    return out;
  }

  private looksLikeFileField(key: string): boolean {
    const k = key.toLowerCase();
    if (k.includes('signature') || k.includes('seal')) return false;
    return k.includes('url') || k.includes('file') || k.includes('photo') || k.includes('document') || k.endsWith('doc') || k.includes('docurl');
  }

  private extractUrls(value: any): string[] {
    if (!value) return [];
    if (Array.isArray(value)) return value.flatMap(v => this.extractUrls(v));
    if (typeof value === 'object') {
      return this.extractUrls(value.url || value.path || value.fileUrl || value.docUrl || value.documentUrl || value.nidUrl || value.birthDocUrl);
    }
    const text = String(value).trim();
    if (!text) return [];
    try {
      const parsed = JSON.parse(text);
      if (parsed !== text) return this.extractUrls(parsed);
    } catch {}
    return text
      .split(',')
      .map(x => x.trim().replace(/^\/+/, ''))
      .filter(x => /^(uploads\/|assets\/|https?:\/\/|data:)/i.test(x) || /\.(png|jpe?g|webp|pdf)$/i.test(x));
  }

  fileUrl(url: string): string {
    if (!url) return '';
    if (/^(https?:\/\/|data:)/i.test(url)) return url;
    return `${this.BASE}/${url.replace(/^\/+/, '')}`;
  }

  private formatDetailValue(value: any): any {
    if (value == null) return '';
    if (typeof value === 'object') return JSON.stringify(value);
    const text = String(value);
    if (text.length > 240 && (text.trim().startsWith('[') || text.trim().startsWith('{'))) return '';
    return value;
  }

  isPdf(url: string): boolean { return url?.toLowerCase().split('?')[0].endsWith('.pdf'); }

  formatKey(key: string): string {
    return key.replace(/([A-Z])/g, ' $1').replace(/_/g, ' ').replace(/^\w/, c => c.toUpperCase());
  }

  typeClass(type: string): string {
    const m: any = { Birth:'t-birth', Death:'t-death', Family:'t-family', Citizen:'t-citizen', Passport:'t-passport' };
    return m[type] || '';
  }
}
