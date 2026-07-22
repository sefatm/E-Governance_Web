export interface CitizenCertificate {

  id?: number;
  name: string;
  fatherName: string;
  motherName: string;
  nid: string;
  dateOfBirth: string;
  gender: string;
  bloodGroup?: string;
  religion?: string;
  maritalStatus?: string;
  occupation?: string;
  contact: string;
  email?: string;
  address: string;
  permanentAddress?: string;
  division: string;
  district: string;
  certificateType: string;
  purpose: string;
  declaration: boolean;

  photoUrl?: string;
  nidFileUrl?: string;


  status?: string;
  createdAt?: string;
}