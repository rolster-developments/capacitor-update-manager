import { registerPlugin } from '@capacitor/core';
import type { UpdateManagerPlugin } from './definitions';

const UpdateManager = registerPlugin<UpdateManagerPlugin>('UpdateManager', {
  web: () => import('./web').then((m) => new m.UpdateManagerWeb())
});

export * from './definitions';
export { UpdateManager };
