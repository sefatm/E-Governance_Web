import { Component, OnInit } from '@angular/core';
import { GarbageSchedule } from 'src/app/models/garbage-schedule.model';
import { ScheduleService } from 'src/app/services/Garbage-schedule.service';
import { LanguageService } from 'src/app/services/language.service';
import { AuthService } from 'src/app/services/auth.service';


@Component({
  selector: 'app-garbage-schedule',
  templateUrl: './garbage-schedule.component.html',
  styleUrls: ['./garbage-schedule.component.css']
})
export class GarbageScheduleComponent implements OnInit {

  submitted: boolean = false;

  schedules:         GarbageSchedule[] = [];
  filteredSchedules: GarbageSchedule[] = [];

  schedule: GarbageSchedule = { ward: '', area: '', day: '', time: '', status: 'Active' };

  selectedDay  = '';
  editIndex:   number | null = null;
  editId:      number | null = null;
  isSubmitting = false;
  successMsg   = '';
  errorMsg     = '';
  canManage    = false;

  constructor(public ls: LanguageService, private service: ScheduleService, private auth: AuthService) {}

  ngOnInit(): void {
    const role = (this.auth.getCurrentRole() || '').toLowerCase();
    this.canManage = role.includes('admin') || role.includes('department officer');
    this.loadSchedules();
  }

  loadSchedules(): void {
    this.service.getAll().subscribe({
      next: (res) => { this.schedules = res; this.filterData(); },
      error: (err) => console.error('Load failed:', err)
    });
  }

  filterData(): void {
    this.filteredSchedules = !this.selectedDay
      ? this.schedules
      : this.schedules.filter(s => s.day === this.selectedDay);
  }

  saveSchedule(): void {
    if (!this.canManage) return;
    if (!this.schedule.ward || !this.schedule.area || !this.schedule.day) {
      this.errorMsg = 'Please fill all required fields.';
      return;
    }

    this.isSubmitting = true;
    this.successMsg   = '';
    this.errorMsg     = '';

    if (this.editId === null) {
      this.schedule.status = 'Active';
      this.service.create(this.schedule).subscribe({
        next: () => {
          this.successMsg = 'Schedule added successfully!';
          this.isSubmitting = false;
          this.resetForm();
          this.loadSchedules();
          setTimeout(() => this.successMsg = '', 3500);
        },
        error: (err: any) => {
          this.isSubmitting = false;
          this.errorMsg = err?.error?.message || 'Failed to add schedule.';
        }
      });
    } else {
      this.service.update(this.editId, this.schedule).subscribe({
        next: () => {
          this.successMsg = 'Schedule updated successfully!';
          this.isSubmitting = false;
          this.resetForm();
          this.loadSchedules();
          setTimeout(() => this.successMsg = '', 3500);
        },
        error: (err: any) => {
          this.isSubmitting = false;
          this.errorMsg = err?.error?.message || 'Failed to update schedule.';
        }
      });
    }
  }

  editSchedule(index: number): void {
    if (!this.canManage) return;
    const s = this.filteredSchedules[index];
    this.schedule  = { ...s };
    this.editId    = s.id!;
    this.editIndex = index;
    this.successMsg = '';
    this.errorMsg   = '';
  }

  deleteSchedule(index: number): void {
    if (!this.canManage) return;
    const id = this.filteredSchedules[index].id!;
    if (!confirm('Delete this schedule?')) return;
    this.service.delete(id).subscribe({
      next: () => {
        this.successMsg = 'Schedule deleted.';
        this.loadSchedules();
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: (err) => {
        this.errorMsg = 'Failed to delete.';
        console.error(err);
      }
    });
  }

  toggleStatus(s: GarbageSchedule): void {
    if (!this.canManage) return;
    const newStatus = s.status === 'Active' ? 'Done' : 'Active';
    this.service.updateStatus(s.id!, newStatus).subscribe({
      next: () => { s.status = newStatus; },
      error: (err) => console.error('Status update failed:', err)
    });
  }

  resetForm(): void {
    this.schedule  = { ward: '', area: '', day: '', time: '', status: 'Active' };
    this.editId    = null;
    this.editIndex = null;
  }
}
