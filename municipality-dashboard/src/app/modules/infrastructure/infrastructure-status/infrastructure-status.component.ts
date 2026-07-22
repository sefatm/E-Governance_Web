import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LanguageService } from 'src/app/services/language.service';

type InfraType = 'Construction' | 'Road' | 'Drainage' | 'Street Light';

interface DetailField {
  key: string;
  value: string;
}

interface FileEntry {
  label: string;
  url: string;
  isImage: boolean;
}

interface InfraApplication {
  id: number;
  name: string;
  type: InfraType;
  status: string;
  date: string;
  contact: string;
  location: string;
  details: any;
  detailFields: DetailField[];
  fileEntries: FileEntry[];
}

@Component({
  selector: 'app-infrastructure-status',
  templateUrl: './infrastructure-status.component.html',
  styleUrls: ['./infrastructure-status.component.css']
})
export class InfrastructureStatusComponent implements OnInit {

  applications: InfraApplication[] = [];
  filteredData: InfraApplication[] = [];
  expandedKey = '';
  isLoading = false;

  searchText = '';
  selectedType = '';
  selectedStatus = '';

  readonly baseUrl = (environment.serverUrl || environment.apiUrl.replace(/\/api$/, '')).replace(/\/$/, '');

  private readonly hiddenDetailKeys = new Set([
    'id', 'status', 'createdAt', 'updatedAt', 'lat', 'lng'
  ]);

  constructor(public ls: LanguageService, private http: HttpClient) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;

