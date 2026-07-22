import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { WaterBill } from 'src/app/models/water-bill.model';
import { WaterConnection } from 'src/app/models/water-connection.model';
import { WaterBillService } from 'src/app/services/water-bill.service';
import { WaterConnectionService } from 'src/app/services/water-connection.service';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success'|'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-bill',
  templateUrl: './bill.component.html',
  styleUrls: ['./bill.component.css']
})
export class BillComponent implements OnInit {

  bills: WaterBill[] = [];
  connections: WaterConnection[] = [];
  isLoading    = false;
  isSubmitting = false;
  activeTab: 'generate'|'connections'|'bills'|'rates' = 'generate';
  toasts: Toast[] = [];
  assetBill: WaterBill | null = null;
  assetSignatureBase64 = '';
  assetSealBase64 = '';
  assetSignatureName = '';
  assetSealName = '';
  assetSubmitting = false;
  selectedConnection: WaterConnection | null = null;

  readonly rateChart = [
    { type: 'Residential', rate: 0.15, base: 50  },
    { type: 'Commercial',  rate: 0.30, base: 150 },
    { type: 'Industrial',  rate: 0.50, base: 300 },
    { type: 'Government',  rate: 0.10, base: 30  },
  ];

  calculatedUnits = 0;
  ratePerUnit     = 0;
  baseCharge      = 0;
  serviceCharge   = 0;
  totalAmount     = 0;

  readonly months = [
    'January 2026','February 2026','March 2026','April 2026',
    'May 2026','June 2026','July 2026','August 2026',
    'September 2026','October 2026','November 2026','December 2026'
  ];

  bill: WaterBill = this.emptyBill();

  constructor(
    public ls: LanguageService,
    private service: WaterBillService,
    private connectionService: WaterConnectionService,
    private router: Router
  ) {}

  ngOnInit(): void { this.loadBills(); this.loadConnections(); }

  loadBills(): void {
    this.isLoading = true;
    this.service.getAll().subscribe({
      next: r => { this.bills = r; this.isLoading = false; },
      error: () => this.isLoading = false
    });
  }

  loadConnections(): void {
    this.connectionService.getAll().subscribe({
      next: r => this.connections = r,
      error: () => this.showToast('Water connection applications failed to load.', 'error')
    });
  }

  switchTab(tab: 'generate'|'connections'|'bills'|'rates'): void {
    this.activeTab = tab;
    if (tab === 'bills') this.loadBills();
    if (tab === 'connections') this.loadConnections();
  }

  calcBill(): void {
    const prev = Number(this.bill.previousReading) || 0;
    const curr = Number(this.bill.currentReading)  || 0;
    this.calculatedUnits = Math.max(0, curr - prev);

    const rate = this.rateChart.find(r => r.type === this.bill.connectionType)
              ?? this.rateChart[0];

    this.ratePerUnit  = rate.rate;
    this.baseCharge   = rate.base;
    const subtotal    = this.calculatedUnits * rate.rate;
    this.serviceCharge = subtotal * 0.10;
    this.totalAmount  = subtotal + this.serviceCharge + rate.base;

    this.bill.units  = this.calculatedUnits;
    this.bill.amount = Math.round(this.totalAmount * 100) / 100;
  }

