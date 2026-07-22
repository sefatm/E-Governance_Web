import { NgModule }             from '@angular/core';
import { CommonModule }         from '@angular/common';
import { FormsModule }          from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { FarmerDistributionScanComponent }    from './farmer-distribution-scan/farmer-distribution-scan.component';
import { FarmerDistributionHistoryComponent } from './farmer-distribution-history/farmer-distribution-history.component';
import { FarmerG2pComponent }                 from './farmer-g2p/farmer-g2p.component';

const routes: Routes = [
  { path: 'scan',    component: FarmerDistributionScanComponent },
  { path: 'stock-distribution', component: FarmerDistributionHistoryComponent },
  { path: 'history', redirectTo: 'stock-distribution', pathMatch: 'full' },
  { path: 'stock',   redirectTo: 'stock-distribution', pathMatch: 'full' },
  { path: 'g2p',     component: FarmerG2pComponent },
  { path: '',        redirectTo: 'scan', pathMatch: 'full' }
];

@NgModule({
  declarations: [
    FarmerDistributionScanComponent,
    FarmerDistributionHistoryComponent,
    FarmerG2pComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class FarmerDistributionModule {}