import { Component } from '@angular/core';
import { environment } from 'src/environments/environment';
import { VgdCardService } from '../../../services/vgd-card.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-vgd-card-status',
  templateUrl: './vgd-card-status.component.html',
  styleUrls: ['./shared-status.css']
})
export class VgdCardStatusComponent {
  
  nid = '';
  loading = false;
  errorMsg = '';
  result: any = null;
  resultId: number | null = null;

  constructor(public ls: LanguageService, private svc: VgdCardService) {}

  check() {
    if (!this.nid.trim()) return;

    this.loading = true;
    this.errorMsg = '';
    this.result = null;

    this.svc.checkByNid(this.nid.trim()).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.result = res;
        this.resultId = res.id;
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'এই NID দিয়ে কোনো আবেদন পাওয়া যায়নি।';
      }
    });
  }

  get downloadUrl(): string {
    return environment.apiUrl + '/vgd-card/download/' + this.resultId;
  }

  isExpiringSoon(): boolean {
    if (!this.result?.expiryDate) {
      return false;
    }

    const expiryDate = new Date(this.result.expiryDate);
    const today = new Date();

    const diffDays = Math.ceil(
      (expiryDate.getTime() - today.getTime()) /
      (1000 * 60 * 60 * 24)
    );

    return diffDays >= 0 && diffDays <= 30;
  }
}
