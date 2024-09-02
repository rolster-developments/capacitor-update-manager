export type UpdateStatusType =
  | 'mandatory'
  | 'flexible'
  | 'optional'
  | 'error'
  | 'unnecessary';

export enum UpdateStatus {
  Mandatory = 'mandatory',
  Flexible = 'flexible',
  Optional = 'optional',
  Error = 'error',
  Unnecessary = 'unnecessary'
}

export interface UpdateStatusResponse {
  status: UpdateStatusType;
  versionCode?: string;
  versionNumber?: string;
}

export interface VerifyStatusProps {
  minorMandatory?: number;
  splitCount?: number;
}

export interface UpdateManagerPlugin {
  verifyStatus(props?: VerifyStatusProps): Promise<UpdateStatusResponse>;
}
