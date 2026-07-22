import { environment } from 'src/environments/environment';
import { Component, OnInit } from '@angular/core';
import { ETenderService } from 'src/app/services/etender.service';
import { ETenderNotice, ETenderBid, ETenderAward, VendorBlacklist } from 'src/app/models/etender.model';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-etender-admin',
  templateUrl: './etender-admin.component.html',
  styleUrls: ['./etender-admin.component.css']
})
export class ETenderAdminComponent implements OnInit {
  readonly serverUrl = environment.serverUrl;

  // ── Tabs ──────────────────────────────────────────────────────────────────
  activeTab: 'notices' | 'bids' | 'awards' | 'blacklist' = 'notices';

  // ── Data ──────────────────────────────────────────────────────────────────
  notices      : ETenderNotice[]    = [];
  bids         : ETenderBid[]       = [];
  filteredBids : ETenderBid[]       = [];
  awards       : ETenderAward[]     = [];
  blacklist    : VendorBlacklist[]  = [];

  isLoading      = false;
  showForm       = false;
  showAwardModal = false;
  isEditing      = false;
  editingId      : number | null = null;

  // ── Notice form ───────────────────────────────────────────────────────────
  form: ETenderNotice = this.emptyForm();
  categories = ['Road', 'Building', 'IT', 'Supply', 'Water', 'Sanitation', 'Electrical', 'Other'];

  // ── Award modal ───────────────────────────────────────────────────────────
  awardingTenderId    = 0;
  awardingTenderTitle = '';
  awardBidOptions     : ETenderBid[] = [];
  selectedBidId       = 0;
  awardAmount         = 0;
  awardRemarks        = '';

  // ── Bid filter ────────────────────────────────────────────────────────────
  bidFilterTenderId = 0;

  // Gap Fix #1: ⏰ Close expired loading flag
  closeExpiredLoading = false;

  // Gap Fix #2: 📄 Doc verify modal
  showVerifyModal  = false;
  verifyingBid     : ETenderBid | null = null;
  verifyDecision   : boolean | null = null;   // true=Verified, false=Rejected
  verifyRemark     = '';
  verifyLoading    = false;

  // Gap Fix #4: 🏆 Lowest bid per tender (cache)
  lowestBidMap: { [tenderId: number]: ETenderBid } = {};

  // Gap Fix #5: 🚫 Blacklist add modal
  showBlacklistModal = false;
  newVendor          : VendorBlacklist = {};
  blacklistLoading   = false;

  // ── Toast ─────────────────────────────────────────────────────────────────
  toast: { type: 'success' | 'error'; msg: string } | null = null;

  constructor(public ls: LanguageService, private svc: ETenderService) {}

  ngOnInit(): void { this.loadAll(); }

  // ─── Load ─────────────────────────────────────────────────────────────────
  loadAll(): void {
    this.isLoading = true;
    this.svc.getAllNotices().subscribe({
      next: (res) => {
        this.notices   = res;
        this.isLoading = false;
        // Gap Fix #4: Load lowest bids for all tenders
        res.forEach(n => { if (n.id) this.loadLowestBid(n.id); });
      },
      error: () => { this.isLoading = false; }
    });
    this.svc.getAllBids().subscribe({ next: (res) => { this.bids = res; this.filteredBids = res; } });
    this.svc.getAllAwards().subscribe({ next: (res) => { this.awards = res; } });
    this.svc.getAllBlacklisted().subscribe({ next: (res) => { this.blacklist = res; } });
  }

  // Gap Fix #4: Load lowest bid per tender
  loadLowestBid(tenderId: number): void {
    this.svc.getLowestBid(tenderId).subscribe({
      next: (bid) => { if (bid?.id) this.lowestBidMap[tenderId] = bid; },
      error: () => {}
    });
  }

  // ─── Gap Fix #1: ⏰ Close Expired Tenders ────────────────────────────────
  closeExpired(): void {
    if (!confirm('Close all expired Open Tenders?')) return;
    this.closeExpiredLoading = true;
    this.svc.closeExpiredTenders().subscribe({
      next: (res) => {
        this.closeExpiredLoading = false;
        this.showToast('success', res.message || `${res.closedCount} The tender has been closed.`);
        this.loadAll();
      },
      error: () => { this.closeExpiredLoading = false; this.showToast('error', 'The task could not be completed.'); }
    });
  }

