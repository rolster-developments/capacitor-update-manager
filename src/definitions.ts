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
  versionApp: string;
  versionCode?: string;
  versionStore?: string;
}

export interface VerifyStatusOptions {
  minorMandatory?: number;
  patchMandatory?: number;
  splitCount?: number;
}

export interface UpdateManagerPlugin {
  verifyStatus(options?: VerifyStatusOptions): Promise<VerifyStatusResponse>;
}
