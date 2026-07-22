export interface HoldingApplication {
  id?: number;
  status?: string;

  applicantName: string;
  father?: string;
  mother?: string;
  nid: string;

  holdingNo?: string;
  previousHoldingNo?: string;
  road?: string;
  area?: string;
  mouza?: string;
  ward?: number;

  landSize?: number;
  structureType?: string;
  rooms?: number;
  floorsTin?: number;
  floorsPaka?: number;
  unitsPerFloor?: number;
  areaPerFloor?: number;
  constructionYear?: number;
  ownership?: string;
  usageType?: string;

  deedCopy?: boolean;
  mutationCopy?: boolean;
  nidCopy?: boolean;
  citizenship?: boolean;

  contactName: string;
  mobile: string;
  email?: string;
  address: string;

  latitude?: number;
  longitude?: number;

  nidFileUrl?: string;
  deedFileUrl?: string;
  photoUrl?: string;

  rejectReason?: string;
  createdAt?: string;
  applicationDate?: string;
}
