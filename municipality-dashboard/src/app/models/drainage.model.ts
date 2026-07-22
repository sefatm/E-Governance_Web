export interface DrainageRequest {
  id?: number;
  name: string;
  nid: string;
  contact: string;
  district: string;
  upazila: string;
  ward: string;
  area: string;
  type: string;      
  problem: string;   
  length?: number;
  width?: number;
  description: string;
  priority: string;
  agree?: boolean;   
  status?: string;
  createdAt?: string;
}
