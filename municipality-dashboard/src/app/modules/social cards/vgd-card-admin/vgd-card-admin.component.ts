// vgd-card-admin.component.ts — All bugs fixed
import { Component, OnInit } from '@angular/core';
import { VgdCard } from '../../../models/vgd-card.model';
import { VgdCardService } from '../../../services/vgd-card.service';
import { environment } from 'src/environments/environment';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-vgd-card-admin',
  templateUrl: './vgd-card-admin.component.html',
  styleUrls: ['./shared-admin.css']
})
export class VgdCardAdminComponent implements OnInit {

  cards: VgdCard[]         = [];
  filteredCards: VgdCard[] = [];
  pagedCards: VgdCard[]    = [];

  selectedCard: VgdCard | null = null;

  // ✅ FIX Bug 1: viewMode replaces renderModal() — no DOM manipulation
  viewMode  = false;
  activeTab = 'details'; // 'details' | 'distribution' | 'action'

  isLoading     = false;
  actionLoading = false;

  showRejectInput = false;
  rejectionReason = '';

  searchText  = '';
  filterStatus= '';
  filterType  = '';
  filterWard  = '';
  wardList: string[] = [];

  currentPage = 1;
  pageSize    = 10;
  totalPages  = 1;

  successMsg = '';
  errorMsg   = '';

  selectedIds: Set<number> = new Set();

  // Distribution
  distMonth      = new Date().toISOString().slice(0, 7);
  distributedBy  = '';
  distMsg        = '';
  distError      = false;
  distributionHistory: any[] = [];
  historyLoading = false;

  constructor(public ls: LanguageService, private svc: VgdCardService) {}

  ngOnInit(): void { this.load(); }

  // ── LOAD ─────────────────────────────────────────────────
  load(): void {
    this.isLoading = true;
    this.svc.getAll().subscribe({
      next: (data) => {
        this.cards = data;
        this.wardList = [...new Set(data.map(c => c.ward).filter(Boolean))] as string[];
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; this.errorMsg = 'Data load করতে সমস্যা হয়েছে।'; }
    });
  }

  // ── FILTER ───────────────────────────────────────────────
  applyFilter(): void {
    let list = this.cards;
    if (this.searchText.trim()) {
      const q = this.searchText.toLowerCase();
      list = list.filter(c =>
        c.holderName?.toLowerCase().includes(q) || c.nid?.includes(q)
      );
    }
    if (this.filterStatus) list = list.filter(c => c.status?.toLowerCase() === this.filterStatus.toLowerCase());
    if (this.filterType)   list = list.filter(c => c.cardType === this.filterType);
    if (this.filterWard)   list = list.filter(c => c.ward === this.filterWard);

    this.filteredCards = list;
    this.totalPages    = Math.max(1, Math.ceil(list.length / this.pageSize));
    this.currentPage   = 1;
    this.paginate();
  }

  resetFilter(): void {
    this.searchText = ''; this.filterStatus = ''; this.filterType = ''; this.filterWard = '';
    this.applyFilter();
  }

  paginate(): void {
    const s = (this.currentPage - 1) * this.pageSize;
    this.pagedCards = this.filteredCards.slice(s, s + this.pageSize);
  }

  // ── MODAL ────────────────────────────────────────────────
  // ✅ FIX Bug 1: Angular-only modal, no DOM injection
  openViewModal(card: VgdCard): void {
    this.selectedCard    = { ...card };
    this.viewMode        = true;
    this.activeTab       = 'details';
    this.showRejectInput = false;
    this.rejectionReason = '';
    this.distMsg         = '';
    this.distributionHistory = [];
  }

  closeModal(): void {
    this.selectedCard = null;
    this.viewMode     = false;
    this.distMsg      = '';
    this.showRejectInput = false;
    this.rejectionReason = '';
  }

  // ── ACTIONS ──────────────────────────────────────────────
  approve(card: VgdCard | null): void {
    if (!card?.id) return;
    this.pickSignature(signatureBase64 => {
      this.actionLoading = true;
      this.svc.updateStatus(card.id!, 'Approved', 'Admin', undefined, signatureBase64).subscribe({
        next: () => { this.actionLoading = false; this.closeModal(); this.load(); },
        error: () => { this.actionLoading = false; this.errorMsg = 'সমস্যা হয়েছে।'; }
      });
    });
  }

