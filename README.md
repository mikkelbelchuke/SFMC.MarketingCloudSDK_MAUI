# 🚀 Salesforce Marketing Cloud SDK for .NET MAUI

This repository provides a **.NET MAUI binding** for integrating the **Salesforce Marketing Cloud (SFMC) SDK** into iOS and Android applications.  

### 📌 Available API Endpoints & Roadmap

This section outlines the currently available API endpoints in this version of the SFMC.MarketingCloudSDK.MAUI package and provides insight into the upcoming features planned for future releases.

⸻

✅ Available API Endpoints (Current Version)

The following API endpoints are implemented and ready for use in this version:

#### 🔹 ANDROID

| Endpoint          | Description |
| -------------     | ------------ |
| ```InitializeSDK(activity, notificationDrawable, mcApplicationId, mcAccessToken, fcmSenderId, marketingCloudUrl, inboxEnabled, analyticsEnabled, isDebug)```   | Initializes the Salesforce Marketing Cloud SDK.|
| ```TogglePushPermission(granted)```  | Enables or disables push notifications based on user permission.|
| ```SetContactKey(contactKey)``` | Sets the contact key for the current user.|
| ```setProfileAttribute(key,value)``` | Sets a custom profile attribute for the user.|

#### 🔹 iOS

| Endpoint          | Description |
| -------------     | ------------ |
| ```InitializeSDKWithAppId(appId, accessToken, appEndpointURL, mid)```   | Initializes the Salesforce Marketing Cloud SDK.|
| ```SetupMobilePush()```  | Enables or disables push notifications based on user permission.|
| ```SetContactKeyWithContactKey(contactKey)``` | Sets the contact key for the current user.|
| ```SetProfileAttribute(key,value)``` | Sets a custom profile attribute for the user.|
| ```RegisterDeviceToken(token)``` | Sets a custom profile attribute for the user.|


---

### 🔜 Upcoming Features (Next Versions)

In the next releases, we plan to expand the API capabilities with the following features:

🚀 Planned Enhancements

| Feature Group          | Expected Version | Status |
| -------------     | ------------ | ------------ |
| Inbox Message Management | v1.1.0 | 🔄 In Progress|
| General SDK Information | v1.1.1 | 🆕 Planned|
| User & Device Management | v1.1.2 | 🆕 Planned|
| Attribute Management | v1.1.3 | 🆕 Planned|
| Push Notification Management | v1.1.4 | 🆕 Planned|



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

Add SFMC SDK

```sh
dotnet add package SFMC.MarketingCloudSDK.MAUI
```

### 3️⃣ **Configure SFMC Settings**
Create a **configuration file** to dynamically load Marketing Cloud credentials in **`Config/MarketingCloudConfig.cs`**.

```csharp
using Microsoft.Extensions.Configuration;

public class MarketingCloudConfig
{
    public string AppId { get; set; }
    public string AccessToken { get; set; }
    public string AppEndpointURL { get; set; }
    public string Mid { get; set; }
    public string SenderId { get; set; }

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

### 4️⃣ **Add `appsettings.json` for Configuration**
Add **Salesforce Marketing Cloud credentials** inside **`Resources/Raw/appsettings.json`**.

```json
{
  "MarketingCloudConfig": {
    "Debug": {
      "AppId": "",
      "AccessToken": "",
      "AppEndpointURL": "",
      "Mid": "",
      "SenderId": ""
    },
    "Production": {
      "AppId": "",
      "AccessToken": "",
      "AppEndpointURL": "",
      "Mid": "",
      "SenderId": ""
    }
  }
}
```

✅ **This enables different configurations for Development and Production.**


### 5️⃣ **Register the Marketing Cloud SDK in `MauiProgram.cs`**
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
			var config = MarketingCloudConfig.Load(mauiAppBuilder.Configuration);
#if IOS
			events.AddiOS(iOS => iOS.WillFinishLaunching((_, __) =>
		    {
			    MarketingCloudiOS.DotnetMarketingCloud.InitializeSDKWithAppId(
				    config.AppId,
				    config.AccessToken,
				    config.AppEndpointURL,
				    config.Mid
			    );

			    return true;
		    }));
#elif ANDROID
			events.AddAndroid(android => android.OnCreate(async (activity, bundle) =>
			{
				var token = await CrossFirebaseCloudMessaging.Current.GetTokenAsync();

				MarketingCloudAndroid.DotnetMarketingCloud.InitializeSDK(
					activity,
					Resource.Drawable.ic_stat_logo,
					config.AppId,
					config.AccessToken,
					config.SenderId,
					config.AppEndpointURL,
					true, // Enable Inbox
					true, // Enable Analytics
					true // Enable Debug Mode
				);
			}));
#endif
		});

	}
	catch (Exception ex)
	{
		Debug.WriteLine($"Error trying to init RegisterMauiMobilePushSDK: {ex.Message}");
	}

	return mauiAppBuilder;
}
#endregion
```

✅ **This ensures the SDK is initialized, the user is registered, and profile attributes are set at app startup.**

---




##  **iOS Implementation**

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


### 🔹 **Step 2: Add this where you will ask for push permissions**

```csharp
#if IOS
					MarketingCloudiOS.DotnetMarketingCloud.SetupMobilePush();
#endif
```

✅ **This ensures that iOS prompt for permissions.**

---

## 🤖 **Android Implementation**

⚠️ Important: Firebase Cloud Messaging (FCM) Integration Required!

Before integrating Salesforce Marketing Cloud SDK (SFMC) into your Android App, you must first integrate Firebase Cloud Messaging (FCM) into your project.
SFMC relies on Firebase for push notifications, and FCM must be initialized before SFMC SDK to avoid issues with token retrieval.

### 🔹 **Step 1: Update `MainActivity.cs`**

```csharp
public class MainActivity : MauiAppCompatActivity
	{
		protected override void OnCreate(Bundle savedInstanceState)
		{
			base.OnCreate(savedInstanceState);

			CreateNotificationChannelIfNeeded();
		}

		protected override void OnNewIntent(Intent intent)
		{
			base.OnNewIntent(intent);
		}

		private void CreateNotificationChannelIfNeeded()
		{
			if (Build.VERSION.SdkInt >= BuildVersionCodes.O)
			{
				CreateNotificationChannel();
			}
		}

		private void CreateNotificationChannel()
		{
			try
			{
				var channelId = $"{PackageName}.general";
				var notificationManager = (NotificationManager)GetSystemService(NotificationService);
				var channel = new NotificationChannel(channelId, "General", NotificationImportance.Default);
				notificationManager.CreateNotificationChannel(channel);

			}
			catch (Exception ex)
			{
				System.Diagnostics.Debug.WriteLine($"Error trying to create notification channel\n{ex}");
			}
		}

		public override void OnRequestPermissionsResult(int requestCode, string[] permissions, [GeneratedEnum] Permission[] grantResults)
		{
			Platform.OnRequestPermissionsResult(requestCode, permissions, grantResults);
			base.OnRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}
````

### 🔹 **Step 2: Add this where you will ask for push permissions**

```csharp
#if ANDROID
				MarketingCloudAndroid.DotnetMarketingCloud.TogglePushPermission(true/false);
#endif
```

✅ **This ensures that Android prompt for permissions.**