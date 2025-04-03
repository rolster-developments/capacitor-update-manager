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

export interface VerifyStatusResponse {
  status: UpdateStatusType;
  versionCode?: string;
  versionNumber?: string;
}

export interface VerifyStatusOptions {
  minorMandatory?: number;
  patchMandatory?: number;
  splitCount?: number;
}

export interface UpdateManagerPlugin {
  verifyStatus(options?: VerifyStatusOptions): Promise<VerifyStatusResponse>;
}
