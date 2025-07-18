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
  public let appVersion: String

  public let storeVersion: String
    
  public let minimumOsVersion: String
    
  init(appVersion: String, storeVersion: String, minimumOsVersion: String) {
    self.appVersion = appVersion
    self.storeVersion = storeVersion
    self.minimumOsVersion = minimumOsVersion
  }
    
  public static var unknwon: UpdateStatus {
    return UpdateStatus(appVersion: "0.0.0", storeVersion: "0.0.0", minimumOsVersion: "1.0")
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
      do {
        let urlStore = try self.createUrlStoreApplication()
        let updateStatus = try await self.requestUpdateStatus(url: urlStore)
        let status = self.getUpdateStatus(updateStatus: updateStatus, call: call)
                
        call.resolve([
          "status": status,
          "versionApp": updateStatus.appVersion,
          "versionStore": updateStatus.storeVersion
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
        appVersion: currentInstalledVersion,
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

  private func getUpdateStatus(updateStatus: UpdateStatus, call: CAPPluginCall) -> String {
    let minorMandatory = call.getInt("minorMandatory") ?? 2
    let patchMandatory = call.getInt("patchMandatory") ?? 4

    let appVersion = getVersion(version: updateStatus.appVersion)
    let storeVersion = getVersion(version: updateStatus.storeVersion)

    if storeVersion.major > appVersion.major {
      return "mandatory"
    } 
        
    if storeVersion.minor > appVersion.minor {
      let minorCount = storeVersion.minor - appVersion.minor
            
      return minorCount >= minorMandatory ? "mandatory" : "flexible"
    } 
        
    if storeVersion.minor == appVersion.minor && storeVersion.patch > appVersion.patch {
      let patchCount = storeVersion.patch - appVersion.patch

      return patchCount >= patchMandatory ? "mandatory" : patchCount >= minorMandatory ? "flexible" : "optional"
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