  generateBill(): void {
    if (!this.bill.name || !this.bill.mobile || !this.bill.meterNo || !this.bill.month) {
      this.showToast('Please fill all required fields', 'error'); return;
    }
    if (this.bill.currentReading <= this.bill.previousReading) {
      this.showToast('Current reading must be greater than previous reading', 'error'); return;
    }
    this.calcBill();
    this.isSubmitting = true;
    this.bill.status  = 'Unpaid';
    this.bill.billType = 'Auto';

    this.service.create(this.bill).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showToast('Bill generated successfully!', 'success');
        this.bill = this.emptyBill();
        this.calculatedUnits = 0; this.totalAmount = 0;
        this.loadBills();
        setTimeout(() => this.activeTab = 'bills', 1500);
      },
      error: () => {
        this.isSubmitting = false;
        this.showToast('Bill generation failed. Try again.', 'error');
      }
    });
  }

  payBill(bill: WaterBill): void {
    this.router.navigate(['/payment'], { queryParams: {
      serviceType:  'WaterBill',
      amount:       bill.amount,
      description:  `Meter: ${bill.meterNo} | Month: ${bill.month}`,
      serviceRefId: bill.id
    }});
  }

  uploadWaterAssets(bill: WaterBill): void {
    this.assetBill = bill;
    this.assetSignatureBase64 = '';
    this.assetSealBase64 = '';
    this.assetSignatureName = '';
    this.assetSealName = '';
    this.assetSubmitting = false;
  }

  closeAssetModal(): void {
    if (this.assetSubmitting) return;
    this.assetBill = null;
    this.assetSignatureBase64 = '';
    this.assetSealBase64 = '';
    this.assetSignatureName = '';
    this.assetSealName = '';
  }

  onAssetFile(event: Event, type: 'signature'|'seal'): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.showToast(type === 'signature' ? 'Please select a signature image.' : 'Please select a seal image.', 'error');
      input.value = '';
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      if (type === 'signature') {
        this.assetSignatureBase64 = String(reader.result || '');
        this.assetSignatureName = file.name;
      } else {
        this.assetSealBase64 = String(reader.result || '');
        this.assetSealName = file.name;
      }
    };
    reader.readAsDataURL(file);
  }

  submitAssetUpload(): void {
    if (!this.assetBill?.id || !this.assetSignatureBase64 || !this.assetSealBase64) return;
    this.assetSubmitting = true;
    this.service.updateAuthorityAssets(this.assetBill.id, this.assetSignatureBase64, this.assetSealBase64).subscribe({
      next: () => {
        this.showToast('Water bill signature and seal saved.', 'success');
        this.assetSubmitting = false;
        this.assetBill = null;
        this.assetSignatureBase64 = '';
        this.assetSealBase64 = '';
        this.assetSignatureName = '';
        this.assetSealName = '';
        this.loadBills();
      },
      error: err => {
        this.assetSubmitting = false;
        this.showToast(err?.error?.message || 'Upload failed', 'error');
      }
    });
  }

  updateConnectionStatus(app: WaterConnection, status: 'Approved'|'Rejected'): void {
    if (!app.id) return;
    this.connectionService.updateStatus(app.id, status).subscribe({
      next: () => {
        app.status = status;
        this.showToast(`Water connection ${status.toLowerCase()}.`, 'success');
        this.loadConnections();
      },
      error: err => this.showToast(err?.error?.message || 'Status update failed.', 'error')
    });
  }

  openConnectionDetails(app: WaterConnection): void {
    this.selectedConnection = app;
  }

  closeConnectionDetails(): void {
    this.selectedConnection = null;
  }

  connectionDetailFields(app: WaterConnection): { key: string; value: any }[] {
    return [
      { key: 'Application ID', value: app.id },
      { key: 'Applicant Name', value: app.name },
      { key: 'Father / Husband Name', value: app.fatherName },
      { key: 'NID', value: app.nid },
      { key: 'Phone', value: app.phone },
      { key: 'Email', value: app.email },
      { key: 'Connection Type', value: app.connectionType },
      { key: 'Family Members', value: app.members },
      { key: 'District', value: app.district },
      { key: 'Upazila', value: app.upazila },
      { key: 'Ward', value: app.ward },
      { key: 'Address', value: app.address },
      { key: 'Expected Start Date', value: app.startDate },
      { key: 'Usage Purpose', value: app.usage },
      { key: 'Description', value: app.description },
      { key: 'Declaration Accepted', value: app.agree ? 'Yes' : 'No' },
      { key: 'Status', value: app.status || 'Pending' },
      { key: 'Submitted At', value: app.createdAt }
    ].filter(f => f.value !== null && f.value !== undefined && String(f.value).trim() !== '');
  }

  connectionCount(status: string): number {
    return this.connections.filter(c => (c.status || '').toLowerCase() === status.toLowerCase()).length;
  }

  get paidCount(): number {
    return this.bills.filter(b => b.status?.toLowerCase() === 'paid').length;
  }

  showToast(message: string, type: 'success'|'error'): void {
    const t: Toast = { type, message };
    this.toasts.push(t);
    setTimeout(() => { t.removing = true; setTimeout(() => this.toasts = this.toasts.filter(x => x !== t), 300); }, 4000);
  }

  emptyBill(): WaterBill {
    return {
      name: '', email: '', mobile: '', nid: '', meterNo: '', month: '',
      previousReading: 0, currentReading: 0,
      connectionType: 'Residential', billType: 'Auto',
      units: 0, amount: 0, status: 'Unpaid'
    };
  }
}
