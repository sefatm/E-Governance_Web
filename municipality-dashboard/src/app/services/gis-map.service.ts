import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

const BASE = environment.apiUrl + '/map';

/** GeoJSON FeatureCollection থেকে properties array বের করে plain array বানায় */
function extractFeatures(geojson: any): any[] {
  if (Array.isArray(geojson)) return geojson;
  if (geojson?.features) {
    return geojson.features.map((f: any) => {
      const coords = f?.geometry?.coordinates;
      const props  = f?.properties ?? {};
      // GeoJSON coordinates হলো [lng, lat]
      if (coords && coords.length >= 2) {
        props.lat = props.lat ?? coords[1];
        props.lng = props.lng ?? coords[0];
      }
      return props;
    });
  }
  return [];
}

@Injectable({ providedIn: 'root' })
export class GisMapService {

  constructor(private http: HttpClient) {}

  // ── Holdings
  getHoldings(): Observable<any>              { return this.http.get(`${BASE}/holdings`); }
  getHoldingsByWard(ward: number): Observable<any>   { return this.http.get(`${BASE}/holdings/ward/${ward}`); }
  getHoldingsByStatus(status: string): Observable<any> { return this.http.get(`${BASE}/holdings/status/${status}`); }

  // ── Infrastructure — GeoJSON থেকে plain array extract করে return করে
  getAllInfrastructure(): Observable<any> { return this.http.get(`${BASE}/infrastructure/all`); }
  getRoads(): Observable<any[]>            { return this.http.get<any>(`${BASE}/infrastructure/roads`).pipe(map(extractFeatures)); }
  getDrainage(): Observable<any[]>         { return this.http.get<any>(`${BASE}/infrastructure/drainage`).pipe(map(extractFeatures)); }
  getStreetLights(): Observable<any[]>     { return this.http.get<any>(`${BASE}/infrastructure/street-lights`).pipe(map(extractFeatures)); }
  getConstruction(): Observable<any[]>     { return this.http.get<any>(`${BASE}/infrastructure/construction`).pipe(map(extractFeatures)); }

  getComplaints(): Observable<any[]>       { return this.http.get<any>(`${BASE}/complaints`).pipe(map(extractFeatures)); }

  // ── Health & Waste
  getHealthCenters(): Observable<any>    { return this.http.get(`${BASE}/health-centers`); }
  getGarbageZones(): Observable<any>     { return this.http.get(`${BASE}/garbage-zones`); }
  getWastePickups(): Observable<any[]>    { return this.http.get<any>(`${BASE}/waste-pickups`).pipe(map(extractFeatures)); }

  // ── Infrastructure location update (admin pin mode)
  updateRoadLocation(id: number, lat: number, lng: number): Observable<any> {
    return this.http.put(`${environment.apiUrl}/road/location/${id}`, { lat, lng });
  }
  updateDrainageLocation(id: number, lat: number, lng: number): Observable<any> {
    return this.http.put(`${environment.apiUrl}/drainage/location/${id}`, { lat, lng });
  }
  updateStreetLightLocation(id: number, lat: number, lng: number): Observable<any> {
    return this.http.put(`${environment.apiUrl}/street-light/location/${id}`, { lat, lng });
  }
  updateConstructionLocation(id: number, lat: number, lng: number): Observable<any> {
    return this.http.put(`${environment.apiUrl}/construction/location/${id}`, { lat, lng });
  }

  updateComplaintLocation(id: number, lat: number, lng: number): Observable<any> {
    return this.http.put(`${environment.apiUrl}/complaints/location/${id}`, { lat, lng });
  }
  updateHealthCenterLocation(id: number, lat: number, lng: number): Observable<any> {
    return this.http.put(`${environment.apiUrl}/health-center/location/${id}`, { lat, lng });
  }

  // ── Ward Boundaries
  getWardBoundaries(): Observable<any>   { return this.http.get(`${BASE}/wards`); }
}
