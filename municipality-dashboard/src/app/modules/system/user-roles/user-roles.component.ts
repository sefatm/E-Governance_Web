import { environment } from 'src/environments/environment';
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from 'src/app/services/auth.service';
import { LanguageService } from 'src/app/services/language.service';

interface AppUser {
  id: number;
  name: string;
  email: string;
  role: string;
  status: string;
  createdAt?: string;
}

@Component({
  selector: 'app-user-roles',
  templateUrl: './user-roles.component.html',
  styleUrls: ['./user-roles.component.css']
})
export class UserRolesComponent implements OnInit {

  users:         AppUser[] = [];
  filteredUsers: AppUser[] = [];
  pendingUsers:  AppUser[] = [];

  isLoading = false;
  isSaving  = false;

  activeTab: 'pending' | 'all' = 'pending';

  searchText   = '';
  filterRole   = '';
  filterStatus = '';

  roles = [
    'Super Admin',
    'Admin / Municipal Officer',
    'Department Officer',
    'Project Officer',
    'Health / Sanitation Officer',
    'Auditor / Accountant',
    'Citizen'
  ];

  private base = `${environment.apiUrl}`;

  constructor(public ls: LanguageService, 
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit(): void { this.loadUsers(); }

  // ── Computed counts ──────────────────────────────────────
  get activeCount():   number { return this.users.filter(u => u.status === 'Active').length;   }
  get inactiveCount(): number { return this.users.filter(u => u.status === 'Inactive').length; }

  // ── Load all users ───────────────────────────────────────
  loadUsers(): void {
    this.isLoading = true;
    this.http.get<AppUser[]>(`${this.base}/users/getall`).subscribe({
      next: (res) => {
        this.users         = res;
        this.pendingUsers  = res.filter(u => u.status === 'Pending');
        this.filteredUsers = [...res];
        this.isLoading     = false;
        this.filterData();
        // Auto-switch to all tab if no pending
        if (this.pendingUsers.length === 0) this.activeTab = 'all';
      },
      error: (err) => { console.error('Failed to load users:', err); this.isLoading = false; }
    });
  }

  switchTab(tab: 'pending' | 'all'): void {
    this.activeTab = tab;
    this.filterData();
  }

  // ── Filter ───────────────────────────────────────────────
  filterData(): void {
    const s = this.searchText.toLowerCase();
    this.filteredUsers = this.users.filter(u => {
      const matchRole   = !this.filterRole   || u.role   === this.filterRole;
      const matchStatus = !this.filterStatus || u.status === this.filterStatus;
      const matchSearch = !s || u.name?.toLowerCase().includes(s) || u.email?.toLowerCase().includes(s);
      return matchRole && matchStatus && matchSearch;
    });
  }

  clearFilter(): void {
    this.searchText   = '';
    this.filterRole   = '';
    this.filterStatus = '';
    this.filteredUsers = [...this.users];
  }

  // ── Approve ──────────────────────────────────────────────
  approveUser(user: AppUser, selectedRole: string): void {
    const role = selectedRole || user.role;
    this.http.put(`${this.base}/users/update-role/${user.id}`, { role }).subscribe({
      next: () => {
        this.http.put(`${this.base}/users/update-status/${user.id}`, { status: 'Active' }).subscribe({
          next: () => {
            user.role   = role;
            user.status = 'Active';
            this.pendingUsers = this.pendingUsers.filter(u => u.id !== user.id);
            this.filterData();
            this.showToast(`✅  ${user.name} approved as ${role}`);
          },
          error: (err) => { console.error(err); alert('Failed to update status.'); }
        });
      },
      error: (err) => { console.error(err); alert('Failed to update role.'); }
    });
  }

  // ── Reject ───────────────────────────────────────────────
  rejectUser(user: AppUser): void {
    if (!confirm(`Reject and deactivate "${user.name}"?`)) return;
    this.http.put(`${this.base}/users/update-status/${user.id}`, { status: 'Inactive' }).subscribe({
      next: () => {
        user.status = 'Inactive';
        this.pendingUsers = this.pendingUsers.filter(u => u.id !== user.id);
        this.filterData();
        this.showToast(`❌  ${user.name} rejected`);
      },
      error: (err) => { console.error(err); alert('Failed to reject user.'); }
    });
  }

  // ── Update role (existing users) ─────────────────────────
  updateRole(user: AppUser, newRole: string): void {
    if (!newRole || newRole === user.role) {
      this.showToast('ℹ️  No role change detected'); return;
    }
    this.isSaving = true;
    this.http.put(`${this.base}/users/update-role/${user.id}`, { role: newRole }).subscribe({
      next: () => {
        user.role     = newRole;
        this.isSaving = false;
        this.filterData();
        this.showToast(`✅  ${user.name}'s role → "${newRole}"`);
      },
      error: (err) => {
        console.error(err);
        this.isSaving = false;
        alert(err.error?.message || 'Failed to update role.');
      }
    });
  }

  // ── Toggle Active / Inactive ─────────────────────────────
  toggleStatus(user: AppUser): void {
    if (user.status === 'Pending') return;
    const newStatus = user.status === 'Active' ? 'Inactive' : 'Active';
    this.http.put(`${this.base}/users/update-status/${user.id}`, { status: newStatus }).subscribe({
      next: () => {
        user.status = newStatus;
        this.filterData();
        const icon = newStatus === 'Active' ? '✅' : '🔒';
        this.showToast(`${icon}  ${user.name} → ${newStatus}`);
      },
      error: (err) => alert(err.error?.message || 'Failed to update status.')
    });
  }

  // ── Delete ───────────────────────────────────────────────
  deleteUser(userId: number): void {
    if (!confirm('Delete this user permanently? This cannot be undone.')) return;
    this.http.delete(`${this.base}/users/delete/${userId}`).subscribe({
      next: () => { this.loadUsers(); this.showToast('🗑  User deleted'); },
      error: (err) => alert(err.error?.message || 'Failed to delete user.')
    });
  }

  // ── Helpers ──────────────────────────────────────────────
  getCurrentUserId(): number {
    return this.authService.getCurrentUser()?.id || -1;
  }

  /** CSS class for role badge */
  roleClass(role: string): string {
    const map: Record<string, string> = {
      'Super Admin':                 'role-super',
      'Admin / Municipal Officer':   'role-admin',
      'Department Officer':          'role-dept',
      'Project Officer':             'role-proj',
      'Health / Sanitation Officer': 'role-health',
      'Auditor / Accountant':        'role-audit',
      'Citizen':                     'role-citizen',
    };
    return map[role] || '';
  }

  /** CSS class for avatar background */
  avatarClass(role: string): string {
    const map: Record<string, string> = {
      'Super Admin':                 'av-super',
      'Admin / Municipal Officer':   'av-admin',
      'Department Officer':          'av-dept',
      'Project Officer':             'av-proj',
      'Health / Sanitation Officer': 'av-health',
      'Auditor / Accountant':        'av-audit',
      'Citizen':                     'av-citizen',
    };
    return map[role] || 'av-default';
  }

  // ── Toast notification ───────────────────────────────────
  showToast(msg: string): void {
    const el = document.createElement('div');
    el.className = 'ur-toast';
    el.textContent = msg;
    document.body.appendChild(el);
    setTimeout(() => el.classList.add('show'), 10);
    setTimeout(() => {
      el.classList.remove('show');
      setTimeout(() => el.remove(), 300);
    }, 2800);
  }
}
