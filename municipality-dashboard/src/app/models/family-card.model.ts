export interface FamilyCard {
  id?: number;
  cardNo?: string;
  holderName: string;
  nid: string;
  dateOfBirth?: string;
  contact: string;
  address: string;
  ward?: string;
  unionName?: string;
  upazila?: string;       
  district?: string;
  membersCount: number;
  incomeMonthly?: string;
  occupation?: string;
  husbandOrFatherName?: string; 
  hasOtherCard?: boolean;
  photoUrl?: string;
  nidFileUrl?: string;
  status?: string;
  rejectionReason?: string;
  approvedBy?: string;
  approvedAt?: string;
  createdAt?: string;
}

export interface CardStatusResponse {
  cardNo: string;
  status: string;
  holderName: string;
  rejectionReason: string;
}
