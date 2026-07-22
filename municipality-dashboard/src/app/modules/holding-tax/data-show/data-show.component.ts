import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-data-show',
  templateUrl: './data-show.component.html',
  styleUrls: ['./data-show.component.css']
})
export class DataShowComponent implements OnInit {

  applications : any[] = [];
  filteredApps : any[] = [];
  isLoading    = false;

  searchText   = '';
  selectedType = '';
  expandedIndex: number | null = null;
  downloadModalApp: any = null;
  approvalApp: any = null;
  signatureBase64 = '';
  signaturePreview = '';
  sealBase64 = '';
  sealPreview = '';
  approvalBusy = false;
  readonly SERVER = environment.serverUrl;

  private newHoldingUrl  = `${environment.apiUrl}/holding-new-registration/getall`;
  private ownershipUrl   = `${environment.apiUrl}/ownership-transfer/getall`;
  private newStatusUrl   = `${environment.apiUrl}/holding-new-registration/status`;
  private ownStatusUrl   = `${environment.apiUrl}/ownership-transfer/status`;

  constructor(public ls: LanguageService, private http: HttpClient) {}

  ngOnInit(): void { this.loadApplications(); }

  get currentRole(): string { return localStorage.getItem('role') || ''; }
  get isDepartmentOfficer(): boolean { return this.currentRole === 'Department Officer'; }
  get isFinalApprover(): boolean {
    return this.currentRole === 'Admin / Municipal Officer' || this.currentRole === 'Super Admin';
  }

  canApprove(app: any): boolean {
    if (app.type === 'Ownership Transfer') {
      if (app.status === 'Pending') return this.isDepartmentOfficer;
      if (app.status === 'First Approved') return this.isFinalApprover;
      return false;
    }
    if (app.type !== 'New Holding') return app.status === 'Pending';
    if (app.status === 'Pending') return this.isDepartmentOfficer;
    if (app.status === 'First Approved') return this.isFinalApprover;
    return false;
  }

  canReject(app: any): boolean {
    if (app.status === 'Approved' || app.status === 'Rejected') return false;
    if (app.type === 'Ownership Transfer') {
      if (app.status === 'Pending') return this.isDepartmentOfficer || this.isFinalApprover;
      if (app.status === 'First Approved') return this.isFinalApprover;
      return false;
    }
    if (app.type !== 'New Holding') return true;
    if (app.status === 'Pending') return this.isDepartmentOfficer || this.isFinalApprover;
    if (app.status === 'First Approved') return this.isFinalApprover;
    return false;
  }

  approvalLabel(app: any): string {
    return app.status === 'First Approved' ? 'Final Admin Approval' : 'Department Verification';
  }

  openApproval(app: any): void {
    this.approvalApp = app;
    this.signatureBase64 = '';
    this.signaturePreview = '';
    this.sealBase64 = '';
    this.sealPreview = '';
  }

  closeApproval(): void {
    if (this.approvalBusy) return;
    this.approvalApp = null;
  }

  onSignatureSelected(event: Event): void {
    this.readApprovalImage(event, 'signature');
  }

  onSealSelected(event: Event): void {
    this.readApprovalImage(event, 'seal');
  }