  // ─── Gap Fix #2: 📄 Document Verification ────────────────────────────────
  openVerifyModal(bid: ETenderBid): void {
    this.verifyingBid    = bid;
    this.verifyDecision  = null;
    this.verifyRemark    = '';
    this.showVerifyModal = true;
  }

  closeVerifyModal(): void { this.showVerifyModal = false; this.verifyingBid = null; }

  submitVerify(): void {
    if (!this.verifyingBid?.id || this.verifyDecision === null) {
      this.showToast('error', 'Please select Verified or Rejected.');
      return;
    }
    this.verifyLoading = true;
    this.svc.verifyDocument(this.verifyingBid.id, this.verifyDecision, this.verifyRemark).subscribe({
      next: (res) => {
        this.verifyLoading = false;
        this.closeVerifyModal();
        this.showToast('success', res.message || 'Document verification completed.');
        this.loadAll();
      },
      error: (e) => {
        this.verifyLoading = false;
        this.showToast('error', e?.error?.message || 'Verification failed.');
      }
    });
  }

  docStatusLabel(bid: ETenderBid): string {
    if (bid.docVerified === null || bid.docVerified === undefined) return 'Pending';
    return bid.docVerified ? 'Verified' : 'Rejected';
  }

  docStatusClass(bid: ETenderBid): string {
    if (bid.docVerified === null || bid.docVerified === undefined) return 'status-pending';
    return bid.docVerified ? 'status-open' : 'status-closed';
  }

  // ─── Gap Fix #4: 🏆 Lowest bid helpers ───────────────────────────────────
  isLowest(bid: ETenderBid): boolean {
    return !!bid.isLowest;
  }

  getLowestForTender(tenderId: number): ETenderBid | null {
    return this.lowestBidMap[tenderId] || null;
  }

  // ─── Gap Fix #5: 🚫 Blacklist ─────────────────────────────────────────────
  openBlacklistModal(): void { this.newVendor = {}; this.showBlacklistModal = true; }
  closeBlacklistModal(): void { this.showBlacklistModal = false; }

  submitBlacklist(): void {
    if (!this.newVendor.nid && !this.newVendor.email && !this.newVendor.mobile) {
      this.showToast('error', 'NID, Email or Mobile — at least one field is required.');
      return;
    }
    if (!this.newVendor.reason?.trim()) {
      this.showToast('error', 'Please write the reason for blacklisting.');
      return;
    }
    this.blacklistLoading = true;
    this.svc.addBlacklist(this.newVendor).subscribe({
      next: () => {
        this.blacklistLoading = false;
        this.closeBlacklistModal();
        this.showToast('success', 'Vendor successfully blacklisted.');
        this.svc.getAllBlacklisted().subscribe(res => this.blacklist = res);
      },
      error: (e) => {
        this.blacklistLoading = false;
        this.showToast('error', e?.error?.message || 'Blacklist failed.');
      }
    });
  }

  unblockVendor(v: VendorBlacklist): void {
    if (!confirm(`${v.vendorName || 'This vendor'} to unblock?`)) return;
    this.svc.unblockVendor(v.id!).subscribe({
      next: () => {
        this.showToast('success', 'Vendor Unblock completed.');
        this.svc.getAllBlacklisted().subscribe(res => this.blacklist = res);
      },
      error: () => this.showToast('error', 'Unblock failed.')
    });
  }

  // ─── Notice CRUD ─────────────────────────────────────────────────────────
  emptyForm(): ETenderNotice {
    return { title: '', category: 'Road', estimatedCost: 0, emdAmount: 0, startDate: '', endDate: '' };
  }

  openCreate(): void { this.isEditing = false; this.editingId = null; this.form = this.emptyForm(); this.showForm = true; }

  editNotice(n: ETenderNotice): void {
    this.isEditing = true; this.editingId = n.id!;
    this.form = { ...n }; this.showForm = true;
  }

  saveNotice(): void {
    if (!this.form.title || !this.form.estimatedCost || !this.form.emdAmount || !this.form.startDate || !this.form.endDate) {
      this.showToast('error', 'Please fill in all required fields.'); return;
    }
    const call = this.isEditing
      ? this.svc.updateNotice(this.editingId!, this.form)
      : this.svc.createNotice(this.form);
    call.subscribe({
      next: () => { this.showForm = false; this.showToast('success', 'Tender saved successfully.'); this.loadAll(); },
      error: (err) => this.showToast('error', err?.error?.message || 'Failed to save tender.')
    });
  }

