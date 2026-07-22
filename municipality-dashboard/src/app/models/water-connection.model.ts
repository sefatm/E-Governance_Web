export interface WaterConnection {

  id?: number;

  name: string;
  fatherName?: string;
  nid?: string;

  phone: string;
  email?: string;          // ← নতুন field

  district: string;
  upazila: string;
  ward: string;
  address: string;

  connectionType: string;

  members?: number;
  usage?: string;
  startDate?: string;
  description?: string;

  agree?: boolean;

  status: string;
  createdAt?: string;
}
