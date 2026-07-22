export interface FarmerCard {
  id?: number;
  cardNo?: string;
  farmerName: string;
  nid: string;
  dateOfBirth?: string;
  fatherName?: string;
  occupation?: string;
  incomeMonthly?: number;
  contact: string;
  address: string;
  ward?: string;
  unionName?: string;
  upazila?: string;
  district?: string;
  landOwn?: number;
  landVerified?: boolean;
  landVerifiedBy?: string;
  landVerifiedAt?: string;
  landLease?: number;
  landTotal?: number;
  cropTypes?: string;
  farmingSeason?: string;
  bankName?: string;
  bankAccount?: string;
  bankBranch?: string;
  fertilizerQuota?: number;
  seedQuota?: number;
  lastSubsidyDate?: string;
  hasOtherCard?: boolean;
  photoUrl?: string;
  nidFileUrl?: string;
  landDocUrl?: string;
  status?: string;
  rejectionReason?: string;
  approvedBy?: string;
  approvedAt?: string;
  createdAt?: string;
}

export interface FarmerStatusResponse {
  cardNo: string;
  status: string;
  farmerName: string;
  fertilizerQuota: number;
  seedQuota: number;
  rejectionReason: string;
}