  private readApprovalImage(event: Event, kind: 'signature' | 'seal'): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      alert(kind === 'signature' ? 'Please select a signature image.' : 'Please select a seal image.');
      input.value = '';
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const value = String(reader.result || '');
      if (kind === 'signature') { this.signatureBase64 = value; this.signaturePreview = value; }
      else { this.sealBase64 = value; this.sealPreview = value; }
    };
    reader.readAsDataURL(file);
  }

  submitApproval(): void {
    const app = this.approvalApp;
    if (!app || !this.signatureBase64 || !this.sealBase64) return;
    const endpoint = app.type === 'Ownership Transfer'
      ? `${environment.apiUrl}/ownership-transfer/approve/${app.id}`
      : `${environment.apiUrl}/holding-new-registration/approve/${app.id}`;

    this.approvalBusy = true;
    this.http.put<any>(endpoint, { signatureBase64: this.signatureBase64, sealBase64: this.sealBase64 }).subscribe({
      next: (res) => {
        app.status = res.status;
        app.details.status = res.status;
        app.details.approvalStage = res.approvalStage;
        this.approvalBusy = false;
        this.approvalApp = null;
        this.loadApplications();
      },
      error: (err) => {
        this.approvalBusy = false;
        alert(err?.error?.message || 'Approval failed.');
      }
    });
  }

  isFileField(key: string): boolean {
    return /fileurl|photourl|documenturl|attachmenturl/i.test(key || '');
  }

  isApprovalAsset(key: string): boolean {
    return /^(firstSignature|firstSeal|secondSignature|secondSeal)$/i.test(key || '');
  }

  visibleDetailEntries(details: any): { key: string; value: any }[] {
    if (!details) return [];
    return Object.keys(details)
      .filter(key => !this.isFileField(key) && !this.isApprovalAsset(key))
      .map(key => ({ key, value: details[key] }));
  }

  fileEntries(details: any): { key: string; label: string; url: string }[] {
    if (!details) return [];
    const labels: Record<string, string> = {
      currentOwnerNidFileUrl: 'Current Owner NID',
      newOwnerNidFileUrl: 'New Owner NID',
      deedFileUrl: 'Transfer Deed',
      nidFileUrl: 'Applicant NID',
      deedDocumentUrl: 'Land Deed',
      taxReceiptFileUrl: 'Tax Receipt',
      applicantPhotoUrl: 'Applicant Photo',
      photoUrl: 'Applicant Photo'
    };
    return Object.keys(details)
      .filter(key => this.isFileField(key) && !!details[key])
      .map(key => ({ key, label: labels[key] || this.prettyLabel(key), url: String(details[key]) }));
  }

  prettyLabel(key: string): string {
    return (key || '').replace(/Url$/i, '').replace(/([a-z])([A-Z])/g, '$1 $2')
      .replace(/^./, c => c.toUpperCase());
  }

  fileUrl(path: string): string {
    if (!path) return '';
    if (/^https?:\/\//i.test(path) || path.startsWith('data:')) return path;
    return `${this.SERVER}/${path.replace(/^\/+/, '')}`;
  }

  isImage(path: string): boolean {
    return /\.(png|jpe?g|gif|webp|bmp)(\?.*)?$/i.test(path || '');
  }

  isPdf(path: string): boolean {
    return /\.pdf(\?.*)?$/i.test(path || '');
  }


  loadApplications(): void {
    this.isLoading = true;

    forkJoin({
      newHolding: this.http.get<any[]>(this.newHoldingUrl).pipe(catchError(() => of([]))),
      ownership:  this.http.get<any[]>(this.ownershipUrl).pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ newHolding, ownership }) => {
        const newData = newHolding.map((a: any) => ({
          id: a.id, name: a.applicantName || 'N/A',
          type: 'New Holding', status: a.status || 'Pending', details: a
        }));
        const transferData = ownership.map((a: any) => ({
          id: a.id, name: a.currentOwner || 'N/A',
          type: 'Ownership Transfer', status: a.status || 'Pending', details: a
        }));

        this.applications = [...newData, ...transferData];
        this.filteredApps = [...this.applications];
        this.isLoading    = false;
        this.filterData();
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  filterData(): void {
    this.filteredApps = this.applications.filter(a =>
      (!this.searchText || a.name.toLowerCase().includes(this.searchText.toLowerCase())) &&
      (!this.selectedType || a.type === this.selectedType)
    );
  }

  toggleDetails(index: number): void {
    this.expandedIndex = this.expandedIndex === index ? null : index;
  }

  updateStatus(app: any, status: string): void {
    const urlMap: any = {
      'New Holding'       : `${this.newStatusUrl}/${app.id}`,
      'Ownership Transfer': `${this.ownStatusUrl}/${app.id}`
    };
    const url = urlMap[app.type];
    if (!url) return;

    this.http.put(url, { status }).subscribe({
      next: () => {
        app.status = status;
        this.filterData();
      },
      error: (err) => {
        console.error('Status update failed:', err);
        alert('Failed to update status.');
      }
    });
  }

  openDownloadPopup(app: any): void {
    this.downloadModalApp = app;
  }

  closeDownloadPopup(): void {
    this.downloadModalApp = null;
  }

  confirmDownload(): void {
    const app = this.downloadModalApp;
    if (!app) return;
    const urlMap: any = {
      'New Holding'       : `${environment.apiUrl}/holding-new-registration/generate-pdf/${app.id}`,
      'Ownership Transfer': `${environment.apiUrl}/ownership-transfer/generate-pdf/${app.id}`
    };
    const url = urlMap[app.type];
    if (!url) return;

    const a = document.createElement('a');
    a.href = url;
    a.target = '_blank';
    a.download = `certificate-${app.id}.pdf`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    this.downloadModalApp = null;
  }

  isApproved(status?: string): boolean {
    if (!status) return false;
    return status.replace(/['"]/g, '').trim().toLowerCase() === 'approved';
  }

  statusClass(status: string): string {
    const s = (status || '').replace(/['"]/g, '').trim().toLowerCase();
    if (s === 'approved') return 'approved';
    if (s === 'rejected') return 'rejected';
    return 'pending';
  }
}
