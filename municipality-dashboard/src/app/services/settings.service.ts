import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SystemSetting, AuditLog } from '../models/settings.model';

@Injectable({ providedIn: 'root' })
export class SettingsService {

  private settingsBase  = environment.apiUrl + '/settings';
  private auditBase     = environment.apiUrl + '/auditlog';

  constructor(private http: HttpClient) {}

  // ── System Settings 
  getAllSettings(): Observable<SystemSetting[]> {
    return this.http.get<SystemSetting[]>(`${this.settingsBase}/getall`);
  }

  getByCategory(category: string): Observable<SystemSetting[]> {
    return this.http.get<SystemSetting[]>(`${this.settingsBase}/category/${category}`);
  }

  updateSetting(id: number, value: string): Observable<SystemSetting> {
    return this.http.put<SystemSetting>(`${this.settingsBase}/update/${id}`, { value });
  }

  updateByKey(key: string, value: string): Observable<SystemSetting> {
    return this.http.put<SystemSetting>(`${this.settingsBase}/updatekey`, { key, value });
  }

  // ── Audit Logs 
  getAllLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.auditBase}/getall`);
  }

  getLogsByUser(username: string): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.auditBase}/user/${username}`);
  }

  getLogsByModule(module: string): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.auditBase}/module/${module}`);
  }

  clearLogs(): Observable<any> {
    return this.http.delete(`${this.auditBase}/clear`);
  }
}
