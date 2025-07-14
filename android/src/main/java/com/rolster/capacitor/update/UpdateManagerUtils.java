package com.rolster.capacitor.update;

public class UpdateManagerUtils {
    public static String getStatusUpdate(
        int numberStore,
        int numberApp,
        int minorMandatory,
        int patchMandatory, 
        int splitCount) {
        UpdateManagerVersion versionApp = createVersion(numberApp, splitCount);
        UpdateManagerVersion versionStore = createVersion(numberStore, splitCount);

        return getStatusLevel(versionStore, versionApp, minorMandatory, patchMandatory);
    }

    private static String getStatusLevel(
        UpdateManagerVersion versionStore,
        UpdateManagerVersion versionApp,
        int minorMandatory,
        int patchMandatory) {
        if (versionStore.major > versionApp.major) {
            return "mandatory";
        }

        if (versionStore.minor > versionApp.minor) {
            int minorCount = versionStore.minor - versionApp.minor;

            return minorCount >= minorMandatory ? "mandatory" : "flexible";
        }

        if (versionStore.minor == versionApp.minor && versionStore.patch > versionApp.patch) {
            int patchCount = versionStore.patch - versionApp.patch;

            return patchCount >= patchMandatory ? 
                "mandatory" : patchCount >= minorMandatory ? "flexible" : "optional";
        }

        return "unnecessary";
    }

    private static UpdateManagerVersion createVersion(int versionCode, int splitCount) {
        String code = padZeros(String.valueOf(versionCode), splitCount * 3);

        int limitPatch = code.length() - splitCount;
        int limitMinor = limitPatch - splitCount;

        int codePatch = Integer.parseInt(code.substring(limitPatch));
        int codeMinor = Integer.parseInt(code.substring(limitMinor, limitPatch));
        int codeMajor = Integer.parseInt(code.substring(0, limitMinor));

        return new UpdateManagerVersion(codeMajor, codeMinor, codePatch);
    }

    private static String padZeros(String inputString, int length) {
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
}
