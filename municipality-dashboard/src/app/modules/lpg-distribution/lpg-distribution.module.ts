import { NgModule }             from '@angular/core';
import { CommonModule }         from '@angular/common';
import { FormsModule }          from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { LpgDistributionHistoryComponent } from './lpg-distribution-history/lpg-distribution-history.component';
import { LpgScanComponent } from './lpg-scan/lpg-scan.component';

const routes: Routes = [
  { path: 'stock-distribution', component: LpgDistributionHistoryComponent },
  { path: 'history', redirectTo: 'stock-distribution', pathMatch: 'full' },
  { path: 'stock',   redirectTo: 'stock-distribution', pathMatch: 'full' },
  { path: 'scan',    component: LpgScanComponent },
  { path: '',        redirectTo: 'stock-distribution', pathMatch: 'full' }
];

@NgModule({
  declarations: [
    LpgDistributionHistoryComponent,
    LpgScanComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class LpgDistributionModule {}
