import { Component, OnInit } from '@angular/core';
import { VendorBlacklist, VendorBlacklistService } from 'src/app/services/vendor-blacklist.service';
import { LanguageService } from 'src/app/services/language.service';


@Component({
  selector: 'app-vendor-blacklist',
  templateUrl: './vendor-blacklist.component.html',
  styleUrls: ['./vendor-blacklist.component.css']
})
export class VendorBlacklistComponent implements OnInit {

  vendors: VendorBlacklist[] = [];
  filteredVendors: VendorBlacklist[] = [];

  // Filter
  filterActive: 'all' | 'active' | 'unblocked' = 'all';
  searchText = '';

  // Add form
  showAddModal = false;
  addLoading = false;
  newVendor: VendorBlacklist = {};

  // Confirm modal
  confirmAction: 'unblock' | 'delete' | null = null;
  confirmVendor: VendorBlacklist | null = null;
  confirmLoading = false;

  // Toast
  toast: { type: 'success' | 'error'; msg: string } | null = null;

  loading = false;

  constructor(public ls: LanguageService, private svc: VendorBlacklistService) {}

  ngOnInit(): void {
    this.loadAll();
  }

  // ─────────────────────────────────────────────────────────────
  // Load Data
  // ─────────────────────────────────────────────────────────────
  loadAll(): void {
    this.loading = true;

    this.svc.getAll().subscribe({
      next: (data: VendorBlacklist[]) => {
        this.vendors = data;
        this.applyFilter();
        this.loading = false;
      },

      error: (err: any) => {
        console.error(err);
        this.showToast('error', 'Data not loaded. Check the server.');
        this.loading = false;
      }
    });
  }

  // ─────────────────────────────────────────────────────────────
  // Filter & Search
  // ─────────────────────────────────────────────────────────────
  applyFilter(): void {

    let list = [...this.vendors];

    if (this.filterActive === 'active') {
      list = list.filter(v => v.active);
    }
    else if (this.filterActive === 'unblocked') {
      list = list.filter(v => !v.active);
    }

    const q = this.searchText.trim().toLowerCase();

    if (q) {
      list = list.filter(v =>
        (v.vendorName || '').toLowerCase().includes(q) ||
        (v.companyName || '').toLowerCase().includes(q) ||
        (v.nid || '').toLowerCase().includes(q) ||
        (v.email || '').toLowerCase().includes(q) ||
        (v.mobile || '').toLowerCase().includes(q)
      );
    }

    this.filteredVendors = list;
  }

  onSearch(): void {
    this.applyFilter();
  }

  onFilterChange(): void {
    this.applyFilter();
  }

  // ─────────────────────────────────────────────────────────────
  // Counts
  // ─────────────────────────────────────────────────────────────
  get totalCount(): number {
    return this.vendors.length;
  }

  get activeCount(): number {
    return this.vendors.filter(v => v.active).length;
  }

  get unblockedCount(): number {
    return this.vendors.filter(v => !v.active).length;
  }

  // ─────────────────────────────────────────────────────────────
  // Add Modal
  // ─────────────────────────────────────────────────────────────
  openAddModal(): void {
    this.newVendor = {};
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
    this.newVendor = {};
  }

  submitAdd(): void {

    const v = this.newVendor;

    if (!v.nid && !v.email && !v.mobile) {
      this.showToast(
        'error',
        'NID, Email or Mobile — at least one is required.'
      );
      return;
    }

    if (!v.reason || v.reason.trim() === '') {
      this.showToast(
        'error',
        'Blacklist reason is required.'
      );
      return;
    }

    this.addLoading = true;

    this.svc.add(v).subscribe({

      next: () => {

        this.addLoading = false;

        this.closeAddModal();

        this.showToast(
          'success',
          `${v.vendorName || 'Vendor'} successfully blacklisted.`
        );

        this.loadAll();
      },

      error: (err: any) => {

        console.error(err);

        this.addLoading = false;

        const msg =
          err?.error?.message || 'Failed to blacklist vendor.';

        this.showToast('error', msg);
      }
    });
  }

  // ─────────────────────────────────────────────────────────────
  // Confirm Action
  // ─────────────────────────────────────────────────────────────
  openConfirm(
    action: 'unblock' | 'delete',
    vendor: VendorBlacklist
  ): void {

    this.confirmAction = action;
    this.confirmVendor = vendor;
  }

  closeConfirm(): void {

    this.confirmAction = null;
    this.confirmVendor = null;
    this.confirmLoading = false;
  }

  doConfirm(): void {

    if (!this.confirmVendor?.id || !this.confirmAction) {
      return;
    }

    this.confirmLoading = true;

    const obs =
      this.confirmAction === 'unblock'
        ? this.svc.unblock(this.confirmVendor.id)
        : this.svc.delete(this.confirmVendor.id);

    obs.subscribe({

      next: () => {

        const msg =
          this.confirmAction === 'unblock'
            ? `${this.confirmVendor?.vendorName || 'Vendor'} successfully unblocked.`
            : 'Blacklist entry deleted.';

        this.showToast('success', msg);

        this.closeConfirm();

        this.loadAll();
      },

      error: (err: any) => {

        console.error(err);

        this.showToast(
          'error',
          'Failed to complete the action. Please try again.'
        );

        this.confirmLoading = false;
      }
    });
  }

  // ─────────────────────────────────────────────────────────────
  // Toast
  // ─────────────────────────────────────────────────────────────
  showToast(
    type: 'success' | 'error',
    msg: string
  ): void {

    this.toast = { type, msg };

    setTimeout(() => {
      this.toast = null;
    }, 4000);
  }
}