import { environment } from 'src/environments/environment';
import { Component, OnInit } from '@angular/core';
import { HoldingApplication } from 'src/app/models/holding-new-registration.model';
import { HoldingService } from 'src/app/services/holding-new-registration.service';
import { LanguageService } from 'src/app/services/language.service';

declare let L: any;

type TabType = 'form' | 'list' | 'status';

interface Toast {
  type: 'success' | 'error';
  message: string;
  removing?: boolean;
}

@Component({
  selector: 'app-new-registration',
  templateUrl: './new-registration.component.html',
  styleUrls: ['./new-registration.component.css']
})
export class NewRegistrationComponent implements OnInit {
  readonly serverUrl = environment.serverUrl;

  form: HoldingApplication = this.emptyForm();

  isSubmitting = false;
  agreed = false;

  activeTab: TabType = 'form';

  applications: HoldingApplication[] = [];
  isLoading = false;

  currentStep = 1;

  steps = [
    'Applicant Info',
    'Address & Property',
    'Documents',
    'Contact & Submit'
  ];

  statusSearchText = '';
  statusResult: HoldingApplication | null = null;
  statusSearched = false;
  statusLoading = false;
  downloadModalApp: HoldingApplication | null = null;

  nidFile: File | null = null;
  deedFile: File | null = null;
  photoFile: File | null = null;

  photoPreview: string | null = null;

  fileError = '';

  toasts: Toast[] = [];

  // GPS
  gpsLoading = false;
  gpsError = '';

  private regMap: any = null;
  private regMapMarker: any = null;

  constructor(public ls: LanguageService, private holdingService: HoldingService) {}

  ngOnInit(): void {
    this.loadApplications();
  }

  switchTab(tab: TabType): void {

    this.activeTab = tab;

    if (tab === 'list') {
      this.loadApplications();
    }
  }

