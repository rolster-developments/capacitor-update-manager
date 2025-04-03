import { WebPlugin } from '@capacitor/core';
import {
  UpdateManagerPlugin,
  UpdateStatus,
  VerifyStatusOptions,
  VerifyStatusResponse
} from './definitions';

const options: VerifyStatusOptions = {
  minorMandatory: 2,
  patchMandatory: 4,
  splitCount: 2
};

export class UpdateManagerWeb extends WebPlugin implements UpdateManagerPlugin {
  public verifyStatus(_ = options): Promise<VerifyStatusResponse> {
    return Promise.resolve({
      status: UpdateStatus.Unnecessary,
      versionCode: '1.0.0'
    });
  }
}
