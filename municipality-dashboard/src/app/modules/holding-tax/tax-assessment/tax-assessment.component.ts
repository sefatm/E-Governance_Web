import { environment } from 'src/environments/environment';
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-tax-assessment',
  templateUrl: './tax-assessment.component.html',
  styleUrls: ['./tax-assessment.component.css']
})
export class TaxAssessmentComponent implements OnInit {

  form: any = {
    holdingNo: '', ownerName: '', propertyType: '',
    area: null, rate: null, previousDue: null
  };

  result:      any  = null;
  isSubmitting      = false;

  private baseUrl = `${environment.apiUrl}/tax-assessment`;

  constructor(public ls: LanguageService, private http: HttpClient) {}

  ngOnInit(): void {}

  calculateTax(): void {
    if (!this.form.holdingNo || !this.form.ownerName ||
        !this.form.propertyType || !this.form.area || !this.form.rate) {
      alert('Please fill all required fields!');
      return;
    }

    const tax   = this.form.area * this.form.rate;
    const total = tax + (this.form.previousDue || 0);

    this.result = { tax, total };
    this.isSubmitting = true;

    const payload = {
      holdingNo:    this.form.holdingNo,
      ownerName:    this.form.ownerName,
      propertyType: this.form.propertyType,
      area:         this.form.area,
      rate:         this.form.rate,
      previousDue:  this.form.previousDue || 0,
      taxAmount:    tax,
      totalPayable: total,
      status:       'Calculated'
    };

    this.http.post(`${this.baseUrl}/create`, payload).subscribe({
      next: () => { this.isSubmitting = false; },
      error: (err) => {
        this.isSubmitting = false;
        console.error('Assessment save failed:', err);
      }
    });
  }

  resetForm(): void {
    this.form   = { holdingNo: '', ownerName: '', propertyType: '', area: null, rate: null, previousDue: null };
    this.result = null;
  }
}
