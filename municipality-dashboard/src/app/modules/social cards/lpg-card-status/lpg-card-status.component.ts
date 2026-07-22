import { Component } from '@angular/core';
import { environment } from 'src/environments/environment';
import { LpgCardService } from '../../../services/lpg-card.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-lpg-card-status',
  templateUrl: './lpg-card-status.component.html',
  styleUrls: ['./shared-status.css']
})
export class LpgCardStatusComponent {
  nid = ''; loading = false; errorMsg = '';
  result: any = null; resultId: number | null = null;

  constructor(public ls: LanguageService, private svc: LpgCardService) {}

  check() {
    if (!this.nid.trim()) return;
    this.loading = true; this.errorMsg = ''; this.result = null;
    this.svc.checkByNid(this.nid.trim()).subscribe({
      next: (res: any) => { this.loading = false; this.result = res; this.resultId = res.id; },
      error: () => { this.loading = false; this.errorMsg = 'এই NID দিয়ে কোনো আবেদন পাওয়া যায়নি।'; }
    });
  }

  // ✅ FIX: null-guarded — returns empty string when resultId is null (falsy → *ngIf hides the button)
  get downloadUrl(): string {
    if (!this.resultId) return '';
    return environment.apiUrl + '/lpg-card/download/' + this.resultId;
  }
}
