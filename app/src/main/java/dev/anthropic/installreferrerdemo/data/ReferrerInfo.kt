package dev.anthropic.installreferrerdemo.data

/**
 * Holds the attribution data retrieved from the Install Referrer API.
 */
data class ReferrerInfo(
    val referrerUrl: String,
    val referrerClickTimestamp: Long,
    val installBeginTimestamp: Long,
    val referrerClickTimestampServer: Long,
    val installBeginTimestampServer: Long,
    val installVersion: String,
    val googlePlayInstantParam: Boolean
)
