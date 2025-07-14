package com.rolster.capacitor.update.huawei;

import android.content.Context;
import android.content.Intent;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.jos.AppUpdateClient;
import com.huawei.hms.jos.JosApps;
import com.huawei.updatesdk.service.appmgr.bean.ApkUpgradeInfo;
import com.huawei.updatesdk.service.otaupdate.CheckUpdateCallBack;
import com.huawei.updatesdk.service.otaupdate.UpdateKey;
import com.rolster.capacitor.update.UpdateManagerResolver;
import com.rolster.capacitor.update.UpdateManagerUtils;

import java.io.Serializable;

public class HuaweiUpdateManagerResolver implements UpdateManagerResolver {
    private final int HAS_UPGRADE_INFO = 7;

    private final Context context;
    
    private AppUpdateClient appUpdateHuawei;

    public HuaweiUpdateManagerResolver(Context context) {
        this.context = context;
    }

    @Override()
    public void execute(PluginCall call, int numberApp, String versionApp) {
        appUpdateHuawei = JosApps.getAppUpdateClient(this.context);

        JSObject result = new JSObject();

        appUpdateHuawei.checkAppUpdate(this.context, new CheckUpdateCallBack() {
            @Override
            public void onUpdateInfo(Intent intent) {
                int status = intent.getIntExtra(UpdateKey.STATUS, -1);
                Serializable info = intent.getSerializableExtra(UpdateKey.INFO);

                if (status == HAS_UPGRADE_INFO && info instanceof ApkUpgradeInfo upgradeInfo) {
                    int numberStore = upgradeInfo.getVersionCode_();

                    int splitCount = call.getInt("splitCount", 2);
                    int minorMandatory = call.getInt("minorMandatory", 2);
                    int patchMandatory = call.getInt("patchMandatory", 4);

                    result.put("status", UpdateManagerUtils.getStatusUpdate(numberStore, numberApp, minorMandatory, patchMandatory, splitCount));
                    result.put("versionCode", versionApp);
                    result.put("versionNumber", numberStore);
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
}