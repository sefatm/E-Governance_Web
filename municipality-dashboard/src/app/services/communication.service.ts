import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NotificationMessage } from '../models/communication.model';

@Injectable({ providedIn: 'root' })
export class CommunicationService {

  private notifUrl    = environment.apiUrl + '/notification';
  private feedbackUrl = environment.apiUrl + '/feedback';

  constructor(private http: HttpClient) {}

  send(msg: NotificationMessage): Observable<any> {
    return this.http.post(`${this.notifUrl}/send`, msg);
  }

  getAllNotifications(): Observable<NotificationMessage[]> {
    return this.http.get<NotificationMessage[]>(`${this.notifUrl}/getall`);
  }

  getByType(type: string): Observable<NotificationMessage[]> {
    return this.http.get<NotificationMessage[]>(`${this.notifUrl}/type/${type}`);
  }

  getByTag(tag: string): Observable<NotificationMessage[]> {
    return this.http.get<NotificationMessage[]>(`${this.notifUrl}/tag/${tag}`);
  }

  getNotifSummary(): Observable<any> {
    return this.http.get<any>(`${this.notifUrl}/summary`).pipe(
      catchError(() => of({ total: 0, totalSms: 0, totalEmail: 0, totalPush: 0 }))
    );
  }

  deleteNotif(id: number): Observable<any> {
    return this.http.delete(`${this.notifUrl}/${id}`);
  }

  submitFeedback(fb: any): Observable<any> {
    return this.http.post(`${this.feedbackUrl}/submit`, fb);
  }

  getAllFeedbacks(): Observable<any[]> {
    return this.http.get<any[]>(`${this.feedbackUrl}/getall`);
  }

  deleteFeedback(id: number): Observable<any> {
    return this.http.delete(`${this.feedbackUrl}/${id}`);
  }

  updateFeedbackStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.feedbackUrl}/status/${id}`, { status });
  }

  replyFeedback(id: number, payload: any): Observable<any> {
  return this.http.put(`${this.feedbackUrl}/reply/${id}`, payload);
}
}
