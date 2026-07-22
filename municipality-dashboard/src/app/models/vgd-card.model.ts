// src/app/models/vgd-card.model.ts

export interface VgdCard {
  id?: number;
  cardNo?: string;
  cardType: string;       // 'VGD' | 'VGF'
  holderName: string;
  nid: string;
  dateOfBirth?: string;
  contact?: string;
  husbandName?: string;
  fatherName?: string;
  occupation?: string;
  address: string;
  ward?: string;
  unionName?: string;
  upazila?: string;
  district?: string;
  maritalStatus?: string;
  disability?: string;
  hasLand?: boolean;
  landArea?: number;
  incomeMonthly?: string;
  membersCount?: number;
  childrenCount?: number;
  // ✅ FIX Bug 12: hasOtherCard in model
  hasOtherCard?: boolean;
  monthlyRiceKg?: number;
  monthlyWheatKg?: number;
  cashAmount?: number;
  cycleMonths?: number;
  startDate?: string;
  endDate?: string;
  lastReceivedDate?: string;
  bankName?: string;
  bankAccount?: string;
  mobileBanking?: string;
  mobileBankingNo?: string;
  photoUrl?: string;
  nidFileUrl?: string;
  status?: string;
  rejectionReason?: string;
  approvedBy?: string;
  approvedAt?: string;
  createdAt?: string;
}

// ✅ FIX Bug 7: id field added — needed for downloadUrl in status component
export interface VgdStatusResponse {
  id: number;           // ✅ was missing before — caused /download/null 404
  cardNo: string;
  cardType: string;
  status: string;
  holderName: string;
  monthlyRiceKg: number;
  cashAmount: number;
  startDate: string;
  endDate: string;
  lastReceivedDate: string;
  rejectionReason: string;
}