  loadApplications(): void {

    this.isLoading = true;

    this.holdingService.getAllApplications().subscribe({
      next: (res) => {
        this.applications = res;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  nextStep(): void {

    if (this.currentStep === 1) {

      if (!this.form.applicantName || !this.form.nid) {

        this.showToast(
          'Please fill Applicant Name and NID',
          'error'
        );

        return;
      }
    }

    if (this.currentStep === 4) {
      return;
    }

    this.currentStep++;

    if (this.currentStep === 2) {

      setTimeout(() => {
        this.initLocationMap();
      }, 200);
    }

    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }

  prevStep(): void {

    if (this.currentStep > 1) {
      this.currentStep--;
    }

    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }

  // ================= FILE =================

  onFileSelect(
    event: Event,
    type: 'nid' | 'deed' | 'photo'
  ): void {

    const file =
      (event.target as HTMLInputElement)
      .files?.[0];

    if (!file) {
      return;
    }

    this.fileError = '';

    const maxMB =
      type === 'deed'
        ? 5
        : type === 'photo'
        ? 1
        : 2;

    if (file.size > maxMB * 1024 * 1024) {

      this.fileError =
        `File must be under ${maxMB}MB`;

      return;
    }

    if (
      type === 'photo' &&
      !file.type.startsWith('image/')
    ) {

      this.fileError =
        'Photo must be image file';

      return;
    }

    if (type === 'nid') {
      this.nidFile = file;
    }

    if (type === 'deed') {
      this.deedFile = file;
    }

    if (type === 'photo') {

      this.photoFile = file;

      const reader = new FileReader();

      reader.onload = (e: any) => {
        this.photoPreview = e.target.result;
      };

      reader.readAsDataURL(file);
    }
  }

  removeFile(
    type: 'nid' | 'deed' | 'photo'
  ): void {

    if (type === 'nid') {
      this.nidFile = null;
    }

    if (type === 'deed') {
      this.deedFile = null;
    }

    if (type === 'photo') {
      this.photoFile = null;
      this.photoPreview = null;
    }
  }

  // ================= SUBMIT =================

  submitForm(): void {

    if (
      !this.form.applicantName ||
      !this.form.nid ||
      !this.form.mobile ||
      !this.form.contactName ||
      !this.form.address
    ) {

      this.showToast(
        'Please fill all required fields',
        'error'
      );

      return;
    }

    if (!this.agreed) {

      this.showToast(
        'Please accept declaration',
        'error'
      );

      return;
    }

    this.isSubmitting = true;

    this.form.status = 'Pending';

    this.holdingService
      .createApplication(this.form)
      .subscribe({

        next: (res: any) => {

          const id = res?.id;

          if (
            id &&
            (
              this.nidFile ||
              this.deedFile ||
              this.photoFile
            )
          ) {

            this.holdingService.uploadDocuments(
              id,
              this.nidFile || undefined,
              this.deedFile || undefined,
              this.photoFile || undefined
            ).subscribe({

              next: () => {
                this.afterSubmitSuccess();
              },

              error: () => {
                this.afterSubmitSuccess();
              }
            });

          } else {

            this.afterSubmitSuccess();
          }
        },

        error: () => {

          this.isSubmitting = false;

          this.showToast(
            'Application submission failed',
            'error'
          );
        }
      });
  }

  afterSubmitSuccess(): void {

    this.isSubmitting = false;

    this.showToast(
      'Application submitted successfully',
      'success'
    );

    this.form = this.emptyForm();

    this.nidFile = null;
    this.deedFile = null;
    this.photoFile = null;

    this.photoPreview = null;

    this.agreed = false;

    this.currentStep = 1;

    this.loadApplications();

    setTimeout(() => {
      this.activeTab = 'list';
    }, 1500);
  }

  // ================= STATUS =================

  checkStatus(): void {

    if (!this.statusSearchText.trim()) {

      this.showToast(
        'Enter NID or Holding Number',
        'error'
      );

      return;
    }

    this.statusLoading = true;

    this.holdingService
      .getAllApplications()
      .subscribe({

        next: (res) => {

          const txt =
            this.statusSearchText
              .trim()
              .toLowerCase();

          this.statusResult =
            res.find(app =>

              app.nid?.toLowerCase() === txt ||

              app.holdingNo
                ?.toLowerCase() === txt

            ) || null;

          this.statusLoading = false;
          this.statusSearched = true;
        },

        error: () => {
          this.statusLoading = false;
        }
      });
  }

  // ================= DOWNLOAD =================

  openDownloadPopup(app: HoldingApplication | null): void {
    if (!app?.id) return;
    this.downloadModalApp = app;
  }

  closeDownloadPopup(): void {
    this.downloadModalApp = null;
  }

  confirmDownload(): void {
    const id = this.downloadModalApp?.id;
    if (!id) return;
    this.holdingService.downloadCertificate(id);
    this.downloadModalApp = null;
  }

  // ================= STATUS CLASS =================

  isApproved(status?: string): boolean {

    return (
      status || ''
    )
    .trim()
    .toLowerCase() === 'approved';
  }

  getStatusClass(status?: string): string {

    const s =
      (status || '')
      .trim()
      .toLowerCase();

    if (s === 'approved') {
      return 'badge-approved';
    }

    if (s === 'rejected') {
      return 'badge-rejected';
    }

    return 'badge-pending';
  }

  // ================= GPS =================

  captureGps(): void {

    this.gpsError = '';
    this.gpsLoading = true;

    if (!navigator.geolocation) {

      this.gpsError =
        'Browser GPS support করে না';

      this.gpsLoading = false;

      return;
    }

    navigator.geolocation.getCurrentPosition(

      (pos) => {

        this.gpsLoading = false;

        this.setLocation(
          pos.coords.latitude,
          pos.coords.longitude
        );
      },

      () => {

        this.gpsLoading = false;

        this.gpsError =
          'Location detect failed';
      },

      {
        timeout: 10000,
        enableHighAccuracy: true
      }
    );
  }

  setLocation(
    lat: number,
    lng: number
  ): void {

    this.form.latitude = lat;
    this.form.longitude = lng;

    if (!this.regMap) {
      return;
    }

    if (this.regMapMarker) {
      this.regMap.removeLayer(
        this.regMapMarker
      );
    }

    this.regMapMarker =
      L.marker([lat, lng])
      .addTo(this.regMap);

    this.regMap.setView(
      [lat, lng],
      17
    );
  }

  clearLocation(): void {

    this.form.latitude = undefined;
    this.form.longitude = undefined;

    if (
      this.regMap &&
      this.regMapMarker
    ) {

      this.regMap.removeLayer(
        this.regMapMarker
      );

      this.regMapMarker = null;
    }
  }

  initLocationMap(): void {

    const container =
      document.getElementById(
        'reg-location-map'
      );

    if (!container) {
      return;
    }

    if (this.regMap) {
      this.regMap.remove();
    }

    const defaultCenter = [
      23.8103,
      90.4125
    ];

    this.regMap = L.map(
      'reg-location-map',
      {
        center: defaultCenter,
        zoom: 13
      }
    );

    L.tileLayer(
      'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        attribution:
          '© OpenStreetMap'
      }
    ).addTo(this.regMap);

    this.regMap.on(
      'click',
      (e: any) => {

        this.setLocation(
          e.latlng.lat,
          e.latlng.lng
        );
      }
    );

    setTimeout(() => {
      this.regMap.invalidateSize();
    }, 300);
  }

  // ================= TOAST =================

  showToast(
    message: string,
    type: 'success' | 'error'
  ): void {

    const t: Toast = {
      type,
      message
    };

    this.toasts.push(t);

    setTimeout(() => {

      t.removing = true;

      setTimeout(() => {

        this.toasts =
          this.toasts.filter(
            x => x !== t
          );

      }, 300);

    }, 4000);
  }

  // ================= EMPTY FORM =================

  emptyForm(): HoldingApplication {

    return {

      applicantName: '',
      father: '',
      mother: '',
      nid: '',

      holdingNo: '',
      previousHoldingNo: '',

      road: '',
      area: '',
      mouza: '',

      ward: undefined,
      landSize: undefined,

      structureType: '',
      rooms: undefined,

      floorsTin: undefined,
      floorsPaka: undefined,

      unitsPerFloor: undefined,
      areaPerFloor: undefined,

      constructionYear: undefined,

      ownership: '',
      usageType: '',

      deedCopy: false,
      mutationCopy: false,
      nidCopy: false,
      citizenship: false,

      contactName: '',
      mobile: '',
      email: '',
      address: '',

      latitude: undefined,
      longitude: undefined
    };
  }
}