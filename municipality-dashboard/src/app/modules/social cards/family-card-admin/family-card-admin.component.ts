import { Component, OnInit, OnDestroy, Renderer2 } from '@angular/core';
import { FamilyCard } from '../../../models/family-card.model';
import { FamilyCardService } from '../../../services/family-card.service';
import { environment } from 'src/environments/environment';
import { LanguageService } from 'src/app/services/language.service';


@Component({
  selector: 'app-family-card-admin',
  templateUrl: './family-card-admin.component.html',
  styleUrls: ['./shared-admin.css']
})
export class FamilyCardAdminComponent implements OnInit, OnDestroy {

  cards: FamilyCard[] = [];
  filteredCards: FamilyCard[] = [];
  pagedCards: FamilyCard[] = [];

  selectedCard: FamilyCard | null = null;

  // ✅ viewMode = true হলে শুধু View Modal খুলবে (কোনো action button নেই)
  viewMode = false;

  isLoading = true;
  actionLoading = false;

  showRejectInput = false;
  rejectionReason = '';

  searchText = '';
  filterStatus = '';
  filterWard = '';
  wardList: string[] = [];

  currentPage = 1;
  pageSize = 10;
  totalPages = 1;

  successMsg = '';
  errorMsg = '';

  selectedIds: Set<number> = new Set();

  constructor(public ls: LanguageService, 
    private svc: FamilyCardService,
    private renderer: Renderer2
  ) {}

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {}

  // ================= LOAD =================
  load(): void {
    this.isLoading = true;

    this.svc.getAll().subscribe({
      next: (data) => {
        this.cards = data;

        this.wardList = [
          ...new Set(data.map(c => c.ward).filter(Boolean))
        ] as string[];

        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.errorMsg = 'Data load করতে সমস্যা হয়েছে।';
      }
    });
  }

  // ================= FILTER =================
  applyFilter(): void {
    let list = this.cards;

    if (this.searchText.trim()) {
      const q = this.searchText.toLowerCase();
      list = list.filter(c =>
        c.holderName?.toLowerCase().includes(q) ||
        c.nid?.includes(q) ||
        c.cardNo?.includes(q)
      );
    }

    if (this.filterStatus) {
      list = list.filter(c =>
        c.status?.toLowerCase() === this.filterStatus.toLowerCase()
      );
    }

    if (this.filterWard) {
      list = list.filter(c => c.ward === this.filterWard);
    }

    this.filteredCards = list;
    this.totalPages = Math.max(1, Math.ceil(list.length / this.pageSize));
    this.currentPage = 1;

    this.paginate();
  }

  paginate(): void {
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedCards = this.filteredCards.slice(start, start + this.pageSize);
  }

  resetFilter(): void {
    this.searchText = '';
    this.filterStatus = '';
    this.filterWard = '';
    this.applyFilter();
  }

  // ================= MODAL =================

  // ✅ View button click — শুধু data দেখাবে, কোনো action থাকবে না
  openViewModal(card: FamilyCard): void {
    this.selectedCard = { ...card };
    this.viewMode = true;
    this.showRejectInput = false;
    this.rejectionReason = '';
  }

  closeModal(): void {
    this.selectedCard = null;
    this.viewMode = false;
    this.showRejectInput = false;
    this.rejectionReason = '';
  }

  getFileUrl(path: string | null | undefined): string {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    const serverBase = environment.apiUrl.replace(/\/api$/, '');
    return `${serverBase}/${path}`;
  }

  onImgError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/images/no-image.png';
    img.style.opacity = '0.5';
  }

  // ================= ACTIONS =================

  approve(card: FamilyCard | null): void {
    if (!card?.id) return;
    this.pickSignature(signatureBase64 => {
      this.svc.updateStatus(card.id!, 'Approved', 'Admin', undefined, signatureBase64).subscribe({
        next: () => { this.successMsg = 'Approved'; this.load(); }
      });
    });
  }

  private pickSignature(done: (signatureBase64: string) => void): void {
    const input = document.createElement('input');
    input.type = 'file'; input.accept = 'image/*';
    input.onchange = () => {
      const file = input.files?.[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = () => done(String(reader.result || ''));
      reader.readAsDataURL(file);
    };
    input.click();
  }

  rejectSelected(): void {
    if (!this.selectedCard?.id) return;
    if (!this.rejectionReason.trim()) return;

    this.svc.updateStatus(
      this.selectedCard.id,
      'Rejected',
      'Admin',
      this.rejectionReason
    ).subscribe({
      next: () => {
        this.successMsg = 'Rejected';
        this.load();
        this.closeModal();
      }
    });
  }

  // ✅ PDF — table action button থেকে directly call হবে
  downloadPdf(card: FamilyCard | null): void {
    if (!card?.id) return;

    this.svc.downloadCard(card.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `FamilyCard_${card.cardNo || card.id}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.errorMsg = 'PDF download error';
      }
    });
  }

  // ✅ Suspend — table action button থেকে directly call হবে
  suspend(card: FamilyCard | null): void {
    if (!card?.id) return;

    const ok = confirm(`"${card.holderName}" এর কার্ড Suspend করতে চান?`);
    if (!ok) return;

    this.svc.updateStatus(card.id, 'Suspended', 'Admin').subscribe({
      next: () => {
        this.successMsg = 'Suspended';
        this.load();
        this.closeModal();
      }
    });
  }

  // ================= BULK =================
  toggleSelect(id: number): void {
    this.selectedIds.has(id)
      ? this.selectedIds.delete(id)
      : this.selectedIds.add(id);
  }

  toggleSelectAll(): void {
    if (this.selectedIds.size === this.pagedCards.length) {
      this.selectedIds.clear();
    } else {
      this.pagedCards.forEach(c => c.id && this.selectedIds.add(c.id));
    }
  }

  bulkApprove(): void {
    const ids = Array.from(this.selectedIds);
    if (!ids.length) return;

    ids.forEach(id => {
      this.svc.updateStatus(id, 'Approved', 'Admin').subscribe(() => {
        this.load();
      });
    });

    this.selectedIds.clear();
  }

  exportCsv(): void {
    const rows = [
      ['Card No', 'Name', 'NID', 'Ward', 'Status'],
      ...this.filteredCards.map(c => [
        c.cardNo, c.holderName, c.nid, c.ward, c.status
      ])
    ];

    const csv = rows.map(r => r.join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });

    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'family_cards.csv';
    a.click();
  }

  // ================= UTILS =================
  counts(status: string): number {
    return this.cards.filter(c =>
      c.status?.toLowerCase() === status.toLowerCase()
    ).length;
  }

  min(a: number, b: number): number {
    return Math.min(a, b);
  }

  pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

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
    const keys = ['cardNo', 'holderName', 'nid', 'dateOfBirth', 'contact', 'address', 'ward', 'unionName', 'upazila', 'district', 'membersCount', 'incomeMonthly', 'occupation', 'husbandOrFatherName', 'hasOtherCard', 'status', 'rejectionReason', 'approvedBy', 'approvedAt', 'createdAt'];
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
