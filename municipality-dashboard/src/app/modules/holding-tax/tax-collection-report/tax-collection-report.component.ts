import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-tax-collection-report',
  templateUrl: './tax-collection-report.component.html',
  styleUrls: ['./tax-collection-report.component.css']
})
export class TaxCollectionReportComponent implements OnInit {

  data        : any[] = [];
  filteredData: any[] = [];
  searchText  = '';
  fromDate    = '';
  toDate      = '';
  isLoading   = false;

  get totalCollected(): number {
    return this.filteredData.reduce((sum, item) => sum + Number(item.amount || 0), 0);
  }

  constructor(public ls: LanguageService, private http: HttpClient) {}

  ngOnInit(): void { this.loadData(); }

  loadData(): void {
    this.isLoading = true;
    this.http.get<any[]>(`${environment.apiUrl}/tax-payment/getall`).subscribe({
      next: (res) => {
        this.data      = res;
        this.isLoading = false;
        this.applyFilter();
      },
      error: () => {
        this.data         = [];
        this.filteredData = [];
        this.isLoading    = false;
      }
    });
  }

  applyFilter(): void {
    const txt  = this.searchText.toLowerCase();
    const from = this.fromDate ? new Date(this.fromDate) : null;
    const to   = this.toDate   ? new Date(this.toDate)   : null;

    this.filteredData = this.data.filter(item => {
      const matchText = !txt ||
        (item.holdingNo  || '').toLowerCase().includes(txt) ||
        (item.ownerName  || '').toLowerCase().includes(txt);

      let matchDate = true;
      if (item.paymentDate) {
        const d = new Date(item.paymentDate);
        if (from && d < from) matchDate = false;
        if (to   && d > to)   matchDate = false;
      }

      return matchText && matchDate;
    });
  }

  search(): void { this.applyFilter(); }

  clearFilter(): void {
    this.searchText = '';
    this.fromDate   = '';
    this.toDate     = '';
    this.applyFilter();
  }
}
