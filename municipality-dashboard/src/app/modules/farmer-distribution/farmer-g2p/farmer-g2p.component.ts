// farmer-g2p.component.ts
import { Component, OnInit } from '@angular/core';
import { FarmerDistributionService } from '../../../services/farmer-distribution.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-farmer-g2p',
  templateUrl: './farmer-g2p.component.html',
  styleUrls: ['./farmer-g2p.component.css']
})
export class FarmerG2pComponent implements OnInit {

  batches: any[] = [];
  isLoading  = false;
  formOpen   = true;
  creating   = false;
  formMsg    = '';
  formError  = false;

  batchDetail: any = null;

  form = {
    cycleMonth:      new Date().toISOString().slice(0, 7),
    amountPerFarmer: 1500,
    gateway:         'BEFTN',
    ward:            '',
    district:        ''
  };

  constructor(public ls: LanguageService, private svc: FarmerDistributionService) {}

  ngOnInit(): void { this.loadBatches(); }

  loadBatches(): void {
    this.isLoading = true;
    this.svc.getAllBatches().subscribe({
      next: (res: any) => { this.batches = res; this.isLoading = false; },
      error: ()        => { this.isLoading = false; }
    });
  }

  createBatch(): void {
    if (!this.form.cycleMonth || !this.form.amountPerFarmer || this.form.amountPerFarmer <= 0) {
      this.formMsg = 'cycleMonth ও amountPerFarmer দিন।'; this.formError = true; return;
    }
    this.creating = true; this.formMsg = ''; this.formError = false;

    this.svc.createBatch({
      cycleMonth:      this.form.cycleMonth,
      amountPerFarmer: this.form.amountPerFarmer,
      gateway:         this.form.gateway,
      ward:            this.form.ward   || null,
      district:        this.form.district || null,
      submittedBy:     'Admin'
    }).subscribe({
      next: (res: any) => {
        this.creating = false;
        if (res.success) {
          this.formMsg = `Batch তৈরি হয়েছে — ${res.transfersCreated} জন কৃষক। Ref: ${res.batchRef}`;
          this.formError = false;
          if (res.noAccountCount > 0)
            this.formMsg += ` (${res.noAccountCount} জনের bank account নেই — skip হয়েছে)`;
          this.loadBatches();
        } else {
          this.formMsg = res.message; this.formError = true;
        }
      },
      error: (err: any) => {
        this.creating = false;
        this.formMsg = err?.error?.message || 'সমস্যা হয়েছে।'; this.formError = true;
      }
    });
  }

  submitBatch(batch: any): void {
    if (!confirm(`"${batch.batchRef}" batch submit করবেন? ${batch.totalFarmers} জন কৃষকের কাছে ৳${batch.amountPerFarmer} করে যাবে।`)) return;
    this.svc.submitBatch(batch.id, 'Admin').subscribe({
      next: () => { this.loadBatches(); this.formMsg = 'Batch submit হয়েছে।'; this.formError = false; },
      error: (err: any) => { this.formMsg = err?.error?.message || 'Submit সমস্যা।'; this.formError = true; }
    });
  }

  retryBatch(batchId: number): void {
    this.svc.retryFailed(batchId).subscribe({
      next: (res: any) => { this.formMsg = res.message; this.formError = false; this.loadBatches(); if (this.batchDetail) this.viewBatch(batchId); }
    });
  }

  viewBatch(batchId: number): void {
    if (this.batchDetail?.batch?.id === batchId) { this.batchDetail = null; return; }
    this.svc.getBatchDetail(batchId).subscribe({
      next: (res: any) => { this.batchDetail = res; }
    });
  }

  countBatch(status: string): number {
    return this.batches.filter(b => b.status === status).length;
  }
}
