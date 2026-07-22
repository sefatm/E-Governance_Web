import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-tax-due',
  templateUrl: './tax-due.component.html',
  styleUrls: ['./tax-due.component.css']
})
export class TaxDueComponent implements OnInit {

  list      : any[] = [];
  searchText = '';
  isLoading  = false;
  errorMsg   = '';

  private dueListUrl = `${environment.apiUrl}/tax-payment/due-list`;

  constructor(public ls: LanguageService, private http: HttpClient) {}

  ngOnInit(): void { this.loadData(); }

  loadData(): void {
    this.isLoading = true;
    this.errorMsg  = '';

    this.http.get<any[]>(this.dueListUrl).subscribe({
      next: (res) => {
        this.list      = res;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMsg  = 'Failed to load due list. Please try again.';
        console.error('Due list error:', err);
      }
    });
  }

  get filtered(): any[] {
    if (!this.searchText) return this.list;
    const t = this.searchText.toLowerCase();
    return this.list.filter(i =>
      (i.holdingNo  || '').toLowerCase().includes(t) ||
      (i.ownerName  || '').toLowerCase().includes(t)
    );
  }
}
