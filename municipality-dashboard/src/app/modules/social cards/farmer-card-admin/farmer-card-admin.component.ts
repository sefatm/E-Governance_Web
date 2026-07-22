// src/app/modules/social cards/farmer-card-admin/farmer-card-admin.component.ts
import { Component, OnInit } from '@angular/core';
import { FarmerCard } from '../../../models/farmer-card.model';
import { FarmerCardService } from '../../../services/farmer-card.service';
import { environment } from 'src/environments/environment';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-farmer-card-admin',
  templateUrl: './farmer-card-admin.component.html',
  styleUrls: ['./shared-admin.css']
})
export class FarmerCardAdminComponent implements OnInit {

  cards: FarmerCard[]         = [];
  filteredCards: FarmerCard[] = [];
  pagedCards: FarmerCard[]    = [];

  selectedCard: FarmerCard | null = null;

  // ✅ FIX Bug 1: viewMode controls whether angular modal is view-only or action
  // No more DOM manipulation / renderModal() needed
  viewMode = false;

  isLoading     = false;
  actionLoading = false;

  showRejectInput = false;
  rejectionReason = '';

  searchText     = '';
  filterStatus   = '';
  filterDistrict = '';
  filterWard     = '';
  districtList: string[] = [];
  wardList: string[] = [];

  currentPage = 1;
  pageSize    = 10;
  totalPages  = 1;

  successMsg = '';
  errorMsg   = '';

  selectedIds: Set<number> = new Set();

  // ✅ FIX Bug 2: Subsidy modal state is fully independent from selectedCard modal
  // openSubsidyModal() no longer calls closeModal() — so it won't destroy the view
  showSubsidyModal = false;
  subsidyCard: FarmerCard | null = null;
  subsidyFertilizer = 0;
  subsidySeed       = 0;

  constructor(public ls: LanguageService, private svc: FarmerCardService) {}

  ngOnInit(): void {
    this.load();
  }

  // ── LOAD ─────────────────────────────────────────────────────
  load(): void {
    this.isLoading = true;
    this.svc.getAll().subscribe({
      next: (data) => {
        this.cards = data;
        this.districtList = [
          ...new Set(data.map(c => c.district).filter(Boolean))
        ] as string[];
        this.wardList = [
          ...new Set(data.map(c => c.ward).filter(Boolean))
        ].map(w => String(w)).sort((a, b) => Number(a) - Number(b));
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.errorMsg = 'Data load করতে সমস্যা হয়েছে।';
      }
    });
  }

  // ── FILTER ───────────────────────────────────────────────────
  applyFilter(): void {
    let list = this.cards;

    if (this.searchText.trim()) {
      const q = this.searchText.toLowerCase();
      list = list.filter(c =>
        c.farmerName?.toLowerCase().includes(q) ||
        c.nid?.includes(q) ||
        c.cardNo?.toLowerCase().includes(q)
      );
    }

    if (this.filterStatus)
      list = list.filter(c =>
        c.status?.toLowerCase() === this.filterStatus.toLowerCase()
      );

    if (this.filterDistrict)
      list = list.filter(c => c.district === this.filterDistrict);

    if (this.filterWard)
      list = list.filter(c => String(c.ward) === this.filterWard);

    this.filteredCards = list;
    this.totalPages = Math.max(1, Math.ceil(list.length / this.pageSize));
    this.currentPage = 1;
    this.paginate();
  }

  resetFilter(): void {
    this.searchText = '';
    this.filterStatus = '';
    this.filterDistrict = '';
    this.filterWard = '';
    this.applyFilter();
  }

  paginate(): void {
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedCards = this.filteredCards.slice(start, start + this.pageSize);
  }

  // ── MODALS ───────────────────────────────────────────────────

  // ✅ FIX Bug 1: View button → viewMode = true, no DOM injection
  openViewModal(card: FarmerCard): void {
    this.selectedCard   = { ...card };
    this.viewMode       = true;
    this.showRejectInput = false;
    this.rejectionReason = '';
  }

  // Action modal — for approve/reject (called from menu or directly)
  openActionModal(card: FarmerCard): void {
    this.selectedCard   = { ...card };
    this.viewMode       = false;
    this.showRejectInput = false;
    this.rejectionReason = '';
  }

