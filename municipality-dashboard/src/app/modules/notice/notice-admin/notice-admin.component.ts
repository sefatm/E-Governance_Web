import { Component, OnInit } from '@angular/core';
import { Notice } from 'src/app/models/notice.model';
import { NoticeService } from 'src/app/services/notice.service';
import { LanguageService } from 'src/app/services/language.service';


@Component({
  selector: 'app-notice-admin',
  templateUrl: './notice-admin.component.html',
  styleUrls: ['./notice-admin.component.css']
})
export class NoticeAdminComponent implements OnInit {

  notices       : Notice[] = [];
  filteredNotices: Notice[] = [];
  isLoading     = false;
  isSubmitting  = false;
  isEditMode    = false;
  editId        : number | null = null;
  activeFilter  = 'All';
  selectedNotice: Notice | null = null;
  showModal     = false;

  typeOptions     = ['Public', 'Emergency', 'Event', 'News'];
  priorityOptions = ['High', 'Medium', 'Low'];
  statusOptions   = ['Active', 'Inactive', 'Expired'];
  filterOptions   = ['All', 'Public', 'Emergency', 'Event', 'News', 'Active', 'Inactive', 'Expired'];

  form: Notice = this.emptyForm();

  constructor(public ls: LanguageService, private noticeService: NoticeService) {}

  ngOnInit(): void { this.loadAll(); }


  loadAll(): void {
    this.isLoading = true;
    this.noticeService.getAll().subscribe({
      next: (res) => {
        this.notices         = res;
        this.filteredNotices = res;
        this.isLoading       = false;
      },
      error: (err) => { console.error(err); this.isLoading = false; }
    });
  }


  applyFilter(f: string): void {
    this.activeFilter = f;
    if (f === 'All') {
      this.filteredNotices = this.notices;
    } else if (['Active', 'Inactive', 'Expired'].includes(f)) {
      this.filteredNotices = this.notices.filter(n => n.status === f);
    } else {
      this.filteredNotices = this.notices.filter(n => n.type === f);
    }
  }


  save(): void {
    if (!this.form.title.trim() || !this.form.type || !this.form.description.trim()) {
      alert('অনুগ্রহ করে সব বাধ্যতামূলক ফিল্ড পূরণ করুন।');
      return;
    }
    this.isSubmitting = true;

    const payload: Notice = {
      ...this.form,
      title: this.form.title.trim(),
      description: this.form.description.trim(),
      publishDate: this.form.publishDate || new Date().toISOString().slice(0, 10),
      expiryDate: this.form.expiryDate || undefined,
      attachmentUrl: this.form.attachmentUrl?.trim() || undefined
    };

    const req$ = this.isEditMode && this.editId !== null
      ? this.noticeService.update(this.editId, payload)
      : this.noticeService.create(payload);

    req$.subscribe({
      next: () => {
        alert(this.isEditMode ? 'নোটিশ আপডেট হয়েছে!' : 'নোটিশ তৈরি হয়েছে!');
        this.resetForm();
        this.loadAll();
        this.isSubmitting = false;
      },
      error: (err) => {
        alert(err?.error?.message || 'একটি সমস্যা হয়েছে।');
        this.isSubmitting = false;
      }
    });
  }


  edit(notice: Notice): void {
    this.isEditMode = true;
    this.editId     = notice.id!;
    this.form       = { ...notice };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }


  toggleStatus(notice: Notice): void {
    const next = notice.status === 'Active' ? 'Inactive' : 'Active';
    this.noticeService.updateStatus(notice.id!, next).subscribe({
      next: () => { notice.status = next; },
      error: (err) => console.error(err)
    });
  }


  delete(id: number): void {
    if (!confirm('এই নোটিশটি মুছে ফেলবেন?')) return;
    this.noticeService.delete(id).subscribe({
      next: () => this.loadAll(),
      error: (err) => console.error(err)
    });
  }


  viewDetails(notice: Notice): void {
    this.selectedNotice = notice;
    this.showModal      = true;
  }

  closeModal(): void {
    this.showModal      = false;
    this.selectedNotice = null;
  }

  resetForm(): void {
    this.form       = this.emptyForm();
    this.isEditMode = false;
    this.editId     = null;
  }

  emptyForm(): Notice {
    return {
      type: 'Public', title: '', description: '',
      publishDate: new Date().toISOString().slice(0, 10),
      expiryDate: '', status: 'Active', priority: 'Medium',
      attachmentUrl: '', createdBy: 'Admin'
    };
  }

  typeIcon(type: string): string {
    const map: any = {
      Public: 'fas fa-globe',
      Emergency: 'fas fa-exclamation-triangle',
      Event: 'fas fa-calendar-alt',
      News: 'fas fa-newspaper'
    };
    return map[type] || 'fas fa-bell';
  }

  priorityClass(priority: string): string {
    const map: any = { High: 'badge-high', Medium: 'badge-medium', Low: 'badge-low' };
    return map[priority] || 'badge-medium';
  }

  statusClass(status: string): string {
    const map: any = { Active: 'badge-active', Inactive: 'badge-inactive', Expired: 'badge-expired' };
    return map[status] || 'badge-inactive';
  }

  get totalCount() : number { 
    return this.notices.length; }

  get activeCount() : number { 
    return this.notices.filter(n => n.status === 'Active').length; }

  get emergencyCount(): number { 
    return this.notices.filter(n => n.type === 'Emergency').length; }
}
