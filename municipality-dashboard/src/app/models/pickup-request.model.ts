export interface WasteRequest {
  id?: number;
  name: string;
  address: string;
  ward: string;
  phone: string;
  email?: string;
  type: string;
  status: string;
  lat?: number;
  lng?: number;
  createdAt?: string;
}
