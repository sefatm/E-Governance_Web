import { environment } from 'src/environments/environment';
import { Component, HostListener, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { PaymentService } from 'src/app/services/payment.service';
import { PaymentTransaction } from 'src/app/models/payment.model';
import { LanguageService } from 'src/app/services/language.service';

interface Toast { type: 'success' | 'error'; message: string; removing?: boolean; }

@Component({
  selector: 'app-payment-gateway',
  templateUrl: './payment-gateway.component.html',
  styleUrls: ['./payment-gateway.component.css']
})
export class PaymentGatewayComponent implements OnInit {

  paymentForm!: FormGroup;
  isSubmitting  = false;
  isConfirming  = false;
  currentStep   = 1;
  steps         = ['নাগরিক তথ্য', 'পেমেন্ট বিবরণ', 'নিশ্চিত করুন'];
  toasts: Toast[] = [];
  isMobile      = false;

  // After initiate — waiting for confirm
  pendingTxn: any = null;
  providerTxnIdInput = '';   // ngModel binding for Step 4 input (outside <form>)

  // After confirm — show receipt
  success = false;
  receipt: any = null;
  createdTxn: any = null;

  paymentMethods = [
    { val: 'Bkash',  label: 'bKash',          icon: 'fa-mobile-screen',    color: '#E2136E' },
    { val: 'Nagad',  label: 'Nagad',           icon: 'fa-mobile-screen',    color: '#F4821F' },
    { val: 'Rocket', label: 'Rocket',          icon: 'fa-mobile-screen',    color: '#8B1C8C' },
    { val: 'Card',   label: 'Debit / Credit',  icon: 'fa-credit-card',      color: '#185FA5' },
    { val: 'Bank',   label: 'Net Banking',     icon: 'fa-building-columns', color: '#0d5c3a' },
  ];

  private base = `${environment.apiUrl}/payment`;

  constructor(public ls: LanguageService, 
    private fb: FormBuilder,
    private paymentService: PaymentService,
    private http: HttpClient,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.checkDevice();

    this.paymentForm = this.fb.group({
      citizenNid:    ['', Validators.required],
      citizenName:   ['', Validators.required],
      mobile:        ['', [Validators.required, Validators.pattern(/^01[3-9]\d{8}$/)]],
      email:         ['', Validators.email],
      serviceType:   ['', Validators.required],
      serviceRefId:  [null],
      holdingNo:     [''],
      description:   [''],
      amount:        [0, [Validators.required, Validators.min(1)]],
      method:        ['', Validators.required],
      agree:         [false],
      providerTxnId: [''],
      cardNumber:    [''],
      cardName:      [''],
      cardExpiry:    [''],
      cardCvv:       [''],
      bankTxnId:     ['']
    });

    // QueryParams থেকে pre-fill (TaxPayment থেকে আসলে)
    this.route.queryParams.subscribe(p => {
      if (p['serviceType']) this.paymentForm.patchValue({ serviceType: p['serviceType'] });
      if (p['serviceRefId']) this.paymentForm.patchValue({ serviceRefId: Number(p['serviceRefId']) });
      if (p['holdingNo'])    this.paymentForm.patchValue({ holdingNo: p['holdingNo'] });
      if (p['amount'])      this.paymentForm.patchValue({ amount: Number(p['amount']) });
      if (p['description']) this.paymentForm.patchValue({ description: p['description'] });
      if (p['nid'])         this.paymentForm.patchValue({ citizenNid: p['nid'] });
      if (p['name'])        this.paymentForm.patchValue({ citizenName: p['name'] });
      if (p['mobile'])      this.paymentForm.patchValue({ mobile: p['mobile'] });
    });
  }

  @HostListener('window:resize')
  onResize() { this.checkDevice(); }
  checkDevice() { this.isMobile = window.innerWidth < 768; }

  selectedMethod(): any {
    const val = this.paymentForm.get('method')?.value;
    return this.paymentMethods.find(m => m.val === val);
  }

  nextStep(): void {
    if (this.currentStep === 1) {
      const { citizenNid, citizenName, mobile } = this.paymentForm.controls;
      if (citizenNid.invalid || citizenName.invalid || mobile.invalid) {
        this.paymentForm.markAllAsTouched();
        this.showToast('নাগরিকের সব তথ্য পূরণ করুন', 'error'); return;
      }
    }
    if (this.currentStep === 2) {
      const { serviceType, amount, method } = this.paymentForm.controls;
      if (serviceType.invalid || amount.invalid || !method.value) {
        this.showToast('সেবার ধরন, পরিমাণ ও পেমেন্ট পদ্ধতি বেছে নিন', 'error'); return;
      }
    }
    if (this.currentStep < 3) { this.currentStep++; window.scrollTo({ top: 0, behavior: 'smooth' }); }
  }

  prevStep(): void {
    if (this.currentStep > 1) { this.currentStep--; window.scrollTo({ top: 0, behavior: 'smooth' }); }
  }

  // ── STEP A: Initiate transaction ──────────────────────────
  submitPayment(): void {
    if (!this.paymentForm.get('agree')?.value) {
      this.showToast('ঘোষণাপত্রে সম্মতি দিন', 'error'); return;
    }
    const fv = this.paymentForm.value;
    const payload: PaymentTransaction = {
      citizenNid:  fv.citizenNid,
      citizenName: fv.citizenName,
      mobile:      fv.mobile,
      email:       fv.email,
      serviceType: fv.serviceType,
      serviceRefId: fv.serviceRefId,
      holdingNo:    fv.holdingNo,
      description: fv.description,
      amount:      fv.amount,
      method:      fv.method
    };

    this.isSubmitting = true;
    this.paymentService.initiate(payload).subscribe({
      next: (res: any) => {
        this.isSubmitting = false;
        this.pendingTxn   = res.txn;
        // Step 4 — confirm screen দেখাও
        this.currentStep  = 4;
        window.scrollTo({ top: 0, behavior: 'smooth' });
        this.showToast('Transaction তৈরি হয়েছে। পেমেন্ট নিশ্চিত করুন।', 'success');
      },
      error: (err) => {
        this.isSubmitting = false;
        this.showToast(err?.error?.message || 'পেমেন্ট শুরু করতে সমস্যা হয়েছে', 'error');
      }
    });
  }

  // ── STEP B: Confirm (provider TxnId দিয়ে) ────────────────
  confirmPayment(): void {
    const providerTxnId = this.providerTxnIdInput?.trim() ||
                          this.paymentForm.get('providerTxnId')?.value?.trim() ||
                          this.paymentForm.get('bankTxnId')?.value?.trim() || '';
    if (!providerTxnId) {
      this.showToast('Transaction ID is required to confirm payment.', 'error');
      return;
    }

    this.isConfirming = true;
    this.paymentService.confirm(this.pendingTxn.id, providerTxnId).subscribe({
      next: (res) => {
        this.isConfirming = false;
        this.createdTxn   = res.txn || this.pendingTxn;
        this.receipt      = res.receipt;
        this.success      = true;
        window.scrollTo({ top: 0, behavior: 'smooth' });
      },
      error: (err) => {
        this.isConfirming = false;
        this.showToast(err?.error?.message || 'পেমেন্ট নিশ্চিত করতে সমস্যা হয়েছে', 'error');
      }
    });
  }

  // ── Fail (cancel করলে) ────────────────────────────────────
  cancelPayment(): void {
    if (!this.pendingTxn) return;
    this.http.put(`${this.base}/fail/${this.pendingTxn.id}`, { reason: 'User cancelled' }).subscribe();
    this.resetForm();
    this.showToast('পেমেন্ট বাতিল করা হয়েছে।', 'error');
  }

  isInvalid(name: string): boolean {
    const c = this.paymentForm.get(name);
    return !!(c?.invalid && c?.touched);
  }

  showToast(message: string, type: 'success' | 'error'): void {
    const t: Toast = { type, message };
    this.toasts.push(t);
    setTimeout(() => {
      t.removing = true;
      setTimeout(() => { this.toasts = this.toasts.filter(x => x !== t); }, 300);
    }, 5000);
  }

  downloadPdf(): void {
    if (!this.createdTxn?.id) return;
    this.paymentService.downloadReceiptPdfByTxn(this.createdTxn.id, this.receipt?.receiptNo);
  }

  printReceipt(): void {
    window.print();
  }

  resetForm(): void {
    this.paymentForm.reset();
    this.currentStep       = 1;
    this.isSubmitting      = false;
    this.isConfirming      = false;
    this.pendingTxn        = null;
    this.providerTxnIdInput = '';
    this.success           = false;
    this.receipt           = null;
    this.createdTxn        = null;
    this.toasts            = [];
  }
}
