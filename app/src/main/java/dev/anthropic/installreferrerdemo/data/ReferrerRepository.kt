package dev.anthropic.installreferrerdemo.data

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class ReferrerResult {
    data class Success(val info: ReferrerInfo) : ReferrerResult()
    data class Error(val message: String) : ReferrerResult()
}

class ReferrerRepository(private val context: Context) {

    suspend fun fetchReferrerInfo(): ReferrerResult = suspendCoroutine { continuation ->
        val referrerClient = InstallReferrerClient.newBuilder(context).build()

        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        try {
                            val response = referrerClient.installReferrer
                            val info = ReferrerInfo(
                                referrerUrl = response.installReferrer,
                                referrerClickTimestamp = response.referrerClickTimestampSeconds,
                                installBeginTimestamp = response.installBeginTimestampSeconds,
                                referrerClickTimestampServer = response.referrerClickTimestampServerSeconds,
                                installBeginTimestampServer = response.installBeginTimestampServerSeconds,
                                installVersion = response.installVersion ?: "N/A",
                                googlePlayInstantParam = response.googlePlayInstantParam
                            )
                            continuation.resume(ReferrerResult.Success(info))
                        } catch (e: Exception) {
                            continuation.resume(
                                ReferrerResult.Error("Failed to read referrer data: ${e.message}")
                            )
                        } finally {
                            referrerClient.endConnection()
                        }
                    }

                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        continuation.resume(
                            ReferrerResult.Error("Install Referrer API is not supported on this device.")
                        )
                    }

                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        continuation.resume(
                            ReferrerResult.Error("Unable to connect to the Play Store service.")
                        )
                    }

                    else -> {
                        continuation.resume(
                            ReferrerResult.Error("Unknown response code: $responseCode")
                        )
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Connection closed unexpectedly; the client can retry if needed.
            }
        })
    }
}
