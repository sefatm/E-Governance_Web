import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HoldingApplication } from '../models/holding-new-registration.model';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class HoldingService {

  private baseUrl = environment.apiUrl + '/holding-new-registration';

  constructor(private http: HttpClient) {}

  createApplication(data: HoldingApplication): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, data);
  }

  getAllApplications(): Observable<HoldingApplication[]> {
    return this.http.get<HoldingApplication[]>(`${this.baseUrl}/getall`);
  }

  getById(id: number): Observable<HoldingApplication> {
    return this.http.get<HoldingApplication>(`${this.baseUrl}/${id}`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/status/${id}`, { status });
  }

  updateLocation(id: number, latitude: number, longitude: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/location/${id}`, { latitude, longitude });
  }

  uploadDocuments(id: number, nidFile?: File, deedFile?: File, photo?: File): Observable<any> {
    const fd = new FormData();
    if (nidFile)  fd.append('nidFile',  nidFile,  nidFile.name);
    if (deedFile) fd.append('deedFile', deedFile, deedFile.name);
    if (photo)    fd.append('photo',    photo,    photo.name);
    return this.http.post(`${this.baseUrl}/upload/${id}`, fd);
  }

  downloadCertificate(id: number): void {
    const a = document.createElement('a');
    a.href = `${this.baseUrl}/generate-pdf/${id}`;
    a.target = '_blank';
    a.download = `holding-certificate-${id}.pdf`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }
}
