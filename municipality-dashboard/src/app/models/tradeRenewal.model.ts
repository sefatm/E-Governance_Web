export interface TradeRenewal {
  id?: number;

  licenseNumber:    string;
  licenseExpiry:    string;
  issuingAuthority: string;
  businessName:     string;
  businessType:     string;
  address:          string;
  wardNo:           string;
  holdingNo:        string;

  applicantName:    string;
  fatherName:       string;
  motherName:       string;
  dateOfBirth:      string;
  nid:              string;
  contact:          string;
  email?:           string;

  renewalPeriod:    number;
  annualIncome:     number;
  taxPaid:          number;
  purpose:          string; 
  declaration:      boolean;

  nidFileUrl?:      string;
  photoUrl?:        string;
  licenseFileUrl?:  string;

  status?:          string;
  createdAt?:       string;
}
