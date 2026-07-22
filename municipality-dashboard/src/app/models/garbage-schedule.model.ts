export interface GarbageSchedule {
  id?: number;

  ward: string;
  area: string;
  day: string;
  time: string;

  status?: string;
}