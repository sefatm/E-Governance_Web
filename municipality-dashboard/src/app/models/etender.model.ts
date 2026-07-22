// ── Gap Fix #6: ETenderBid interface এ নতুন backend fields যোগ করা হয়েছে
export interface ETenderNotice {
  id?: number;
  tenderNo?: string;
  title: string;
  category: string;
  description?: string;
  estimatedCost: number;
  emdAmount: number;
  startDate: string;
  endDate: string;
  workLocation?: string;
  documentUrl?: string;
  status?: string;
  createdAt?: string;
}

export interface ETenderBid {
  id?: number;
  tenderId: number;
  bidderName: string;
  companyName: string;
  nid: string;
  mobile: string;
  email?: string;
  bidAmount: number;
  completionDays: number;
  experienceYears?: number;
  previousWorks?: string;
  emdReceiptNo?: string;
  status?: string;
  submittedAt?: string;

  // ── নতুন fields (Gap Fix #6) ─────────────────────────────────────────────
  documentUrl?: string;          // Bidder এর uploaded document path
  docVerified?: boolean | null;  // null=Pending, true=Verified, false=Rejected
  docRemark?: string;            // Admin এর verification note
  isLowest?: boolean;            // সবচেয়ে কম bid কিনা (auto-calculated)
}

export interface ETenderAward {
  id?: number;
  tenderId: number;
  bidId: number;
  awardedTo?: string;
  awardedAmount?: number;
  awardDate?: string;
  remarks?: string;
  createdAt?: string;
}

export interface VendorBlacklist {
  id?: number;
  nid?: string;
  email?: string;
  mobile?: string;
  vendorName?: string;
  companyName?: string;
  reason?: string;
  blacklistedBy?: string;
  blacklistedAt?: string;
  active?: boolean;
}

export interface BlacklistCheckResult {
  blacklisted: boolean;
  message: string;
}
