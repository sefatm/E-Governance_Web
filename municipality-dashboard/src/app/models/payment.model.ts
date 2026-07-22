export interface PaymentTransaction {
  id?: number;
  txnRef?: string;
  citizenNid: string;
  citizenName: string;
  mobile: string;
  email?: string;
  serviceType: string;
  serviceRefId?: number;
  holdingNo?: string;
  description?: string;
  amount: number;
  method: string;
  providerTxnId?: string;
  cardLast4?: string;
  status?: string;
  failureReason?: string;
  paidAt?: string;
  createdAt?: string;
}

export interface PaymentReceipt {
  id?: number;
  receiptNo?: string;
  txnId?: number;
  citizenNid?: string;
  citizenName?: string;
  serviceType?: string;
  description?: string;
  amount?: number;
  method?: string;
  issuedAt?: string;
}

export interface PaymentSummary {
  totalCollected: number;
  completed: number;
  pending: number;
  failed: number;
  refunded: number;
}