    forkJoin({
      construction: this.http.get<any[]>(`${environment.apiUrl}/construction/getall`).pipe(catchError(() => of([]))),
      road: this.http.get<any[]>(`${environment.apiUrl}/road/getall`).pipe(catchError(() => of([]))),
      drainage: this.http.get<any[]>(`${environment.apiUrl}/drainage/getall`).pipe(catchError(() => of([]))),
      light: this.http.get<any[]>(`${environment.apiUrl}/street-light/getall`).pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ construction, road, drainage, light }) => {
        this.applications = [
          ...construction.map(app => this.toItem('Construction', app)),
          ...road.map(app => this.toItem('Road', app)),
          ...drainage.map(app => this.toItem('Drainage', app)),
          ...light.map(app => this.toItem('Street Light', app))
        ].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
        this.filterData();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Load failed:', err);
        this.isLoading = false;
      }
    });
  }

  filterData(): void {
    const query = this.searchText.trim().toLowerCase();
    const status = this.selectedStatus.toLowerCase();

    this.filteredData = this.applications.filter(app => {
      const haystack = [
        app.name,
        app.contact,
        app.location,
        app.type,
        app.details?.ward,
        app.details?.plotNo,
        app.details?.problemType,
        app.details?.description
      ].filter(Boolean).join(' ').toLowerCase();

      return (!query || haystack.includes(query)) &&
        (!this.selectedType || app.type === this.selectedType) &&
        (!status || app.status.toLowerCase() === status);
    });

    if (this.expandedKey && !this.filteredData.some(app => this.itemKey(app) === this.expandedKey)) {
      this.expandedKey = '';
    }
  }

  reset(): void {
    this.searchText = '';
    this.selectedType = '';
    this.selectedStatus = '';
    this.expandedKey = '';
    this.filterData();
  }

  toggleDetails(item: InfraApplication): void {
    const key = this.itemKey(item);
    this.expandedKey = this.expandedKey === key ? '' : key;
  }

  updateStatus(app: InfraApplication, status: string): void {
    const urlMap: Record<InfraType, string> = {
      Construction: `${environment.apiUrl}/construction/status/${app.id}`,
      Road: `${environment.apiUrl}/road/status/${app.id}`,
      Drainage: `${environment.apiUrl}/drainage/status/${app.id}`,
      'Street Light': `${environment.apiUrl}/street-light/status/${app.id}`
    };

    this.http.put(urlMap[app.type], { status }).subscribe({
      next: () => {
        app.status = status;
        app.details.status = status;
        this.filterData();
      },
      error: (err) => {
        console.error('Status update failed:', err);
        alert('Failed to update status');
      }
    });
  }

  countByStatus(status: string): number {
    const target = status.toLowerCase();
    return this.applications.filter(app => app.status.toLowerCase() === target).length;
  }

  itemKey(item: InfraApplication): string {
    return `${item.type}-${item.id}`;
  }

  isExpanded(item: InfraApplication): boolean {
    return this.expandedKey === this.itemKey(item);
  }

  trackByItem(_: number, item: InfraApplication): string {
    return this.itemKey(item);
  }

  statusClass(status: string): string {
    const s = (status || '').trim().toLowerCase();
    if (s === 'resolved') return 'resolved';
    if (s === 'rejected') return 'rejected';
    if (s === 'in progress') return 'in-progress';
    return 'pending';
  }

  typeIcon(type: string): string {
    const map: Record<string, string> = {
      Road: 'fas fa-road',
      Drainage: 'fas fa-water',
      'Street Light': 'fas fa-lightbulb',
      Construction: 'fas fa-hard-hat'
    };
    return map[type] || 'fas fa-tools';
  }

  typeClass(type: string): string {
    return (type || '').toLowerCase().replace(/\s+/g, '-');
  }

  applicantInitial(item: InfraApplication): string {
    return (item.name || item.type || '?').trim().charAt(0).toUpperCase();
  }

  fileUrl(path: string): string {
    const value = `${path || ''}`.trim();
    if (!value) return '';
    if (/^(https?:|data:|blob:)/i.test(value)) return value;
    return `${this.baseUrl}/${value.replace(/^\/+/, '')}`;
  }

  private toItem(type: InfraType, app: any): InfraApplication {
    const name = this.clean(app?.applicantName || app?.name || app?.fullName || 'Unknown Applicant');
    const status = this.clean(app?.status || 'Pending');
    const date = app?.createdAt || app?.date || app?.startDate || new Date().toISOString();
    const contact = this.clean(app?.contact || app?.mobile || app?.phone || '-');
    const location = this.clean(app?.location || app?.address || app?.roadName || app?.areaName || '-');

    const item: InfraApplication = {
      id: Number(app?.id || 0),
      name,
      type,
      status,
      date,
      contact,
      location,
      details: app || {},
      detailFields: [],
      fileEntries: []
    };

    item.detailFields = this.buildDetailFields(item);
    item.fileEntries = this.buildFileEntries(item.details);
    return item;
  }

  private buildDetailFields(item: InfraApplication): DetailField[] {
    const priority = [
      ['Application ID', `#${item.id}`],
      ['Applicant Name', item.name],
      ['Contact', item.contact],
      ['Type', item.type],
      ['Location', item.location],
      ['Ward', item.details?.ward],
      ['District', item.details?.district],
      ['Upazila', item.details?.upazila],
      ['Plot / Holding No', item.details?.plotNo || item.details?.holdingNo],
      ['Problem Type', item.details?.problemType],
      ['Building Type', item.details?.buildingType],
      ['Floors', item.details?.floors],
      ['Area', item.details?.area ? `${item.details.area} sq ft` : ''],
      ['Land Size', item.details?.landSize],
      ['Start Date', item.details?.startDate],
      ['Engineer Name', item.details?.engineerName],
      ['License No', item.details?.licenseNo],
      ['Description', item.details?.description]
    ];

    const seen = new Set<string>();
    const fields: DetailField[] = [];

    priority.forEach(([key, value]) => {
      if (this.hasValue(value)) {
        seen.add(key.toLowerCase());
        fields.push({ key, value: this.clean(value) });
      }
    });

    Object.keys(item.details || {}).forEach(key => {
      const value = item.details[key];
      const formatted = this.formatKey(key);
      if (this.hiddenDetailKeys.has(key) || seen.has(formatted.toLowerCase()) || this.isFileKey(key, value) || !this.hasValue(value)) {
        return;
      }
      fields.push({ key: formatted, value: this.clean(value) });
    });

    return fields;
  }

  private buildFileEntries(details: any): FileEntry[] {
    const files: FileEntry[] = [];
    Object.keys(details || {}).forEach(key => {
      const value = details[key];
      if (!this.isFileKey(key, value)) return;

      this.extractUrls(value).forEach((url, index) => {
        files.push({
          label: `${this.formatKey(key)}${index > 0 ? ` ${index + 1}` : ''}`,
          url,
          isImage: /\.(png|jpe?g|webp|gif|bmp|svg)$/i.test(url) || /^data:image\//i.test(url)
        });
      });
    });
    return files;
  }

  private extractUrls(value: any): string[] {
    if (!this.hasValue(value)) return [];
    if (Array.isArray(value)) return value.flatMap(v => this.extractUrls(v));
    if (typeof value === 'object') return this.extractUrls(value.url || value.path || value.fileUrl || value.documentUrl);
    return `${value}`.split(',').map(v => v.trim()).filter(Boolean);
  }

  private isFileKey(key: string, value: any): boolean {
    const name = key.toLowerCase();
    const fileLikeKey = /(file|photo|image|document|attachment|drawing|plan|nid|license)/.test(name);
    const valueText = typeof value === 'string' ? value.toLowerCase() : '';
    const fileLikeValue = /uploads\/|^https?:\/\/|^data:|\.pdf$|\.png$|\.jpe?g$|\.webp$/.test(valueText);
    return this.hasValue(value) && (fileLikeKey || fileLikeValue);
  }

  private formatKey(key: string): string {
    return key
      .replace(/([A-Z])/g, ' $1')
      .replace(/_/g, ' ')
      .replace(/\s+/g, ' ')
      .trim()
      .replace(/^./, ch => ch.toUpperCase());
  }

  private clean(value: any): string {
    if (value === null || value === undefined || value === '') return '-';
    return `${value}`;
  }

  private hasValue(value: any): boolean {
    return value !== null && value !== undefined && `${value}`.trim() !== '';
  }
}
