import Foundation
import Capacitor

struct AppStoreInfo: Decodable {
    let results: [AppStoreInfoResult]
    
    struct AppStoreInfoResult: Decodable {
        let trackId: Int?
        let currentVersionReleaseDate: String?
        let minimumOsVersion: String?
        let releaseNotes: String?
        let version: String?
    }
}

struct UpdateStatus {
    public let currentVersion: String

    public let storeVersion: String
    
    public let minimumOsVersion: String
    
    init(currentVersion: String, storeVersion: String, minimumOsVersion: String) {
        self.currentVersion = currentVersion
        self.storeVersion = storeVersion
        self.minimumOsVersion = minimumOsVersion
    }
    
    public static var unknwon: UpdateStatus {
        return UpdateStatus(currentVersion: "0.0.0", storeVersion: "0.0.0", minimumOsVersion: "1.0")
    }
}

struct Version {
    public let major: Int

    public let minor: Int
    
    public let patch: Int
    
    init(major: Int?, minor: Int?, patch: Int?) {
        self.major = major ?? 0
        self.minor = minor ?? 0
        self.patch = patch ?? 0
    }
}

enum UpdateManagerError: Error {
    case malformedUrl
}

@objc(UpdateManagerPlugin)
public class UpdateManagerPlugin: CAPPlugin {
    @objc func verifyStatus(_ call: CAPPluginCall) {
        Task {
            let minorMandatory = call.getInt("minorMandatory") ?? 2
            
            do {
                let urlStore = try self.createUrlStoreApplication()
                let updateStatus = try await self.requestUpdateStatus(url: urlStore)
                let updateLevel = self.getUpdateLevel(updateStatus: updateStatus, minorMandatory: minorMandatory)
                
                call.resolve([
                    "status": updateLevel,
                    "versionCode": updateStatus.storeVersion
                ])
            } catch {
                call.reject("Error in check status version in AppStore")
            }
        }
    }
    
    private func requestUpdateStatus(url: URL) async throws -> UpdateStatus {
        do {
            var request = URLRequest(url: url, cachePolicy: .reloadIgnoringLocalAndRemoteCacheData, timeoutInterval: 30)
            request.httpMethod = "GET"
            
            let (data, _) = try await URLSession.shared.data(for: request)
            
            let response = try JSONDecoder().decode(AppStoreInfo.self, from: data)
            
            let currentInstalledVersion = Bundle.main.releaseVersionNumber ?? "0.0.0"
            
            return UpdateStatus(
                currentVersion: currentInstalledVersion,
                storeVersion: response.results.first?.version ?? "0.0.0",
                minimumOsVersion: response.results.first?.minimumOsVersion ?? "1.0"
            )
        } catch {
            throw UpdateManagerError.malformedUrl
        }
    }
    
    private func createUrlStoreApplication() throws -> URL {
        let bundleIdentifier = Bundle.main.bundleIdentifier ?? "";
        
        guard let url = URL(string: "https://itunes.apple.com/lookup?bundleId=" + bundleIdentifier) else {
            throw UpdateManagerError.malformedUrl
        }

        return url
    }

    private func getUpdateLevel(updateStatus: UpdateStatus, minorMandatory: Int) -> String {
        let currentVersion = getVersion(version: updateStatus.currentVersion)
        let storeVersion = getVersion(version: updateStatus.storeVersion)

        if storeVersion.major > currentVersion.major {
            return "mandatory"
        } 
        
        if storeVersion.minor > currentVersion.minor {
            let minorCount = storeVersion.minor - currentVersion.minor
            
            return minorCount >= minorMandatory ? "mandatory" : "flexible"
        } 
        
        if storeVersion.minor == currentVersion.minor && storeVersion.patch > currentVersion.patch {
            let patchCount = storeVersion.patch - currentVersion.patch

            return patchCount >= minorMandatory ? "flexible" : "optional"
        }
        
        return "unnecessary"
    }
    
    private func getVersion(version: String) -> Version {
        let results = version.split{$0 == "."}.map {String($0)}.map {Int($0) ?? 0}
        
        return Version(major: results[0], minor: results[1], patch: results[2])
    }
}

extension Bundle {
    var releaseVersionNumber: String? {
        return infoDictionary?["CFBundleShortVersionString"] as? String
    }
}
