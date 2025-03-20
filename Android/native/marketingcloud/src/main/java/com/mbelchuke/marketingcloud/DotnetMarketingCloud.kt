package com.mbelchuke.marketingcloud

import com.salesforce.marketingcloud.MCLogListener
import com.salesforce.marketingcloud.sfmcsdk.components.logging.LogListener
import com.salesforce.marketingcloud.sfmcsdk.components.logging.LogLevel
import com.salesforce.marketingcloud.MCLogListener.AndroidLogListener
import com.salesforce.marketingcloud.MarketingCloudSdk
import org.json.JSONObject

import android.app.Activity
import android.util.Log
import com.salesforce.marketingcloud.MarketingCloudConfig
import com.salesforce.marketingcloud.messages.inbox.InboxMessage
import com.salesforce.marketingcloud.sfmcsdk.InitializationStatus
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdk
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdkModuleConfig
import com.salesforce.marketingcloud.notifications.NotificationCustomizationOptions


const val LOG_TAG = "~#MarketingCloud"

class DotnetMarketingCloud {
    companion object {

        @JvmStatic
        fun initializeSDK(
            activity: Activity,
            notificationDrawable: Int,
            mcApplicationId: String,
            mcAccessToken: String,
            fcmSenderId: String,
            marketingCloudUrl: String,
            inboxEnabled: Boolean,
            analyticsEnabled: Boolean,
            isDebug: Boolean
        ) {
            val application = activity.application

            if (isDebug) {
                SFMCSdk.setLogging(LogLevel.DEBUG, LogListener.AndroidLogger())
                MarketingCloudSdk.setLogLevel(MCLogListener.VERBOSE)
                MarketingCloudSdk.setLogListener(AndroidLogListener())
                SFMCSdk.requestSdk {

                    // Specifically get the push state information
                    with(it.getSdkState()["PUSH"] as JSONObject) {

                        // General Troubleshooting
                        Log.i("~#SdkState", "initConfig: ${this["initConfig"]}")
                        Log.i("~#SdkState", "initStatus: ${this["initStatus"]}")
                        Log.i("~#SdkState", "PushMessageManager: ${(this["PushMessageManager"] as JSONObject).toString(2)}")
                        Log.i("~#SdkState", "RegistrationManager: ${(this["RegistrationManager"] as JSONObject).toString(2)}")

                        // Get Everything
                        Log.i("~#SdkState", "InApp Events: ${(this["Event"] as JSONObject).toString(2)}")
                    }
                }
            }

            SFMCSdk.configure(application, SFMCSdkModuleConfig.build {
                pushModuleConfig = MarketingCloudConfig.builder().apply {
                    setApplicationId(mcApplicationId)
                    setAccessToken(mcAccessToken)
                    setSenderId(fcmSenderId)
                    setMarketingCloudServerUrl(marketingCloudUrl)
                    setNotificationCustomizationOptions(
                        NotificationCustomizationOptions.create(notificationDrawable)
                    )
                    setAnalyticsEnabled(analyticsEnabled)
                    setLegacyEncryptionDependencyForciblyRemoved(true)
                    setInboxEnabled(inboxEnabled)
                    setDelayRegistrationUntilContactKeyIsSet(true)
                }.build(application)
            }) {
                initStatus ->
                when (initStatus.status) {
                    InitializationStatus.SUCCESS -> {
                        Log.v(LOG_TAG, "Marketing Cloud initialization successful.")
                        SFMCSdk.requestSdk { sdk ->
                            Log.v(LOG_TAG, sdk.getSdkState().toString(2)
                            )
                        }
                    }

                    InitializationStatus.FAILURE -> {
                        // Given that this app is used to show SDK functionality we will hard exit if SDK init outright failed.
                        Log.e(
                            LOG_TAG,
                            "Marketing Cloud initialization failed.  Exiting Learning App with exception."
                        )
                        //throw RuntimeException("Init failed")

                    }
                }
            }
        }

        @JvmStatic
        fun togglePushPermission(granted: Boolean)
        {
            try {
                Log.v(LOG_TAG, "Trying to toggle push permission in SFMC to ${granted}.")

                SFMCSdk.requestSdk { sfmcSdk ->
                    sfmcSdk.mp { push ->
                        Log.v(LOG_TAG, "Running checks in pushMessageManager and calling apply result.")

                        if(granted){
                            sfmcSdk.mp {
                                it.pushMessageManager.enablePush()
                                Log.v(LOG_TAG, "Push is enabled")
                            }
                        }else{
                            sfmcSdk.mp {
                                it.pushMessageManager.disablePush()
                                Log.v(LOG_TAG, "Push is not enabled")
                            }
                        }

                        Log.v(LOG_TAG, "pushMessageManager apply called.")
                    }
                }
            }
            catch (e: Exception) {
                // Exception handler
                Log.v(LOG_TAG, "Failed to set contact key in SFMC: ${e.message}")

            }
        }

        @JvmStatic
        fun setContactKey(contactKey: String) {
            try {
                SFMCSdk.requestSdk { sfmcSdk ->
                    sfmcSdk.identity.setProfileId(contactKey)
                }
                Log.v(LOG_TAG, "Finished setting contact key in SFMC.")
            }
            catch (e: Exception) {
                // Exception handler
                Log.v(LOG_TAG, "Failed to set contact key in SFMC: ${e.message}")
            }
        }

        @JvmStatic
        fun setProfileAttribute(key: String, value: String) {
            try {
                SFMCSdk.requestSdk { sfmcSdk ->
                    sfmcSdk.identity.setProfileAttribute(key,value)
                }
                Log.v(LOG_TAG, "Finished setting contact key in SFMC.")
            }
            catch (e: Exception) {
                // Exception handler
                Log.v(LOG_TAG, "Failed to set contact key in SFMC: ${e.message}")
            }
        }


        @JvmStatic
        fun getUnreadMessageCount(): Int {
            var unreadCount = 0
            SFMCSdk.requestSdk { sdk ->
                sdk.mp { push ->
                    unreadCount = push.inboxMessageManager.unreadMessageCount
                }
            }
            Log.v(LOG_TAG, "Unread message count: $unreadCount")
            return unreadCount
        }

        @JvmStatic
        fun markMessageRead(messageId: String) {
            SFMCSdk.requestSdk { sdk ->
                sdk.mp { push ->
                    val message = push.inboxMessageManager.messages.firstOrNull { it.id == messageId }
                    message?.let {
                        push.inboxMessageManager.setMessageRead(it)
                        Log.v(LOG_TAG, "Marked message $messageId as read.")
                    }
                }
            }
        }

        @JvmStatic
        fun deleteMessage(messageId: String) {
            SFMCSdk.requestSdk { sdk ->
                sdk.mp { push ->
                    val message = push.inboxMessageManager.messages.firstOrNull { it.id == messageId }
                    message?.let {
                        push.inboxMessageManager.deleteMessage(it)
                        Log.v(LOG_TAG, "Deleted message $messageId.")
                    }
                }
            }
        }
    }
}