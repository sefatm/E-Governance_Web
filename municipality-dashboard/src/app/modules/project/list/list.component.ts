import { Component, OnInit } from '@angular/core';
import { ProjectListService } from 'src/app/services/project-list.service';
import { AuthService } from 'src/app/services/auth.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.css']
})
export class ListComponent implements OnInit {

  projects: any[] = [];
  form: any = {};
  editId: number | null = null;

  // ── Role ─────────────────────────────────────────────────
  role: string = '';

  get isCitizen(): boolean { return this.role === 'Citizen'; }
  get canManage(): boolean {
    return ['Super Admin','Admin / Municipal Officer',
            'Department Officer','Project Officer'].includes(this.role);
  }

  constructor(public ls: LanguageService, 
    private service: ProjectListService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.role = this.authService.getCurrentRole() || '';
    this.loadProjects();
  }

  loadProjects(): void {
    this.service.getAll().subscribe({
      next: (res: any[]) => { this.projects = res; },
      error: (err: any) => { console.error(err); }
    });
  }

  saveProject(): void {
    if (!this.canManage) return;
    if (!this.form.name || !this.form.location || !this.form.startDate || !this.form.endDate) {
      alert('Please fill all required fields');
      return;
    }
    if (this.editId === null) {
      this.form.status = 'Ongoing';
      this.service.create(this.form).subscribe({
        next: () => { alert('Project Added!'); this.loadProjects(); this.form = {}; },
        error: (err: any) => { console.error(err); }
      });
    } else {
      this.service.update(this.editId, this.form).subscribe({
        next: () => { alert('Project Updated!'); this.loadProjects(); this.form = {}; this.editId = null; },
        error: (err: any) => { console.error(err); }
      });
    }
  }

  getProgress(startDate: string, endDate: string): number {
    const start = new Date(startDate).getTime();
    const end   = new Date(endDate).getTime();
    const now   = new Date().getTime();
    return Math.min(100, Math.max(0, ((now - start) / (end - start)) * 100));
  }

  updateProgress(project: any, value: number): void {
    if (!this.canManage) return;
    project.progress = Number(value);
    this.service.updateProgress(project.id, project.progress).subscribe({
      next: () => {},
      error: (err: any) => { console.error(err); }
    });
  }

  editProject(project: any): void {
    if (!this.canManage) return;
    this.form   = { ...project };
    this.editId = project.id;
  }

  deleteProject(id: number): void {
    if (!this.canManage) return;
    if (!confirm('Are you sure?')) return;
    this.service.delete(id).subscribe({
      next: () => { alert('Deleted!'); this.loadProjects(); },
      error: (err: any) => { console.error(err); }
    });
  }

  updateStatus(project: any, status: string): void {
    if (!this.canManage) return;
    project.status = status;
    this.service.updateStatus(project.id, status).subscribe({
      next: () => {},
      error: (err: any) => { console.error(err); }
    });
  }
}
