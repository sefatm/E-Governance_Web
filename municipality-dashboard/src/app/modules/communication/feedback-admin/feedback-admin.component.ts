import { Component, OnInit } from '@angular/core';
import { CommunicationService } from 'src/app/services/communication.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-feedback-admin',
  templateUrl: './feedback-admin.component.html',
  styleUrls: ['../communication-shared.css', './feedback-admin.component.css']
})
export class FeedbackAdminComponent implements OnInit {

  feedbacks: any[] = [];
  filteredFeedbacks: any[] = [];
  isLoading = false;
  isReplying = false;
  successMsg = '';
  errorMsg = '';
  searchText = '';
  filterStatus = '';
  filterCategory = '';
  expandedIndex: number | null = null;
  showReplyModal = false;
  selectedFb: any = null;
  replyText = '';
  replyStatus = 'UnderReview';
  statuses = ['Pending', 'UnderReview', 'Resolved', 'Closed'];

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

  summary = {
    total: 0,
    pending: 0,
    underReview: 0,
    resolved: 0
  };

  avgRating = 0;

  constructor(public ls: LanguageService, private commService: CommunicationService) {}

  ngOnInit(): void {
    this.loadFeedbacks();
  }

  loadFeedbacks(): void {
    this.isLoading = true;
    this.commService.getAllFeedbacks().subscribe({
      next: (res: any[]) => {
        this.feedbacks = res || [];
        this.filteredFeedbacks = [...this.feedbacks];
        this.calculateSummary();
        this.calculateAvgRating();
        this.isLoading = false;
      },
      error: (err: any) => {
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  calculateSummary(): void {
    this.summary.total = this.feedbacks.length;
    this.summary.pending = this.feedbacks.filter(f => f.status === 'Pending').length;
    this.summary.underReview = this.feedbacks.filter(f => f.status === 'UnderReview').length;
    this.summary.resolved = this.feedbacks.filter(f => f.status === 'Resolved').length;
  }

  calculateAvgRating(): void {
    if (!this.feedbacks.length) {
      this.avgRating = 0;
      return;
    }
    const total =
    this.feedbacks.reduce((sum, f) => sum + (f.rating || 0), 0);
    this.avgRating = Number((total / this.feedbacks.length).toFixed(1));
  }

  filterData(): void {
    const txt = this.searchText.toLowerCase();
    this.filteredFeedbacks = this.feedbacks.filter(f => {
      const matchText =
        !txt ||
        (f.citizenName || '').toLowerCase().includes(txt) ||
        (f.nid || '').toLowerCase().includes(txt) ||
        (f.subject || '').toLowerCase().includes(txt);

      const matchStatus =
        !this.filterStatus ||
        f.status === this.filterStatus;

      const matchCategory =
        !this.filterCategory ||
        f.category === this.filterCategory;

      return matchText && matchStatus && matchCategory;
    });
  }

  clearFilter(): void {

    this.searchText = '';
    this.filterStatus = '';
    this.filterCategory = '';
    this.filteredFeedbacks = [...this.feedbacks];
  }

  toggleDetails(index: number): void {
    this.expandedIndex =
      this.expandedIndex === index ? null : index;
  }

  stars(rating: number): number[] {
    return Array(rating).fill(0);
  }

  statusClass(status: string): string {
    switch (status) {
      case 'Pending':
        return 'status-pending';
      case 'UnderReview':
        return 'status-review';
      case 'Resolved':
        return 'status-resolved';
      case 'Closed':
        return 'status-closed';
      default:
        return '';
    }
  }

  openReply(fb: any): void {
    this.selectedFb = fb;
    this.replyText = fb.adminReply || '';
    this.replyStatus = fb.status || 'UnderReview';
    this.showReplyModal = true;
  }

  submitReply(): void {
    if (!this.replyText.trim()) {
      this.errorMsg = 'Reply message is required.';
      return;
    }

    if (!this.selectedFb) return;
    this.isReplying = true;
    const payload = {
      reply: this.replyText,
      status: this.replyStatus
    };

    this.commService.replyFeedback(this.selectedFb.id, payload)
      .subscribe({
        next: () => {
          this.selectedFb.adminReply = this.replyText;
          this.selectedFb.status = this.replyStatus;
          this.selectedFb.repliedAt = new Date();
          this.showReplyModal = false;
          this.isReplying = false;
          this.calculateSummary();
        },
        error: (err: any) => {
          this.isReplying = false;
          console.error(err);
          this.errorMsg = 'Reply failed.';
        }
      });
  }

  updateStatus(item: any, status: string): void {
    this.commService.updateFeedbackStatus(item.id, status)
      .subscribe({
        next: () => {
          item.status = status;
          this.calculateSummary();
        },
        error: (err: any) => {
          console.error(err);
        }
      });
  }
}
