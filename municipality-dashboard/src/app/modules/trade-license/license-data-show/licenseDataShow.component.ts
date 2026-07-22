import { environment } from 'src/environments/environment';
import { Component, HostListener, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TradeLicenseService } from 'src/app/services/trade-license.service';
import { TradeRenewalService } from 'src/app/services/trade-renewal.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success'|'error'|'info'; message: string; removing?: boolean; }
interface FileCard { label: string; path: string; }
interface DetailRow { label: string; value: any; }

@Component({
  selector: 'app-license-data-show',
  templateUrl: './licenseDataShow.component.html',
  styleUrls: ['./licenseDataShow.component.css']
})
export class LicenseDataShowComponent implements OnInit {

  activeTab: 'license'|'renewal' = 'license';
  licenses:  any[] = [];
  renewals:  any[] = [];
  isLoading  = false;
  toasts: Toast[] = [];

  selectedItem: any = null;
  selectedType: 'license'|'renewal' = 'license';
  showDetail   = false;

  showRejectModal = false;
  rejectTarget: { id: number; type: 'license'|'renewal' } | null = null;
  rejectReason = '';

  readonly BASE = `${environment.serverUrl || environment.apiUrl || 'http://localhost:8080'}`.replace(/\/$/, '');

  lightbox: { url: string; label: string; isImage: boolean } | null = null;

  constructor(
    public ls: LanguageService,
    private licenseService: TradeLicenseService,
    private renewalService: TradeRenewalService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.loadLicenses();
    this.loadRenewals();
  }

  get currentRole(): string {
    return localStorage.getItem('role') ||
           localStorage.getItem('userRole') ||
           localStorage.getItem('authority') || '';
  }

  private roleKey(role: string): string {
    return String(role || '')
      .trim()
      .replace(/^ROLE[_\s-]*/i, '')
      .replace(/_/g, ' ')
      .replace(/\/+/g, ' ')
      .replace(/-/g, ' ')
      .replace(/[^a-zA-Z0-9]/g, '')
      .toLowerCase();
  }

  get isDepartmentOfficer(): boolean {
    const r = this.roleKey(this.currentRole);
    return r === 'departmentofficer' || r === 'deptofficer';
  }

  get isFinalApprover(): boolean {
    const r = this.roleKey(this.currentRole);
    return r === 'adminmunicipalofficer' || r === 'admin' || r === 'superadmin';
  }

  get activeItems(): any[] {
    return this.activeTab === 'license' ? this.licenses : this.renewals;
  }

  get activeTitle(): string {
    return this.activeTab === 'license' ? 'New Trade License Applications' : 'Trade License Renewal Applications';
  }

  loadLicenses(): void {
    this.isLoading = true;
    this.licenseService.getAll().subscribe({
      next: r  => { this.licenses = Array.isArray(r) ? r : []; this.isLoading = false; },
      error: () => { this.isLoading = false; this.showToast('Failed to load licenses', 'error'); }
    });
  }

  loadRenewals(): void {
    this.renewalService.getAll().subscribe({
      next:  r  => this.renewals = Array.isArray(r) ? r : [],
      error: () => this.showToast('Failed to load renewals', 'error')
    });
  }

  canApproveLicense(item: any): boolean {
    if (item?.status === 'Pending') return this.isDepartmentOfficer;
    if (item?.status === 'First Approved') return this.isFinalApprover;
    return false;
  }

  canRejectLicense(item: any): boolean {
    if (!item || item.status === 'Approved' || item.status === 'Rejected') return false;
    if (item.status === 'Pending') return this.isDepartmentOfficer || this.isFinalApprover;
    if (item.status === 'First Approved') return this.isFinalApprover;
    return false;
  }

  licenseApprovalLabel(item: any): string {
    return item?.status === 'First Approved' ? 'Final Admin Approval' : 'Department Verification';
  }

  canApproveRenewal(item: any): boolean {
    if (item?.status === 'Pending') return this.isDepartmentOfficer;
    if (item?.status === 'First Approved') return this.isFinalApprover;
    return false;
  }

  renewalApprovalLabel(item: any): string {
    return item?.status === 'First Approved' ? 'Final Renewal Approval' : 'Department Renewal Verification';
  }

  onLicenseSignatureSelected(event: Event, item: any): void {
    this.sendSignature(event, `${this.BASE}/api/tradeLicense/approve/${item.id}`, () => this.loadLicenses(), 'Approval completed.');
  }

  onRenewalSignatureSelected(event: Event, item: any): void {
    this.sendSignature(event, `${this.BASE}/api/trade-renewal/approve/${item.id}`, () => this.loadRenewals(), 'Renewal approval completed.');
  }

