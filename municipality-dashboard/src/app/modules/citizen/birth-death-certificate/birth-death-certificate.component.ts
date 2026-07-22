import { environment } from 'src/environments/environment';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-birth-death-certificate',
  templateUrl: './birth-death-certificate.component.html',
  styleUrls: ['./birth-death-certificate.component.css']
})
export class BirthDeathCertificateComponent implements OnInit {

  activeTab: 'birth' | 'death' = 'birth';

  birthForm!: FormGroup;
  deathForm!: FormGroup;

  files: { [key: string]: File | null } = {
    fatherNid: null,
    motherNid: null,
    vaccine:   null,
    deathNid:  null,
    medical:   null,
  };

  loading      = false;
  errorMsg      = '';
  birthSuccess = false;
  deathSuccess = false;

  private readonly API = `${environment.apiUrl}/birth-death`;

  constructor(public ls: LanguageService, private fb: FormBuilder, private http: HttpClient) {}

  ngOnInit(): void {
    this.buildForms();
  }

  private buildForms(): void {
    this.birthForm = this.fb.group({
      nameBn:          ['মাহাদী হাসান', Validators.required],
      nameEn:          ['Mahadi Hasan', Validators.required],
      dob:             ['1999-01-01', Validators.required],
      placeOfBirth:    ['Madaripur', Validators.required],
      gender:          ['Male', Validators.required],
      bloodGroup:      ['AB+'],
      mobile:          ['01728444584', Validators.required],
      email:           ['sefatmahmud995@gmail.com'],
      presentAddress:  ['Master colony, College road, Purasova, Madaripur', Validators.required],
      permanentAddress:['Master colony, College road, Purasova, Madaripur', Validators.required],
      fathersName:     ['Md. Moinul Islam', Validators.required],
      fathersDob:      ['1950-01-01'],
      fathersNid:      ['1234567890', Validators.required],
      fathersMobile:   ['01765477654'],
      fathersEmail:    ['fathert@email.com'],
      mothersName:     ['Fatima Khatun', Validators.required],
      mothersDob:      ['1970-01-01'],
      mothersNid:      ['8765498765', Validators.required],
      mothersMobile:   ['01740040161'],
      mothersEmail:    ['mother@email.com'],
      emergencyName:   ['Md. Moinul Islam'],
      emergencyPhone:  ['01712345678'],
      paymentMethod:   ['bKash', Validators.required],
      amount:          ['100', Validators.required],
    });

    this.deathForm = this.fb.group({
      nameBn:          ['মোঃ মইনুল ইসলাম', Validators.required],
      nameEn:          ['Md. Moinul Islam', Validators.required],
      dob:             ['1950-01-01'],
      dateOfDeath:     ['2023-01-01', Validators.required],
      placeOfDeath:    ['Madaripur', Validators.required],
      gender:          ['Male', Validators.required],
      birthNo:         ['19508765987345654'],
      nid:             ['9887875087'],
      presentAddress:  ['Master colony, College road, Purasova, Madaripur', Validators.required],
      permanentAddress:['Master colony, College road, Purasova, Madaripur', Validators.required],
      applicantName:   ['Mahadi Hasan', Validators.required],
      relation:        ['Son', Validators.required],
      mobile:          ['01728444584', Validators.required],
      email:           ['sefatmahmud995@gmail.com'],
      paymentMethod:   ['bKash', Validators.required],
      amount:          ['100', Validators.required],
    });
  }

  switchTab(tab: 'birth' | 'death'): void {
    this.activeTab    = tab;
    this.birthSuccess = false;
    this.deathSuccess = false;
  }

  isInvalid(form: FormGroup, control: string): boolean {
    const c = form.get(control);
    return !!(c && c.invalid && (c.dirty || c.touched));
  }

