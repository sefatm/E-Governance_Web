import { Component, EventEmitter, HostListener, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { AuthService } from 'src/app/services/auth.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit, OnDestroy {

  @Input()  isCollapsed: boolean = false;
  @Input()  isMobileOpen: boolean = false;
  @Output() sidebarToggle = new EventEmitter<void>();

  role: string | null = null;
  isMobile: boolean = false;

  private routerSub: Subscription | null = null;

  constructor(
    private router: Router,
    private authService: AuthService,
    public ls: LanguageService
  ) {}
  
  ngOnInit(): void {
    this.refreshRole();
    this.checkScreenSize();

    this.routerSub = this.router.events.pipe(
      filter((event: any) => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.refreshRole();
    });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  private refreshRole(): void {
    this.role = this.authService.getCurrentRole() || localStorage.getItem('role');
  }

  @HostListener('window:resize')
  checkScreenSize() {
    this.isMobile = window.innerWidth <= 768;
    if (!this.isMobile) this.isMobileOpen = false;
  }

  toggleSidebar() { this.sidebarToggle.emit(); }

  closeMobileSidebar() {
    this.isMobileOpen = false;
    this.sidebarToggle.emit();
  }

  // ─── Role Check Helpers ───────────────────────────────────────────────
  get isSuperAdmin():      boolean { return this.role === 'Super Admin'; }
  get isAdmin():           boolean { return this.role === 'Admin' || this.role === 'Admin / Municipal Officer'; }
  get isDeptOfficer():     boolean { return this.role === 'Department Officer'; }
  get isProjectOfficer():  boolean { return this.role === 'Project Officer'; }
  get isHealthOfficer():   boolean { return this.role === 'Health / Sanitation Officer'; }
  get isAccountant():      boolean { return this.role === 'Auditor / Accountant'; }
  get isCitizen():         boolean { return this.role === 'Citizen'; }
  get isElectionOfficer(): boolean { return this.role === 'ElectionOfficer'; }

  get isAdminLevel():    boolean { return this.isSuperAdmin || this.isAdmin; }
  get isElectionAdmin(): boolean { return this.isSuperAdmin || this.isAdmin || this.isElectionOfficer; }
  get isSystemAdmin():   boolean { return this.isSuperAdmin || this.isAdmin; }
  get isFinanceRole():   boolean { return this.isSuperAdmin || this.isAdmin || this.isAccountant; }
  get isHealthRole():    boolean { return this.isSuperAdmin || this.isAdmin || this.isHealthOfficer; }
  get isInfraRole():     boolean { return this.isSuperAdmin || this.isAdmin || this.isDeptOfficer || this.isProjectOfficer; }
  get isSocialAdmin():   boolean { return this.isSuperAdmin || this.isAdmin || this.isDeptOfficer; }
  get isStaff():         boolean { return !this.isCitizen; }

  readonly employeeModuleReady = false;
  // ─── Menu Toggle State ────────────────────────────────────────────────
  openCitizenMenu        = false;
  openTradeLicenseMenu   = false;
  openHoldingMenu        = false;
  openComplaintMenu      = false;
  openInfrastructureMenu = false;
  openHealthMenu         = false;
  openProjectMenu        = false;
  openRolesMenu          = false;
  openSystemMenu         = false;
  openWaterMenu          = false;
  openWasteMenu          = false;
  openWardMenu           = false;
  openNoticeMenu         = false;
  openGisMenu            = false;
  openCommMenu           = false;
  openPaymentMenu        = false;
  openReportMenu         = false;
  openVotingMenu         = false;
  openTenderMenu         = false;
  openSocialCardMenu     = false;
  openFamilyCardSub      = false;
  openFarmerCardSub      = false;
  openLpgCardSub         = false;
  openVgdCardSub         = false;
  openFarmDistMenu       = false;
  openEmployeeMenu       = false;

  toggleCitizenMenu()        { this.openCitizenMenu        = !this.openCitizenMenu; }
  toggleTradeLicenseMenu()   { this.openTradeLicenseMenu   = !this.openTradeLicenseMenu; }
  toggleHoldingMenu()        { this.openHoldingMenu        = !this.openHoldingMenu; }
  toggleComplaintMenu()      { this.openComplaintMenu      = !this.openComplaintMenu; }
  toggleInfrastructureMenu() { this.openInfrastructureMenu = !this.openInfrastructureMenu; }
  toggleHealthMenu()         { this.openHealthMenu         = !this.openHealthMenu; }
  toggleProjectMenu()        { this.openProjectMenu        = !this.openProjectMenu; }
  toggleRolesMenu()          { this.openRolesMenu          = !this.openRolesMenu; }
  toggleSystemMenu()         { this.openSystemMenu         = !this.openSystemMenu; }
  toggleWaterMenu()          { this.openWaterMenu          = !this.openWaterMenu; }
  toggleWasteMenu()          { this.openWasteMenu          = !this.openWasteMenu; }
  toggleWardMenu()           { this.openWardMenu           = !this.openWardMenu; }
  toggleNoticeMenu()         { this.openNoticeMenu         = !this.openNoticeMenu; }
  toggleGisMenu()            { this.openGisMenu            = !this.openGisMenu; }
  toggleCommMenu()           { this.openCommMenu           = !this.openCommMenu; }
  togglePaymentMenu()        { this.openPaymentMenu        = !this.openPaymentMenu; }
  toggleReportMenu()         { this.openReportMenu         = !this.openReportMenu; }
  toggleVotingMenu()         { this.openVotingMenu         = !this.openVotingMenu; }
  toggleTenderMenu()         { this.openTenderMenu         = !this.openTenderMenu; }
  toggleSocialCardMenu()     { this.openSocialCardMenu     = !this.openSocialCardMenu; }
  toggleFarmDistMenu()       { this.openFarmDistMenu       = !this.openFarmDistMenu; }
  toggleEmployeeMenu()       { this.openEmployeeMenu       = !this.openEmployeeMenu; }

  logout() { this.authService.logout(); }
}
