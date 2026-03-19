# Understanding Attribution for Mobile Apps: Pre-installed and Play Store Downloads

In mobile app marketing, **attribution** is crucial to understanding how users discover your app. Whether the app is pre-loaded on a device or downloaded from the Play Store, attribution helps track the source of the user's engagement. This data is critical for evaluating marketing efforts, optimizing campaigns, and refining user acquisition strategies.

In this article, we will explore:

- What attribution is and why it matters
- Attribution for **pre-installed apps**
- Attribution for apps **downloaded from the Play Store** using the Install Referrer API
- A working **sample project** demonstrating the Install Referrer API

---

## What is Attribution?

Attribution is the process of identifying how a user first interacts with your app. It helps you understand whether the user came through an ad campaign, an organic search, or a pre-installation on the device.

Proper attribution allows developers and marketers to:

- Measure the effectiveness of their campaigns
- Optimize ad spend across channels
- Understand user acquisition funnels
- Make data-driven decisions about marketing strategy

---

## Two Types of Attribution

Attribution can be split into two main categories based on how the app reaches the user's device.

### 1. Attribution for Pre-installed Apps

Pre-installed apps come loaded on the device before the user purchases it. This is common in partnerships between app developers and device manufacturers (OEMs) or campaigns targeting specific device users.

#### Challenges with Pre-installed Apps

Tracking attribution for pre-installed apps is more complex because the app is already on the device when the user interacts with it:

- **No Play Store Interaction**: Since the app isn't downloaded via the Play Store, the traditional Install Referrer API won't work. The Play Store doesn't track the initial interaction for pre-installed apps.
- **Alternative Attribution Methods**: Attribution for pre-installed apps typically relies on SDKs provided by third-party attribution platforms such as **Adjust**, **AppsFlyer**, and **Kochava**. These platforms integrate with OEMs to track pre-installation data and record user engagement after device activation.

#### How Pre-install Campaigns Work

1. **OEM Partnerships** — App developers partner with OEMs to pre-load their apps on devices. The OEM provides attribution information such as device activation data or first app launch events.
2. **SDK Integration** — Developers integrate an attribution SDK into their app. When the user launches the pre-installed app, the SDK sends data to the attribution platform, which records the user's first interaction and ties it back to the pre-install campaign.
3. **Attribution Data** — The SDK provides information like the source of the pre-installation (e.g., device manufacturer, location, or bundle deal).

---

### 2. Attribution for Play Store Downloads: The Install Referrer API

For apps downloaded from the Play Store, Google provides a standard way to track attribution: the **Play Install Referrer API**. This API is widely used to capture the source of an app install, whether through an ad campaign or an organic download.

#### What the Install Referrer API Captures

```
Data                    | Description
----------------------- | --------------------------------------------------------
Referrer URL            | The URL containing tracking parameters for the app download
Click Timestamp         | When the user clicked the referral link
Install Begin Timestamp | When the install process started
Server Timestamps       | Server-side click and install timestamps for accuracy
Install Version         | The version of the app that was installed
Google Play Instant     | Whether the app was launched as a Google Play Instant experience
```

---

## Implementing the Install Referrer API

Let's walk through a complete implementation. You can find the full sample project on [GitHub — InstallReferrerDemo](https://github.com/RandhirGupta/InstallReferrerDemo).

### Step 1: Add the Dependency

