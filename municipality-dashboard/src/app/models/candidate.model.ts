export interface Nominee {
  id?: number;

  name: string;
  fathersName: string;
  mothersName: string;
  nid: string;
  mobileNumber: string;
  dob: string;

  electionType: string;
  area: string;

  party: string;
  symbol?: string;            
  symbolFileUrl?: string;      

  zoneId: number | null;
  centerId: number | null;

  declaration: string;
  hasCriminalRecord: boolean;

  status: string;
  rejectReason?: string;

  showReject?: boolean;       
}
