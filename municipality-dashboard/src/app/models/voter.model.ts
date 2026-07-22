export interface Voter {
  id?: number;
  name: string;
  dob: string;
  gender: string;
  fatherName: string;
  motherName: string;
  nid: string;
  mobile: string;
  email?: string;           
  district: string;
  upazila: string;
  area: string;
  address: string;
  electionType: string;
  zoneId: number | null;
  centerId: number | null;
  registrationDate: string;
  status: string;
  photoUrl?: string;
  rejectReason?: string;
  showReject?: boolean;
}