Include the Install Referrer library in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.android.installreferrer:installreferrer:2.2")
}
```

### Step 2: Create a Data Model

Define a data class to hold the attribution information:

```kotlin
data class ReferrerInfo(
    val referrerUrl: String,
    val referrerClickTimestamp: Long,
    val installBeginTimestamp: Long,
    val referrerClickTimestampServer: Long,
    val installBeginTimestampServer: Long,
    val installVersion: String,
    val googlePlayInstantParam: Boolean
)
```

### Step 3: Build the Repository Layer

Wrap the Install Referrer API in a repository using Kotlin coroutines for clean async handling:

```kotlin
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
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Connection closed unexpectedly
            }
        })
    }
}
```

### Step 4: Create the ViewModel

Use a ViewModel to manage the UI state and trigger the referrer fetch:

```kotlin
class ReferrerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReferrerRepository(application)
    private val _uiState = MutableStateFlow<ReferrerUiState>(ReferrerUiState.Idle)
    val uiState: StateFlow<ReferrerUiState> = _uiState.asStateFlow()

    fun fetchReferrer() {
        _uiState.value = ReferrerUiState.Loading
        viewModelScope.launch {
            when (val result = repository.fetchReferrerInfo()) {
                is ReferrerResult.Success -> _uiState.value = ReferrerUiState.Success(result.info)
                is ReferrerResult.Error -> _uiState.value = ReferrerUiState.Error(result.message)
            }
        }
    }
}
```

### Step 5: Build the UI with Jetpack Compose

Display the attribution data in a clean, card-based layout:

```kotlin
@Composable
fun ReferrerInfoCard(info: ReferrerInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Attribution Data", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(label = "Referrer URL", value = info.referrerUrl.ifEmpty { "N/A" })
            InfoRow(label = "Click Timestamp", value = formatTimestamp(info.referrerClickTimestamp))
            InfoRow(label = "Install Timestamp", value = formatTimestamp(info.installBeginTimestamp))
            InfoRow(label = "Install Version", value = info.installVersion)
            InfoRow(label = "Google Play Instant", value = if (info.googlePlayInstantParam) "Yes" else "No")
        }
    }
}
```

---

## Testing the Install Referrer

To test the Install Referrer API locally, you can use the `adb` command to simulate a referrer broadcast:

```bash
adb shell am broadcast \
  -a com.android.vending.INSTALL_REFERRER \
  -n dev.anthropic.installreferrerdemo/.MainActivity \
  --es "referrer" "utm_source=test_source&utm_medium=test_medium&utm_campaign=test_campaign"
```

> **Note**: The broadcast method works with the older `BroadcastReceiver` approach. For the Install Referrer API v2+, testing is best done through the Play Store with a real install link or by using the Play Console's internal testing tracks.

---

## Key Differences: Pre-installed vs. Play Store Attribution

```
Aspect           | Pre-installed Apps                  | Play Store Downloads
---------------- | ----------------------------------- | ---------------------------
Tracking Method  | Third-party SDK (Adjust,            | Play Install Referrer API
                 |   AppsFlyer, Kochava)               |
Data Source      | OEM / device manufacturer           | Google Play Store
Trigger Event    | Device activation / first app launch| App download and install
Referrer URL     | Not available                       | Available via API
Complexity       | Higher — requires OEM partnerships  | Lower — standard API integration
Cost             | Typically involves OEM partnership   | Free to use
                 |   fees                              |
```

---

## Best Practices

1. **Fetch referrer data early** — Call the Install Referrer API as soon as the app launches for the first time. The data may not be available indefinitely.
2. **Cache the data** — Store the referrer information locally after the first fetch so you don't lose it if the connection drops.
3. **Handle errors gracefully** — Not all devices support the API. Always provide fallback messaging.
4. **Combine with analytics** — Send the attribution data to your analytics platform (Firebase, Mixpanel, etc.) for deeper funnel analysis.
5. **Respect user privacy** — Be transparent about data collection and comply with relevant privacy regulations.

---

## Conclusion

Attribution is a fundamental part of mobile app marketing. For **pre-installed apps**, it requires working with OEM partners and third-party SDKs. For **Play Store downloads**, Google's Install Referrer API provides a simple and secure way to capture campaign data.

The [InstallReferrerDemo](https://github.com/RandhirGupta/InstallReferrerDemo) sample project demonstrates a clean, production-ready implementation using Kotlin, Jetpack Compose, and the MVVM pattern. Clone it, try it out, and adapt it for your own app's attribution needs.

---

*If you found this article helpful, give it a clap and follow for more Android development content!*
