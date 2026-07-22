
export interface TradeLicenseApplication {
  businessName: string;
  businessType: string;
  licensePeriod: number;
  ownerName: string;
  fatherName: string;
  motherName: string;
  dateOfBirth: string;
  nid: string;
  phone: string;
  email?: string;
  address: string;
  wardNo: string;
  holdingNo: string;
  income: number;
  tax: number;
}