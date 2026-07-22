// src/app/modules/social cards/farmer-card-status/farmer-card-status.component.ts
import { Component } from '@angular/core';
import { environment } from 'src/environments/environment';
import { FarmerCardService } from '../../../services/farmer-card.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-farmer-card-status',
  templateUrl: './farmer-card-status.component.html',
  styleUrls: ['./shared-status.css']
})
export class FarmerCardStatusComponent {

  nid      = '';
  loading  = false;
  errorMsg = '';
  result: any  = null;

  // ✅ FIX Bug 5: resultId সঠিকভাবে backend response থেকে নেওয়া হচ্ছে
  resultId: number | null = null;

  constructor(public ls: LanguageService, private svc: FarmerCardService) {}

  check(): void {
    if (!this.nid.trim()) return;

    this.loading  = true;
    this.errorMsg = '';
    this.result   = null;
    this.resultId = null;

    this.svc.checkByNid(this.nid.trim()).subscribe({
      next: (res: any) => {
        this.loading = false;

        // ✅ FIX Bug 5: id field map করা হচ্ছে — backend এখন id পাঠাবে
        this.result = {
          ...res,
          holderName: res.farmerName  // status component uses holderName
        };

        // ✅ resultId = res.id — download URL এ null যাবে না
        this.resultId = res.id ?? null;
      },
      error: () => {
        this.loading  = false;
        this.errorMsg = 'এই NID দিয়ে কোনো কৃষক কার্ড আবেদন পাওয়া যায়নি।';
      }
    });
  }

  // ✅ FIX Bug 5: resultId null হলে URL তৈরি হবে না
  get downloadUrl(): string | null {
    if (!this.resultId) return null;
    return environment.apiUrl + '/farmer-card/download/' + this.resultId;
  }
}
