import { environment } from 'src/environments/environment';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../services/auth.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-admin-profile',
  templateUrl: './admin-profile.component.html',
  styleUrls: ['./admin-profile.component.css']
})
export class AdminProfileComponent implements OnInit {

  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  currentUser: any  = null;
  photoPreview: string = '';
  selectedFile: File | null = null;

  successMsg  = '';
  errorMsg    = '';
  pwSuccess   = '';
  pwError     = '';
  photoMsg    = '';
  photoError  = '';
  isLoading   = false;
  photoLoading = false;

  private readonly BASE = `${environment.apiUrl}`;
  private readonly IMG  = `${environment.serverUrl}`;

  constructor(public ls: LanguageService, 
    private fb: FormBuilder,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();

    this.profileForm = this.fb.group({
      name:  [this.currentUser?.name  || '', Validators.required],
      email: [this.currentUser?.email || '', [Validators.required, Validators.email]],
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      password:        ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    });

    if (this.currentUser?.photoUrl) {
      this.photoPreview = `${this.IMG}/${this.currentUser.photoUrl}`;
    }

    if (this.currentUser?.id) {
      this.http.get<any>(`${this.BASE}/auth/profile/${this.currentUser.id}`)
        .subscribe({
          next: (data) => {
            this.profileForm.patchValue({ name: data.name, email: data.email });
            if (data.photoUrl) {
              this.photoPreview = `${this.IMG}/${data.photoUrl}`;
              this.authService.updatePhotoInSession(data.photoUrl);
            }
          }
        });
    }
  }

  onPhotoSelected(event: any): void {
    const file: File = event.target.files[0];
    if (!file) return;

    this.photoMsg   = '';
    this.photoError = '';

    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif'];
    if (!allowedTypes.includes(file.type)) {
      this.photoError = '❌ Only JPG, PNG, WEBP, GIF images are allowed.';
      this.selectedFile = null;
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.photoError = '❌ File size must be less than 5MB.';
      this.selectedFile = null;
      return;
    }

    this.selectedFile = file;

    const reader = new FileReader();
    reader.onload = e => this.photoPreview = reader.result as string;
    reader.readAsDataURL(file);
  }

  onUploadPhoto(): void {
    if (!this.selectedFile || !this.currentUser?.id) return;

    this.photoMsg    = '';
    this.photoError  = '';
    this.photoLoading = true;

    const fd = new FormData();
    fd.append('photo', this.selectedFile);

    this.http.post<any>(`${this.BASE}/auth/profile/${this.currentUser.id}/photo`, fd)
      .subscribe({
        next: (res) => {
          this.photoLoading = false;
          this.photoMsg     = '✅ Photo uploaded successfully!';
          this.selectedFile = null;
          this.authService.updatePhotoInSession(res.photoUrl);
          this.currentUser  = this.authService.getCurrentUser();
          this.photoPreview = `${this.IMG}/${res.photoUrl}`;
        },
        error: (err) => {
          this.photoLoading = false;
          this.photoError = '❌ ' + (err.error?.message || 'Upload failed. Try again.');
        }
      });
  }

  onSubmit(): void {
    if (this.profileForm.invalid || !this.currentUser?.id) return;
    this.isLoading  = true;
    this.successMsg = '';
    this.errorMsg   = '';

    this.http.put(`${this.BASE}/auth/profile/${this.currentUser.id}`, this.profileForm.value)
      .subscribe({
        next: () => {
          this.isLoading  = false;
          this.successMsg = '✅ Profile updated successfully!';
          const { name, email } = this.profileForm.value;
          this.authService.updateProfileInSession(name, email);
          this.currentUser = this.authService.getCurrentUser();
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMsg  = '❌ ' + (err.error?.message || 'Failed to update profile. Try again.');
        }
      });
  }

  onChangePassword(): void {
    const { currentPassword, password, confirmPassword } = this.passwordForm.value;
    this.pwSuccess = '';
    this.pwError   = '';

    if (this.passwordForm.invalid) return;
    if (password !== confirmPassword) {
      this.pwError = '❌ Passwords do not match.';
      return;
    }

    this.http.put(`${this.BASE}/auth/change-password/${this.currentUser.id}`,
      { currentPassword, newPassword: password })
      .subscribe({
        next: () => {
          this.pwSuccess = '✅ Password changed successfully!';
          this.passwordForm.reset();
        },
        error: (err) => {
          this.pwError = '❌ ' + (err.error?.message || 'Failed to change password. Try again.');
        }
      });
  }

  getRoleColor(): string {
    const colors: any = {
      'Super Admin': '#e74c3c',
      'Admin':       '#e67e22',
      'Citizen':     '#2ecc71',
      'Officer':     '#3498db',
    };
    return colors[this.currentUser?.role] || '#7f8c8d';
  }
}
