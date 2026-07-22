export interface Complaint {

  id?: number;
  name: string;
  ward: string;
  area: string;
  category: string;
  description: string;
  contact: string;
  location: string;
  status?: string;
  remarks?: string;
  imageUrl?: string;
  createdAt?: Date;
}