  closeModal(): void {
    this.selectedCard   = null;
    this.viewMode       = false;
    this.showRejectInput = false;
    this.rejectionReason = '';
  }

  // ── ACTIONS ──────────────────────────────────────────────────

  approve(card: FarmerCard | null): void {
    if (!card?.id) return;
    this.pickSignature(signatureBase64 => {
      this.actionLoading = true;
      this.svc.updateStatus(card.id!, 'Approved', 'Admin', undefined, signatureBase64).subscribe({
        next: () => { this.actionLoading = false; this.successMsg = '✅ অনুমোদিত।'; this.closeModal(); this.load(); setTimeout(() => this.successMsg = '', 3000); },
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

    if (!this.showRejectInput) {
      this.showRejectInput = true;
      return;
    }

    if (!this.rejectionReason.trim()) {
      this.errorMsg = 'কারণ লিখুন।';
      return;
    }

    this.actionLoading = true;
    this.svc.updateStatus(
      this.selectedCard.id!, 'Rejected', 'Admin', this.rejectionReason
    ).subscribe({
      next: () => {
        this.actionLoading = false;
        this.closeModal();
        this.load();
      },
      error: () => {
        this.actionLoading = false;
        this.errorMsg = 'সমস্যা হয়েছে।';
      }
    });
  }

  // ✅ FIX Bug 4: suspend() — directly from table button, no modal needed
  suspend(card: FarmerCard | null): void {
    if (!card?.id) return;
    const ok = confirm(`"${card.farmerName}" এর কার্ড Suspend করতে চান?`);
    if (!ok) return;

    this.svc.updateStatus(card.id, 'Suspended', 'Admin').subscribe({
      next: () => {
        this.successMsg = 'Suspended।';
        this.closeModal();
        this.load();
        setTimeout(() => this.successMsg = '', 3000);
      }
    });
  }

  // ✅ FIX Bug 4: downloadPdf() — directly from table button, works for any status
  downloadPdf(card: FarmerCard | null): void {
    if (!card?.id) return;

    this.svc.downloadCard(card.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `FarmerCard_${card.cardNo || card.id}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        this.errorMsg = 'PDF download করতে সমস্যা হয়েছে।';
      }
    });
  }

  // ── SUBSIDY ──────────────────────────────────────────────────

  // ✅ FIX Bug 2: no longer calls closeModal() first
  // Subsidy modal is independent — selectedCard modal can remain open or closed
  openSubsidyModal(card: FarmerCard): void {
    this.subsidyCard        = { ...card };
    this.subsidyFertilizer  = card.fertilizerQuota || 0;
    this.subsidySeed        = card.seedQuota || 0;
    this.showSubsidyModal   = true;
    // Don't close view modal — they're independent
  }

  saveSubsidy(): void {
    if (!this.subsidyCard?.id) return;

    this.svc.updateSubsidy(this.subsidyCard.id, {
      fertilizerQuota: this.subsidyFertilizer,
      seedQuota:       this.subsidySeed,
      lastSubsidyDate: new Date().toISOString().split('T')[0]
    }).subscribe({
      next: () => {
        this.successMsg = `✅ ${this.subsidyCard!.farmerName} এর ভর্তুকি আপডেট হয়েছে।`;
        this.showSubsidyModal = false;
        this.load();
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: (err) => {
        this.errorMsg = err?.error?.message || 'ভর্তুকি আপডেট করতে সমস্যা হয়েছে।';
      }
    });
  }

  // ── BULK ─────────────────────────────────────────────────────
  toggleSelect(id: number): void {
    this.selectedIds.has(id)
      ? this.selectedIds.delete(id)
      : this.selectedIds.add(id);
  }

  toggleSelectAll(): void {
    if (this.selectedIds.size === this.pagedCards.length)
      this.selectedIds.clear();
    else
      this.pagedCards.forEach(c => c.id && this.selectedIds.add(c.id));
  }

  bulkApprove(): void {
    if (!this.selectedIds.size) return;
    if (!confirm(`${this.selectedIds.size}টি কার্ড approve করবেন?`)) return;

    let done = 0;
    const ids = Array.from(this.selectedIds);
    ids.forEach(id => {
      this.svc.updateStatus(id, 'Approved', 'Admin').subscribe(() => {
        if (++done === ids.length) {
          this.selectedIds.clear();
          this.load();
        }
      });
    });
  }

  exportCsv(): void {
    const rows = [
      ['#', 'Card No', 'Farmer Name', 'NID', 'District', 'Land Total', 'Fertilizer Quota', 'Seed Quota', 'Status'],
      ...this.filteredCards.map((c, i) => [
        i + 1, c.cardNo || '', c.farmerName, c.nid,
        c.district || '', c.landTotal || 0,
        c.fertilizerQuota || 0, c.seedQuota || 0, c.status || ''
      ])
    ];
    const blob = new Blob([rows.map(r => r.join(',')).join('\n')], { type: 'text/csv' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'farmer_cards.csv';
    a.click();
  }

  // ── UTILS ─────────────────────────────────────────────────────
  // ── Land Verification ──────────────────────────────────────
  // Table-এ inline verify button — list থেকে সরাসরি
  verifyLandInline(card: FarmerCard): void {
    if (!confirm(`"${card.farmerName}"-এর জমি যাচাই করতে চান?`)) return;
    this.verifyLand(card, true);
  }

  verifyLand(card: FarmerCard, verify: boolean = true): void {
    this.actionLoading = true;
    this.svc.verifyLand(card.id!, verify, 'Admin').subscribe({
      next: () => {
        this.actionLoading = false;
        // Update local card state so UI reflects immediately
        const target = this.cards.find(c => c.id === card.id);
        if (target) target.landVerified = verify;
        if (this.selectedCard?.id === card.id)
          this.selectedCard = { ...this.selectedCard!, landVerified: verify } as FarmerCard;
        this.successMsg = verify ? '✅ জমি যাচাই সম্পন্ন।' : '⚠️ জমি যাচাই বাতিল।';
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: () => {
        this.actionLoading = false;
        this.errorMsg = 'জমি যাচাইয়ে সমস্যা হয়েছে।';
        setTimeout(() => this.errorMsg = '', 3000);
      }
    });
  }

  counts(status: string): number {
    return this.cards.filter(c =>
      c.status?.toLowerCase() === status.toLowerCase()
    ).length;
  }

  totalApprovedLand(): string {
    return this.cards
      .filter(c => c.status === 'Approved')
      .reduce((s, c) => s + (c.landTotal || 0), 0)
      .toFixed(2);
  }

  min(a: number, b: number): number { return Math.min(a, b); }

  pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  getFileUrl(path: string | null | undefined): string {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    return environment.apiUrl.replace(/\/api$/, '') + '/' + path;
  }

  onImgError(event: Event): void {
    const target = event.target as HTMLElement;
    if (target) target.style.display = 'none';
  }

  // ================= STANDARD CITIZEN-SERVICE STYLE VIEW =================
  expandedIndex: number | null = null;

  toggleDetails(index: number): void {
    this.expandedIndex = this.expandedIndex === index ? null : index;
  }

  applicantInitial(card: any): string {
    const name = (card?.farmerName || card?.holderName || card?.farmerName || 'A').toString();
    return name.charAt(0).toUpperCase();
  }

  applicantName(card: any): string {
    return card?.farmerName || card?.holderName || card?.farmerName || '—';
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
    const keys = ['cardNo', 'farmerName', 'nid', 'dateOfBirth', 'fatherName', 'occupation', 'contact', 'address', 'ward', 'unionName', 'upazila', 'district', 'landOwn', 'landLease', 'landTotal', 'landVerified', 'cropTypes', 'farmingSeason', 'bankName', 'bankAccount', 'bankBranch', 'fertilizerQuota', 'seedQuota', 'lastSubsidyDate', 'hasOtherCard', 'status', 'rejectionReason', 'approvedBy', 'approvedAt', 'createdAt'];
    return keys
      .map(key => ({ key, value: card?.[key] }))
      .filter(item => item.value !== undefined && item.value !== null && item.value !== '');
  }

  fileEntries(card: any): { label: string; url: string }[] {
    const files = [['Photo', 'photoUrl'], ['NID Document', 'nidFileUrl'], ['Land Document', 'landDocUrl']];
    return files
      .map((f: any) => ({ label: f[0], url: card?.[f[1]] }))
      .filter((f: any) => !!f.url);
  }

  isPdf(url: string): boolean {
    return (url || '').toLowerCase().endsWith('.pdf');
  }

}
