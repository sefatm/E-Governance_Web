export interface LpgCard {
  id?: number;
  cardNo?: string;

  holderName: string;
  nid: string;
  contact: string;
  address: string;

  ward?: string;
  upazila?: string;          
  district?: string;
  unionName?: string;
  dateOfBirth?: string;
  membersCount?: number;
  stoveCount?: number;        
  hasGasLine?: boolean;    

  dealerName?: string;
  dealerCode?: string;       
  dealerContact?: string;  

  monthlyQuota?: number;
  cylinderSize?: string;     
  lastCollectedAt?: string;  

  incomeMonthly?: number;
  occupation?: string;
  hasOtherCard?: boolean;

  photoUrl?: string;
  nidFileUrl?: string;

  status?: string;
  rejectionReason?: string;
  approvedBy?: string;
  approvedAt?: string;
  createdAt?: string;
}

export interface LpgStatusResponse {
  cardNo: string;
  status: string;
  holderName: string;
  monthlyQuota: number;
  cylinderSize: string;
  dealerName: string;
  lastCollectedAt: string;
  rejectionReason: string;
}
