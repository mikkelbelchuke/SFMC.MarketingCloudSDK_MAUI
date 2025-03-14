# 🚀 Salesforce Marketing Cloud SDK for .NET MAUI

This repository provides a **.NET MAUI binding** for integrating the **Salesforce Marketing Cloud (SFMC) SDK** into iOS and Android applications.  

## 📌 Prerequisites

### 1️⃣ **Generate an Authentication Certificate**
Before integrating the SDK, follow the **Salesforce MobilePush** guide to create the required **APNs authentication key**:  
🔗 [Salesforce MobilePush Prerequisites](https://developer.salesforce.com/docs/marketing/mobilepush/guide/prerequisites.html#recommended-create-an-authentication-key)

### 2️⃣ **Install Required NuGet Packages**
Run the following commands to install the necessary dependencies:

```sh
dotnet add package Microsoft.Extensions.Configuration.Json
dotnet add package Microsoft.Extensions.Configuration.Binder
```

---

## 📱 **iOS Implementation**

### 🔹 **Step 1: Update `AppDelegate.cs`**
Modify your `AppDelegate.cs` to handle **push notification registration** and **error handling**.

```csharp
[Export("application:didRegisterForRemoteNotificationsWithDeviceToken:")]
public void RegisteredForRemoteNotifications(UIApplication application, NSData deviceToken)
{
    Console.WriteLine("✅ Successfully registered for push notifications");

    // Pass device token to SFMC via Swift binding
    MarketingCloudiOS.DotnetMarketingCloud.RegisterDeviceToken(deviceToken);
}

[Export("application:didFailToRegisterForRemoteNotificationsWithError:")]
public void FailedToRegisterForRemoteNotifications(UIApplication application, NSError error)
{
    Debug.WriteLine($"❌ Push registration failed: {error.Description}");
}

[Export("application:didReceiveRemoteNotification:fetchCompletionHandler:")]
public void DidReceiveRemoteNotification(UIApplication application, NSDictionary notification, Action<UIBackgroundFetchResult> completionHandler)
{
    Console.WriteLine("📩 Received remote notification");

    // Handle the notification when the app is in the foreground
    completionHandler(UIBackgroundFetchResult.NewData);
}
```

✅ **This ensures that iOS handles push notifications correctly and registers the device with SFMC.**

---

### 🔹 **Step 2: Configure SFMC Settings**
Create a **configuration file** to dynamically load Marketing Cloud credentials in **`Config/MarketingCloudConfig.cs`**.

```csharp
using Microsoft.Extensions.Configuration;

public class MarketingCloudConfig
{
    public string AppId { get; set; }
    public string AccessToken { get; set; }
    public string AppEndpointURL { get; set; }
    public string Mid { get; set; }

    public static MarketingCloudConfig Load(IConfiguration configuration)
    {
#if DEBUG
        string environment = "Debug";
#else
        string environment = "Production";
#endif
        return configuration.GetRequiredSection($"MarketingCloudConfig:{environment}").Get<MarketingCloudConfig>()
            ?? throw new Exception("❌ Marketing Cloud configuration is missing.");
    }
}
```

✅ **This allows for easy environment-based configuration (`Debug` vs `Production`).**

---

### 🔹 **Step 3: Add `appsettings.json` for Configuration**
Add **Salesforce Marketing Cloud credentials** inside **`Resources/Raw/appsettings.json`**.

```json
{
  "MarketingCloudConfig": {
    "Debug": {
      "AppId": "7827e05b-315f-4b7a-abdf-d64ed934d36f",
      "AccessToken": "f19wBCRFvlM2V7FTaIKBH1qp",
      "AppEndpointURL": "https://mc8j2ghzb85r2zywfzdynn1hhjc8.device.marketingcloudapis.com/",
      "Mid": "536004638"
    },
    "Production": {
      "AppId": "7827e05b-315f-4b7a-abdf-d64ed934d36f",
      "AccessToken": "f19wBCRFvlM2V7FTaIKBH1qp",
      "AppEndpointURL": "https://mc8j2ghzb85r2zywfzdynn1hhjc8.device.marketingcloudapis.com/",
      "Mid": "536004638"
    }
  }
}
```

✅ **This enables different configurations for Development and Production.**

---

### 🔹 **Step 4: Register the Marketing Cloud SDK in `MauiProgram.cs`**
Modify **`MauiProgram.cs`** to initialize the Marketing Cloud SDK during app startup.

#### **📌 Add the registration method**
```csharp
.RegisterMarketingCloudSDK();
```

#### **📌 Implement the `RegisterMarketingCloudSDK` method**
Inside **`MauiProgram.cs`**, add the following extension method:

```csharp
#region MarketingCloudSDK
private static MauiAppBuilder RegisterMarketingCloudSDK(this MauiAppBuilder mauiAppBuilder)
{
    try
    {
        mauiAppBuilder.ConfigureLifecycleEvents(events =>
        {
#if IOS
            events.AddiOS(iOS => iOS.WillFinishLaunching((_, __) =>
            {
                var config = MarketingCloudConfig.Load(mauiAppBuilder.Configuration);

                // Initialize SFMC SDK with App Credentials
                MarketingCloudiOS.DotnetMarketingCloud.ConfigureSDKWithAppId(
                    config.AppId, config.AccessToken, config.AppEndpointURL, config.Mid
                );

                // Register user & set profile attributes
                MarketingCloudiOS.DotnetMarketingCloud.RegisterUserWithContactKey("mikkeltest@twoday.com");
                MarketingCloudiOS.DotnetMarketingCloud.SetProfileAttributeWithKey("FirstName", "Mikkel");
                MarketingCloudiOS.DotnetMarketingCloud.SetProfileAttributeWithKey("LastName", "Belchuke");

                return true;
            }));
#elif ANDROID
            events.AddAndroid(android => android.OnCreate((activity, bundle) =>
            {
                // TODO: Implement Android setup
            }));
#endif
        });
    }
    catch (Exception ex)
    {
        Debug.WriteLine($"❌ Error initializing RegisterMarketingCloudSDK: {ex.Message}");
    }
    return mauiAppBuilder;
}
#endregion
```

✅ **This ensures the SDK is initialized, the user is registered, and profile attributes are set at app startup.**

---

## 🎯 **Final Summary**
| **Step** | **Action** |
|----------|-----------|
| ✅ **1** | Install required NuGet packages |
| ✅ **2** | Update `AppDelegate.cs` to handle push notification registration |
| ✅ **3** | Create `Config/MarketingCloudConfig.cs` to manage Salesforce credentials |
| ✅ **4** | Add `Resources/Raw/appsettings.json` for environment-based configurations |
| ✅ **5** | Modify `MauiProgram.cs` to initialize the Marketing Cloud SDK |

🚀 **Now, your .NET MAUI app is fully integrated with the Salesforce Marketing Cloud SDK!** 🎯🔥

