package com.rolster.capacitor.update;

import com.getcapacitor.PluginCall;

public interface UpdateManagerResolver {
  void execute(PluginCall call, int numberApp, String versionApp);
}
