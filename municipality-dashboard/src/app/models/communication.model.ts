export interface NotificationMessage {
  id?: number;
  type: string;
  title?: string;
  message: string;
  recipientType: string;
  recipientVal?: string;
  recipientName?: string;
  sentBy?: string;
  status?: string;
  serviceTag?: string;
  createdAt?: string;
}

export interface CitizenFeedback {
  id?: number;
  citizenName: string;
  nid?: string;
  mobile: string;
  email?: string;
  ward?: string;
  category: string;
  subject: string;
  message: string;
  rating?: number;
  status?: string;
  adminReply?: string;
  repliedAt?: string;
  createdAt?: string;
  agree?: boolean;
}
