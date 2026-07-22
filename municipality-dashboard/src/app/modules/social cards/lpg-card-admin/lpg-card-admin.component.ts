import { Component, OnInit, OnDestroy } from '@angular/core';
import { LpgCard } from '../../../models/lpg-card.model';
import { LpgCardService } from '../../../services/lpg-card.service';
import { environment } from 'src/environments/environment';
import { LanguageService } from 'src/app/services/language.service';

// ── FIX 3: recordCollection() error handling যোগ করা হয়েছে ─────────────────
// ── FIX 4: status filter case-insensitive ────────────────────────────────────

@Component({
  selector: 'app-lpg-card-admin',
  templateUrl: './lpg-card-admin.component.html',
  styleUrls: ['./shared-admin.css']
})
export class LpgCardAdminComponent implements OnInit {

  cards: LpgCard[]         = [];
  filteredCards: LpgCard[] = [];
  pagedCards: LpgCard[]    = [];
  selectedCard: LpgCard | null = null;

  isLoading     = true;
  actionLoading = false;
  showRejectInput = false;
  rejectionReason = '';

  searchText   = '';
  filterStatus = '';
  filterDealer = '';
  filterWard   = '';

  dealerList: string[] = [];
  wardList:   string[] = [];

  currentPage = 1;
  pageSize    = 10;
  totalPages  = 1;

  successMsg = '';
  errorMsg   = '';

  selectedIds: Set<number> = new Set();

  private readonly API_BASE = environment.apiUrl.replace(/\/api$/, '');

  constructor(public ls: LanguageService, private svc: LpgCardService) {}

  ngOnInit() { this.load(); }

  load() {
    this.isLoading = true;
    this.svc.getAll().subscribe({
      next: data => {
        this.cards      = data;
        this.dealerList = [...new Set(data.map(c => c.dealerName).filter(Boolean))] as string[];
        this.wardList   = [...new Set(data.map(c => c.ward).filter(Boolean))] as string[];
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; this.errorMsg = 'Data load করতে সমস্যা হয়েছে।'; }
    });
  }

  applyFilter() {
    let list = this.cards;
    if (this.searchText.trim()) {
      const q = this.searchText.toLowerCase();
      list = list.filter(c => c.holderName?.toLowerCase().includes(q) || c.nid?.includes(q));
    }
    // FIX 4: case-insensitive
    if (this.filterStatus)
      list = list.filter(c => c.status?.toLowerCase() === this.filterStatus.toLowerCase());
    if (this.filterDealer) list = list.filter(c => c.dealerName === this.filterDealer);
    if (this.filterWard)   list = list.filter(c => c.ward === this.filterWard);

    this.filteredCards = list;
    this.totalPages    = Math.max(1, Math.ceil(list.length / this.pageSize));
    this.currentPage   = 1;
    this.paginate();
  }

  resetFilter() { this.searchText=''; this.filterStatus=''; this.filterDealer=''; this.filterWard=''; this.applyFilter(); }

  paginate() {
    const s = (this.currentPage - 1) * this.pageSize;
    this.pagedCards = this.filteredCards.slice(s, s + this.pageSize);
  }

  openModal(card: LpgCard, reject = false) {
    this.selectedCard    = { ...card };
    this.showRejectInput = reject;
    this.rejectionReason = '';
  }

  closeModal() {
    this.selectedCard = null;
  }

