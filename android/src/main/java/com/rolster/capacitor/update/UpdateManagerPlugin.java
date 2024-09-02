package com.rolster.capacitor.update;

import android.content.Intent;
import android.content.pm.PackageManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.jos.AppUpdateClient;
import com.huawei.hms.jos.JosApps;
import com.huawei.updatesdk.service.appmgr.bean.ApkUpgradeInfo;
import com.huawei.updatesdk.service.otaupdate.CheckUpdateCallBack;
import com.huawei.updatesdk.service.otaupdate.UpdateKey;

import java.io.Serializable;

@CapacitorPlugin(name = "UpdateManager")
public class UpdateManagerPlugin extends Plugin {
    private final int HAS_UPGRADE_INFO = 7;
    
    private AppUpdateManager appUpdateGoogle;
    
    private AppUpdateInfo appInfoGoogle;
    
    private AppUpdateClient appUpdateHuawei;
    
    private ApkUpgradeInfo appInfoHuawei;
    
    @PluginMethod
    public void verifyStatus(PluginCall call) {
        if (hasHuaweiServicesAvailable()) {
            checkForHuaweiUpdate(call);
            return;
        } 
        
        if (hasGoogleServicesAvailable()) {
            checkForGoogleUpdate(call);
            return;
        }

        call.reject("API services for Update not available");
    }
    
    private void checkForGoogleUpdate(PluginCall call) {
        appUpdateGoogle = AppUpdateManagerFactory.create(getContext());

        JSObject result = new JSObject();

        appUpdateGoogle.getAppUpdateInfo().addOnSuccessListener(appInfoGoogle -> {
            if (appInfoGoogle.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                try {
                    int versionCode = appInfoGoogle.availableVersionCode();
                    this.appInfoGoogle = appInfoGoogle;

                    result.put("status", getStatusUpdate(call, versionCode));
                    result.put("versionNumber", versionCode);
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
    
    private void checkForHuaweiUpdate(PluginCall call) {
        appUpdateHuawei = JosApps.getAppUpdateClient(getContext());
        UpdateManagerPlugin plugin = this;

        JSObject result = new JSObject();

        appUpdateHuawei.checkAppUpdate(getContext(), new CheckUpdateCallBack() {
            @Override
            public void onUpdateInfo(Intent intent) {
                int status = intent.getIntExtra(UpdateKey.STATUS, -1);
                Serializable info = intent.getSerializableExtra(UpdateKey.INFO);

                if (status == HAS_UPGRADE_INFO && info instanceof ApkUpgradeInfo upgradeInfo) {
                    int versionCode = upgradeInfo.getVersionCode_();
                    plugin.appInfoHuawei = upgradeInfo;

                    result.put("status", getStatusUpdate(call, versionCode));
                    result.put("versionNumber", versionCode);
                } else {
                    result.put("status", "unnecessary");
                }

                call.resolve(result);
            }

            @Override
            public void onMarketInstallInfo(Intent intent) {
            }

            @Override
            public void onMarketStoreError(int i) {
                result.put("status", "error");
                result.put("msgError", "Huawei MarketStoreError");
                call.resolve(result);
            }

            @Override
            public void onUpdateStoreError(int i) {
                result.put("status", "error");
                result.put("msgError", "Huawei UpdateStoreError");
                call.resolve(result);
            }
        });
    }

    private String getStatusUpdate(PluginCall call, int versionCode) {
        int minorMandatory = call.getInt("minorMandatory", 2);
        int splitCount = call.getInt("splitCount", 2);

        UpdateManagerVersion storeVersion = createVersion(versionCode, splitCount);
        UpdateManagerVersion currentVersion = createVersion(getCurrentNumber(), splitCount);

        return getStatusLevel(storeVersion, currentVersion, minorMandatory);
    }

    private String getStatusLevel(UpdateManagerVersion storeVersion, UpdateManagerVersion currentVersion, int minorMandatory) {
        if (storeVersion.major > currentVersion.major) {
            return "mandatory";
        }

        if (storeVersion.minor > currentVersion.minor) {
            int minorCount = storeVersion.minor - currentVersion.minor;

            return minorCount >= minorMandatory ? "mandatory" : "flexible";
        }

        if (storeVersion.minor == currentVersion.minor && storeVersion.patch > currentVersion.patch) {
            int patchCount = storeVersion.patch - currentVersion.patch;

            return patchCount >= minorMandatory ? "flexible" : "optional";
        }

        return "unnecessary";
    }

    private int getCurrentNumber() {
        try {
            var packageManager = getContext().getPackageManager();
            var packageName = getContext().getPackageName();

            return packageManager.getPackageInfo(packageName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    private UpdateManagerVersion createVersion(int versionCode, int splitCount) {
        String code = padZeros(String.valueOf(versionCode), splitCount * 3);

        int limitPatch = code.length() - splitCount;
        int limitMinor = limitPatch - splitCount;

        int codePatch = Integer.parseInt(code.substring(limitPatch));
        int codeMinor = Integer.parseInt(code.substring(limitMinor, limitPatch));
        int codeMajor = Integer.parseInt(code.substring(0, limitMinor));

        return new UpdateManagerVersion(codeMajor, codeMinor, codePatch);
    }

    private String padZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }

        StringBuilder sb = new StringBuilder();

        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }

        sb.append(inputString);

        return sb.toString();
    }

    private boolean hasGoogleServicesAvailable() {
        GoogleApiAvailability services = GoogleApiAvailability.getInstance();
        int status = services.isGooglePlayServicesAvailable(getContext());

        return status == com.google.android.gms.common.ConnectionResult.SUCCESS;
    }

    private boolean hasHuaweiServicesAvailable() {
        HuaweiApiAvailability services = HuaweiApiAvailability.getInstance();
        int status = services.isHuaweiMobileServicesAvailable(getContext());

        return status == com.huawei.hms.api.ConnectionResult.SUCCESS;
    }
}
