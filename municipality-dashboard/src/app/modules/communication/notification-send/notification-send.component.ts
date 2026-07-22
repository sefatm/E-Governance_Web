import { Component, OnInit } from '@angular/core';
import { NotificationMessage } from 'src/app/models/communication.model';
import { CommunicationService } from 'src/app/services/communication.service';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-notification-send',
  templateUrl: './notification-send.component.html',
  styleUrls: ['../communication-shared.css', './notification-send.component.css']
})
export class NotificationSendComponent implements OnInit {

  activeType   : 'SMS' | 'Email' | 'Push' = 'SMS';
  isSubmitting = false;
  successMsg   = '';
  errorMsg     = '';

  serviceTags = ['General','WaterBill','TradeLicense','HoldingTax','Election','Health','ETender'];
  wardOptions = Array.from({ length: 9 }, (_, i) => `${i + 1}`);

  form: NotificationMessage = this.emptyForm();

  // ✅ Push recipient type — All (topic) or Individual (FCM token)
  // Ward push নেই কারণ ward-wise FCM subscription setup লাগে
  get pushRecipientTypes() {
    return ['All', 'Individual'];
  }

  constructor(public ls: LanguageService, private commService: CommunicationService) {}
  ngOnInit(): void {}

  selectType(type: 'SMS' | 'Email' | 'Push'): void {
    this.activeType = type;
    this.form       = this.emptyForm();
    this.form.type  = type;
    // ✅ Push-এ Ward option নেই
    if (type === 'Push' && this.form.recipientType === 'Ward') {
      this.form.recipientType = 'All';
    }
    this.successMsg = '';
    this.errorMsg   = '';
  }

  emptyForm(): NotificationMessage {
    return {
      type: this.activeType || 'SMS',
      title: '', message: '',
      recipientType: 'All',
      recipientVal: '', recipientName: '',
      serviceTag: 'General', sentBy: 'Admin'
    };
  }

  sendNotification(): void {
    if (!this.form.message?.trim()) {
      this.errorMsg = 'Please enter message content.'; return;
    }
    if (this.form.recipientType === 'Individual' && !this.form.recipientVal?.trim()) {
      this.errorMsg = 'Please enter recipient contact.'; return;
    }
    if (this.form.recipientType === 'Ward' && !this.form.recipientVal?.trim()) {
      this.errorMsg = 'Please enter ward number.'; return;
    }
    if ((this.form.type === 'Email' || this.form.type === 'Push') && !this.form.title?.trim()) {
      this.errorMsg = 'Please enter a subject/title.'; return;
    }

    this.isSubmitting = true;
    this.successMsg   = '';
    this.errorMsg     = '';

    this.commService.send(this.form).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        const status = res?.status || 'Sent';
        const target = this.form.recipientType === 'Individual'
          ? `→ ${this.form.recipientVal}`
          : this.form.recipientType === 'Ward'
            ? `→ Ward ${this.form.recipientVal}`
            : '→ All Citizens';

        // ✅ Queued status সঠিকভাবে handle
        if (status === 'Queued') {
          this.successMsg = `✅ ${this.form.type} queued successfully! ${target} — Background-এ পাঠানো হচ্ছে।`;
        } else {
          this.successMsg = `✅ ${this.form.type} ${status} successfully! ${target}`;
        }
        this.form = this.emptyForm();
        setTimeout(() => this.successMsg = '', 8000);
      },
      error: (err: any) => {
        this.isSubmitting = false;
        this.errorMsg = err?.error?.message || `Failed to send ${this.activeType}. Please try again.`;
      }
    });
  }

  resetForm(): void {
    this.form       = this.emptyForm();
    this.successMsg = '';
    this.errorMsg   = '';
  }

  get charCount(): number { return this.form.message?.length || 0; }
  get isSmsType():   boolean { return this.activeType === 'SMS'; }
  get isEmailType(): boolean { return this.activeType === 'Email'; }
  get isPushType():  boolean { return this.activeType === 'Push'; }
}
