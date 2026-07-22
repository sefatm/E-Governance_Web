import { Component } from '@angular/core';
import { environment } from 'src/environments/environment';
import { FamilyCardService } from '../../../services/family-card.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-family-card-status',
  templateUrl: './family-card-status.component.html',
  styleUrls: ['./shared-status.css']
})
export class FamilyCardStatusComponent {
  nid = ''; loading = false; errorMsg = '';
  result: any = null; resultId: number | null = null;

  constructor(public ls: LanguageService, private svc: FamilyCardService) {}

  check() {
    if (!this.nid.trim()) return;
    this.loading = true; this.errorMsg = ''; this.result = null;
    this.svc.checkByNid(this.nid.trim()).subscribe({
      next: (res: any) => { this.loading = false; this.result = res; this.resultId = res.id; },
      error: () => { this.loading = false; this.errorMsg = 'এই NID দিয়ে কোনো আবেদন পাওয়া যায়নি।'; }
    });
  }

  get downloadUrl(): string {
    return environment.apiUrl + '/family-card/download/' + this.resultId;
  }
}