  onFileChange(event: Event, key: string): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      if (file.size > 5 * 1024 * 1024) {
        this.errorMsg = 'File size must be less than 5 MB.';
        return;
      }
      this.files[key] = file;
    }
  }

  removeFile(key: string): void {
    this.files[key] = null;
  }

  onSubmitBirth(): void {
    if (this.birthForm.invalid) {
      this.birthForm.markAllAsTouched();
      return;
    }

    const v = this.birthForm.value;

    const fd = new FormData();
    fd.append('name',            v.nameEn);
    fd.append('nameBn',          v.nameBn || '');
    fd.append('dob',             v.dob);
    fd.append('placeOfBirth',    v.placeOfBirth);
    fd.append('genderOfBirth',   v.gender);
    fd.append('address',         v.presentAddress);
    fd.append('permanentAddress',v.permanentAddress || '');
    fd.append('mobileNumber',    v.mobile);
    fd.append('email',           v.email || '');
    fd.append('fathersName',     v.fathersName);
    fd.append('fathersDob',      v.fathersDob || '');
    fd.append('fathersNid',      v.fathersNid);
    fd.append('fathersEmail',    v.fathersEmail || '');
    fd.append('fathersContact',  v.fathersMobile || '');
    fd.append('mothersName',     v.mothersName);
    fd.append('mothersDob',      v.mothersDob || '');
    fd.append('mothersNid',      v.mothersNid);
    fd.append('mothersEmail',    v.mothersEmail || '');
    fd.append('mothersContact',  v.mothersMobile || '');
    fd.append('emergencyName',   v.emergencyName || '');
    fd.append('emergencyPhone',  v.emergencyPhone || '');
    fd.append('paymentMethod',   v.paymentMethod);
    fd.append('amount',          String(v.amount));

    if (this.files['fatherNid']) fd.append('fatherNid', this.files['fatherNid']!);
    if (this.files['motherNid']) fd.append('motherNid', this.files['motherNid']!);
    if (this.files['vaccine'])   fd.append('vaccine',   this.files['vaccine']!);

    this.loading = true;

    this.http.post(`${this.API}/create-birth`, fd).subscribe({
      next: () => {
        this.loading      = false;
        this.birthSuccess = true;
        this.birthForm.reset();
        this.files['fatherNid'] = null;
        this.files['motherNid'] = null;
        this.files['vaccine']   = null;
        setTimeout(() => (this.birthSuccess = false), 5000);
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Submission failed. Please try again.';
      },
    });
  }

  onSubmitDeath(): void {
    if (this.deathForm.invalid) {
      this.deathForm.markAllAsTouched();
      return;
    }

    const v = this.deathForm.value;

    const fd = new FormData();
    fd.append('name',            v.nameEn);
    fd.append('nameBn',          v.nameBn || '');
    fd.append('dob',             v.dob || '');
    fd.append('dateOfDeath',     v.dateOfDeath);
    fd.append('placeOfDeath',    v.placeOfDeath);
    fd.append('gender',          v.gender);
    fd.append('birthNo',         v.birthNo || '');
    fd.append('nid',             v.nid || '');
    fd.append('address',         v.presentAddress);
    fd.append('permanentAddress',v.permanentAddress || '');
    fd.append('applicantName',   v.applicantName);
    fd.append('relation',        v.relation);
    fd.append('mobileNumber',    v.mobile);
    fd.append('email',           v.email || '');
    fd.append('paymentMethod',   v.paymentMethod);
    fd.append('amount',          String(v.amount));

    if (this.files['deathNid']) fd.append('deathNid', this.files['deathNid']!);
    if (this.files['medical'])  fd.append('medical',  this.files['medical']!);

    this.loading = true;

    this.http.post(`${this.API}/create-death`, fd).subscribe({
      next: () => {
        this.loading      = false;
        this.deathSuccess = true;
        this.deathForm.reset();
        this.files['deathNid'] = null;
        this.files['medical']  = null;
        setTimeout(() => (this.deathSuccess = false), 5000);
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Submission failed. Please try again.';
      },
    });
  }

  resetBirthForm(): void {
    this.birthForm.reset();
    this.files['fatherNid'] = null;
    this.files['motherNid'] = null;
    this.files['vaccine']   = null;
    this.birthSuccess       = false;
  }

  resetDeathForm(): void {
    this.deathForm.reset();
    this.files['deathNid'] = null;
    this.files['medical']  = null;
    this.deathSuccess      = false;
  }
}
