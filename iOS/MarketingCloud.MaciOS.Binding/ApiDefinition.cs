using Foundation;
using UIKit;

namespace MarketingCloudiOS
{
	[BaseType(typeof(NSObject))]
	interface DotnetMarketingCloud
	{
		// +(NSString * _Nonnull)getStringWithMyString:(NSString * _Nonnull)myString __attribute__((warn_unused_result("")));
		[Static]
		[Export("getStringWithMyString:")]
		string GetString(string myString);

		// +(void)initializeSDKWithAppId:(NSString * _Nonnull)appId accessToken:(NSString * _Nonnull)accessToken appEndpointURL:(NSString * _Nonnull)appEndpointURL mid:(NSString * _Nonnull)mid;
		[Static]
		[Export("initializeSDKWithAppId:accessToken:appEndpointURL:mid:")]
		void InitializeSDKWithAppId(string appId, string accessToken, string appEndpointURL, string mid);

		// +(BOOL)application:(UIApplication * _Nonnull)application didFinishLaunchingWithOptions:(NSDictionary<UIApplicationLaunchOptionsKey,id> * _Nullable)launchOptions __attribute__((warn_unused_result("")));
		[Static]
		[Export("application:didFinishLaunchingWithOptions:")]
		bool Application(UIApplication application, [NullAllowed] NSDictionary<NSString, NSObject> launchOptions);

		// +(void)setContactKeyWithContactKey:(NSString * _Nonnull)contactKey;
		[Static]
		[Export("setContactKeyWithContactKey:")]
		void SetContactKeyWithContactKey(string contactKey);

		// +(void)setProfileAttributeWithKey:(NSString * _Nonnull)key value:(NSString * _Nonnull)value;
		[Static]
		[Export("setProfileAttributeWithKey:value:")]
		void SetProfileAttributeWithKey(string key, string value);

		// +(void)setupMobilePush;
		[Static]
		[Export("setupMobilePush")]
		void SetupMobilePush();

		// +(void)registerDeviceToken:(NSData * _Nonnull)deviceToken;
		[Static]
		[Export("registerDeviceToken:")]
		void RegisterDeviceToken(NSData deviceToken);

	}
}