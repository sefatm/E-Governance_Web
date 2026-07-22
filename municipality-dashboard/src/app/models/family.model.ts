export interface FamilyCertificate {
  id?: number;

  headName: string;
  nid: string;

  memberCount: number;

  members?: string;
  membersJson?: string;

  address: string;
  permanentAddress?: string;

  division?: string;
  district?: string;

  contact: string;
  purpose: string;

  status?: string;

  certificateNo?: string;

  headPhotoUrl?: string;
  headNidUrl?: string;

  createdAt?: string;
}