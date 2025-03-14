//
//  DotnetMarketingCloud.swift
//  MarketingCloud
//
//  Created by .NET MAUI team on 6/18/24.
//

import Foundation
import SFMCSDK
import MarketingCloudSDK

@objc(DotnetMarketingCloud)
public class DotnetMarketingCloud : NSObject
{

    
    @objc
    public static func getString(myString: String) -> String {
        return myString  + " from swift!"
    }

    // Define features of MobilePush your app will use.
    public static let inbox : Bool = false
    public static let location : Bool = false
    public static let analytics : Bool = true

    static let logger = Logger();
    
    @objc
    public static func requestNotificationPermission() {
            logger.debug("📡 Requesting push notification permission...")

            DispatchQueue.main.async {
                UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
                    if let error = error {
                        logger.error("Push Notification Authorization Error: \(error.localizedDescription)")
                    } else {
                        logger.debug("Push Notification Authorization Granted: \(granted)")
                        if granted {
                            DispatchQueue.main.async {
                                UIApplication.shared.registerForRemoteNotifications()
                            }
                        }
                    }
                }
            }
        }
        
    @objc
    public static func configureSDK(
        appId: String,
        accessToken: String,
        appEndpointURL: String,
        mid: String) {

        // Enable logging for debugging early on. Debug level is not recommended for production apps,
        // as significant data about MobilePush will be logged to the console.
        #if DEBUG
        SFMCSdk.setLogger(logLevel: .debug)
        #endif
        
        let appEndpoint = URL(string: appEndpointURL)!

        // Use the Mobile Push Config Builder to configure the Mobile Push Module.
        let mobilePushConfiguration = PushConfigBuilder(appId: appId)
            .setAccessToken(accessToken)
            .setMarketingCloudServerUrl(appEndpoint)
            .setMid(mid)
            .setInboxEnabled(inbox)
            .setLocationEnabled(location)
            .setAnalyticsEnabled(analytics)
            .build()

        // Set the completion handler to take action when module initialization is completed. The result indicates if initialization was sucesfull or not.
        // Seting the completion handler is optional.
        let completionHandler: (OperationResult) -> () = { result in
            if result == .success {
                // module is fully configured and ready for use
                self.setupMobilePush()
            } else if result == .error {
                // module failed to initialize, check logs for more details
            } else if result == .cancelled {
                // module initialization was cancelled (for example due to re-confirguration triggered before init was completed)
            } else if result == .timeout {
                // module failed to initialize due to timeout, check logs for more details
            }
        }
       

        // Initialize the SDK with the built configuration
        SFMCSdk.initializeSdk(ConfigBuilder()
            .setPush(config: mobilePushConfiguration, onCompletion: completionHandler)
            .build())
    }

    @objc
    public static func registerUser(contactKey: String)  {
        SFMCSdk.requestPushSdk { mp in
            SFMCSdk.identity.setProfileId(contactKey)
        }
    }

   @objc
   public static func setProfileAttribute(key: String, value: String) {
        SFMCSdk.requestPushSdk { mp in
            SFMCSdk.identity.setProfileAttributes([key: value])
        }
    }
        

    @objc
    public static func setupMobilePush() {

        // Set the MarketingCloudSDKURLHandlingDelegate to a class adhering to the protocol.
        // In this example, the AppDelegate class adheres to the protocol (see below)
        // and handles URLs passed back from the SDK.
        // For more information, see https://salesforce-marketingcloud.github.io/MarketingCloudSDK-iOS/sdk-implementation/implementation-urlhandling.html
        SFMCSdk.requestPushSdk { mp in
            //mp.setURLHandlingDelegate(self)
        }


        // Make sure to dispatch this to the main thread, as UNUserNotificationCenter will present UI.
        DispatchQueue.main.async {
            // Set the UNUserNotificationCenterDelegate to a class adhering to thie protocol.
            // In this exmple, the AppDelegate class adheres to the protocol (see below)
            // and handles Notification Center delegate methods from iOS.
            //UNUserNotificationCenter.current().delegate = self // --- TODO ---

            // Request authorization from the user for push notification alerts.
            UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge], completionHandler: {(_ granted: Bool, _ error: Error?) -> Void in
                if error == nil {
                    if granted == true {
                        // Your application may want to do something specific if the user has granted authorization
                        // for the notification types specified; it would be done here.
                    }
                }
            })

            // In any case, your application should register for remote notifications *each time*
            // your application launches to ensure that the push token used by MobilePush (for silent push)
            // is updated if necessary.

            // Registering in this manner does *not* mean that a user will see a notification - it only means
            // that the application will receive a unique push token from iOS.
            UIApplication.shared.registerForRemoteNotifications()
        }
    }
    
    @objc
    public static func registerDeviceToken(_ deviceToken: NSData) {
        logger.debug("📡 Registering device token with SFMC...")

        SFMCSdk.requestPushSdk { pushInstance in
            pushInstance.setDeviceToken(deviceToken as Data)
        }
    }
    
    @objc
    public static func failedToRegisterForRemoteNotificationsWithError(_ error: Error) {
        logger.error("📡 Failed to register for remote notifications: \(error.localizedDescription)")
    }

    // MobilePush SDK: REQUIRED IMPLEMENTATION
    @objc
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print(error)
    }

    // MobilePush SDK: REQUIRED IMPLEMENTATION
    /** This delegate method offers an opportunity for applications with the "remote-notification" background mode to fetch appropriate new data in response to an incoming remote notification.
    //You should call the fetchCompletionHandler as soon as you're finished performing that operation,
    // so the system can accurately estimate its power and data cost.
    // This method will be invoked even if the application was launched or resumed because of the remote notification.
    // The respective delegate methods will be invoked first.
    // Note that this behavior is in contrast to application:didReceiveRemoteNotification:, which is not called in those cases, and which will not be invoked if this method is implemented. **/
    @objc
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        SFMCSdk.requestPushSdk { mp in
            mp.setNotificationUserInfo(userInfo)
        }
        completionHandler(.newData)
    }

    // MobilePush SDK: REQUIRED IMPLEMENTATION
    // The method will be called on the delegate when the user responded to the notification by opening the application,
    // dismissing the notification or choosing a UNNotificationAction.
    // The delegate must be set before the application returns from applicationDidFinishLaunching:.
    @available(iOS 10.0, *)
    @objc
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        // Required: tell the MarketingCloudSDK about the notification. This will collect MobilePush analytics
        // and process the notification on behalf of your application.
        SFMCSdk.requestPushSdk { mp in
            mp.setNotificationRequest(response.notification.request)
        }
        completionHandler()
    }

    // MobilePush SDK: REQUIRED IMPLEMENTATION
    // The method will be called on the delegate only if the application is in the foreground.
    // If the method is not implemented or the handler is not called in a timely manner then the notification will not be presented.
    // The application can choose to have the notification presented as a sound, badge, alert and/or in the notification list.
    // This decision should be based on whether the information in the notification is otherwise visible to the user.
    @available(iOS 10.0, *)
    @objc
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler(.alert)
    }
}
