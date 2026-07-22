import { Component, OnInit } from '@angular/core';
import { Election } from 'src/app/models/election.model';
import { ElectionService } from 'src/app/services/election.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-e-management',
  templateUrl: './e-management.component.html',
  styleUrls: ['./e-management.component.css']
})
export class EManagementComponent implements OnInit {

  elections:  Election[] = [];
  isEditMode  = false;
  isSaving    = false;
  submitted   = false;

  form: Election = this.emptyForm();

  toast: { type: 'success' | 'error'; msg: string } | null = null;

  constructor(public ls: LanguageService, private electionService: ElectionService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.electionService.getAll().subscribe({
      next: (res) => this.elections = res,
      error: ()    => this.showToast('error', 'The selection list was not loaded.')
    });
  }

  save(): void {
    this.submitted = true;
    if (!this.form.name || !this.form.type || !this.form.startDate || !this.form.endDate) return;

    this.isSaving = true;
    this.electionService.create(this.form).subscribe({
      next: () => {
        this.isSaving = false;
        this.showToast('success', 'Election saved successfully.');
        this.reset();
        this.load();
      },
      error: (err) => {
        this.isSaving = false;
        this.showToast('error', err.error?.message || 'Saving failed. Please try again.');
      }
    });
  }

  changeStatus(id: number, status: string): void {
    this.electionService.updateStatus(id, status).subscribe({
      next: () => { this.showToast('success', `Status → ${status}`); this.load(); },
      error: ()  => this.showToast('error', 'Status has not changed.')
    });
  }

  delete(id?: number): void {
    if (!id) return;
    if (!confirm('Are you sure you want to delete this election?')) return;
    this.electionService.delete(id).subscribe({
      next: () => { this.showToast('success', 'Election deleted successfully.'); this.load(); },
      error: (err) => this.showToast('error', err.error?.message || 'Delete failed. Please try again.')
    });
  }

  reset(): void {
    this.form      = this.emptyForm();
    this.isEditMode = false;
    this.submitted  = false;
  }

  showToast(type: 'success' | 'error', msg: string): void {
    this.toast = { type, msg };
    setTimeout(() => this.toast = null, 4000);
  }

  private emptyForm(): Election {
    return { name: '', type: '', area: '', startDate: '', endDate: '', status: 'UPCOMING', description: '' };
  }
}
