import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { LanguageService } from 'src/app/services/language.service';
interface SmartBin { id:number; binCode:string; location:string; ward:string; fillLevel:number; lastCollected?:string; status:string; binType:string; lat?:number; lng?:number; }
@Component({selector:'app-smart-bin',templateUrl:'./smart-bin.component.html',styleUrls:['./smart-bin.component.css']})
export class SmartBinComponent implements OnInit {
 bins:SmartBin[]=[]; successMsg=''; errorMsg=''; filterStatus=''; newBin:any={binCode:'',location:'',ward:'',binType:'General',fillLevel:0,status:'Normal'}; private base=`${environment.apiUrl}/smart-bin`;
 get filteredBins(){return !this.filterStatus?this.bins:this.bins.filter(b=>b.status===this.filterStatus);}
 get fullBinsCount(){return this.bins.filter(b=>b.fillLevel>=80).length;}
 get collectedToday(){return this.bins.filter(b=>b.status==='Collected').length;}
 get maintenanceCount(){return this.bins.filter(b=>b.status==='Maintenance').length;}
 constructor(public ls:LanguageService,private http:HttpClient){}
 ngOnInit(){this.loadBins();}
 loadBins(){this.http.get<SmartBin[]>(`${this.base}/getall`).subscribe({next:r=>this.bins=r,error:()=>this.errorMsg='Failed to load smart bins.'});}
 createBin(){if(!this.newBin.binCode||!this.newBin.location){this.errorMsg='Bin code and location are required.';return;}this.http.post<SmartBin>(`${this.base}/create`,this.newBin).subscribe({next:r=>{this.bins.unshift(r);this.newBin={binCode:'',location:'',ward:'',binType:'General',fillLevel:0,status:'Normal'};this.toast('Smart bin created.');},error:e=>this.errorMsg=e?.error?.message||'Create failed.'});}
 collectBin(bin:SmartBin){this.http.put<SmartBin>(`${this.base}/collect/${bin.id}`,{}).subscribe({next:r=>{Object.assign(bin,r);this.toast(`Bin at "${bin.location}" marked as collected.`);},error:()=>this.errorMsg='Update failed.'});}
 markMaintenance(bin:SmartBin){this.http.put<SmartBin>(`${this.base}/maintenance/${bin.id}`,{}).subscribe({next:r=>{Object.assign(bin,r);this.toast(`Bin at "${bin.location}" marked for maintenance.`);},error:()=>this.errorMsg='Update failed.'});}
 promptFill(bin:SmartBin){const raw=prompt('Enter fill level (0-100)',String(bin.fillLevel));if(raw===null)return;const level=Number(raw);if(!Number.isFinite(level)||level<0||level>100){this.errorMsg='Fill level must be between 0 and 100.';return;}this.updateFill(bin,level);}
 updateFill(bin:SmartBin,level:number){this.http.put<SmartBin>(`${this.base}/fill-level/${bin.id}`,{fillLevel:level}).subscribe({next:r=>Object.assign(bin,r),error:()=>this.errorMsg='Fill level update failed.'});}
 getColor(level:number){return level>=80?'red':level>=50?'orange':'green';}
 fillClass(level:number){return level>=80?'fill--high':level>=50?'fill--mid':'fill--low';}
 statusClass(status:string){const m:any={Full:'status--full',Normal:'status--normal',Collected:'status--collected',Maintenance:'status--maintenance'};return m[status]||'';}
 private toast(m:string){this.successMsg=m;setTimeout(()=>this.successMsg='',3500);}
}