  private pickSignature(done: (signatureBase64: string) => void): void {
    const input = document.createElement('input');
    input.type = 'file'; input.accept = 'image/*';
    input.onchange = () => { const file = input.files?.[0]; if (!file) return; const reader = new FileReader(); reader.onload = () => done(String(reader.result || '')); reader.readAsDataURL(file); };
    input.click();
  }

  rejectSelected(): void {
    if (!this.selectedCard) return;
    if (!this.showRejectInput) { this.showRejectInput = true; return; }
    if (!this.rejectionReason.trim()) { this.errorMsg = 'কারণ লিখুন।'; return; }
    this.actionLoading = true;
    this.svc.updateStatus(this.selectedCard.id!, 'Rejected', 'Admin', this.rejectionReason).subscribe({
      next: () => { this.actionLoading = false; this.closeModal(); this.load(); },
      error: () => { this.actionLoading = false; }
    });
  }

  // ✅ FIX Bug 2: suspend() now called directly from table button
  suspend(card: VgdCard | null): void {
    if (!card?.id) return;
    if (!confirm(`"${card.holderName}" এর কার্ড Suspend করতে চান?`)) return;
    this.svc.updateStatus(card.id, 'Suspended', 'Admin').subscribe({
      next: () => { this.closeModal(); this.load(); }
    });
  }

