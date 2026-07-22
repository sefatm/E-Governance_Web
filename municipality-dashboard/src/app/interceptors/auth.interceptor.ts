import { Injectable } from '@angular/core';
import {
  HttpRequest, HttpHandler, HttpEvent, HttpInterceptor,
  HttpErrorResponse, HttpClient
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap, finalize } from 'rxjs/operators';
import { Router, NavigationStart } from '@angular/router';
import { Injector } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  private isRefreshing    = false;
  private refreshSubject  = new BehaviorSubject<string | null>(null);

  // URLs যেগুলো থেকে 403 এলে access-denied navigate করব না
  // (dashboard background call — catchError দিয়ে handle হবে)
  // এই patterns এ 403 আসলে navigate করব না — component নিজে handle করবে
  private readonly SILENT_403_PATTERNS = [
    // Dashboard data calls
    '/api/etender/bid/getall',
    '/api/etender/notice/',
    '/api/tradeLicense/getall',
    '/api/complaints/getall',
    '/api/complaint/',
    '/api/tax-assessment/getall',
    '/api/family-card/getall',
    '/api/farmer-card/getall',
    '/api/lpg-card/getall',
    '/api/vgd-card/getall',
    '/api/vgd/card/getall',
    '/api/report/',
    '/api/citizen/getall',
    // E-Voting
    '/api/voter/',
    '/api/nominee/',
    '/api/election/',
    '/api/vote/',
    '/api/audit/',
    '/api/auditlog/',
    // Map / GIS
    '/api/map/',
    '/api/gis/',
    '/api/road/',
    '/api/construction/',
    '/api/drainage/',
    '/api/street-light/',
    // Other data APIs — all GET calls
    '/api/ward/',
    '/api/zone-center/',
    '/api/notice/',
    '/api/water-connection/',
    '/api/water-bill/',
    '/api/garbage-schedule/',
    '/api/smart-bin/',
    '/api/waste-request/',
    '/api/waste-collection-log/',
    '/api/payment/',
    '/api/notification/',
    '/api/feedback/',
    '/api/holding-new-registration/',
    '/api/ownership-transfer/',
    '/api/birth-death/',
    '/api/epi/',
    '/api/health-notice/',
    '/api/health-center/',
    '/api/sanitation/',
    '/api/project-list/',
    '/api/project-budget/',
    '/api/tcb/',
    '/api/farmer/',
    '/api/vgd/',
    '/api/lpg-card/',
    // Profile photo upload & auth actions
    '/api/auth/profile/',
    '/api/auth/upload-photo',
    '/api/auth/change-password',
  ];

  constructor(private router: Router, private injector: Injector) {}

  private get http(): HttpClient {
    return this.injector.get(HttpClient);
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    // Login / refresh bypass
    if (req.url.includes('/api/auth/refresh') || req.url.includes('/api/auth/login')) {
      return next.handle(req);
    }

    const token   = localStorage.getItem('token');
    const authReq = token ? this.addToken(req, token) : req;

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {

        // ── 401 handling ────────────────────────────────────────────────
        if (error.status === 401) {
          const code            = error.error?.code;
          const hasRefreshToken = !!localStorage.getItem('refreshToken');

          if (code === 'REFRESH_EXPIRED') {
            this.forceLogout('Refresh token expired.');
            return throwError(() => error);
          }

          if ((code === 'TOKEN_EXPIRED' || code === 'MISSING_TOKEN') && hasRefreshToken) {
            return this.handleRefresh(req, next);
          }

          if (token && hasRefreshToken) {
            return this.handleRefresh(req, next);
          }

          if (!token) {
            this.router.navigate(['/login']);
            return throwError(() => error);
          }

          return throwError(() => error);
        }

        // ── 403 handling ────────────────────────────────────────────────
        if (error.status === 403) {
          const url = req.url;
          const method = req.method;

          // Silent: GET calls এবং explicitly listed patterns
          const isSilentPattern = this.SILENT_403_PATTERNS.some(p => url.includes(p));
          const isGetRequest    = method === 'GET';
          // /api/auth/** সবসময় silent — component নিজে error handle করবে
          const isAuthEndpoint  = url.includes('/api/auth/');

          if (isSilentPattern || isGetRequest || isAuthEndpoint) {
            return throwError(() => error);
          }

          // অন্য POST/PUT/DELETE 403 — navigate to access-denied
          this.router.navigate(['/access-denied']);
          return throwError(() => error);
        }

        return throwError(() => error);
      })
    );
  }

  private handleRefresh(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.isRefreshing) {
      return this.refreshSubject.pipe(
        filter(t => t !== null),
        take(1),
        switchMap(t => next.handle(this.addToken(req, t!)))
      );
    }

    this.isRefreshing = true;
    this.refreshSubject.next(null);

    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      this.isRefreshing = false;
      this.forceLogout('No refresh token.');
      return throwError(() => new Error('No refresh token'));
    }

    return this.http.post<any>(`${environment.apiUrl}/auth/refresh`, { refreshToken }).pipe(
      switchMap(res => {
        localStorage.setItem('token', res.token);
        if (res.refreshToken) localStorage.setItem('refreshToken', res.refreshToken);
        this.refreshSubject.next(res.token);
        return next.handle(this.addToken(req, res.token));
      }),
      catchError((err: HttpErrorResponse) => {
        // শুধু refresh token সত্যিই invalid/expired হলে logout করব।
        // Network error (0), backend restart, CORS, 5xx বা সাময়িক failure-এ
        // valid session/localStorage মুছে দেওয়া যাবে না। পরের request আবার refresh চেষ্টা করবে।
        const code = err.error?.code;
        const refreshActuallyExpired =
          err.status === 401 && (code === 'REFRESH_EXPIRED' || code === 'TOKEN_EXPIRED');

        if (refreshActuallyExpired) {
          this.forceLogout('Refresh token expired. Please login again.');
        } else {
          console.warn('[AuthInterceptor] Refresh temporarily failed; session retained.', {
            status: err.status,
            code,
            message: err.message
          });
          this.refreshSubject.next(null);
        }

        return throwError(() => err);
      }),
      finalize(() => { this.isRefreshing = false; })
    );
  }

  private addToken(req: HttpRequest<any>, token: string): HttpRequest<any> {
    return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  private forceLogout(reason: string = ''): void {
    console.warn('[AuthInterceptor] Force logout:', reason);
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('role');
    localStorage.removeItem('currentUser');
    sessionStorage.clear();
    this.router.navigate(['/login'], { queryParams: { reason: 'session_expired' } });
  }
}
