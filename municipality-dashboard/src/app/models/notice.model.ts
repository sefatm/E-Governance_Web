export interface Notice {
  id?        : number;
  type       : string;   
  title      : string;
  description: string;
  publishDate: string;   
  expiryDate ?: string;
  status     ?: string; 
  priority   ?: string; 
  attachmentUrl?: string;
  createdBy  ?: string;
  createdAt  ?: string;
}
