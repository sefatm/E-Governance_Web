export interface ProjectListItem {
  id?: number;
  name: string;
  location: string;
  startDate: string;
  endDate: string;
  status?: string;
  progress?: number;     
  createdAt?: string;
}