  private sendSignature(event: Event, url: string, reload: () => void, fallbackMessage: string): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.showToast('Please select a signature image.', 'error');
      input.value = '';
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const signatureBase64 = String(reader.result || '');
      this.http.put<any>(url, { signatureBase64 }).subscribe({
        next: (res) => {
          this.showToast(res?.message || fallbackMessage, 'success');
          reload();
        },
        error: (err) => this.showToast(err?.error?.message || 'Approval failed.', 'error')
      });
      input.value = '';
    };
    reader.readAsDataURL(file);
  }

  openReject(id: number, type: 'license'|'renewal'): void {
    this.rejectTarget    = { id, type };
    this.rejectReason    = '';
    this.showRejectModal = true;
  }

  confirmReject(): void {
    if (!this.rejectTarget) return;
    const { id, type } = this.rejectTarget;
    const item = type === 'license'
      ? this.licenses.find(l => l.id === id)
      : this.renewals.find(r => r.id === id);
    if (item) item.status = 'Rejected';
    const obs = type === 'license'
      ? this.licenseService.updateStatus(id, 'Rejected')
      : this.renewalService.updateStatus(id, 'Rejected');
    obs.subscribe({
      next: () => {
        this.showToast('Application rejected.', 'success');
        this.showRejectModal = false;
        type === 'license' ? this.loadLicenses() : this.loadRenewals();
      },
      error: () => {
        if (item) item.status = 'Pending';
        this.showToast('Failed to reject', 'error');
      }
    });
  }

  viewDetail(item: any, type: 'license'|'renewal' = this.activeTab): void {
    this.selectedItem = item;
    this.selectedType = type;
    this.showDetail = true;
  }

  closeDetail(): void {
    this.showDetail = false;
    this.selectedItem = null;
  }

  getDisplayName(item: any): string {
    return this.firstValue(item, ['ownerName', 'applicantName', 'name', 'fullName']) || '—';
  }

  getBusinessName(item: any): string {
    return this.firstValue(item, ['businessName']) || item?.originalLicense?.businessName || '—';
  }

  getBusinessType(item: any): string {
    return this.firstValue(item, ['businessType']) || item?.originalLicense?.businessType || '—';
  }

  getMobile(item: any): string {
    return this.firstValue(item, ['mobile', 'contact', 'mobileNumber', 'phone']) || '—';
  }

  getLicenseNo(item: any): string {
    return this.firstValue(item, ['licenseNumber', 'licenseNo']) || item?.originalLicense?.licenseNumber || '—';
  }

  getAppliedDate(item: any): any {
    return this.firstValue(item, ['appliedDate', 'createdAt', 'date']);
  }

  getPhoto(item: any): string {
    return this.firstValue(item, [
      'photoUrl', 'photo_url', 'ownerPhotoUrl', 'owner_photo_url', 'applicantPhotoUrl',
      'applicant_photo_url', 'imageUrl', 'image_url', 'photo', 'pictureUrl', 'picture_url'
    ]) || item?.originalLicense?.photoUrl || item?.originalLicense?.photo_url || '';
  }

  getPhotoLabel(item: any): string {
    return `${this.getDisplayName(item)} Photo`;
  }

  getDetailRows(item: any): DetailRow[] {
    const pairs: Array<[string, any]> = [
      ['Business Name', this.getBusinessName(item)],
      ['Business Type', this.getBusinessType(item)],
      ['License No.', this.getLicenseNo(item)],
      ['Owner / Applicant', this.getDisplayName(item)],
      ["Father's Name", item?.fatherName],
      ["Mother's Name", item?.motherName],
      ['Date of Birth', item?.dateOfBirth],
      ['NID', item?.nid],
      ['Mobile', this.getMobile(item)],
      ['Email', item?.email],
      ['Address', item?.address],
      ['Ward No.', item?.wardNo],
      ['Holding No.', item?.holdingNo],
      ['Income', item?.income ? '৳ ' + item.income : '—'],
      ['Tax / Fee', item?.tax ? '৳ ' + item.tax : item?.fee ? '৳ ' + item.fee : '—'],
      ['Applied Date', this.formatDate(this.getAppliedDate(item))],
      ['Expiry Date', this.formatDate(item?.expiryDate)],
      ['Renewal Reason', item?.reason],
      ['Previous License', item?.previousLicenseNo || item?.oldLicenseNo],
      ['Approval Stage', item?.approvalStage],
      ['Status', item?.status]
    ];
    return pairs
      .filter(([_, value]) => value !== undefined && value !== null && value !== '')
      .map(([label, value]) => ({ label, value }));
  }

  getDocuments(item: any): FileCard[] {
    const docs: FileCard[] = [];
    const add = (label: string, value: any) => {
      if (!value) return;
      this.splitFileValues(value).forEach(path => {
        if (path && !docs.some(d => d.path === path)) docs.push({ label, path });
      });
    };

    add('Applicant Photo', this.getPhoto(item));
    add('NID File', this.firstValue(item, ['nidFileUrl', 'nid_file_url', 'nidUrl', 'nid_url']));
    add('Tax Receipt', this.firstValue(item, ['taxReceiptFileUrl', 'tax_receipt_file_url', 'taxReceiptUrl']));
    add('Old License / Previous Certificate', this.firstValue(item, ['licenseFileUrl', 'license_file_url', 'oldLicenseFileUrl', 'previousLicenseFileUrl']));
    add('Trade License Copy', this.firstValue(item, ['tradeLicenseFileUrl', 'trade_license_file_url']));
    add('Supporting Document', this.firstValue(item, ['documentUrl', 'document_url', 'docUrl', 'doc_url']));

    // Renewal sometimes keeps previous-license files inside originalLicense.
    if (item?.originalLicense) {
      add('Original License Photo', this.getPhoto(item.originalLicense));
      add('Original License NID File', this.firstValue(item.originalLicense, ['nidFileUrl', 'nid_file_url']));
      add('Original Tax Receipt', this.firstValue(item.originalLicense, ['taxReceiptFileUrl', 'tax_receipt_file_url']));
    }

    // Auto-detect extra uploaded file fields, without showing approval signatures.
    Object.keys(item || {}).forEach(key => {
      const lower = key.toLowerCase();
      const value = item[key];
      if (!value || typeof value !== 'string') return;
      if (lower.includes('signature') || lower.includes('seal')) return;
      if (lower.includes('file') || lower.includes('url') || lower.includes('photo') || lower.includes('document')) {
        add(this.toLabel(key), value);
      }
    });

    return docs;
  }

  private splitFileValues(value: any): string[] {
    if (!value) return [];
    if (Array.isArray(value)) return value.flatMap(v => this.splitFileValues(v));
    if (typeof value === 'object') return this.splitFileValues(value.url || value.path || value.fileUrl || value.file_url || '');
    const raw = String(value).trim();
    if (!raw) return [];
    if (raw.startsWith('data:')) return [raw];
    try {
      const parsed = JSON.parse(raw);
      return this.splitFileValues(parsed);
    } catch {}
    return raw.split(',').map(x => x.trim()).filter(x => !!x);
  }

  private firstValue(item: any, keys: string[]): string {
    for (const key of keys) {
      const value = item?.[key];
      if (value !== undefined && value !== null && String(value).trim() !== '') return String(value);
    }
    return '';
  }

  fileUrl(path: string): string {
    if (!path) return '';
    const p = String(path).trim();
    if (/^(https?:|data:|blob:)/i.test(p)) return p;
    return `${this.BASE}/${p.replace(/^\/+/, '')}`;
  }

  isImage(path: string): boolean {
    if (!path) return false;
    return /^data:image\//i.test(path) || /\.(jpg|jpeg|png|gif|webp|bmp|svg)$/i.test(path.split('?')[0]);
  }

  isPdf(path: string): boolean {
    if (!path) return false;
    return /\.pdf$/i.test(path.split('?')[0]);
  }

  openLightbox(path: string, label: string): void {
    if (!path) return;
    this.lightbox = { url: this.fileUrl(path), label, isImage: this.isImage(path) };
  }

  closeLightbox(): void { this.lightbox = null; }

  @HostListener('document:keydown.escape')
  onEsc(): void { this.closeLightbox(); }

  downloadLicense(id: number): void {
    this.downloadBlob(`${this.BASE}/api/tradeLicense/certificate/${id}`, `TradeLicense_${id}.pdf`);
  }

  downloadRenewal(id: number): void {
    this.downloadBlob(`${this.BASE}/api/trade-renewal/certificate/${id}`, `TradeRenewal_${id}.pdf`);
  }

  private downloadBlob(url: string, filename: string): void {
    this.showToast('PDF download starting...', 'success');
    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (blob: Blob) => {
        const fileUrl  = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href  = fileUrl;
        link.download = filename;
        link.click();
        window.URL.revokeObjectURL(fileUrl);
        this.showToast('PDF downloaded successfully!', 'success');
      },
      error: (err) => this.showToast(err?.error?.message || 'Failed to download PDF', 'error')
    });
  }

  badgeClass(status: string): string {
    const s = (status || '').toLowerCase();
    if (s === 'approved') return 'badge-approved';
    if (s === 'rejected') return 'badge-rejected';
    if (s === 'first approved') return 'badge-info';
    return 'badge-pending';
  }

  private formatDate(value: any): string {
    if (!value) return '—';
    try {
      const d = new Date(value);
      if (!isNaN(d.getTime())) return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
    } catch {}
    return String(value);
  }

  private toLabel(key: string): string {
    return key
      .replace(/_/g, ' ')
      .replace(/([a-z])([A-Z])/g, '$1 $2')
      .replace(/\b\w/g, c => c.toUpperCase());
  }

  showToast(message: string, type: 'success'|'error'|'info'): void {
    const t: Toast = { type, message };
    this.toasts.push(t);
    setTimeout(() => {
      t.removing = true;
      setTimeout(() => this.toasts = this.toasts.filter(x => x !== t), 300);
    }, 4000);
  }
}
