import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {

    const token =
      localStorage.getItem('token') ||
      localStorage.getItem('accessToken') ||
      localStorage.getItem('authToken') ||
      localStorage.getItem('jwt');

    if (!token) {
      this.router.navigate(['/login']);
      return false;
    }

    const allowedRoles: string[] = route.data?.['roles'];
    if (!allowedRoles || allowedRoles.length === 0) {
      return true;
    }

    const userRole = this.getCurrentRole();
    if (!userRole) {
      this.router.navigate(['/login']);
      return false;
    }

    const current = this.normalizeRole(userRole);
    const allowed = allowedRoles.map(r => this.normalizeRole(r));

    const hasAccess = allowed.includes(current) ||
      (current === 'admin' && allowed.includes('adminmunicipalofficer')) ||
      (current === 'adminmunicipalofficer' && allowed.includes('admin')) ||
      (current === 'departmentofficer' && allowed.includes('deptofficer')) ||
      (current === 'deptofficer' && allowed.includes('departmentofficer'));

    if (!hasAccess) {
      console.warn(`[AuthGuard] Blocked — role: "${userRole}" normalized: "${current}" | required: [${allowedRoles.join(', ')}]`);
      this.router.navigate(['/access-denied']);
      return false;
    }

    return true;
  }

  private getCurrentRole(): string {
    const directRole =
      localStorage.getItem('role') ||
      localStorage.getItem('userRole') ||
      localStorage.getItem('authority') ||
      '';

    const userLikeKeys = ['currentUser', 'user', 'authUser'];
    for (const key of userLikeKeys) {
      const raw = localStorage.getItem(key);
      if (!raw) continue;

      try {
        const parsed = JSON.parse(raw);
        const role = parsed?.role || parsed?.user?.role || parsed?.authority || parsed?.authorities?.[0]?.authority || parsed?.authorities?.[0];
        if (role) return String(role);
      } catch {
        // ignore invalid JSON
      }
    }

    return directRole;
  }

  private normalizeRole(role: string): string {
    return String(role || '')
      .trim()
      .replace(/^ROLE[_\s-]*/i, '')
      .replace(/_/g, ' ')
      .replace(/\/+/g, ' ')
      .replace(/-/g, ' ')
      .replace(/[^a-zA-Z0-9]/g, '')
      .toLowerCase();
  }
}
