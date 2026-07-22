export interface WaterBill {
  id?: number;
  name: string;
  email?: string;
  mobile?: string;
  nid?: string;
  meterNo: string;
  month: string;
  previousReading: number;
  currentReading: number;
  units?: number;
  amount?: number;
  connectionType: string;
  billType: string;
  status?: string;
  paymentMethod?: string;
  txnRef?: string;
  receiptNo?: string;
  authoritySignature?: string;
  authoritySeal?: string;
  paidAt?: string;
  createdAt?: string;
}
