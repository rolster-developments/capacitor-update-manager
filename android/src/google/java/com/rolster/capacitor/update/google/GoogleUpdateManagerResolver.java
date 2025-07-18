package com.rolster.capacitor.update.google;

import android.content.Context;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.rolster.capacitor.update.UpdateManagerResolver;
import com.rolster.capacitor.update.UpdateManagerUtils;

public class GoogleUpdateManagerResolver implements UpdateManagerResolver {
  private final Context context;

  private AppUpdateManager appUpdateGoogle;
    
  public GoogleUpdateManagerResolver(Context context) {
    this.context = context;
  }

  @Override()
  public void execute(PluginCall call, int appCode, String appVersion) {
    appUpdateGoogle = AppUpdateManagerFactory.create(this.context);

    JSObject result = new JSObject();
    result.put("versionApp", appVersion);
    result.put("versionCode", appCode);

    appUpdateGoogle.getAppUpdateInfo().addOnSuccessListener(appInfoGoogle -> {
      if (appInfoGoogle.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
        try {
          int storeCode = appInfoGoogle.availableVersionCode();

          int splitCount = call.getInt("splitCount", 2);
          int minorMandatory = call.getInt("minorMandatory", 2);
          int patchMandatory = call.getInt("patchMandatory", 4);

          String status = UpdateManagerUtils.getStatusUpdate(storeCode, appCode, minorMandatory, patchMandatory, splitCount);

          result.put("status", status);
          result.put("versionCode", storeCode);
        } catch (Exception error) {
          result.put("status", "error");
          result.put("msgError", error.getMessage());
        }
      } else {
        result.put("status", "unnecessary");
      }

      call.resolve(result);
    }).addOnFailureListener(error -> {
      result.put("status", "error");
      result.put("msgError", error.getMessage());

      call.resolve(result);
    });
  }
}
