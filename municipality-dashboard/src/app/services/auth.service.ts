import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';

export interface AppUser {
  id:        number;
  name:      string;
  email:     string;
  role:      string;
  status?:   string;
  photoUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = environment.apiUrl;
  photoRefresh$  = new Subject<string>();
  userRefresh$   = new Subject<{name: string; email: string}>();
  constructor(private http: HttpClient, private router: Router) {}
  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/forgot-password`, { email });
  }

  register(name: string, email: string, password: string, role: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/register`, { name, email, password, role });
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/login`, { email, password });
  }

  saveSession(data: any): void {
    localStorage.setItem('token', data.token);
    localStorage.setItem('role', data.role);
    if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken);
    if (data.expiresIn)    localStorage.setItem('tokenExpiresIn', String(data.expiresIn));
    localStorage.setItem('currentUser', JSON.stringify({
      id:       data.id,
      name:     data.name,
      email:    data.email,
      role:     data.role,
      photoUrl: data.photoUrl || ''
    }));
  }

  updatePhotoInSession(photoUrl: string): void {
    const user = this.getCurrentUser();
    if (!user) return;
    const updated = { ...user, photoUrl };
    localStorage.setItem('currentUser', JSON.stringify(updated));
    this.photoRefresh$.next(photoUrl);
  }

  updateProfileInSession(name: string, email: string): void {
    const user = this.getCurrentUser();
    if (!user) return;
    const updated = { ...user, name, email };
    localStorage.setItem('currentUser', JSON.stringify(updated));
    this.userRefresh$.next({ name, email });
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('tokenExpiresIn');
    localStorage.removeItem('role');
    localStorage.removeItem('currentUser');
    sessionStorage.removeItem('voter_id');
    sessionStorage.removeItem('voter_name');
    sessionStorage.removeItem('vote_done_voter');
    sessionStorage.removeItem('vote_done_candidate');
    sessionStorage.removeItem('vote_done_party');
    this.router.navigate(['/login']);
  }

  getCurrentUser(): AppUser | null {
    const str = localStorage.getItem('currentUser');
    return str ? JSON.parse(str) : null;
  }

  getCurrentRole(): string {
    return this.getCurrentUser()?.role || '';
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  hasRole(...roles: string[]): boolean {
    return roles.includes(this.getCurrentRole());
  }

  getAllUsers(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${this.baseUrl}/users/getall`);
  }

  updateUserRole(id: number, role: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/users/update-role/${id}`, { role });
  }

  updateUserStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/users/update-status/${id}`, { status });
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/users/delete/${id}`);
  }

  validateResetToken(token: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/auth/reset-password/validate?token=${token}`);
  }

  resetPassword(token: string, password: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/reset-password`, { token, password });
  }
}
