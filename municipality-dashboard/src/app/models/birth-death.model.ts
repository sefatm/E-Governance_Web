export interface BirthApplication {
  id?: number;

  nameBn: string;
  nameEn: string;
  dob: string;
  placeOfBirth: string;
  gender: string;
  bloodGroup?: string;

  mobile: string;
  email?: string;

  presentAddress: string;
  permanentAddress: string;

  fathersName: string;
  fathersDob?: string;
  fathersNid: string;
  fathersMobile?: string;
  fathersEmail?: string;

  mothersName: string;
  mothersDob?: string;
  mothersNid: string;
  mothersMobile?: string;
  mothersEmail?: string;

  emergencyName?: string;
  emergencyPhone?: string;

  paymentMethod: string;
  amount: number;

  status?: string;
}

export interface DeathApplication {
  id?: number;

  nameBn: string;
  nameEn: string;
  dob?: string;
  dateOfDeath: string;
  placeOfDeath: string;
  gender: string;

  birthNo?: string;
  nid?: string;

  presentAddress: string;
  permanentAddress: string;

  applicantName: string;
  relation: string;
  mobile: string;
  email?: string;

  paymentMethod: string;
  amount: number;

  status?: string;
}