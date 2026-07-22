import { NgModule }             from '@angular/core';
import { CommonModule }         from '@angular/common';
import { FormsModule }          from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { TcbDistributionComponent } from './tcb-distribution/tcb-distribution.component';
import { TcbStockComponent }        from './tcb-stock/tcb-stock.component';
import { TcbScanComponent }         from './tcb-scan/tcb-scan.component';

const routes: Routes = [
  { path: 'scan',         component: TcbScanComponent },
  { path: 'stock',        component: TcbStockComponent },
  { path: 'distribution', component: TcbDistributionComponent },
  { path: '',             redirectTo: 'scan', pathMatch: 'full' }
];

@NgModule({
  declarations: [
    TcbScanComponent,
    TcbStockComponent,
    TcbDistributionComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class TcbModule {}
