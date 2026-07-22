 import { TranslatePipe } from './pipes/translate.pipe';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { DashboardComponent } from './modules/dashboard/dashboard.component';
import { SidebarComponent } from './layout/sidebar/sidebar.component';
import { NavbarComponent } from './layout/navbar/navbar.component';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RegisterComponent } from './modules/auth/register/register.component';
import { LoginComponent } from './modules/auth/login/login.component';
import { FooterComponent } from './layout/footer/footer.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { ForgotPasswordComponent } from './modules/auth/forgot-password/forgot-password.component';
import { BirthDeathCertificateComponent } from './modules/citizen/birth-death-certificate/birth-death-certificate.component';
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
import { SanitationMonitoringComponent } from './modules/health/sanitation-monitoring/sanitation-monitoring.component';
import { HealthCenterInfoComponent } from './modules/health/health-center-info/health-center-info.component';
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
import { HoldingLocationMapComponent } from './modules/map/holding-location-map/holding-location-map.component';
import { InfrastructureMapComponent } from './modules/map/infrastructure-map/infrastructure-map.component';
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
import { FeedbackAdminComponent } from './modules/communication/feedback-admin/feedback-admin.component';
import { FeedbackSubmitComponent } from './modules/communication/feedback-submit/feedback-submit.component';
import { NoticeAdminComponent } from './modules/notice/notice-admin/notice-admin.component';
import { NoticePublicComponent } from './modules/notice/notice-public/notice-public.component';
import { EVotingAuditLogsComponent } from './modules/e-voting/e-voting-audit-logs/e-voting-audit-logs.component';
import { PassportAdminComponent } from './modules/citizen/passport-admin/passport-admin.component';
import { WardMapComponent } from './modules/map/ward-map/ward-map.component';
import { JavaDatePipe } from './pipes/java-date.pipe';
import { UserApprovalComponent } from './modules/system/user-approval/user-approval.component';
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
import { CardAnalyticsComponent } from './modules/social cards/card-analytics/card-analytics.component';
import { VendorBlacklistComponent } from './modules/e-tender/vendor-blacklist/vendor-blacklist.component';
import { VoteCompleteComponent } from './modules/e-voting/vote-complete/vote-complete.component';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { AccessDeniedComponent } from './modules/auth/access-denied/access-denied.component';


@NgModule({
  declarations: [
    TranslatePipe,
    AppComponent,
    DashboardComponent,
    SidebarComponent,
    NavbarComponent,
    RegisterComponent,
    LoginComponent,
    FooterComponent,
    MainLayoutComponent,
    ForgotPasswordComponent,
    BirthDeathCertificateComponent,
    CitizenComponent,
    FamilyComponent,
    StatusComponent,
    NewTradeLicenseComponent,
    LicenseRenewalComponent,
    NewRegistrationComponent,
    TaxAssessmentComponent,
    TaxPaymentComponent,
    TaxDueComponent,
    OwnershipTransferComponent,
    TaxCollectionReportComponent,
    SubmitComplaintComponent,
    TrackingComponent,
    ComplaintResolutionComponent,
    RoadComponent,
    DrainageComponent,
    StreetLightComponent,
    ConstructionPermissionComponent,
    PublicHealthNoticesComponent,
    SanitationMonitoringComponent,
    HealthCenterInfoComponent,
    ListComponent,
    BudgetComponent,
    SuperAdminComponent,
    AdminOfficerComponent,
    DepartmentOfficerComponent,
    CitizenUserComponent,
    AccountantComponent,
    HealthComponent,
    ProjectComponent,
    GeneralSettingsComponent,
    UserRolesComponent,
    AuditLogsComponent,
    AdminProfileComponent,
    SettingsComponent,
    ConnectionComponent,
    BillComponent,
    WaterUsageComponent,
    BillStatusPayComponent,
    WardListComponent,
    PopulationComponent,
    GarbageScheduleComponent,
    PickupRequestComponent,
    SmartBinComponent,
    CollectionReportComponent,
    PaymentGatewayComponent,
    PaymentHistoryComponent,
    PaymentAdminComponent,
    CitizenReportComponent,
    ServiceReportComponent,
    MonthlyYearlyAnalyticsComponent,
    HoldingLocationMapComponent,
    InfrastructureMapComponent,
    AdminApplicationsComponent,
    PassportComponent,
    LicenseStatusComponent,
    DataShowComponent,
    EManagementComponent,
    VoterRegisterComponent,
    VoterApprovalComponent,
    CandidateComponent,
    CandidateApprovalComponent,
    CastVotingComponent,
    VotingComponent,
    ElectionResultComponent,
    AnalyticsDashboardComponent,
    CheckStatusComponent,
    InfrastructureStatusComponent,
    LicenseDataShowComponent,
    ETenderNoticeComponent,
    ETenderBidComponent,
    ETenderAdminComponent,
    NotificationSendComponent,
    NotificationLogComponent,
    FeedbackAdminComponent,
    FeedbackSubmitComponent,
    NoticeAdminComponent,
    NoticePublicComponent,
    EVotingAuditLogsComponent,
    PassportAdminComponent,
    WardMapComponent,
    JavaDatePipe,
    UserApprovalComponent,
    FamilyCardApplyComponent,
    FamilyCardAdminComponent,
    FamilyCardStatusComponent,
    FarmerCardApplyComponent,
    FarmerCardAdminComponent,
    FarmerCardStatusComponent,
    LpgCardApplyComponent,
    LpgCardAdminComponent,
    LpgCardStatusComponent,
    VgdCardApplyComponent,
    VgdCardAdminComponent,
    VgdCardStatusComponent,
    EpiRegisterComponent,
    EpiAdminComponent,
    ResetPasswordComponent,
    CardAnalyticsComponent,
    VendorBlacklistComponent,
    VoteCompleteComponent,
    AccessDeniedComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [
    {
       provide:  HTTP_INTERCEPTORS,
       useClass: AuthInterceptor,
       multi:    true
     }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
