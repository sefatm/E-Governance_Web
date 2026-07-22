export interface OwnershipTransfer {
  id?:              number;
  currentOwner:     string;
  currentOwnerNid?: string;  
  newOwner:         string;
  newOwnerNid?:     string;   
  contact:          string;
  relationship?:    string;  

  holdingNumber:    string;
  wardNo?:          string;   
  address:          string;
  reason:           string;

  currentOwnerNidFileUrl?: string;  
  newOwnerNidFileUrl?:     string;  
  deedFileUrl?:            string;  

  declaration?:     boolean;  
  status?:          string;
  rejectReason?:    string;
  createdAt?:       string;
}
