import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { LanguageService } from 'src/app/services/language.service';

interface AppUser {
  id       : number;
  name     : string;
  email    : string;
  role     : string;
  status   : string;
  createdAt?: any;
}

@Component({
  selector: 'app-user-approval',
  templateUrl: './user-approval.component.html',
  styleUrls: ['./user-approval.component.css']
})
export class UserApprovalComponent implements OnInit {

  allUsers     : AppUser[] = [];
  filteredUsers: AppUser[] = [];
  isLoading    = false;

  activeTab    = 'pending';
  searchText   = '';
  filterRole   = '';
  filterStatus = '';

  roles = [
    'Citizen', 'Admin / Municipal Officer', 'Department Officer',
    'Project Officer', 'Auditor / Accountant',
    'Health / Sanitation Officer', 'Super Admin'
  ];

  private base = `${environment.apiUrl}`;

  constructor(public ls: LanguageService, private http: HttpClient) {}

  ngOnInit(): void { this.loadAll(); }

  loadAll(): void {
    this.isLoading = true;
    this.http.get<AppUser[]>(`${this.base}/users/getall`).subscribe({
      next: (res) => {
        this.allUsers = res;
        this.applyFilter();
        this.isLoading = false;
      },
      error: (err) => { console.error(err); this.isLoading = false; }
    });
  }

  setTab(tab: string): void {
    this.activeTab = tab;
    this.searchText = '';
    this.filterRole = '';
    this.filterStatus = '';
    this.applyFilter();
  }

  applyFilter(): void {
    const base = this.activeTab === 'pending'
      ? this.allUsers.filter(u => u.status?.toLowerCase() === 'pending')
      : this.allUsers;

    const txt = this.searchText.toLowerCase();
    this.filteredUsers = base.filter(u => {
      const matchSearch = !txt ||
        (u.name  || '').toLowerCase().includes(txt) ||
        (u.email || '').toLowerCase().includes(txt);
      const matchRole   = !this.filterRole   || u.role   === this.filterRole;
      const matchStatus = !this.filterStatus || u.status === this.filterStatus;
      return matchSearch && matchRole && matchStatus;
    });
  }

  resetFilter(): void {
    this.searchText  = '';
    this.filterRole  = '';
    this.filterStatus = '';
    this.applyFilter();
  }

  approve(user: AppUser): void {
    this.http.put(`${this.base}/users/update-status/${user.id}`, { status: 'Active' }).subscribe({
      next: () => { user.status = 'Active'; this.applyFilter(); },
      error: (err) => alert(err.error?.message || 'Failed to approve.')
    });
  }

  reject(user: AppUser): void {
    if (!confirm(`Reject "${user.name}"? They will not be able to login.`)) return;
    this.http.put(`${this.base}/users/update-status/${user.id}`, { status: 'Inactive' }).subscribe({
      next: () => { user.status = 'Inactive'; this.applyFilter(); },
      error: (err) => alert(err.error?.message || 'Failed to reject.')
    });
  }

  deactivate(user: AppUser): void {
    if (!confirm(`Deactivate "${user.name}"?`)) return;
    this.http.put(`${this.base}/users/update-status/${user.id}`, { status: 'Inactive' }).subscribe({
      next: () => { user.status = 'Inactive'; this.applyFilter(); },
      error: (err) => alert(err.error?.message || 'Failed to deactivate.')
    });
  }

  reactivate(user: AppUser): void {
    this.http.put(`${this.base}/users/update-status/${user.id}`, { status: 'Active' }).subscribe({
      next: () => { user.status = 'Active'; this.applyFilter(); },
      error: (err) => alert(err.error?.message || 'Failed to reactivate.')
    });
  }

  updateRole(user: AppUser, newRole: string): void {
    if (!newRole || newRole === user.role) return;
    this.http.put(`${this.base}/users/update-role/${user.id}`, { role: newRole }).subscribe({
      next: () => { user.role = newRole; },
      error: (err) => alert(err.error?.message || 'Failed to update role.')
    });
  }

  // Helpers
  statusClass(status: string): string {
    const s = (status || '').toLowerCase();
    if (s === 'active')   return 'badge-active';
    if (s === 'inactive') return 'badge-inactive';
    if (s === 'pending')  return 'badge-pending';
    return 'badge-pending';
  }

  roleClass(role: string): string {
    const map: any = {
      'Super Admin'                : 'role-super',
      'Admin / Municipal Officer'  : 'role-admin',
      'Department Officer'         : 'role-dept',
      'Project Officer'            : 'role-proj',
      'Health / Sanitation Officer': 'role-health',
      'Auditor / Accountant'       : 'role-audit',
      'Citizen'                    : 'role-citizen',
    };
    return map[role] || 'role-citizen';
  }

  get pendingCount(): number { return this.allUsers.filter(u => u.status?.toLowerCase() === 'pending').length; }
  get activeCount() : number { return this.allUsers.filter(u => u.status?.toLowerCase() === 'active').length; }
  get totalCount()  : number { return this.allUsers.length; }
}
