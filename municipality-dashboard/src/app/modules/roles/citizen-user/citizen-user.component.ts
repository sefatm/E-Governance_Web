import { environment } from 'src/environments/environment';
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LanguageService } from 'src/app/services/language.service';

interface AppUser {
  id: number; name: string; email: string; role: string; status: string;
}

@Component({
  selector: 'app-citizen-user',
  templateUrl: './citizen-user.component.html',
  styleUrls: ['./citizen-user.component.css']
})
export class CitizenUserComponent implements OnInit {

  users: AppUser[] = [];
  isLoading = false;
  private base = `${environment.apiUrl}/users`;

  constructor(public ls: LanguageService, private http: HttpClient) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.isLoading = true;
    this.http.get<AppUser[]>(`${this.base}/getall`).subscribe({
      next: (res) => {
        this.users     = res.filter(u => u.role === 'Citizen');
        this.isLoading = false;
      },
      error: (err) => { console.error(err); this.isLoading = false; }
    });
  }

  toggleStatus(user: AppUser): void {
    const newStatus = user.status === 'Active' ? 'Inactive' : 'Active';
    this.http.put(`${this.base}/update-status/${user.id}`, { status: newStatus }).subscribe({
      next: () => { user.status = newStatus; },
      error: (err) => alert(err.error?.message || 'Failed.')
    });
  }
}
