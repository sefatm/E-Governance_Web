import { Component, OnInit, OnDestroy } from '@angular/core';
import { HoldingService } from 'src/app/services/holding-new-registration.service';
import { LanguageService } from 'src/app/services/language.service';

declare const L: any;

@Component({
  selector: 'app-holding-location-map',
  templateUrl: './holding-location-map.component.html',
  styleUrls: ['./holding-location-map.component.css']
})
export class HoldingLocationMapComponent implements OnInit, OnDestroy {

  private map       : any   = null;
  private markers   : any[] = [];
  private tempMarker: any   = null;

  holdings         : any[] = [];
  filteredHoldings : any[] = [];
  isLoading        = false;

  selectedHolding  : any  = null;
  pinModeActive    = false;
  pendingLocation  : any  = null;
  isSavingPin      = false;

  successMsg = '';
  errorMsg   = '';

  searchText   = '';
  filterWard   = '';
  filterStatus = '';

  private wardColors: any = {
    1:'#2563eb', 2:'#059669', 3:'#d97706', 4:'#7c3aed',
    5:'#db2777', 6:'#0891b2', 7:'#65a30d', 8:'#dc2626', 9:'#0d9488'
  };

  constructor(public ls: LanguageService, private holdingService: HoldingService) {}

  ngOnInit(): void { this.loadHoldings(); }

  ngOnDestroy(): void {
    if (this.map) { this.map.remove(); this.map = null; }
  }

  loadHoldings(): void {
    this.isLoading = true;
    this.holdingService.getAllApplications().subscribe({
      next: (res) => {
        this.holdings = res;
        this.filteredHoldings = [...res];
        this.isLoading = false;
        setTimeout(() => this.initMap(), 150);
      },
      error: () => {
        this.isLoading = false;
        setTimeout(() => this.initMap(), 150);
      }
    });
  }

  private initMap(): void {
    if (typeof L === 'undefined') { console.error('Leaflet not loaded'); return; }
    if (this.map) this.map.remove();

    this.map = L.map('holding-map-container', {
      center: [23.1650, 90.1890], zoom: 14
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19, attribution: '© OpenStreetMap'
    }).addTo(this.map);

    this.map.on('click', (e: any) => {
      if (!this.pinModeActive || !this.selectedHolding) return;
      this.pendingLocation = { lat: e.latlng.lat, lng: e.latlng.lng };
      if (this.tempMarker) this.map.removeLayer(this.tempMarker);
      this.tempMarker = L.marker([e.latlng.lat, e.latlng.lng])
        .addTo(this.map).bindPopup('Click Save Pin to confirm').openPopup();
    });

    this.renderPins();
  }

  renderPins(): void {
    this.markers.forEach(m => this.map?.removeLayer(m));
    this.markers = [];

    this.filteredHoldings.filter(h => h.latitude && h.longitude).forEach(h => {
      const color = this.wardColors[h.ward] || '#374151';
      const icon  = L.divIcon({
        html: `<div style="background:${color};width:28px;height:28px;border-radius:50% 50% 50% 0;transform:rotate(-45deg);border:2px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,.3);display:flex;align-items:center;justify-content:center">
                 <span style="transform:rotate(45deg);font-size:12px;color:#fff">H</span></div>`,
        className: '', iconSize: [28,28], iconAnchor: [14,28], popupAnchor: [0,-28]
      });
      const marker = L.marker([h.latitude, h.longitude], { icon })
        .addTo(this.map)
        .bindPopup(this.buildPopup(h));
      marker.on('click', () => this.selectedHolding = h);
      this.markers.push(marker);
    });

    if (this.markers.length > 0) {
      this.map.fitBounds(L.featureGroup(this.markers).getBounds().pad(0.15));
    }
  }

  private buildPopup(h: any): string {
    const color  = this.wardColors[h.ward] || '#374151';
    const status = this.cleanStatus(h.status);
    return `<div style="min-width:180px">
      <div style="background:${color};padding:8px 12px;border-radius:6px 6px 0 0;color:#fff;font-weight:700;font-size:13px">
        ${h.holdingNo || 'N/A'} <span style="float:right;opacity:.8;font-size:11px">Ward ${h.ward}</span>
      </div>
      <div style="padding:8px 12px;font-size:12px;color:#374151">
        <div><b>Owner:</b> ${h.applicantName || '—'}</div>
        <div><b>Area:</b> ${h.area || '—'}</div>
        <div><b>Status:</b> <span style="color:${status==='Approved'?'#059669':'#d97706'}">${status}</span></div>
      </div></div>`;
  }

  cleanStatus(status: string): string {
    return (status || 'Pending').replace(/['"]/g, '').trim();
  }

  applyFilter(): void {
    this.filteredHoldings = this.holdings.filter(h => {
      const s = this.cleanStatus(h.status);
      return (
        (!this.searchText   || (h.holdingNo || '').toLowerCase().includes(this.searchText.toLowerCase()) ||
                               (h.applicantName || '').toLowerCase().includes(this.searchText.toLowerCase())) &&
        (!this.filterWard   || String(h.ward) === String(this.filterWard)) &&
        (!this.filterStatus || s === this.filterStatus)
      );
    });
    this.renderPins();
  }

  resetFilter(): void {
    this.searchText = ''; this.filterWard = ''; this.filterStatus = '';
    this.filteredHoldings = [...this.holdings];
    this.renderPins();
  }

  selectHolding(h: any): void {
    this.selectedHolding = h;
    if (h.latitude && h.longitude && this.map)
      this.map.setView([h.latitude, h.longitude], 16, { animate: true });
  }

  enablePinMode(): void {
    if (!this.selectedHolding) {
      this.errorMsg = 'Please select a holding from the list first.';
      setTimeout(() => this.errorMsg = '', 3000);
      return;
    }
    this.pinModeActive = true; this.pendingLocation = null;
    this.successMsg = ''; this.errorMsg = '';
    if (this.map) this.map.getContainer().style.cursor = 'crosshair';
  }

  cancelPinMode(): void {
    this.pinModeActive = false; this.pendingLocation = null;
    if (this.tempMarker) { this.map?.removeLayer(this.tempMarker); this.tempMarker = null; }
    if (this.map) this.map.getContainer().style.cursor = '';
  }

  savePin(): void {
    if (!this.pendingLocation || !this.selectedHolding) return;
    this.isSavingPin = true;
    this.holdingService.updateLocation(
      this.selectedHolding.id,
      this.pendingLocation.lat,
      this.pendingLocation.lng
    ).subscribe({
      next: () => {
        this.selectedHolding.latitude  = this.pendingLocation.lat;
        this.selectedHolding.longitude = this.pendingLocation.lng;
        this.isSavingPin = false;
        this.cancelPinMode();
        this.renderPins();
        this.successMsg = `Location saved for ${this.selectedHolding.holdingNo || 'holding'}`;
        setTimeout(() => this.successMsg = '', 4000);
      },
      error: (err: any) => {
        this.isSavingPin = false;
        this.errorMsg = err?.error?.message || 'Failed to save location. Please try again.';
        setTimeout(() => this.errorMsg = '', 5000);
      }
    });
  }

  get pinnedCount()   : number { return this.holdings.filter(h => h.latitude && h.longitude).length; }
  get unpinnedCount() : number { return this.holdings.length - this.pinnedCount; }
  getWardColor(ward: number): string { return this.wardColors[ward] || '#374151'; }
}
