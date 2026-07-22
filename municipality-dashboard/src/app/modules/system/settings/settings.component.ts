import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { AuthService } from 'src/app/services/auth.service';
import { SettingsService } from 'src/app/services/settings.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success'|'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {

  activeTab: 'password'|'preferences' = 'password';
  isSaving = false;
  toasts: Toast[] = [];
 
  // Password
  currentPassword = '';
  newPassword     = '';
  confirmPassword = '';
  showCurrent     = false;
  showNew         = false;
  showConfirm     = false;
 
  // Preferences
  language     = 'English';
  itemsPerPage = '10';
  emailNotif   = true;
  smsNotif     = false;
  pushNotif    = true;
 
  private readonly BASE = `${environment.apiUrl}`;
 
  constructor(public ls: LanguageService, 
    private http: HttpClient,
    private authService: AuthService,
    private settingsService: SettingsService
  ) {}
 
  ngOnInit(): void {}
 
  changePassword(): void {
    if (!this.currentPassword || !this.newPassword || !this.confirmPassword) {
      this.showToast('Please fill all password fields', 'error'); 
      return;
    }
    if (this.newPassword.length < 6) {
      this.showToast('New password must be at least 6 characters', 'error'); 
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.showToast('New password and confirm password do not match', 'error'); 
      return;
    }
 
    const user = this.authService.getCurrentUser();
    if (!user?.id) { this.showToast('User not found. Please login again.', 'error'); 
      return; }
 
    this.isSaving = true;
    this.http.put(`${this.BASE}/auth/change-password/${user.id}`, {
      currentPassword: this.currentPassword,
      password:        this.newPassword
    }).subscribe({
      next: () => {
        this.isSaving = false;
        this.showToast('Password changed successfully!', 'success');
        this.currentPassword = '';
        this.newPassword     = '';
        this.confirmPassword = '';
      },
      error: (err) => {
        this.isSaving = false;
        this.showToast(err?.error?.message || 'Password change failed. Check current password.', 'error');
      }
    });
  }
 
  savePreferences(): void {
    this.isSaving = true;
    const updates = [
      { key: 'default_language', value: this.language },
      { key: 'items_per_page',   value: this.itemsPerPage }
    ];
    let done = 0;
    updates.forEach(u => {
      this.settingsService.updateByKey(u.key, u.value).subscribe({
        next:  () => { if (++done === updates.length) { 
          this.isSaving = false; this.showToast('Preferences saved!', 'success'); }},

        error: () => { if (++done === updates.length) { 
          this.isSaving = false; this.showToast('Some preferences could not be saved.', 'error'); }}
      });
    });
  }
 
  get strength(): { label: string; cls: string; width: string } {
    const p = this.newPassword;
    if (!p) return { label: '', cls: '', width: '0%' };
    let s = 0;
    if (p.length >= 6)  s++;
    if (p.length >= 10) s++;
    if (/[A-Z]/.test(p))      s++;
    if (/[0-9]/.test(p))      s++;
    if (/[^a-zA-Z0-9]/.test(p)) s++;
    if (s <= 1) return { label: 'Weak',   cls: 's-weak',   width: '25%' };
    if (s <= 3) return { label: 'Medium', cls: 's-medium', width: '60%' };
    return             { label: 'Strong', cls: 's-strong', width: '100%' };
  }
 
  showToast(message: string, type: 'success'|'error'): void {
    const t: Toast = { type, message };
    this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => this.toasts = this.toasts.filter(x => x !== t), 300); }, 4000);
  }
}
