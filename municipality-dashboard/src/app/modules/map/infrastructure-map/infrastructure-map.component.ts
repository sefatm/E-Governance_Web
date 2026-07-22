import { Component, OnDestroy, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { GisMapService } from 'src/app/services/gis-map.service';
import { LanguageService } from 'src/app/services/language.service';
import { AuthService } from 'src/app/services/auth.service';

declare const L: any;

interface Layer {
  key: string;
  label: string;
  icon: string;
  emoji: string;
  color: string;
  visible: boolean;
  count: number;
  pinnable: boolean;
}

@Component({
  selector: 'app-infrastructure-map',
  templateUrl: './infrastructure-map.component.html',
  styleUrls: ['./infrastructure-map.component.css']
})
export class InfrastructureMapComponent implements OnInit, OnDestroy {
  private map: any = null;
  private markers: { [key: string]: any[] } = {};
  private wardLayer: any = null;
  private userMarker: any = null;
  private tempMarker: any = null;

  isLoading = false;
  locating = false;
  selectedItem: any = null;
  selectedType = '';
  pinModeActive = false;
  pendingLocation: { lat: number; lng: number } | null = null;
  isSavingPin = false;
  successMsg = '';
  errorMsg = '';

  searchText = '';
  filterWard = '';
  filterStatus = '';
  filterType = '';
  activeTab = 'all';
  wardVisible = true;
  wardGeoJson: any = null;

  wardOptions = ['1','2','3','4','5','6','7','8','9'];
  statusOptions = ['Pending','Approved','Rejected','In Progress','Completed','Resolved','Active'];

  data: { [key: string]: any[] } = {
    road: [], drainage: [], streetLight: [], construction: [],
    complaint: [], health: [], waste: [], wastePickup: [], holding: []
  };

  layers: Layer[] = [
    { key:'road',         label:'Road',          icon:'fas fa-road',       emoji:'🛣️', color:'#2563eb', visible:true,  count:0, pinnable:true },
    { key:'drainage',     label:'Drainage',      icon:'fas fa-water',      emoji:'💧', color:'#0891b2', visible:true,  count:0, pinnable:true },
    { key:'streetLight',  label:'Street Light',  icon:'fas fa-lightbulb',  emoji:'💡', color:'#d97706', visible:true,  count:0, pinnable:true },
    { key:'construction', label:'Construction',  icon:'fas fa-hard-hat',   emoji:'🏗️', color:'#7c3aed', visible:true,  count:0, pinnable:true },
    { key:'complaint',    label:'Complaint',     icon:'fas fa-bullhorn',   emoji:'📣', color:'#dc2626', visible:true,  count:0, pinnable:true },
    { key:'health',       label:'Health Center', icon:'fas fa-clinic-medical', emoji:'🏥', color:'#059669', visible:true, count:0, pinnable:true },
    { key:'wastePickup',  label:'Waste Pickup',  icon:'fas fa-truck-pickup', emoji:'🚛', color:'#16a34a', visible:true, count:0, pinnable:false },
    { key:'waste',        label:'Waste Zone',    icon:'fas fa-trash-alt',  emoji:'♻️', color:'#4b5563', visible:false, count:0, pinnable:false },
    { key:'holding',      label:'Holding',       icon:'fas fa-home',       emoji:'🏠', color:'#0f766e', visible:false, count:0, pinnable:false }
  ];

  private priorityColors: any = { High:'#dc2626', Medium:'#d97706', Low:'#059669' };

  constructor(
    public ls: LanguageService,
    private gisMapService: GisMapService,
    private authService: AuthService
  ) {}

  ngOnInit(): void { this.loadAll(); }
  ngOnDestroy(): void { if (this.map) { this.map.remove(); this.map = null; } }

  loadAll(): void {
    this.isLoading = true;
    forkJoin({
      road: this.gisMapService.getRoads(),
      drainage: this.gisMapService.getDrainage(),
      streetLight: this.gisMapService.getStreetLights(),
      construction: this.gisMapService.getConstruction(),
      complaint: this.gisMapService.getComplaints(),
      health: this.gisMapService.getHealthCenters(),
      waste: this.gisMapService.getGarbageZones(),
      wastePickup: this.gisMapService.getWastePickups(),
      holding: this.gisMapService.getHoldings(),
      wards: this.gisMapService.getWardBoundaries()
    }).subscribe({
      next: (res: any) => {
        this.data['road'] = this.toItems(res.road);
        this.data['drainage'] = this.toItems(res.drainage);
        this.data['streetLight'] = this.toItems(res.streetLight);
        this.data['construction'] = this.toItems(res.construction);
        this.data['complaint'] = this.toItems(res.complaint);
        this.data['health'] = this.toItems(res.health);
        this.data['waste'] = this.toItems(res.waste);
        this.data['wastePickup'] = this.toItems(res.wastePickup);
        this.data['holding'] = this.toItems(res.holding);
        this.wardGeoJson = res.wards;
        this.finishLoad();
      },
      error: () => {
        Object.keys(this.data).forEach(k => this.data[k] = []);
        this.errorMsg = 'Unable to load map data from the server.';
        this.finishLoad();
      }
    });
  }

  private toItems(value: any): any[] {
    if (Array.isArray(value)) return value;
    if (value?.features) {
      return value.features.map((f: any) => {
        const c = f?.geometry?.coordinates;
        return { ...(f.properties || {}), lat: c?.[1], lng: c?.[0] };
      });
    }
    return [];
  }

  private finishLoad(): void {
    this.layers.forEach(l => l.count = this.data[l.key]?.length || 0);
    this.isLoading = false;
    setTimeout(() => this.initMap(), 100);
  }

  private initMap(): void {
    if (typeof L === 'undefined') { this.errorMsg = 'Leaflet map library is not loaded.'; return; }
    if (this.map) this.map.remove();

    this.map = L.map('infra-map-container', { center: [23.1668, 90.1988], zoom: 14, zoomControl: true });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);

    this.map.on('click', (e: any) => {
      if (!this.pinModeActive || !this.selectedItem) return;
      this.pendingLocation = { lat: e.latlng.lat, lng: e.latlng.lng };
      if (this.tempMarker) this.map.removeLayer(this.tempMarker);
      this.tempMarker = L.marker([e.latlng.lat, e.latlng.lng])
        .addTo(this.map).bindPopup('Selected location — click Save Pin').openPopup();
    });

    this.layers.forEach(l => this.markers[l.key] = []);
    this.renderWardLayer();
    this.renderAll();
    setTimeout(() => this.fitAll(), 150);
  }

  renderAll(): void {
    if (!this.map) return;
    this.layers.forEach(layer => {
      (this.markers[layer.key] || []).forEach(m => this.map.removeLayer(m));
      this.markers[layer.key] = [];
      if (!layer.visible) return;

      this.getFilteredItems(layer.key).forEach((item: any) => {
        const lat = Number(item.latitude ?? item.lat);
        const lng = Number(item.longitude ?? item.lng);
        if (!Number.isFinite(lat) || !Number.isFinite(lng) || lat === 0 || lng === 0) return;

        const pColor = this.priorityColors[item.priority] || layer.color;
        const divIcon = L.divIcon({
          html: `<div class="infra-pin" style="--pc:${layer.color};--prio:${pColor}"><span class="ip-emoji">${layer.emoji}</span><div class="ip-prio-dot" style="background:${pColor}"></div></div>`,
          className: '', iconSize: [36,36], iconAnchor: [18,36], popupAnchor: [0,-36]
        });
        const marker = L.marker([lat,lng], { icon: divIcon }).addTo(this.map).bindPopup(this.buildPopup(item, layer));
        marker.on('click', () => { this.selectedItem = item; this.selectedType = layer.key; });
        this.markers[layer.key].push(marker);
      });
    });
  }

  private renderWardLayer(): void {
    if (!this.map) return;
    if (this.wardLayer) this.map.removeLayer(this.wardLayer);
    if (!this.wardVisible || !this.wardGeoJson?.features?.length) return;
    this.wardLayer = L.geoJSON(this.wardGeoJson, {
      style: { color: '#0f766e', weight: 2, fillOpacity: 0.08, dashArray: '5,5' },
      onEachFeature: (feature: any, layer: any) => {
        const p = feature.properties || {};
        layer.bindPopup(`<b>Ward ${p.wardNo ?? '—'}</b><br>Population: ${p.population ?? '—'}<br>Area: ${p.areaSqKm ?? '—'} sq km`);
      }
    }).addTo(this.map);
  }

  toggleWardLayer(): void { this.wardVisible = !this.wardVisible; this.renderWardLayer(); }

  locateMe(): void {
    if (!navigator.geolocation) { this.errorMsg = 'Geolocation is not supported by this browser.'; return; }
    this.locating = true;
    navigator.geolocation.getCurrentPosition(
      pos => {
        this.locating = false;
        const lat = pos.coords.latitude, lng = pos.coords.longitude;
        if (this.userMarker) this.map.removeLayer(this.userMarker);
        this.userMarker = L.marker([lat,lng]).addTo(this.map).bindPopup('Your current location').openPopup();
        this.map.setView([lat,lng], 17, { animate: true });
      },
      err => { this.locating = false; this.errorMsg = err.message || 'Unable to read current location.'; },
      { enableHighAccuracy: true, timeout: 12000, maximumAge: 30000 }
    );
  }

  fitAll(): void {
    if (!this.map) return;
    const points: any[] = [];
    this.layers.filter(l => l.visible).forEach(l => {
      this.getFilteredItems(l.key).forEach(i => {
        const lat = Number(i.latitude ?? i.lat), lng = Number(i.longitude ?? i.lng);
        if (Number.isFinite(lat) && Number.isFinite(lng) && lat !== 0 && lng !== 0) points.push([lat,lng]);
      });
    });
    if (points.length) this.map.fitBounds(L.latLngBounds(points).pad(0.15));
  }

  private buildPopup(item: any, layer: Layer): string {
    const name = item.applicantName || item.ownerName || item.centerName || item.name || item.roadName || item.area || 'N/A';
    const status = item.status || '—';
    return `<div class="infra-popup"><div class="ip-header" style="background:${layer.color}"><span>${layer.emoji} ${layer.label}</span><span class="ip-ward">Ward ${item.ward || '—'}</span></div><div class="ip-body"><div class="ip-row"><b>Name:</b> ${name}</div>${item.area ? `<div class="ip-row"><b>Area:</b> ${item.area}</div>` : ''}${item.location ? `<div class="ip-row"><b>Location:</b> ${item.location}</div>` : ''}${item.category ? `<div class="ip-row"><b>Category:</b> ${item.category}</div>` : ''}<div class="ip-row"><b>Status:</b> ${status}</div></div></div>`;
  }

  enablePinMode(): void {
    const layer = this.getLayerByKey(this.selectedType);
    if (!this.selectedItem || !layer?.pinnable) { this.errorMsg = 'Select a pinnable map item first.'; return; }
    this.pinModeActive = true;
    this.pendingLocation = null;
    this.map?.getContainer().style.setProperty('cursor','crosshair');
  }

  cancelPinMode(): void {
    this.pinModeActive = false;
    this.pendingLocation = null;
    if (this.tempMarker) { this.map?.removeLayer(this.tempMarker); this.tempMarker = null; }
    if (this.map) this.map.getContainer().style.cursor = '';
  }

  savePin(): void {
    if (!this.pendingLocation || !this.selectedItem) return;
    const {lat,lng} = this.pendingLocation;
    const id = this.selectedItem.roadId || this.selectedItem.drainageId || this.selectedItem.lightId || this.selectedItem.siteId || this.selectedItem.complaintId || this.selectedItem.centerId || this.selectedItem.id;
    if (!id) { this.errorMsg = 'Selected item id was not found.'; return; }
    let obs$: any;
    switch (this.selectedType) {
      case 'road': obs$ = this.gisMapService.updateRoadLocation(id,lat,lng); break;
      case 'drainage': obs$ = this.gisMapService.updateDrainageLocation(id,lat,lng); break;
      case 'streetLight': obs$ = this.gisMapService.updateStreetLightLocation(id,lat,lng); break;
      case 'construction': obs$ = this.gisMapService.updateConstructionLocation(id,lat,lng); break;
      case 'complaint': obs$ = this.gisMapService.updateComplaintLocation(id,lat,lng); break;
      case 'health': obs$ = this.gisMapService.updateHealthCenterLocation(id,lat,lng); break;
      default: this.errorMsg = 'This layer does not support pin updates.'; return;
    }
    this.isSavingPin = true;
    obs$.subscribe({
      next: () => {
        this.selectedItem.lat = lat; this.selectedItem.lng = lng;
        this.selectedItem.latitude = lat; this.selectedItem.longitude = lng;
        this.isSavingPin = false; this.cancelPinMode(); this.renderAll();
        this.successMsg = 'Location saved successfully.';
        setTimeout(() => this.successMsg = '', 3500);
      },
      error: (err: any) => { this.isSavingPin = false; this.errorMsg = err?.error?.message || 'Location save failed.'; }
    });
  }

  toggleLayer(layer: Layer): void { layer.visible = !layer.visible; this.renderAll(); }
  setTab(tab: string): void { this.activeTab = tab; this.filterType = tab === 'all' ? '' : tab; }
  applyFilter(): void { this.renderAll(); }
  resetFilter(): void { this.searchText=''; this.filterWard=''; this.filterStatus=''; this.filterType=''; this.activeTab='all'; this.renderAll(); }

  getFilteredItems(key: string): any[] {
    return (this.data[key] || []).filter(item => {
      const txt = this.searchText.trim().toLowerCase();
      const haystack = [item.name,item.applicantName,item.ownerName,item.centerName,item.area,item.roadName,item.location,item.category,item.holdingNo]
        .filter(Boolean).join(' ').toLowerCase();
      const matchSearch = !txt || haystack.includes(txt);
      const matchWard = !this.filterWard || String(item.ward).replace(/^0+/, '') === this.filterWard;
      const matchStatus = !this.filterStatus || String(item.status || '').toLowerCase() === this.filterStatus.toLowerCase();
      return matchSearch && matchWard && matchStatus;
    });
  }

  selectItem(item: any, key: string): void {
    this.selectedItem = item; this.selectedType = key;
    const lat = Number(item.latitude ?? item.lat), lng = Number(item.longitude ?? item.lng);
    if (Number.isFinite(lat) && Number.isFinite(lng) && lat && lng && this.map) this.map.setView([lat,lng],16,{animate:true});
  }

  get listItems(): any[] {
    if (this.activeTab === 'all') return this.layers.flatMap(l => this.getFilteredItems(l.key).map(i => ({...i,_type:l.key,_layer:l})));
    return this.getFilteredItems(this.activeTab).map(i => ({...i,_type:this.activeTab,_layer:this.layers.find(l=>l.key===this.activeTab)}));
  }

  get totalCount(): number { return this.layers.reduce((s,l)=>s+l.count,0); }
  get pinnedCount(): number { return this.layers.reduce((s,l)=>s+(this.data[l.key]||[]).filter((i:any)=>Number(i.lat ?? i.latitude) && Number(i.lng ?? i.longitude)).length,0); }
  get unpinnedCount(): number { return this.totalCount - this.pinnedCount; }
  getLayerByKey(key: string): Layer | undefined { return this.layers.find(l=>l.key===key); }
  get canManagePins(): boolean {
    return this.authService.hasRole(
      'Super Admin',
      'Admin',
      'Admin / Municipal Officer',
      'Department Officer',
      'Project Officer',
      'Health / Sanitation Officer'
    );
  }
  itemKey(item: any): string {
    return String(item?.id ?? item?.roadId ?? item?.drainageId ?? item?.lightId ?? item?.siteId ?? item?.complaintId ?? item?.centerId ?? item?.requestId ?? item?.zoneId ?? item?.holdingId ?? '');
  }
  isSelectedItem(item: any): boolean {
    return !!this.selectedItem && this.selectedType === item._type && this.itemKey(this.selectedItem) === this.itemKey(item);
  }
  canPinSelected(): boolean { return this.canManagePins && !!this.getLayerByKey(this.selectedType)?.pinnable; }
}
