import { Component, OnInit, OnDestroy } from '@angular/core';
import { WardService, Ward } from 'src/app/services/ward.service';
import { LanguageService } from 'src/app/services/language.service';

declare const L: any;


@Component({
  selector: 'app-ward-map',
  templateUrl: './ward-map.component.html',
  styleUrls: ['./ward-map.component.css']
})
export class WardMapComponent implements OnInit, OnDestroy {

  private map: any     = null;
  private layers: any[] = [];

  wards:       Ward[] = [];
  selectedWard: Ward | null = null;
  isLoading    = false;

  private center = [23.1675, 90.2065]; // Madaripur Municipality center
  private colors = ['#2563eb','#059669','#d97706','#7c3aed','#db2777',
                    '#0891b2','#65a30d','#dc2626','#0d9488'];

filterOptions = ['All', 'Active', 'Inactive'];
activeFilter = 'All';

get totalPopulation(): number {
  return this.wards.reduce((sum, w) => sum + (w.population || 0), 0);
}

get activeCount(): number {
  return this.wards.filter(w => w.status === 'Active').length;
}

get filteredWards(): Ward[] {
  if (this.activeFilter === 'All') {
    return this.wards;
  }

  return this.wards.filter(
    w => w.status === this.activeFilter
  );
}

applyFilter(filter: string): void {
  this.activeFilter = filter;
  if (this.selectedWard && !this.filteredWards.some(w => w.id === this.selectedWard?.id)) {
    this.selectedWard = null;
  }
  this.renderWards();
}

  constructor(public ls: LanguageService, private wardService: WardService) {}

  ngOnInit(): void { this.loadWards(); }

  ngOnDestroy(): void { if (this.map) { this.map.remove(); this.map = null; } }

  loadWards(): void {
    this.isLoading = true;
    this.wardService.getAllWithBoundaries().subscribe({
      next: (res) => {
        this.wards     = res;
        this.isLoading = false;
        setTimeout(() => this.initMap(), 150);
      },
      error: () => { this.isLoading = false; setTimeout(() => this.initMap(), 150); }
    });
  }

