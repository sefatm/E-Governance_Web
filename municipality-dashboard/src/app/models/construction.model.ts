export interface ConstructionRequest {

  id?: number;

  applicantName: string;
  guardianName?: string;
  nid: string;
  contact: string;
  email?: string;

  district: string;
  upazila: string;
  ward: string;
  plotNo: string;
  location: string;

  buildingType: string;
  floors: number;
  area: number;
  landSize?: string;
  startDate: string;

  engineerName?: string;
  licenseNo?: string;

  description?: string;

  status?: string;
  createdAt?: Date;

  agree?: boolean;
}
