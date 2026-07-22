import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegisterComponent } from './modules/auth/register/register.component';
import { LoginComponent } from './modules/auth/login/login.component';
import { DashboardComponent } from './modules/dashboard/dashboard.component';
import { AuthGuard } from './auth.guard';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { CitizenComponent } from './modules/citizen/citizen/citizen.component';
import { FamilyComponent } from './modules/citizen/family/family.component';
import { StatusComponent } from './modules/citizen/status/status.component';
import { NewTradeLicenseComponent } from './modules/trade-license/new-trade-license/new-trade-license.component';
import { LicenseRenewalComponent } from './modules/trade-license/license-renewal/license-renewal.component';
import { NewRegistrationComponent } from './modules/holding-tax/new-registration/new-registration.component';
import { TaxAssessmentComponent } from './modules/holding-tax/tax-assessment/tax-assessment.component';
import { TaxPaymentComponent } from './modules/holding-tax/tax-payment/tax-payment.component';
import { TaxDueComponent } from './modules/holding-tax/tax-due/tax-due.component';
import { OwnershipTransferComponent } from './modules/holding-tax/ownership-transfer/ownership-transfer.component';
import { TaxCollectionReportComponent } from './modules/holding-tax/tax-collection-report/tax-collection-report.component';
import { SubmitComplaintComponent } from './modules/complaint/submit-complaint/submit-complaint.component';
import { TrackingComponent } from './modules/complaint/tracking/tracking.component';
import { ComplaintResolutionComponent } from './modules/complaint/complaint-resolution/complaint-resolution.component';
import { RoadComponent } from './modules/infrastructure/road/road.component';
import { DrainageComponent } from './modules/infrastructure/drainage/drainage.component';
import { StreetLightComponent } from './modules/infrastructure/street-light/street-light.component';
import { ConstructionPermissionComponent } from './modules/infrastructure/construction-permission/construction-permission.component';
import { PublicHealthNoticesComponent } from './modules/health/public-health-notices/public-health-notices.component';
import { HealthCenterInfoComponent } from './modules/health/health-center-info/health-center-info.component';
import { SanitationMonitoringComponent } from './modules/health/sanitation-monitoring/sanitation-monitoring.component';
import { ListComponent } from './modules/project/list/list.component';
import { BudgetComponent } from './modules/project/budget/budget.component';
import { SuperAdminComponent } from './modules/roles/super-admin/super-admin.component';
import { AdminOfficerComponent } from './modules/roles/admin-officer/admin-officer.component';
import { DepartmentOfficerComponent } from './modules/roles/department-officer/department-officer.component';
import { CitizenUserComponent } from './modules/roles/citizen-user/citizen-user.component';
import { AccountantComponent } from './modules/roles/accountant/accountant.component';
import { HealthComponent } from './modules/roles/health/health.component';
import { ProjectComponent } from './modules/roles/project/project.component';
import { GeneralSettingsComponent } from './modules/system/general-settings/general-settings.component';
import { UserRolesComponent } from './modules/system/user-roles/user-roles.component';
import { AuditLogsComponent } from './modules/system/audit-logs/audit-logs.component';
import { AdminProfileComponent } from './modules/profile/admin-profile/admin-profile.component';
import { SettingsComponent } from './modules/profile/settings/settings.component';
import { ConnectionComponent } from './modules/water/connection/connection.component';
import { BillComponent } from './modules/water/bill/bill.component';
import { WaterUsageComponent } from './modules/water/water-usage/water-usage.component';
import { BillStatusPayComponent } from './modules/water/bill-status-pay/bill-status-pay.component';
import { WardListComponent } from './modules/ward/ward-list/ward-list.component';
import { PopulationComponent } from './modules/ward/population/population.component';
import { GarbageScheduleComponent } from './modules/waste/garbage-schedule/garbage-schedule.component';
import { PickupRequestComponent } from './modules/waste/pickup-request/pickup-request.component';
import { SmartBinComponent } from './modules/waste/smart-bin/smart-bin.component';
import { CollectionReportComponent } from './modules/waste/collection-report/collection-report.component';
import { CitizenReportComponent } from './modules/report-analytics/citizen-report/citizen-report.component';
import { ServiceReportComponent } from './modules/report-analytics/service-report/service-report.component';
import { MonthlyYearlyAnalyticsComponent } from './modules/report-analytics/monthly-yearly-analytics/monthly-yearly-analytics.component';
import { AdminApplicationsComponent } from './modules/citizen/admin-applications/admin-applications.component';
import { PassportComponent } from './modules/citizen/passport/passport.component';
import { LicenseStatusComponent } from './modules/trade-license/license-status/license-status.component';
import { DataShowComponent } from './modules/holding-tax/data-show/data-show.component';
import { EManagementComponent } from './modules/e-voting/e-management/e-management.component';
import { VoterRegisterComponent } from './modules/e-voting/voter-register/voter-register.component';
import { VoterApprovalComponent } from './modules/e-voting/voter-approval/voter-approval.component';
import { CandidateComponent } from './modules/e-voting/candidate/candidate.component';
import { CandidateApprovalComponent } from './modules/e-voting/candidate-approval/candidate-approval.component';
import { CastVotingComponent } from './modules/e-voting/cast-voting/cast-voting.component';
import { VotingComponent } from './modules/e-voting/voting/voting.component';
import { ElectionResultComponent } from './modules/e-voting/election-result/election-result.component';
import { AnalyticsDashboardComponent } from './modules/e-voting/analytics-dashboard/analytics-dashboard.component';
import { CheckStatusComponent } from './modules/infrastructure/check-status/check-status.component';
import { InfrastructureStatusComponent } from './modules/infrastructure/infrastructure-status/infrastructure-status.component';
import { LicenseDataShowComponent } from './modules/trade-license/license-data-show/licenseDataShow.component';
import { ETenderNoticeComponent } from './modules/e-tender/etender-notice/etender-notice.component';
import { ETenderBidComponent } from './modules/e-tender/etender-bid/etender-bid.component';
import { ETenderAdminComponent } from './modules/e-tender/etender-admin/etender-admin.component';
import { PaymentGatewayComponent } from './modules/payment/payment-gateway/payment-gateway.component';
import { PaymentHistoryComponent } from './modules/payment/payment-history/payment-history.component';
import { PaymentAdminComponent } from './modules/payment/payment-admin/payment-admin.component';
import { NotificationSendComponent } from './modules/communication/notification-send/notification-send.component';
import { NotificationLogComponent } from './modules/communication/notification-log/notification-log.component';
import { FeedbackSubmitComponent } from './modules/communication/feedback-submit/feedback-submit.component';
import { FeedbackAdminComponent } from './modules/communication/feedback-admin/feedback-admin.component';
import { HoldingLocationMapComponent } from './modules/map/holding-location-map/holding-location-map.component';
import { InfrastructureMapComponent } from './modules/map/infrastructure-map/infrastructure-map.component';
import { NoticeAdminComponent } from './modules/notice/notice-admin/notice-admin.component';
import { NoticePublicComponent } from './modules/notice/notice-public/notice-public.component';
import { EVotingAuditLogsComponent } from './modules/e-voting/e-voting-audit-logs/e-voting-audit-logs.component';
import { PassportAdminComponent } from './modules/citizen/passport-admin/passport-admin.component';
import { WardMapComponent } from './modules/map/ward-map/ward-map.component';
import { UserApprovalComponent } from './modules/system/user-approval/user-approval.component';
import { BirthDeathCertificateComponent } from './modules/citizen/birth-death-certificate/birth-death-certificate.component';
import { FamilyCardApplyComponent } from './modules/social cards/family-card-apply/family-card-apply.component';
import { FamilyCardAdminComponent } from './modules/social cards/family-card-admin/family-card-admin.component';
import { FamilyCardStatusComponent } from './modules/social cards/family-card-status/family-card-status.component';
import { FarmerCardApplyComponent } from './modules/social cards/farmer-card-apply/farmer-card-apply.component';
import { FarmerCardAdminComponent } from './modules/social cards/farmer-card-admin/farmer-card-admin.component';
import { FarmerCardStatusComponent } from './modules/social cards/farmer-card-status/farmer-card-status.component';
import { LpgCardApplyComponent } from './modules/social cards/lpg-card-apply/lpg-card-apply.component';
import { LpgCardAdminComponent } from './modules/social cards/lpg-card-admin/lpg-card-admin.component';
import { LpgCardStatusComponent } from './modules/social cards/lpg-card-status/lpg-card-status.component';
import { VgdCardApplyComponent } from './modules/social cards/vgd-card-apply/vgd-card-apply.component';
import { VgdCardAdminComponent } from './modules/social cards/vgd-card-admin/vgd-card-admin.component';
import { VgdCardStatusComponent } from './modules/social cards/vgd-card-status/vgd-card-status.component';
import { EpiRegisterComponent } from './modules/health/epi-register/epi-register.component';
import { EpiAdminComponent } from './modules/health/epi-admin/epi-admin.component';
import { ResetPasswordComponent } from './modules/auth/reset-password/reset-password.component';
import { ForgotPasswordComponent } from './modules/auth/forgot-password/forgot-password.component';
import { CardAnalyticsComponent } from './modules/social cards/card-analytics/card-analytics.component';
import { VoteCompleteComponent } from './modules/e-voting/vote-complete/vote-complete.component';
import { VendorBlacklistComponent } from './modules/e-tender/vendor-blacklist/vendor-blacklist.component';
import { AccessDeniedComponent } from './modules/auth/access-denied/access-denied.component';