  closeNotice(id: number): void {
    if (!confirm('Are you sure you want to close this tender?')) return;
    this.svc.updateNoticeStatus(id, 'Closed').subscribe(() => { this.showToast('success', 'Tender closed successfully.'); this.loadAll(); });
  }

  deleteNotice(id: number): void {
    if (!confirm('Are you sure you want to delete this tender?')) return;
    this.svc.deleteNotice(id).subscribe(() => { this.showToast('success', 'Tender deleted successfully.'); this.loadAll(); });
  }

  // ─── Bid management ───────────────────────────────────────────────────────
  filterBids(): void {
    this.filteredBids = this.bidFilterTenderId
      ? this.bids.filter(b => b.tenderId === +this.bidFilterTenderId)
      : this.bids;
  }

  updateBidStatus(id: number, status: string): void {
    this.svc.updateBidStatus(id, status).subscribe(() => this.loadAll());
  }

  // ─── Award ────────────────────────────────────────────────────────────────
  openAwardModal(tenderId: number): void {
    this.awardingTenderId    = tenderId;
    this.awardingTenderTitle = this.notices.find(n => n.id === tenderId)?.title || '';
    this.awardBidOptions     = this.bids.filter(b => b.tenderId === tenderId && b.status !== 'Rejected' && b.status !== 'Selected');
    this.selectedBidId = 0; this.awardAmount = 0; this.awardRemarks = '';
    this.showAwardModal = true;
  }

  onAwardBidSelect(): void {
    const bid = this.awardBidOptions.find(b => b.id === +this.selectedBidId);
    if (bid) this.awardAmount = bid.bidAmount;
  }

  confirmAward(): void {
    if (!this.selectedBidId) { this.showToast('error', 'Please select a bid.'); return; }
    const payload: ETenderAward = {
      tenderId: this.awardingTenderId, bidId: +this.selectedBidId,
      awardedAmount: this.awardAmount, remarks: this.awardRemarks
    };
    this.svc.awardTender(payload).subscribe({
      next: () => { this.showToast('success', 'Tender Award completed!'); this.showAwardModal = false; this.loadAll(); },
      error: (err) => this.showToast('error', err?.error?.message || 'Award failed.')
    });
  }

  // ─── Deadline helpers (Gap Fix #1) ────────────────────────────────────────
  daysLeft(endDate?: string): number {
    if (!endDate) return 0;
    const diff = new Date(endDate).getTime() - new Date().setHours(0, 0, 0, 0);
    return Math.ceil(diff / 86400000);
  }

  deadlineLabel(endDate?: string): string {
    const d = this.daysLeft(endDate);
    if (d < 0)   return `${Math.abs(d)}d ago`;
    if (d === 0) return 'Today!';
    return `${d}d left`;
  }

  deadlinePillClass(endDate?: string): string {
    const d = this.daysLeft(endDate);
    if (d < 0)   return 'pill-expired';
    if (d <= 3)  return 'pill-urgent';
    if (d <= 7)  return 'pill-warn';
    return 'pill-safe';
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────
  statusClass(status: string): string {
    const m: any = {
      Open: 'status-open', Closed: 'status-closed', Awarded: 'status-awarded',
      Cancelled: 'status-cancelled', Submitted: 'status-pending',
      'Under Review': 'status-review', Selected: 'status-open', Rejected: 'status-closed'
    };
    return m[status] || 'status-pending';
  }

  fmt(v: number): string { return v != null ? '৳ ' + Number(v).toLocaleString('en-IN') : '—'; }

  getTenderTitle(tenderId: number): string {
    return this.notices.find(n => n.id === tenderId)?.title || '#' + tenderId;
  }

  getTenderNo(tenderId: number): string {
    return this.notices.find(n => n.id === tenderId)?.tenderNo || '#' + tenderId;
  }

  get openCount()    { return this.notices.filter(n => n.status === 'Open').length; }
  get awardedCount() { return this.notices.filter(n => n.status === 'Awarded').length; }
  get activeBlacklistCount() { return this.blacklist.filter(v => v.active).length; }

  // ─── Toast ────────────────────────────────────────────────────────────────
  showToast(type: 'success' | 'error', msg: string): void {
    this.toast = { type, msg };
    setTimeout(() => this.toast = null, 4000);
  }
}
