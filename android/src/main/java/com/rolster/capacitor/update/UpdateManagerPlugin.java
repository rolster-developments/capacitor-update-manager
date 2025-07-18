package com.rolster.capacitor.update;

import android.content.Context;
import android.content.pm.PackageManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "UpdateManager")
public class UpdateManagerPlugin extends Plugin {
  private UpdateManagerResolver updateManagerResolver;

  @Override
  public void load() {
    try {
      String updateManagerClass = BuildConfig.IS_HMS ?
        "com.rolster.capacitor.update.huawei.HuaweiUpdateManagerResolver" :
        "com.rolster.capacitor.update.google.GoogleUpdateManagerResolver";
            
      updateManagerResolver = (UpdateManagerResolver) Class.forName(updateManagerClass)
        .getConstructor(Context.class)
        .newInstance(getContext());
    } catch (Exception e) {
      throw new RuntimeException("Error init UpdateManagerResolver", e);
    }
  }
    
  @PluginMethod
  public void verifyStatus(PluginCall call) {
    try {
      var packageManager = getContext().getPackageManager();
      var packageName = getContext().getPackageName();

      var packageInfo = packageManager.getPackageInfo(packageName, 0);

      String appVersion = packageInfo.versionName;
      int appCode = packageInfo.versionCode;

      updateManagerResolver.execute(call, appCode, appVersion);
    } catch (PackageManager.NameNotFoundException e) {
      JSObject result = new JSObject();

      result.put("status", "error");
      result.put("msgError", e.getMessage());
      call.resolve(result);
    }
  }
}
