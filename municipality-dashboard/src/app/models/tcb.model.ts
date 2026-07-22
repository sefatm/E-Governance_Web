// src/app/models/tcb.model.ts

export interface TcbStock {
  id?: number;
  batchLabel: string;
  cycleMonth: string;
  ward: string;
  dealerName?: string;
  oilLitre: number;
  riceKg: number;
  lentilKg: number;
  sugarKg: number;
  cashAmount: number;

  oilPricePerLitre: number;
  ricePricePerKg: number;
  lentilPricePerKg: number;
  sugarPricePerKg: number;
  totalCards: number;
  distributed?: number;
  remaining?: number;
  createdAt?: string;
}

export interface DistributionSession {
  id?: number;
  sessionCode?: string;
  stockId?: number;
  dealerName: string;
  ward: string;
  cycleMonth: string;
  status?: string;
  openedAt?: string;
  closedAt?: string;
  totalScanned?: number;
}

export interface ScanResult {
  success: boolean;
  message: string;
  holderName?: string;
  cardNo?: string;
  cardType?: string;
  ward?: string;
  oil?: number;
  rice?: number;
  lentil?: number;
  sugar?: number;
  cash?: number;
  scannedAt?: string;
}

export interface DistributionLog {
  id: number;
  sessionId: number;
  cardNo: string;
  cardType: string;
  holderName: string;
  ward: string;
  oilLitre: number;
  riceKg: number;
  lentilKg: number;
  sugarKg: number;
  cashAmount: number;

  oilPricePerLitre: number;
  ricePricePerKg: number;
  lentilPricePerKg: number;
  sugarPricePerKg: number;
  scannedAt: string;
  scannedBy: string;
}