  // FIX 3: recordCollection with proper error handling
  recordCollection(card: LpgCard) {
    if (!confirm(`${card.holderName} এর সিলিন্ডার সংগ্রহ রেকর্ড করবেন?`)) return;
    this.svc.recordCollection(card.id!).subscribe({
      next: () => {
        this.successMsg = `✅ ${card.holderName} এর সংগ্রহ রেকর্ড হয়েছে।`;
        this.load();
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: (err) => {
        this.errorMsg = '❌ ' + (err?.error?.message || 'সংগ্রহ রেকর্ড করতে সমস্যা হয়েছে।');
        setTimeout(() => this.errorMsg = '', 4000);
      }
    });
  }

  approve(card: LpgCard) {
    this.pickSignature(signatureBase64 => {
      this.actionLoading = true;
      this.svc.updateStatus(card.id!, 'Approved', 'Admin', undefined, signatureBase64).subscribe({
        next: () => { this.actionLoading = false; this.successMsg = '✅ অনুমোদিত।'; this.closeModal(); this.load(); },
        error: () => { this.actionLoading = false; alert('সমস্যা হয়েছে।'); }
      });
    });
  }

  private pickSignature(done: (signatureBase64: string) => void): void {
    const input = document.createElement('input');
    input.type = 'file'; input.accept = 'image/*';
    input.onchange = () => { const file = input.files?.[0]; if (!file) return; const reader = new FileReader(); reader.onload = () => done(String(reader.result || '')); reader.readAsDataURL(file); };
    input.click();
  }

  rejectSelected() {
    if (!this.selectedCard) return;
    if (!this.showRejectInput) { this.showRejectInput = true; return; }
    if (!this.rejectionReason.trim()) { alert('কারণ লিখুন।'); return; }
    this.actionLoading = true;
    this.svc.updateStatus(this.selectedCard.id!, 'Rejected', 'Admin', this.rejectionReason).subscribe({
      next: () => { this.actionLoading = false; this.closeModal(); this.load(); },
      error: () => { this.actionLoading = false; alert('সমস্যা হয়েছে।'); }
    });
  }

  suspend(card: LpgCard) {
    this.actionLoading = true;
    this.svc.updateStatus(card.id!, 'Suspended', 'Admin').subscribe({
      next: () => { this.actionLoading = false; this.closeModal(); this.load(); },
      error: () => { this.actionLoading = false; alert('সমস্যা হয়েছে।'); }
    });
  }

  downloadPdf(card: LpgCard) {
    this.svc.downloadCard(card.id!).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a'); a.href = url; a.download = `LPGCard_${card.cardNo}.pdf`; a.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('PDF download করতে সমস্যা হয়েছে।')
    });
  }

  // Bulk
  toggleSelect(id: number) { this.selectedIds.has(id) ? this.selectedIds.delete(id) : this.selectedIds.add(id); }
  toggleSelectAll() {
    if (this.selectedIds.size === this.pagedCards.length) this.selectedIds.clear();
    else this.pagedCards.forEach(c => c.id && this.selectedIds.add(c.id));
  }

  bulkApprove() {
    if (!this.selectedIds.size) return;
    if (!confirm(`${this.selectedIds.size}টি কার্ড approve করবেন?`)) return;
    let done = 0;
    const ids = Array.from(this.selectedIds);
    ids.forEach(id => this.svc.updateStatus(id, 'Approved', 'Admin').subscribe(() => {
      if (++done === ids.length) { this.selectedIds.clear(); this.load(); }
    }));
  }

  exportCsv() {
    const rows = [
      ['#','Card No','Holder Name','NID','Ward','Dealer','Monthly Quota','Status','Created At'],
      ...this.filteredCards.map((c,i) => [i+1, c.cardNo||'', c.holderName, c.nid, c.ward||'', c.dealerName||'', c.monthlyQuota||'', c.status||'', c.createdAt||''])
    ];
    const blob = new Blob([rows.map(r=>r.join(',')).join('\n')], {type:'text/csv'});
    const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download='lpg_cards.csv'; a.click();
  }


  counts(s: string) { return this.cards.filter(c => c.status?.toLowerCase()===s.toLowerCase()).length; }
  totalMonthlyQuota() { return this.cards.filter(c=>c.status?.toLowerCase()==='approved').reduce((s,c)=>s+(c.monthlyQuota||0),0); }
  min(a:number,b:number){return Math.min(a,b);}
  pageNumbers(){return Array.from({length:this.totalPages},(_,i)=>i+1);}

  getFileUrl(path: string | null | undefined): string {
    if (!path) return '';
    return this.API_BASE + '/' + path;
  }

  onImgError(event: any) {
    event.target.style.display = 'none';
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
    const keys = ['cardNo', 'holderName', 'nid', 'contact', 'address', 'ward', 'unionName', 'upazila', 'district', 'dateOfBirth', 'membersCount', 'stoveCount', 'hasGasLine', 'dealerName', 'dealerCode', 'dealerContact', 'monthlyQuota', 'cylinderSize', 'lastCollectedAt', 'incomeMonthly', 'occupation', 'hasOtherCard', 'status', 'rejectionReason', 'approvedBy', 'approvedAt', 'createdAt'];
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
