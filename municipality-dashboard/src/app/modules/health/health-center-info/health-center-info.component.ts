import { Component, OnInit } from '@angular/core';
import { HealthCenterService } from 'src/app/services/health-center-info.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-health-center-info',
  templateUrl: './health-center-info.component.html',
  styleUrls: ['./health-center-info.component.css']
})
export class HealthCenterInfoComponent implements OnInit {

  centers: any[] = [];
  form: any = {};

  constructor(public ls: LanguageService, private service: HealthCenterService) {}

  ngOnInit(): void {
    this.loadCenters();
  }

  loadCenters() {
    this.service.getAll().subscribe(res => {
      this.centers = res;
    });
  }

  addCenter() {

    if (!this.form.name || !this.form.type || !this.form.location) {
      alert('Please fill required fields');
      return;
    }

    this.service.create(this.form).subscribe(() => {
      alert('Health Center Added Successfully!');
      this.form = {};
      this.loadCenters();
    });
  }

  deleteCenter(id?: number) {

  if (!id) return;

  this.service.delete(id)
    .subscribe({
      next: () => {
        alert("Deleted successfully!");
        this.loadCenters();
      },
      error: (err) => console.error(err)
    });
}

}
