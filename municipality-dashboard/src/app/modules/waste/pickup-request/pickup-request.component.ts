import { Component, OnInit } from '@angular/core';
import { WasteRequest } from 'src/app/models/pickup-request.model';
import { WasteRequestService } from 'src/app/services/pickup-request.service';
import { LanguageService } from 'src/app/services/language.service';
import { AuthService } from 'src/app/services/auth.service';

@Component({ selector:'app-pickup-request', templateUrl:'./pickup-request.component.html', styleUrls:['./pickup-request.component.css'] })
export class PickupRequestComponent implements OnInit {
  requests: WasteRequest[] = [];
  submitted=false; isSubmitting=false; isLocating=false; errorMsg=''; successMsg=''; lookupPhone='';
  isAdmin=false;
  request: WasteRequest = this.emptyRequest();
  constructor(public ls:LanguageService, private service:WasteRequestService, private auth:AuthService) {}
  ngOnInit():void {
    const r=(this.auth.getCurrentRole()||'').toLowerCase();
    this.isAdmin=r.includes('admin') || r.includes('department officer');
    if(this.isAdmin) this.loadRequests();
  }
  loadRequests():void { this.service.getAll().subscribe({next:r=>this.requests=r,error:e=>{console.error(e);this.errorMsg='Unable to load pickup requests.';}}); }
  loadMyRequests():void {
    const phone=(this.lookupPhone||this.request.phone||'').trim();
    if(!phone){this.errorMsg='Enter your mobile number first.';return;}
    this.service.getByPhone(phone).subscribe({next:r=>{this.requests=r;this.errorMsg='';},error:()=>this.errorMsg='Unable to load your requests.'});
  }
  useMyLocation():void {
    if(!navigator.geolocation){this.errorMsg='Geolocation is not supported by this browser.';return;}
    this.isLocating=true;
    navigator.geolocation.getCurrentPosition(p=>{this.request.lat=p.coords.latitude;this.request.lng=p.coords.longitude;this.isLocating=false;this.successMsg='GPS location captured.';setTimeout(()=>this.successMsg='',2500);},()=>{this.isLocating=false;this.errorMsg='Location permission denied or unavailable.';},{enableHighAccuracy:true,timeout:12000});
  }
  submitRequest():void {
    this.submitted=true;
    if(!this.request.name||!this.request.address||!this.request.phone||!this.request.ward||!this.request.type){this.errorMsg='Please fill all required fields.';return;}
    this.isSubmitting=true;
    this.service.create(this.request).subscribe({next:()=>{const phone=this.request.phone;this.isSubmitting=false;this.successMsg='Pickup request submitted successfully.';this.errorMsg='';this.lookupPhone=phone;this.resetForm();this.loadMyRequests();setTimeout(()=>this.successMsg='',3500);},error:e=>{this.isSubmitting=false;this.errorMsg=e?.error?.message||'Submission failed';}});
  }
  updateStatus(r:WasteRequest,status:string):void {if(!r.id)return;this.service.updateStatus(r.id,status).subscribe({next:()=>r.status=status,error:e=>console.error(e)});}
  deleteRequest(id:number):void {if(!confirm('Delete this request?'))return;this.service.delete(id).subscribe({next:()=>this.requests=this.requests.filter(r=>r.id!==id),error:e=>console.error(e)});}
  resetForm():void {this.submitted=false;this.request=this.emptyRequest();}
  private emptyRequest():WasteRequest{return {name:'',address:'',ward:'',phone:'',email:'',type:'',status:'Pending'};}
}
