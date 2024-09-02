import { WebPlugin } from '@capacitor/core';
import {
  UpdateManagerPlugin,
  UpdateStatus,
  UpdateStatusResponse,
  VerifyStatusProps
} from './definitions';

const verifyStatusProps: VerifyStatusProps = {
  minorMandatory: 2,
  splitCount: 2
};

export class UpdateManagerWeb extends WebPlugin implements UpdateManagerPlugin {
  public verifyStatus(_ = verifyStatusProps): Promise<UpdateStatusResponse> {
    return Promise.resolve({
      status: UpdateStatus.Unnecessary,
      versionCode: '5.0.0'
    });
  }
}
