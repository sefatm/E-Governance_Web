import { Component, OnInit } from '@angular/core';
import { Sanitation } from 'src/app/models/sanitation.model';
import { SanitationService } from 'src/app/services/sanitation.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-sanitation-monitoring',
  templateUrl: './sanitation-monitoring.component.html',
  styleUrls: ['./sanitation-monitoring.component.css']
})
export class SanitationMonitoringComponent implements OnInit {

  records: Sanitation[] = [];
  isSubmitting = false;
  submitted    = false;
  successMsg   = '';
  errorMsg     = '';

  form: Sanitation = this.emptyForm();

  constructor(public ls: LanguageService, private service: SanitationService) {}

  ngOnInit(): void { this.loadRecords(); }

  loadRecords(): void {
    this.service.getAll().subscribe({
      next:  (res) => this.records = res,
      error: ()    => this.errorMsg = 'রেকর্ড লোড করতে সমস্যা হয়েছে।'
    });
  }

  addRecord(): void {
    this.submitted = true;
    if (!this.form.area || !this.form.issue) {
      this.errorMsg = 'এলাকা ও সমস্যার ধরন আবশ্যক।';
      setTimeout(() => this.errorMsg = '', 3000);
      return;
    }
    if (this.form.email && !this.isValidEmail(this.form.email)) {
      this.errorMsg = 'সঠিক ইমেইল ঠিকানা দিন।';
      setTimeout(() => this.errorMsg = '', 3000);
      return;
    }
    this.isSubmitting = true;
    this.successMsg   = '';
    this.errorMsg     = '';

    this.service.create(this.form).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.submitted    = false;
        this.successMsg   = 'অভিযোগ সফলভাবে দাখিল হয়েছে!';
        this.loadRecords();
        this.resetForm();
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: () => {
        this.isSubmitting = false;
        this.errorMsg = 'দাখিল করতে সমস্যা হয়েছে।';
        setTimeout(() => this.errorMsg = '', 3000);
      }
    });
  }

  updateStatus(record: Sanitation, status: string): void {
    if (!record.id) return;
    this.service.updateStatus(record.id, status).subscribe({
      next:  () => record.status = status,
      error: () => { this.errorMsg = 'স্ট্যাটাস পরিবর্তন করা যায়নি।'; setTimeout(() => this.errorMsg = '', 3000); }
    });
  }

  deleteRecord(id?: number): void {
    if (!id || !confirm('এই রেকর্ডটি মুছে ফেলবেন?')) return;
    this.service.delete(id).subscribe({
      next: () => {
        this.records  = this.records.filter(r => r.id !== id);
        this.successMsg = 'রেকর্ড মুছে ফেলা হয়েছে।';
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: () => { this.errorMsg = 'মুছতে সমস্যা হয়েছে।'; setTimeout(() => this.errorMsg = '', 3000); }
    });
  }

  isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  resetForm(): void { this.form = this.emptyForm(); }

  emptyForm(): Sanitation {
    return { name: '', email: '', area: '', issue: '', description: '', status: 'Pending' };
  }
}
