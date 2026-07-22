export interface EpiChild {
  id?: number;
  childName:    string;
  dateOfBirth:  string;
  gender:       string;
  fatherName:   string;
  motherName:   string;
  guardianNid?:   string;
  fatherNid?: string;
  motherNid?: string;
  guardianPhone:  string;
  guardianEmail?: string;
  ward:         string;
  unionName?:   string;
  upazila?:     string;
  district?:    string;
  address?:     string;
  presentAddress?: string;
  permanentAddress?: string;
  birthPlace?: string;
  childPhotoUrl?: string;
  fatherNidFileUrl?: string;
  motherNidFileUrl?: string;
  cardNo?:        string;
  registeredBy?:  string;
  createdAt?:     string;
}

export interface VaccinationRecord {
  id?: number;
  vaccineName:  string;
  doseNo:       string;
  scheduledDate:  string;
  givenDate?:     string;
  status:         string;
  givenBy?:       string;
  healthCenter?:  string;
  batchNo?:       string;
  remarks?:       string;
}
