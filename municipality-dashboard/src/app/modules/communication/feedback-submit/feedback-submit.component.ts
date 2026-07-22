import { Component, OnInit } from '@angular/core';
import { CommunicationService } from 'src/app/services/communication.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-feedback-submit',
  templateUrl: './feedback-submit.component.html',
  styleUrls: ['../communication-shared.css', './feedback-submit.component.css']
})
export class FeedbackSubmitComponent implements OnInit {

  isSubmitting = false;
  successMsg = '';
  errorMsg = '';
  selectedRating = 0;
  hoveredStar = 0;
  success = false;
  submittedId = '';

  form: any = {
    citizenName: '',
    nid: '',
    mobile: '',
    email: '',
    ward: '',
    category: '',
    subject: '',
    message: '',
    rating: 0,
    agree: false
  };

  categories = [
    'Water Supply',
    'Trade License',
    'Holding Tax',
    'Complaint',
    'E-Voting',
    'Road',
    'Waste Management',
    'General'
  ];

  constructor(public ls: LanguageService, private commService: CommunicationService) {}

  ngOnInit(): void {}

  // STAR RATING
  setRating(r: number): void {
    this.selectedRating = r;
    this.form.rating = r;
  }

  submitForm(): void {
    if (
      !this.form.citizenName ||
      !this.form.mobile ||
      !this.form.category ||
      !this.form.subject ||
      !this.form.message ||
      !this.form.rating
    ) {
      this.errorMsg = 'Please fill all required fields.';
      return;
    }
    this.isSubmitting = true;
    this.errorMsg = '';
    this.commService.submitFeedback(this.form).subscribe({
      next: (res: any) => {
        this.isSubmitting = false;
        this.success = true;
        this.submittedId =
          res?.id ||
          Math.floor(100000 + Math.random() * 900000).toString();

      },

      error: (err: any) => {
        this.isSubmitting = false;
        this.errorMsg = err?.error?.message || 'Submission failed. Please try again.';
      }
    });
  }

  resetForm(): void {
    this.success = false;
    this.form = {
      citizenName: '',
      nid: '',
      mobile: '',
      email: '',
      ward: '',
      category: '',
      subject: '',
      message: '',
      rating: 0,
      agree: false
    };

    this.selectedRating = 0;
    this.hoveredStar = 0;
  }
}