  private initMap(): void {
    if (typeof L === 'undefined') return;
    if (this.map) this.map.remove();

    this.map = L.map('ward-map-container', { center: this.center, zoom: 14 });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19, attribution: '© OpenStreetMap'
    }).addTo(this.map);

    this.renderWards();
  }

  renderWards(): void {
    this.layers.forEach(l => this.map?.removeLayer(l));
    this.layers = [];

    this.filteredWards.forEach((ward) => {
      const sourceIndex = this.wards.findIndex(w => w.id === ward.id || w.number === ward.number);
      const color = this.colors[(sourceIndex >= 0 ? sourceIndex : ward.number - 1) % this.colors.length];
      // Use ward.number-1 as boundary index so Ward 1 → WARD_BOUNDARIES[0]
      const boundaryIndex = (ward.number >= 1 && ward.number <= 9) ? ward.number - 1 : sourceIndex;

      let latlngs: number[][];
      if (ward.boundaryGeoJson) {
        try {
          const coords: number[][] = JSON.parse(ward.boundaryGeoJson);
          latlngs = coords.map(([lng, lat]) => [lat, lng]);
        } catch {
          latlngs = this.buildPolygon(boundaryIndex);
        }
      } else {
        latlngs = this.buildPolygon(boundaryIndex);
      }

      const layer = L.polygon(latlngs, {
        color, weight: 2, fillColor: color, fillOpacity: 0.25
      }).addTo(this.map);

      layer.bindPopup(this.buildPopup(ward, color));
      layer.on('click', () => { this.selectedWard = ward; });
      layer.on('mouseover', function(this: any) { this.setStyle({ fillOpacity: 0.5 }); });
      layer.on('mouseout',  function(this: any) { this.setStyle({ fillOpacity: 0.25 }); });

      const bounds = layer.getBounds();
      const label  = L.divIcon({
        html: `<div style="background:${color};color:#fff;font-size:11px;font-weight:700;padding:2px 7px;border-radius:10px;white-space:nowrap;box-shadow:0 1px 4px rgba(0,0,0,.2)">W${ward.number}</div>`,
        className: '', iconSize: [32, 20], iconAnchor: [16, 10]
      });
      const marker = L.marker(bounds.getCenter(), { icon: label }).addTo(this.map);

      this.layers.push(layer, marker);
    });
  }

  /**
   * Real approximate boundaries for Madaripur Municipality's 9 wards.
   * Coordinates are [lat, lng] (Leaflet order).
   * Ward number is 1-based; index is 0-based (ward.number - 1).
   * These are used as fallback when ward_boundary table has no data.
   */
  private readonly WARD_BOUNDARIES: number[][][] = [
    // Ward 1 — south-center
    [[23.1580,90.1995],[23.1580,90.2055],[23.1630,90.2055],[23.1630,90.1995],[23.1580,90.1995]],
    // Ward 2 — south-right (Kulpaddi area)
    [[23.1570,90.2055],[23.1570,90.2145],[23.1635,90.2145],[23.1635,90.2055],[23.1570,90.2055]],
    // Ward 3 — center
    [[23.1630,90.1995],[23.1630,90.2065],[23.1690,90.2065],[23.1690,90.1995],[23.1630,90.1995]],
    // Ward 4 — center-right
    [[23.1635,90.2055],[23.1635,90.2145],[23.1700,90.2145],[23.1700,90.2055],[23.1635,90.2055]],
    // Ward 5 — west
    [[23.1540,90.1925],[23.1540,90.1995],[23.1640,90.1995],[23.1640,90.1925],[23.1540,90.1925]],
    // Ward 6 — east (2 No Sakuni area)
    [[23.1545,90.2140],[23.1545,90.2230],[23.1630,90.2230],[23.1630,90.2140],[23.1545,90.2140]],
    // Ward 7 — northwest
    [[23.1650,90.1920],[23.1650,90.2000],[23.1730,90.2000],[23.1730,90.1920],[23.1650,90.1920]],
    // Ward 8 — north-center
    [[23.1690,90.1995],[23.1690,90.2075],[23.1775,90.2075],[23.1775,90.1995],[23.1690,90.1995]],
    // Ward 9 — northeast (College Road area)
    [[23.1700,90.2070],[23.1700,90.2200],[23.1810,90.2200],[23.1810,90.2070],[23.1700,90.2070]],
  ];

  private buildPolygon(index: number): number[][] {
    // Use real ward boundary if index is within range
    if (index >= 0 && index < this.WARD_BOUNDARIES.length) {
      return this.WARD_BOUNDARIES[index];
    }
    // Generic grid fallback for any extra wards
    const [lat, lng] = this.center;
    const cols  = 3;
    const row   = Math.floor(index / cols);
    const col   = index % cols;
    const dLat  = 0.012;
    const dLng  = 0.015;
    const baseLat = lat - 0.02 + row * dLat;
    const baseLng = lng - 0.025 + col * dLng;
    return [
      [baseLat,           baseLng          ],
      [baseLat + dLat,    baseLng          ],
      [baseLat + dLat,    baseLng + dLng   ],
      [baseLat,           baseLng + dLng   ],
    ];
  }

  private buildPopup(ward: Ward, color: string): string {
    return `
      <div style="min-width:180px;font-family:sans-serif">
        <div style="background:${color};color:#fff;padding:8px 12px;border-radius:6px 6px 0 0;font-weight:700">
          Ward ${ward.number} — ${ward.name}
        </div>
        <div style="padding:10px 12px;font-size:12px;color:#374151;line-height:1.8">
          <div><b>Representative:</b> ${ward.representative || '—'}</div>
          <div><b>Population:</b> ${ward.population?.toLocaleString() || '—'}</div>
          <div><b>Area:</b> ${ward.area || '—'} sq km</div>
          <div><b>Status:</b>
            <span style="color:${ward.status==='Active'?'#059669':'#dc2626'}">${ward.status || 'Active'}</span>
          </div>
        </div>
      </div>`;
  }

  zoomToWard(ward: Ward, index: number): void {
    this.selectedWard = ward;
    if (!this.map) return;

    const boundaryIndex = (ward.number >= 1 && ward.number <= 9) ? ward.number - 1 : index;

    let latlngs: number[][];
    if (ward.boundaryGeoJson) {
      try {
        const coords: number[][] = JSON.parse(ward.boundaryGeoJson);
        latlngs = coords.map(([lng, lat]) => [lat, lng]);
      } catch {
        latlngs = this.buildPolygon(boundaryIndex);
      }
    } else {
      latlngs = this.buildPolygon(boundaryIndex);
    }

    const bounds = L.latLngBounds(latlngs);
    this.map.fitBounds(bounds.pad(0.15), { animate: true });
  }

selectWard(ward: Ward): void {
  this.selectedWard = ward;

  const index = this.wards.findIndex(w => w.number === ward.number);
  if (index !== -1) {
    this.zoomToWard(ward, index);
  }
}

getWardColor(index: number): string {
  return this.colors[index % this.colors.length];
}
}
