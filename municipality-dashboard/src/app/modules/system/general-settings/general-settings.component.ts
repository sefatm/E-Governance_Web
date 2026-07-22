import { Component, OnInit } from '@angular/core';
import { SystemSetting, AuditLog } from 'src/app/models/settings.model';
import { SettingsService } from 'src/app/services/settings.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success'|'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-general-settings',
  templateUrl: './general-settings.component.html',
  styleUrls: ['./general-settings.component.css']
})
export class GeneralSettingsComponent implements OnInit {

  settings:         SystemSetting[] = [];
  filteredSettings: SystemSetting[] = [];
  isLoading   = false;
  activeCategory = 'All';
  editingId:  number|null = null;
  editVal     = '';
  isSaving    = false;
  toasts: Toast[] = [];
  activeTab: 'settings'|'audit' = 'settings';
  categories  = ['All','General','Contact','System','Financial'];

  logs:         AuditLog[] = [];
  filteredLogs: AuditLog[] = [];
  isLoadingLogs = false;
  auditSearch   = '';
  auditModule   = '';
  auditModules: string[] = [];

  readonly seedSql = `INSERT INTO system_setting (setting_key, setting_val, label, category) VALUES
('municipality_name',    'Dhaka North City Corporation', 'Municipality Name',   'General'),
('municipality_address', 'Gulshan, Dhaka-1212',          'Office Address',      'General'),
('municipality_phone',   '02-9898989',                   'Contact Phone',       'Contact'),
('municipality_email',   'info@dncc.gov.bd',             'Contact Email',       'Contact'),
('municipality_website', 'www.dncc.gov.bd',              'Official Website',    'Contact'),
('holding_tax_rate',     '7',                            'Holding Tax Rate (%)', 'Financial'),
('water_rate_residential','0.15',                        'Water Rate - Residential (৳/L)', 'Financial'),
('water_rate_commercial', '0.30',                        'Water Rate - Commercial (৳/L)',  'Financial'),
('items_per_page',        '10',                          'Items Per Page',       'System'),
('default_language',      'English',                     'Default Language',     'System');`;

  constructor(public ls: LanguageService, private settingsService: SettingsService) {}

  ngOnInit(): void { this.loadSettings(); }

  switchTab(tab: 'settings'|'audit'): void {
    this.activeTab = tab;
    if (tab === 'audit' && this.logs.length === 0) this.loadLogs();
  }

  loadSettings(): void {
    this.isLoading = true;
    this.settingsService.getAllSettings().subscribe({
      next: r => {
        this.settings = r;
        this.filteredSettings = r;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  loadLogs(): void {
    this.isLoadingLogs = true;
    this.settingsService.getAllLogs().subscribe({
      next: r => {
        this.logs = r;
        this.filteredLogs = r;
        this.auditModules = [...new Set(r.map(l => l.module).filter(Boolean))];
        this.isLoadingLogs = false;
      },
      error: () => this.isLoadingLogs = false
    });
  }

  filterByCategory(cat: string): void {
    this.activeCategory = cat;
    this.filteredSettings = cat === 'All' ? this.settings
      : this.settings.filter(s => s.category === cat);
  }

  filterLogs(): void {
    const txt = this.auditSearch.toLowerCase();
    this.filteredLogs = this.logs.filter(l =>
      (!txt || l.username?.toLowerCase().includes(txt) || l.module?.toLowerCase().includes(txt) || l.action?.toLowerCase().includes(txt)) &&
      (!this.auditModule || l.module === this.auditModule)
    );
  }

  startEdit(s: SystemSetting): void { this.editingId = s.id!; this.editVal = s.settingVal; }
  cancelEdit(): void { this.editingId = null; this.editVal = ''; }

  saveEdit(s: SystemSetting): void {
    if (!this.editVal.trim()) { this.showToast('Value cannot be empty', 'error'); return; }
    this.isSaving = true;
    this.settingsService.updateSetting(s.id!, this.editVal).subscribe({
      next: res => {
        s.settingVal = res.settingVal;
        s.updatedAt  = res.updatedAt;
        this.editingId = null;
        this.isSaving  = false;
        this.showToast(`"${s.label}" updated successfully`, 'success');
      },
      error: err => {
        this.isSaving = false;
        this.showToast(err?.error?.message || 'Update failed', 'error');
      }
    });
  }

  clearLogs(): void {
    if (!confirm('Are you sure you want to clear all audit logs?')) return;
    this.settingsService.clearLogs().subscribe({
      next: () => { this.logs = []; this.filteredLogs = []; this.showToast('Audit logs cleared', 'success'); },
      error: () => this.showToast('Failed to clear logs', 'error')
    });
  }

  copySql(): void {
    navigator.clipboard.writeText(this.seedSql).then(() => this.showToast('SQL copied to clipboard!', 'success'));
  }

  showToast(message: string, type: 'success'|'error'): void {
    const t: Toast = { type, message };
    this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => this.toasts = this.toasts.filter(x => x !== t), 300); }, 4000);
  }
}