  // ✅ FIX Bug 2 + Bug 8: downloadPdf works for any status
  downloadPdf(card: VgdCard | null): void {
    if (!card?.id) return;
    this.svc.downloadCard(card.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a   = document.createElement('a');
        a.href = url;
        a.download = `${card.cardType}Card_${card.cardNo}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => { this.errorMsg = 'PDF download করতে সমস্যা হয়েছে।'; }
    });
  }

  // ── DISTRIBUTION ─────────────────────────────────────────
  // ✅ FIX Bug 6: inserts into vgd_distribution, duplicate-month guard
  recordDistribution(): void {
    if (!this.selectedCard?.id) return;
    this.actionLoading = true;
    this.distMsg = '';

    this.svc.recordDistribution(
      this.selectedCard.id,
      this.distMonth,
      this.distributedBy || 'Admin',
      ''
    ).subscribe({
      next: (res: any) => {
        this.actionLoading = false;
        this.distMsg       = res.message;
        this.distError     = !res.success;
        if (res.success) { this.loadHistory(); this.load(); }
      },
      error: (err: any) => {
        this.actionLoading = false;
        this.distMsg       = err?.error?.message || 'সমস্যা হয়েছে।';
        this.distError     = true;
      }
    });
  }

  loadHistory(): void {
    if (!this.selectedCard?.id) return;
    this.historyLoading = true;
    this.svc.getDistributionHistory(this.selectedCard.id).subscribe({
      next: (res: any) => { this.distributionHistory = res; this.historyLoading = false; },
      error: ()        => { this.historyLoading = false; }
    });
  }

  // ── RENEWAL ──────────────────────────────────────────────
  renew(card: VgdCard | null): void {
    if (!card?.id) return;
    if (!confirm(`"${card.holderName}" এর কার্ড ${card.cycleMonths} মাস নবায়ন করবেন?`)) return;
    this.svc.renew(card.id).subscribe({
      next: (res: any) => {
        this.successMsg = res.message;
        this.closeModal(); this.load();
        setTimeout(() => this.successMsg = '', 3000);
      }
    });
  }

  // ── BULK ─────────────────────────────────────────────────
  toggleSelect(id: number): void {
    this.selectedIds.has(id) ? this.selectedIds.delete(id) : this.selectedIds.add(id);
  }
  toggleSelectAll(): void {
    if (this.selectedIds.size === this.pagedCards.length) this.selectedIds.clear();
    else this.pagedCards.forEach(c => c.id && this.selectedIds.add(c.id));
  }
  bulkApprove(): void {
    if (!this.selectedIds.size) return;
    let done = 0; const ids = Array.from(this.selectedIds);
    ids.forEach(id => this.svc.updateStatus(id, 'Approved', 'Admin').subscribe(() => {
      if (++done === ids.length) { this.selectedIds.clear(); this.load(); }
    }));
  }

  exportCsv(): void {
    const rows = [
      ['#','Card No','Type','Holder Name','NID','Ward','Rice Kg','Cash','End Date','Status'],
      ...this.filteredCards.map((c, i) => [
        i+1, c.cardNo||'', c.cardType, c.holderName, c.nid,
        c.ward||'', c.monthlyRiceKg||0, c.cashAmount||0,
        c.endDate||'', c.status||''
      ])
    ];
    const blob = new Blob([rows.map(r=>r.join(',')).join('\n')],{type:'text/csv'});
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'vgd_cards.csv'; a.click();
  }

  // ── UTILS ─────────────────────────────────────────────────
  isExpiringSoon(card: VgdCard): boolean {
    if (!card.endDate || card.status !== 'Approved') return false;
    const diff = new Date(card.endDate).getTime() - Date.now();
    return diff > 0 && diff < 30 * 24 * 60 * 60 * 1000; // 30 days
  }

  counts(status: string): number {
    return this.cards.filter(c => c.status?.toLowerCase() === status.toLowerCase()).length;
  }
  countType(t: string): number {
    return this.cards.filter(c => c.cardType === t && c.status === 'Approved').length;
  }

  getFileUrl(path: string | null | undefined): string {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    return environment.apiUrl.replace(/\/api$/, '') + '/' + path;
  }
  onImgError(e: Event): void {
    (e.target as HTMLImageElement).style.opacity = '0.3';
  }

  min(a: number, b: number): number { return Math.min(a, b); }
  pageNumbers(): number[] { return Array.from({length: this.totalPages}, (_, i) => i+1); }

  // ================= STANDARD CITIZEN-SERVICE STYLE VIEW =================
  expandedIndex: number | null = null;

  toggleDetails(index: number): void {
    this.expandedIndex = this.expandedIndex === index ? null : index;
  }

  applicantInitial(card: any): string {
    const name = (card?.holderName || card?.holderName || card?.farmerName || 'A').toString();
    return name.charAt(0).toUpperCase();
  }

  applicantName(card: any): string {
    return card?.holderName || card?.holderName || card?.farmerName || '—';
  }

  applicantSub(card: any): string {
    return card?.nid || card?.contact || card?.cardNo || '—';
  }


  openRejectModal(card: any): void {
    this.selectedCard = { ...card };
    this.showRejectInput = true;
    this.rejectionReason = '';
  }

  statusClass(status: string | undefined): string {
    const s = (status || '').toLowerCase();
    if (s.includes('approved')) return 's-approved';
    if (s.includes('reject')) return 's-rejected';
    if (s.includes('suspend')) return 's-suspended';
    return 's-pending';
  }

  formatKey(key: string): string {
    return key
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, c => c.toUpperCase());
  }

  detailFields(card: any): { key: string; value: any }[] {
    const keys = ['cardNo', 'cardType', 'holderName', 'nid', 'dateOfBirth', 'contact', 'husbandName', 'fatherName', 'occupation', 'address', 'ward', 'unionName', 'upazila', 'district', 'maritalStatus', 'disability', 'hasLand', 'landArea', 'incomeMonthly', 'membersCount', 'childrenCount', 'hasOtherCard', 'monthlyRiceKg', 'monthlyWheatKg', 'cashAmount', 'cycleMonths', 'startDate', 'endDate', 'lastReceivedDate', 'bankName', 'bankAccount', 'mobileBanking', 'mobileBankingNo', 'status', 'rejectionReason', 'approvedBy', 'approvedAt', 'createdAt'];
    return keys
      .map(key => ({ key, value: card?.[key] }))
      .filter(item => item.value !== undefined && item.value !== null && item.value !== '');
  }

  fileEntries(card: any): { label: string; url: string }[] {
    const files = [['Photo', 'photoUrl'], ['NID Document', 'nidFileUrl']];
    return files
      .map((f: any) => ({ label: f[0], url: card?.[f[1]] }))
      .filter((f: any) => !!f.url);
  }

  isPdf(url: string): boolean {
    return (url || '').toLowerCase().endsWith('.pdf');
  }

}
