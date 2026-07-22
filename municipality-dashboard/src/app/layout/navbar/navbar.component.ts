import { Component, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { LanguageService } from '../../services/language.service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {

  @Output() toggleSidebar = new EventEmitter<void>();

  showProfile = false;
  showNotif   = false;

  currentUser: any = null;
  photoUrl: string = '';

  // ── Real notifications from backend ──
  notifications:    any[] = [];
  unreadCount:      number = 0;
  notifLoading:     boolean = false;
  expandedNotifId:  number | null = null;

  // ── Search ──
  searchText  = '';
  searchResults: any[] = [];
  showSearch  = false;
  searchLoading = false;

  // ── Language ──
  currentLang: string = 'en';

  private get imgBase(): string {
    return environment.apiUrl.replace('/api', '');
  }

  private photoSub!: Subscription;
  private userSub!:  Subscription;
  private langSub!:  Subscription;

  constructor(
    private router: Router,
    private http: HttpClient,
    private authService: AuthService,
    public ls: LanguageService
  ) {}

  ngOnInit(): void {
    this.setDate();
    this.loadUser();
    this.loadNotifications();

    this.langSub = this.ls.lang$.subscribe(lang => {
      this.currentLang = lang;
      this.setDate();
    });

    this.photoSub = this.authService.photoRefresh$.subscribe((newPhotoUrl: string) => {
      this.photoUrl    = `${this.imgBase}/${newPhotoUrl}`;
      this.currentUser = this.authService.getCurrentUser();
    });

    this.userSub = this.authService.userRefresh$.subscribe(({ name, email }) => {
      this.currentUser = this.authService.getCurrentUser();
    });
  }

  ngOnDestroy(): void {
    if (this.photoSub) this.photoSub.unsubscribe();
    if (this.userSub)  this.userSub.unsubscribe();
    if (this.langSub)  this.langSub.unsubscribe();
  }

  setLang(lang: string): void {
    this.ls.set(lang as any);
  }

  loadUser(): void {
    this.currentUser = this.authService.getCurrentUser();
    if (!this.currentUser?.id) return;

    if (this.currentUser.photoUrl) {
      this.photoUrl = `${this.imgBase}/${this.currentUser.photoUrl}`;
    }

    this.http.get<any>(`${environment.apiUrl}/auth/profile/${this.currentUser.id}`)
      .subscribe({
        next: (data) => {
          if (data.photoUrl) {
            this.photoUrl = `${this.imgBase}/${data.photoUrl}`;
            this.authService.updatePhotoInSession(data.photoUrl);
            this.currentUser = this.authService.getCurrentUser();
          }
        },
        error: () => {}
      });
  }

  getInitials(): string {
    const name = this.currentUser?.name || '';
    return name.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2);
  }

  getRoleIcon(): string {
    const icons: Record<string, string> = {
      'Super Admin':                 'fa-crown',
      'Admin / Municipal Officer':   'fa-user-shield',
      'Department Officer':          'fa-sitemap',
      'Project Officer':             'fa-project-diagram',
      'Health / Sanitation Officer': 'fa-heartbeat',
      'Auditor / Accountant':        'fa-calculator',
      'ElectionOfficer':             'fa-vote-yea',
      'Citizen':                     'fa-user',
    };
    return icons[this.currentUser?.role] || 'fa-user-circle';
  }

  getRoleBadgeClass(): string {
    const classes: Record<string, string> = {
      'Super Admin':                 'badge-super',
      'Admin / Municipal Officer':   'badge-admin',
      'Department Officer':          'badge-dept',
      'Project Officer':             'badge-project',
      'Health / Sanitation Officer': 'badge-health',
      'Auditor / Accountant':        'badge-accountant',
      'ElectionOfficer':             'badge-election',
      'Citizen':                     'badge-citizen',
    };
    return classes[this.currentUser?.role] || '';
  }

  onToggle() { this.toggleSidebar.emit(); }
  logout() { this.authService.logout(); }

  setDate() {
    setTimeout(() => {
      const el = document.getElementById('nb-date');
      if (!el) return;
      const now = new Date();
      const locale = this.ls.current === 'bn' ? 'bn-BD' : 'en-GB';
      el.textContent = now.toLocaleDateString(locale, {
        weekday: 'short', day: '2-digit', month: 'short', year: 'numeric'
      });
    }, 0);
  }

  private notificationStorageKey(): string {
    const userId = this.currentUser?.id || localStorage.getItem('userId') || localStorage.getItem('id') || 'guest';
    return `egov_read_notifications_${userId}`;
  }

  private getReadNotificationIds(): Set<number> {
    try {
      const raw = localStorage.getItem(this.notificationStorageKey()) || '[]';
      const arr = JSON.parse(raw);
      return new Set((Array.isArray(arr) ? arr : []).map((x: any) => Number(x)).filter((x: number) => !Number.isNaN(x)));
    } catch {
      return new Set<number>();
    }
  }

  private saveReadNotificationIds(ids: Set<number>): void {
    localStorage.setItem(this.notificationStorageKey(), JSON.stringify(Array.from(ids).slice(-300)));
  }

  private refreshUnreadCount(): void {
    const readIds = this.getReadNotificationIds();
    this.unreadCount = this.notifications.filter(n => !readIds.has(Number(n.id))).length;
  }

  private markLoadedNotificationsAsRead(): void {
    const readIds = this.getReadNotificationIds();
    this.notifications.forEach(n => {
      if (n?.id !== undefined && n?.id !== null) {
        readIds.add(Number(n.id));
      }
    });
    this.saveReadNotificationIds(readIds);
    this.refreshUnreadCount();
  }

  isNotificationUnread(n: any): boolean {
    if (!n?.id) return false;
    return !this.getReadNotificationIds().has(Number(n.id));
  }

  loadNotifications(markAsViewed: boolean = false): void {
    this.notifLoading = true;
    this.http.get<any[]>(`${environment.apiUrl}/notification/getall`).subscribe({
      next: (data) => {
        this.notifications = data
          .filter((n: any) => (n.type || '').toLowerCase() === 'push')
          .sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
          .slice(0, 5);
        this.refreshUnreadCount();
        if (markAsViewed) {
          this.markLoadedNotificationsAsRead();
        }
        this.notifLoading  = false;
      },
      error: () => { this.notifLoading = false; }
    });
  }

  getNotifIcon(n: any): string {
    const tag = (n.serviceTag || '').toLowerCase();
    const type = (n.type || '').toLowerCase();
    if (tag.includes('complaint'))    return 'fa-exclamation-circle nd-orange';
    if (tag.includes('tax'))          return 'fa-check-circle nd-green';
    if (tag.includes('election'))     return 'fa-vote-yea nd-purple';
    if (tag.includes('health'))       return 'fa-heartbeat nd-red';
    if (type === 'email')             return 'fa-envelope nd-blue';
    if (type === 'sms')               return 'fa-sms nd-green';
    if (type === 'push')              return 'fa-bell nd-purple';
    return 'fa-file-alt nd-blue';
  }

  timeAgo(dateStr: string): string {
    if (!dateStr) return '';
    const diff = (Date.now() - new Date(dateStr).getTime()) / 1000;
    if (this.ls.current === 'bn') {
      if (diff < 60)    return `${Math.floor(diff)} সেকেন্ড আগে`;
      if (diff < 3600)  return `${Math.floor(diff/60)} মিনিট আগে`;
      if (diff < 86400) return `${Math.floor(diff/3600)} ঘণ্টা আগে`;
      return `${Math.floor(diff/86400)} দিন আগে`;
    }
    if (diff < 60)    return `${Math.floor(diff)} seconds ago`;
    if (diff < 3600)  return `${Math.floor(diff/60)} minutes ago`;
    if (diff < 86400) return `${Math.floor(diff/3600)} hours ago`;
    return `${Math.floor(diff/86400)} days ago`;
  }

  onSearch(query: string): void {
    this.searchText = query;
    if (!query || query.trim().length < 2) {
      this.showSearch   = false;
      this.searchResults = [];
      return;
    }
    this.showSearch    = true;
    this.searchLoading = true;

    const q = query.toLowerCase();
    this.http.get<any[]>(`${environment.apiUrl}/notification/getall`).subscribe({
      next: (data) => {
        const notifs = data
          .filter((n: any) =>
            (n.title || '').toLowerCase().includes(q) ||
            (n.message || '').toLowerCase().includes(q) ||
            (n.serviceTag || '').toLowerCase().includes(q)
          )
          .slice(0, 6)
          .map((n: any) => ({
            label: n.title || n.message,
            sub:   n.serviceTag || n.type,
            icon:  'fa-bell',
            route: null
          }));

        const builtIn = [
          { label: this.ls.t('sb.dashboard'),              icon: 'fa-gauge',                  route: '/dashboard' },
          { label: this.ls.t('cs.birthDeath'),             icon: 'fa-certificate',            route: '/birth-death' },
          { label: this.ls.t('cs.citizenCert'),            icon: 'fa-id-card',                route: '/citizen' },
          { label: this.ls.t('cs.familyCert'),             icon: 'fa-users',                  route: '/family' },
          { label: this.ls.t('cs.passport'),               icon: 'fa-passport',               route: '/passport' },
          { label: this.ls.t('cs.appStatus'),              icon: 'fa-magnifying-glass',       route: '/status' },
          { label: this.ls.t('cs.allApplyData'),           icon: 'fa-list',                   route: '/applications' },
          { label: this.ls.t('cs.passportAdmin'),          icon: 'fa-passport',               route: '/passport-admin' },
          { label: this.ls.t('tl.applyLicense'),          icon: 'fa-file-contract',          route: '/trade-license' },
          { label: this.ls.t('tl.renewal'),               icon: 'fa-arrows-rotate',          route: '/license-renewal' },
          { label: this.ls.t('tl.statusCheck'),           icon: 'fa-shield-check',           route: '/license-verification' },
          { label: this.ls.t('tl.allApplyData'),          icon: 'fa-table',                  route: '/licenseDataShow' },
          { label: this.ls.t('ht.newRegistration'),       icon: 'fa-building',               route: '/new-registration' },
          { label: this.ls.t('ht.taxAssessment'),         icon: 'fa-calculator',             route: '/tax-assessment' },
          { label: this.ls.t('ht.taxPayment'),            icon: 'fa-money-bill',             route: '/tax-payment' },
          { label: this.ls.t('ht.taxDueList'),            icon: 'fa-triangle-exclamation',   route: '/tax-due' },
          { label: this.ls.t('ht.ownershipTransfer'),     icon: 'fa-right-left',             route: '/ownership-transfer' },
          { label: this.ls.t('ht.collectionReport'),      icon: 'fa-chart-bar',              route: '/tax-collection-report' },
          { label: this.ls.t('cp.submit'),                icon: 'fa-triangle-exclamation',   route: '/complaint-submit' },
          { label: this.ls.t('cp.allComplaints'),         icon: 'fa-list',                   route: '/complaint-resolution' },
          { label: this.ls.t('cp.tracking'),              icon: 'fa-location-crosshairs',    route: '/tracking' },
          { label: this.ls.t('cp.resolution'),            icon: 'fa-gavel',                  route: '/complaint-resolution' },
          { label: this.ls.t('inf.road'),                 icon: 'fa-road',                   route: '/road' },
          { label: this.ls.t('inf.drainage'),             icon: 'fa-water',                  route: '/drainage' },
          { label: this.ls.t('inf.streetLight'),          icon: 'fa-lightbulb',              route: '/street-light' },
          { label: this.ls.t('inf.construction'),         icon: 'fa-helmet-safety',          route: '/construction-permission' },
          { label: this.ls.t('hl.notices'),               icon: 'fa-bullhorn',               route: '/health-notices' },
          { label: this.ls.t('hl.epiRegister'),           icon: 'fa-syringe',                route: '/epi-register' },
          { label: this.ls.t('hl.epiAdmin'),              icon: 'fa-syringe',                route: '/epi-admin' },
          { label: this.ls.t('hl.sanitation'),            icon: 'fa-toilet',                 route: '/sanitation-monitoring' },
          { label: this.ls.t('hl.centerInfo'),            icon: 'fa-hospital',               route: '/health-center-info' },
          { label: this.ls.t('pj.list'),                  icon: 'fa-diagram-project',        route: '/list' },
          { label: this.ls.t('pj.budget'),                icon: 'fa-coins',                  route: '/budget' },
          { label: this.ls.t('wt.connection'),            icon: 'fa-droplet',                route: '/connection' },
          { label: this.ls.t('wt.bill'),                  icon: 'fa-file-invoice',           route: '/bill' },
          { label: this.ls.t('wt.usageReport'),           icon: 'fa-chart-line',             route: '/report' },
          { label: this.ls.t('ws.schedule'),              icon: 'fa-calendar-days',          route: '/schedule' },
          { label: this.ls.t('ws.pickupRequest'),         icon: 'fa-truck-pickup',           route: '/request' },
          { label: this.ls.t('ws.smartBin'),              icon: 'fa-trash-can',              route: '/smart-bin' },
          { label: this.ls.t('pay.online'),               icon: 'fa-credit-card',            route: '/payment' },
          { label: this.ls.t('pay.history'),              icon: 'fa-clock-rotate-left',      route: '/payment-history' },
          { label: this.ls.t('rp.citizen'),               icon: 'fa-users',                  route: '/report/citizen' },
          { label: this.ls.t('rp.service'),               icon: 'fa-chart-pie',              route: '/report/service' },
          { label: this.ls.t('rp.analytics'),             icon: 'fa-chart-bar',              route: '/report/analytics' },
          { label: this.ls.t('ev.voterReg'),              icon: 'fa-vote-yea',               route: '/voter-registration' },
          { label: this.ls.t('ev.candidate'),             icon: 'fa-person',                 route: '/candidate' },
          { label: this.ls.t('et.notices'),               icon: 'fa-file-contract',          route: '/etender-notices' },
          { label: this.ls.t('et.bid'),                   icon: 'fa-hand',                   route: '/etender-bid' },
          { label: this.ls.t('et.admin'),                 icon: 'fa-gavel',                  route: '/etender-admin' },
          { label: this.ls.t('et.blacklist'),             icon: 'fa-ban',                    route: '/etender-blacklist' },
          { label: this.ls.t('cm.sendNotif'),             icon: 'fa-paper-plane',            route: '/notification-send' },
          { label: this.ls.t('cm.notifLog'),              icon: 'fa-bell',                   route: '/notification-log' },
          { label: this.ls.t('ad.profile'),               icon: 'fa-user-circle',            route: '/profile' },
          { label: this.ls.t('ad.settings'),              icon: 'fa-gear',                   route: '/settings' },
          { label: this.ls.t('ad.systemSettings'),        icon: 'fa-sliders',                route: '/system-settings' },
          { label: this.ls.t('ad.roles'),                 icon: 'fa-shield',                 route: '/roles' },
          { label: this.ls.t('ad.auditLogs'),             icon: 'fa-scroll',                 route: '/audit-logs' },
          { label: this.ls.t('ad.userApproval'),          icon: 'fa-user-check',             route: '/user-approval' },
        ].filter(r => r.label.toLowerCase().includes(q) || (r.route || '').toLowerCase().includes(q));

        this.searchResults = [...builtIn, ...notifs].slice(0, 8);
        this.searchLoading = false;
      },
      error: () => {
        this.searchLoading = false;
        this.showSearch = false;
      }
    });
  }

  goTo(route: string | null): void {
    if (route) {
      this.router.navigate([route]);
      this.showSearch  = false;
      this.searchText  = '';
      this.searchResults = [];
    }
  }

  clearSearch(): void {
    this.searchText   = '';
    this.showSearch   = false;
    this.searchResults = [];
  }

  toggleNotifExpand(id: number): void {
    this.expandedNotifId = this.expandedNotifId === id ? null : id;
    if (id) {
      const readIds = this.getReadNotificationIds();
      readIds.add(Number(id));
      this.saveReadNotificationIds(readIds);
      this.refreshUnreadCount();
    }
  }

  toggleNotifAndLoad() {
    this.showNotif = !this.showNotif;
    if (this.showNotif) {
      this.showProfile    = false;
      this.expandedNotifId = null;
      this.loadNotifications(true);
    } else {
      this.expandedNotifId = null;
    }
  }

  toggleProfile() {
    this.showProfile = !this.showProfile;
    if (this.showProfile) this.showNotif = false;
  }

  toggleNotif() { this.toggleNotifAndLoad(); }

  closeAll() {
    this.showProfile = false;
    this.showNotif   = false;
  }

  toggleFullscreen() {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen();
    } else {
      document.exitFullscreen();
    }
  }
}
