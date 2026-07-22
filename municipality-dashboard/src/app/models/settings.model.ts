export interface SystemSetting {
  id?: number;
  settingKey: string;
  settingVal: string;
  label: string;
  category?: string;
  updatedAt?: string;
}

export interface AuditLog {
  id?: number;
  username: string;
  userRole?: string;
  action: string;
  module: string;
  details?: string;
  ipAddress?: string;
  status?: string;
  createdAt?: string;
}
