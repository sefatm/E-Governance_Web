export interface Zone {
  id: number;
  name: string;
}

export interface Center {
  id: number;
  name: string;
  zoneId: number;
}

export interface AuditLog {
  id?: number;
  action: string;        
  nid: string;
  electionId: number;
  details: string;
  timestamp: string;
}