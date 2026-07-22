import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LanguageService } from 'src/app/services/language.service';
@Component({selector:'app-collection-report',templateUrl:'./collection-report.component.html',styleUrls:['./collection-report.component.css']})
export class CollectionReportComponent implements OnInit {
 reports:any[]=[]; filteredData:any[]=[]; isLoading=false; errorMsg=''; searchText=''; selectedStatus=''; fromDate=''; toDate='';
 get totalCollections(){return this.filteredData.length;}
 get completedCount(){return this.filteredData.filter(r=>['Completed','Collected'].includes(r.status)).length;}
 get pendingCount(){return this.filteredData.filter(r=>r.status==='Pending').length;}
 get inProgressCount(){return this.filteredData.filter(r=>r.status==='In Progress'||r.status==='Assigned').length;}
 private pickupUrl=`${environment.apiUrl}/waste-request/getall`;
 private logUrl=`${environment.apiUrl}/waste-collection-log/getall`;
 constructor(public ls:LanguageService,private http:HttpClient){}
 ngOnInit(){this.loadData();}
 loadData(){this.isLoading=true;this.errorMsg='';forkJoin({pickups:this.http.get<any[]>(this.pickupUrl).pipe(catchError(()=>of([]))),logs:this.http.get<any[]>(this.logUrl).pipe(catchError(()=>of([])))}).subscribe({next:({pickups,logs})=>{const pickupRows=pickups.map(p=>({id:p.id,type:p.type||'Pickup Request',area:p.address||'—',ward:p.ward||'—',date:p.createdAt||'—',status:p.status||'Pending',source:'pickup',collector:'—',vehicle:'—',weight:null}));const logRows=logs.map(l=>({id:l.id,type:l.wasteType||'Collection',area:l.area||'—',ward:l.ward||'—',date:l.collectionDate||'—',status:l.status||'Completed',source:'log',collector:l.collectorName||'—',vehicle:l.vehicleNo||'—',weight:l.estimatedWeightKg}));this.reports=[...logRows,...pickupRows];this.filteredData=[...this.reports];this.isLoading=false;this.applyFilter();},error:()=>{this.isLoading=false;this.errorMsg='Failed to load report data.';}});}
 applyFilter(){const txt=this.searchText.toLowerCase();const from=this.fromDate?new Date(this.fromDate):null;const to=this.toDate?new Date(this.toDate):null;this.filteredData=this.reports.filter(r=>{const mt=!txt||[r.area,r.ward,r.type,r.collector,r.vehicle].some((v:any)=>(v||'').toString().toLowerCase().includes(txt));let md=true;if(from||to){const d=new Date(r.date);if(!isNaN(d.getTime())){if(from&&d<from)md=false;if(to&&d>to)md=false;}}const ms=!this.selectedStatus||r.status===this.selectedStatus;return mt&&md&&ms;});}
 updateStatus(item:any,status:string){if(item.source!=='pickup')return;this.http.put(`${environment.apiUrl}/waste-request/status/${item.id}`,{status}).subscribe({next:()=>{item.status=status;this.applyFilter();},error:e=>console.error(e)});}
 clearFilter(){this.searchText='';this.selectedStatus='';this.fromDate='';this.toDate='';this.applyFilter();}
 getStatusColor(status:string){switch(status){case 'Completed':case 'Collected':return 'green';case 'Pending':return 'orange';case 'In Progress':case 'Assigned':return 'blue';case 'Cancelled':return 'red';case 'Active':return 'purple';default:return 'black';}}
}
