import { Component, OnInit } from '@angular/core';
import { WardService, Ward } from 'src/app/services/ward.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-ward-list',
  templateUrl: './ward-list.component.html',
  styleUrls: ['./ward-list.component.css']
})
export class WardListComponent implements OnInit {

  wards:       Ward[] = [];
  showForm     = false;
  editId:      number | null = null;
  searchText   = '';
  filterStatus = '';
  isSubmitting = false;
  successMsg   = '';
  errorMsg     = '';

  form: Ward = this.emptyForm();

  constructor(public ls: LanguageService, private wardService: WardService) {}

  ngOnInit(): void { this.loadWards(); }

  loadWards(): void {
    this.wardService.getAll().subscribe({
      next: (res) => { this.wards = res; },
      error: (err) => console.error('Load failed:', err)
    });
  }

  get filtered(): Ward[] {
    const s = this.searchText.toLowerCase();
    return this.wards.filter(w =>
      (!s || w.name.toLowerCase().includes(s) ||
             (w.representative || '').toLowerCase().includes(s)) &&
      (!this.filterStatus || w.status === this.filterStatus)
    );
  }

  get totalPopulation(): number { 
    return this.wards.reduce((s, w) => s + (w.population || 0), 0); }

  get activeCount(): number { 
    return this.wards.filter(w => w.status === 'Active').length; }

  openAdd(): void { this.form = this.emptyForm(); this.editId = null; this.showForm = true; this.successMsg = ''; this.errorMsg = ''; }

  openEdit(ward: Ward): void {
    this.form = { ...ward, area: ward.area ?? null };
    this.editId   = ward.id!;
    this.showForm = true;
    this.successMsg = ''; this.errorMsg = '';
  }

  save(): void {
    if (!this.form.name || !this.form.number) {
      this.errorMsg = 'Ward Name and Number are required.'; return;
    }
    this.isSubmitting = true; this.errorMsg = '';

    const payload: Ward = {
      ...this.form,
      number: Number(this.form.number),
      area: this.form.area === null || this.form.area === undefined ? null : Number(this.form.area),
      population: this.form.population === null || this.form.population === undefined ? 0 : Number(this.form.population)
    };

    const obs = this.editId === null
      ? this.wardService.create(payload)
      : this.wardService.update(this.editId, payload);

    obs.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMsg = this.editId === null ? 'Ward added successfully!' : 'Ward updated successfully!';
        this.showForm = false; this.editId = null;
        this.loadWards();
        setTimeout(() => this.successMsg = '', 4000);
      },
      error: (err: any) => {
        this.isSubmitting = false;
        this.errorMsg = err?.error?.message || 'Save failed.';
      }
    });
  }

  cancel(): void { this.showForm = false; this.editId = null; this.form = this.emptyForm(); this.errorMsg = ''; }

  delete(ward: Ward): void {
    if (!confirm(`Delete "${ward.name}"? This cannot be undone.`)) return;
    this.wardService.delete(ward.id!).subscribe({
      next: () => { this.successMsg = `${ward.name} deleted.`; this.loadWards(); setTimeout(() => this.successMsg = '', 3000); },
      error: (err) => { this.errorMsg = 'Delete failed.'; console.error(err); }
    });
  }

  toggleStatus(ward: Ward): void {
    const newStatus = ward.status === 'Active' ? 'Inactive' : 'Active';
    this.wardService.updateStatus(ward.id!, newStatus).subscribe({
      next: () => { ward.status = newStatus; },
      error: (err) => console.error(err)
    });
  }

  private emptyForm(): Ward {
    return { number: 0, name: '', area: null, population: 0, representative: '', contact: '', status: 'Active' };
  }
}
