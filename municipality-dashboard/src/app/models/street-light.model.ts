export interface LightRequest {
  id?: number;
  name: string;
  nid: string;
  contact: string;
  district: string;
  upazila: string;
  ward: string;
  location: string;
  problemType: string;
  count?: number;
  lightType: string;   
  description: string;
  priority: string;
  agree?: boolean;     
  status?: string;
  createdAt?: string;
}
