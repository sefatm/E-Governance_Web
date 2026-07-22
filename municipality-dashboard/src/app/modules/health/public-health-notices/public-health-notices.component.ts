import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-public-health-notices',
  templateUrl: './public-health-notices.component.html',
  styleUrls: ['./public-health-notices.component.css']
})
export class PublicHealthNoticesComponent implements OnInit {

  notices: any[]   = [];
  isEditMode       = false;
  submitted        = false;
  isSaving         = false;
  successMsg       = '';
  errorMsg         = '';

  form: any = this.emptyForm();

  constructor(public ls: LanguageService, private http: HttpClient) {}

  ngOnInit(): void { this.loadNotices(); }

  loadNotices(): void {
    this.http.get(`${environment.apiUrl}/health-notice/getall`).subscribe({
      next:  (res: any) => this.notices = res,
      error: () => { this.errorMsg = 'বিজ্ঞপ্তি লোড করতে সমস্যা।'; setTimeout(() => this.errorMsg = '', 3000); }
    });
  }

  saveNotice(): void {
    this.submitted = true;
    if (!this.form.title || !this.form.date) {
      this.errorMsg = 'শিরোনাম ও তারিখ আবশ্যক।';
      setTimeout(() => this.errorMsg = '', 3000);
      return;
    }
    this.isSaving = true;
    const payload = { title: this.form.title, description: this.form.description, date: this.form.date, status: 'Active' };

    const req$ = this.isEditMode
      ? this.http.put(`${environment.apiUrl}/health-notice/update/${this.form.id}`, payload)
      : this.http.post(`${environment.apiUrl}/health-notice/create`, payload);

    req$.subscribe({
      next: () => {
        this.isSaving  = false;
        this.submitted = false;
        this.successMsg = this.isEditMode ? 'বিজ্ঞপ্তি আপডেট হয়েছে।' : 'বিজ্ঞপ্তি যোগ করা হয়েছে।';
        setTimeout(() => this.successMsg = '', 3000);
        this.resetForm();
        this.loadNotices();
      },
      error: () => {
        this.isSaving = false;
        this.errorMsg = 'সংরক্ষণ করতে সমস্যা হয়েছে।';
        setTimeout(() => this.errorMsg = '', 3000);
      }
    });
  }

  editNotice(notice: any): void {
    this.form       = { ...notice };
    this.isEditMode = true;
    this.submitted  = false;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  deleteNotice(id: number): void {
    if (!confirm('এই বিজ্ঞপ্তিটি মুছে ফেলবেন?')) return;
    this.http.delete(`${environment.apiUrl}/health-notice/${id}`).subscribe({
      next: () => {
        this.successMsg = 'বিজ্ঞপ্তি মুছে ফেলা হয়েছে।';
        setTimeout(() => this.successMsg = '', 3000);
        this.loadNotices();
      },
      error: () => { this.errorMsg = 'মুছতে সমস্যা হয়েছে।'; setTimeout(() => this.errorMsg = '', 3000); }
    });
  }

  resetForm(): void {
    this.form       = this.emptyForm();
    this.isEditMode = false;
    this.submitted  = false;
  }

  emptyForm(): any {
    return { id: null, title: '', description: '', date: '' };
  }
}
