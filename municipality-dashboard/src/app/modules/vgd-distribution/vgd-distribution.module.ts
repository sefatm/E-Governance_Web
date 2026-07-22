import { NgModule }             from '@angular/core';
import { CommonModule }         from '@angular/common';
import { FormsModule }          from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { VgdStockComponent }    from './vgd-stock/vgd-stock.component';
import { VgdScanComponent }     from './vgd-scan/vgd-scan.component';
import { VgdHistoryComponent }  from './vgd-history/vgd-history.component';

const routes: Routes = [
  { path: 'stock', component: VgdStockComponent },
  { path: 'scan',    component: VgdScanComponent },
  { path: 'history', component: VgdHistoryComponent },
  { path: '',        redirectTo: 'stock', pathMatch: 'full' }
];

@NgModule({
  declarations: [
    VgdStockComponent,
    VgdScanComponent,
    VgdHistoryComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class VgdDistributionModule {}
