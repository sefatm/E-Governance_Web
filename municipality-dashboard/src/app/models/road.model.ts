export interface RoadRequest {
  id?: number;
  name: string;
  nid?: string;
  contact: string;
  district: string;
  upazila: string;
  ward: string;
  area: string;
  roadName: string;
  type: string;
  roadCondition: string;
  length?: number;
  width?: number;
  description: string;
  priority: string;
  agree?: boolean;  
  status?: string;
  createdAt?: string;
  latitude?: number;
  longitude?: number;
}
