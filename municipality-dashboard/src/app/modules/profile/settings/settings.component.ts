import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {

  settingsForm!: FormGroup;

  constructor(public ls: LanguageService, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.settingsForm = this.fb.group({
      currentPassword: [''],
      newPassword: [''],
      confirmPassword: [''],
      theme: ['light'],
      notifications: [true]
    });
  }

  onSubmit() {
    if (this.settingsForm.valid) {

      const data = this.settingsForm.value;

      // Password match check
      if (data.newPassword !== data.confirmPassword) {
        alert('Password does not match!');
        return;
      }

      console.log('Settings Updated:', data);
      alert('Settings updated successfully!');

    }
  }

}
