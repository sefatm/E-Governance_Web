import { Component, OnInit } from '@angular/core';
import { ProjectBudget } from '../../../models/project-budget.model';
import { ProjectBudgetService } from 'src/app/services/project-budget.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-budget',
  templateUrl: './budget.component.html',
  styleUrls: ['./budget.component.css']
})
export class BudgetComponent implements OnInit {

  projects:  ProjectBudget[] = [];
  form:      any = this.emptyForm();
  editId:    number | null = null;

  submitted  = false;
  isSaving   = false;
  successMsg = '';
  errorMsg   = '';

  constructor(public ls: LanguageService, private service: ProjectBudgetService) {}

  ngOnInit(): void { this.loadData(); }

  loadData(): void {
    this.service.getAll().subscribe({
      next:  (res) => this.projects = res,
      error: () => { this.errorMsg = 'বাজেট লোড করতে সমস্যা।'; setTimeout(() => this.errorMsg = '', 3000); }
    });
  }

  saveProject(): void {
    this.submitted = true;
    if (!this.form.name) {
      this.errorMsg = 'প্রকল্পের নাম আবশ্যক।';
      setTimeout(() => this.errorMsg = '', 3000);
      return;
    }
    this.isSaving = true;

    const req$ = this.editId === null
      ? this.service.create(this.form)
      : this.service.update(this.editId, this.form);

    req$.subscribe({
      next: () => {
        this.isSaving  = false;
        this.submitted = false;
        this.successMsg = this.editId === null ? 'বাজেট যোগ করা হয়েছে।' : 'বাজেট আপডেট হয়েছে।';
        setTimeout(() => this.successMsg = '', 3000);
        this.cancelEdit();
        this.loadData();
      },
      error: () => {
        this.isSaving = false;
        this.errorMsg = 'সংরক্ষণ করতে সমস্যা হয়েছে।';
        setTimeout(() => this.errorMsg = '', 3000);
      }
    });
  }

  editProject(p: ProjectBudget): void {
    this.form      = { ...p };
    this.editId    = p.id!;
    this.submitted = false;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelEdit(): void {
    this.form      = this.emptyForm();
    this.editId    = null;
    this.submitted = false;
  }

  deleteProject(id?: number): void {
    if (!id || !confirm('এই বাজেট এন্ট্রিটি মুছে ফেলবেন?')) return;
    this.service.delete(id).subscribe({
      next: () => {
        this.successMsg = 'বাজেট মুছে ফেলা হয়েছে।';
        setTimeout(() => this.successMsg = '', 3000);
        this.loadData();
      },
      error: () => { this.errorMsg = 'মুছতে সমস্যা হয়েছে।'; setTimeout(() => this.errorMsg = '', 3000); }
    });
  }

  calculateRemaining(p: any): number {
    return (p.budget || 0) - (p.expense || 0);
  }

  getUsagePct(p: any): number {
    if (!p.budget || p.budget === 0) return 0;
    return Math.min(100, Math.round((p.expense / p.budget) * 100));
  }

  getTotalBudget():    number { return this.projects.reduce((s, p) => s + (p.budget  || 0), 0); }
  getTotalExpense():   number { return this.projects.reduce((s, p) => s + (p.expense || 0), 0); }
  getTotalRemaining(): number { return this.getTotalBudget() - this.getTotalExpense(); }
  getAlertCount():     number { return this.projects.filter(p => this.calculateRemaining(p) < 10000).length; }

  emptyForm(): any {
    return { name: '', budget: 0, expense: 0 };
  }
}