const ADMIN_ROLES        = ['Admin', 'Super Admin', 'Admin / Municipal Officer', 'ElectionOfficer', 'Department Officer'];
const SUPER_ADMIN_ROLES  = ['Super Admin'];
const SYSTEM_ADMIN_ROLES = ['Super Admin', 'Admin', 'Admin / Municipal Officer', 'Department Officer'];
const FINANCE_ROLES      = ['Super Admin', 'Admin', 'Admin / Municipal Officer', 'Department Officer', 'Auditor / Accountant'];
const HEALTH_ROLES       = ['Super Admin', 'Admin', 'Admin / Municipal Officer', 'Department Officer', 'Health / Sanitation Officer'];
const INFRA_ROLES        = ['Super Admin', 'Admin / Municipal Officer', 'Department Officer', 'Project Officer'];
const SOCIAL_ADMIN_ROLES = ['Super Admin', 'Admin', 'Admin / Municipal Officer', 'Department Officer'];

const routes: Routes = [
 // Default
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

  // Auth (NO layout)
  { path: 'login',               component: LoginComponent },
  { path: 'register',            component: RegisterComponent },
  { path: 'access-denied',       component: AccessDeniedComponent },
  { path: 'auth/forgot-password',component: ForgotPasswordComponent },
  { path: 'auth/reset-password', component: ResetPasswordComponent },

  // Main Layout (Navbar + Sidebar)
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],

    children: [
      { path: 'dashboard', component: DashboardComponent },

      { path: 'birth-death', component: BirthDeathCertificateComponent },
      { path: 'citizen', component: CitizenComponent },
      { path: 'family', component: FamilyComponent },
      { path: 'passport', component: PassportComponent },
      { path: 'passport-admin', component: PassportAdminComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES.concat(['Department Officer']) } },
      { path: 'status', component: StatusComponent },
      { path: 'applications', component: AdminApplicationsComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES.concat(['Department Officer']) } },

      { path: 'trade-license', component: NewTradeLicenseComponent },
      { path: 'license-renewal', component: LicenseRenewalComponent },
      { path: 'license-verification', component: LicenseStatusComponent },
      { path: 'licenseDataShow', component: LicenseDataShowComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES.concat(['Department Officer']) } },

      { path: 'new-registration', component: NewRegistrationComponent },
      { path: 'tax-assessment', component: TaxAssessmentComponent },
      { path: 'tax-payment', component: TaxPaymentComponent },
      { path: 'tax-due', component: TaxDueComponent },
      { path: 'ownership-transfer', component: OwnershipTransferComponent },
      { path: 'tax-collection-report', component: TaxCollectionReportComponent },
      { path: 'tax-applications', component: DataShowComponent },

      { path: 'complaint-submit', component: SubmitComplaintComponent },
      { path: 'tracking', component: TrackingComponent },
      { path: 'complaint-resolution', component: ComplaintResolutionComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },

      { path: 'road', component: RoadComponent },
      { path: 'drainage', component: DrainageComponent },
      { path: 'street-light', component: StreetLightComponent },
      { path: 'construction-permission', component: ConstructionPermissionComponent },
      { path: 'check-status', component: CheckStatusComponent, canActivate: [AuthGuard] },
      { path: 'infrastructure-status', component: InfrastructureStatusComponent, canActivate: [AuthGuard], data: { roles: ['Super Admin', 'Admin / Municipal Officer', 'Department Officer', 'Project Officer'] } },

      { path: 'health-notices', component: PublicHealthNoticesComponent},
      { path: 'epi-register', component: EpiRegisterComponent },
      { path: 'epi-admin', component: EpiAdminComponent },
      { path: 'sanitation-monitoring', component: SanitationMonitoringComponent},
      { path: 'health-center-info', component: HealthCenterInfoComponent},

      { path: 'list', component: ListComponent },
      { path: 'budget', component: BudgetComponent },

      { path: 'super-admin',        component: SuperAdminComponent,        canActivate: [AuthGuard], data: { roles: SUPER_ADMIN_ROLES } },
      { path: 'admin-officer',      component: AdminOfficerComponent,      canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },
      { path: 'department-officer', component: DepartmentOfficerComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },
      { path: 'citizen-user',       component: CitizenUserComponent,       canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },
      { path: 'accountant',         component: AccountantComponent,        canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },
      { path: 'health',             component: HealthComponent,            canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },
      { path: 'project',            component: ProjectComponent,           canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },

      { path: 'settings',        component: SettingsComponent },
      { path: 'system-settings', component: GeneralSettingsComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },
      { path: 'roles',           component: UserRolesComponent,       canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },
      { path: 'audit-logs',      component: AuditLogsComponent,       canActivate: [AuthGuard], data: { roles: SUPER_ADMIN_ROLES } },
      { path: 'user-approval',   component: UserApprovalComponent,    canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },

      { path: 'profile', component: AdminProfileComponent },
      //{ path: 'settings', component: SettingsComponent },

      { path: 'connection', component: ConnectionComponent },
      { path: 'bill', redirectTo: 'water-admin', pathMatch: 'full' },
      { path: 'water-admin', component: BillComponent, canActivate: [AuthGuard], data: { roles: FINANCE_ROLES.concat(['Department Officer']) } },
      { path: 'water-bill-status', component: BillStatusPayComponent },
      { path: 'report', component: WaterUsageComponent },

      { path: 'ward-list', component: WardListComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },
      { path: 'population', component: PopulationComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },

      { path: 'schedule', component: GarbageScheduleComponent, canActivate: [AuthGuard] },
      { path: 'request', component: PickupRequestComponent, canActivate: [AuthGuard] },
      { path: 'smart-bin', component: SmartBinComponent, canActivate: [AuthGuard], data: { roles: SOCIAL_ADMIN_ROLES } },
      { path: 'collection-report', component: CollectionReportComponent, canActivate: [AuthGuard], data: { roles: SOCIAL_ADMIN_ROLES } },

      { path: 'payment',         component: PaymentGatewayComponent,  canActivate: [AuthGuard] },
      { path: 'payment-history', component: PaymentHistoryComponent,   canActivate: [AuthGuard] },
      { path: 'payment-admin',   component: PaymentAdminComponent,     canActivate: [AuthGuard], data: { roles: FINANCE_ROLES } },

      { path: 'report/citizen', component: CitizenReportComponent },
      { path: 'report/service', component: ServiceReportComponent },
      { path: 'report/analytics', component: MonthlyYearlyAnalyticsComponent },

      {
        path: 'election',
        component: EManagementComponent,
        canActivate: [AuthGuard],
        data: { roles: ADMIN_ROLES }
      },
      {
        path: 'voter-approval',
        component: VoterApprovalComponent,
        canActivate: [AuthGuard],
        data: { roles: ADMIN_ROLES }
      },
      {
        path: 'candidate-approval',
        component: CandidateApprovalComponent,
        canActivate: [AuthGuard],
        data: { roles: ADMIN_ROLES }
      },
      {
        path: 'voting-analytics',
        component: AnalyticsDashboardComponent,
        canActivate: [AuthGuard],
        data: { roles: ADMIN_ROLES }
      },
      {
        path: 'audit',
        component: EVotingAuditLogsComponent,
        canActivate: [AuthGuard],
        data: { roles: ADMIN_ROLES }
      },

      // Citizen-facing voting flow
      { path: 'voter-registration', component: VoterRegisterComponent, canActivate: [AuthGuard]},
      { path: 'candidate', component: CandidateComponent, canActivate: [AuthGuard]},
      {
        path: 'vote-cast',
        component: CastVotingComponent,
        canActivate: [AuthGuard]
      },
      {
        path: 'cast-vote',
        component: VotingComponent,
        canActivate: [AuthGuard]
      },
      {
        path: 'vote-complete',
        component: VoteCompleteComponent,
        canActivate: [AuthGuard]
      },
      {
        path: 'vote-result',
        component: ElectionResultComponent,
        canActivate: [AuthGuard]
      },

      { path: 'etender-notices', component: ETenderNoticeComponent },
      { path: 'etender-bid',     component: ETenderBidComponent },
      { path: 'etender-admin',   component: ETenderAdminComponent },
      { path: 'etender-blacklist', component: VendorBlacklistComponent },

      { path: 'notification-send',  component: NotificationSendComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },
      { path: 'notification-log',   component: NotificationLogComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },
      { path: 'feedback',           component: FeedbackSubmitComponent, canActivate: [AuthGuard] },
      { path: 'feedback-admin',     component: FeedbackAdminComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },

      { path: 'map/wards',        component: WardMapComponent, canActivate: [AuthGuard] },
      { path: 'map/holdings',        component: HoldingLocationMapComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES.concat(['Project Officer']) } },
      { path: 'map/infrastructure',  component: InfrastructureMapComponent, canActivate: [AuthGuard] },

      { path: 'notice/admin',  component: NoticeAdminComponent, canActivate: [AuthGuard], data: { roles: SYSTEM_ADMIN_ROLES } },
      { path: 'notice/public', component: NoticePublicComponent },

      { path: 'family-card/apply',  component: FamilyCardApplyComponent },
      { path: 'family-card/admin',  component: FamilyCardAdminComponent },
      { path: 'family-card/status', component: FamilyCardStatusComponent },
      { path: 'tcb', loadChildren: () => import('./modules/tcb/tcb.module').then(m => m.TcbModule), canActivate: [AuthGuard]},
      { path: 'farmer-card/apply',  component: FarmerCardApplyComponent },
      { path: 'farmer-card/admin',  component: FarmerCardAdminComponent },
      { path: 'farmer-card/status', component: FarmerCardStatusComponent },
      { path: 'farmer-distribution', loadChildren: () => import('./modules/farmer-distribution/farmer-distribution.module').then(m => m.FarmerDistributionModule), canActivate: [AuthGuard] },
      { path: 'lpg-card/apply',     component: LpgCardApplyComponent},
      { path: 'lpg-card/admin',     component: LpgCardAdminComponent},
      { path: 'lpg-card/status',    component: LpgCardStatusComponent},
      { path: 'lpg-distribution', loadChildren: () => import('./modules/lpg-distribution/lpg-distribution.module').then(m => m.LpgDistributionModule), canActivate: [AuthGuard] },
      { path: 'vgd-card/apply',     component: VgdCardApplyComponent},
      { path: 'vgd-card/admin',     component: VgdCardAdminComponent},
      { path: 'vgd-card/status',    component: VgdCardStatusComponent},
      { path: 'vgd-distribution', loadChildren: () => import('./modules/vgd-distribution/vgd-distribution.module').then(m => m.VgdDistributionModule), canActivate: [AuthGuard]},
      { path: 'social-cards/analytics', component: CardAnalyticsComponent },

    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
