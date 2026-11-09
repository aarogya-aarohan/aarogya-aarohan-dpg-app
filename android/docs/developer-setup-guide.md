# Developer setup guide: clone, run and build Aarogya Aarohan

This guide takes you from a computer with nothing installed to:

1. a **debug app** running on your phone or emulator, and
2. a **signed release APK** you can share and install.

You do not need prior Android experience. Follow the steps in order, and don't skip any. Each step ends with a check. If the check fails, fix it before moving on; [Troubleshooting](#troubleshooting) lists the common errors.

> **Time needed:** about 1–2 hours the first time. Most of that is downloads: Android Studio, the Android SDK, and about 3–5 GB of build libraries on the first build.

---

## Contents

- [What you need](#what-you-need)
- [Step 1: Install Git](#step-1-install-git)
- [Step 2: Install Java 17](#step-2-install-java-17)
- [Step 3: Install Android Studio and the Android SDK](#step-3-install-android-studio-and-the-android-sdk)
- [Step 4: Download the code](#step-4-download-the-code)
- [Step 5: Create `local.properties`](#step-5-create-localproperties)
- [Step 6: Build the debug APK](#step-6-build-the-debug-apk)
- [Step 7: Run the app on a phone or emulator](#step-7-run-the-app-on-a-phone-or-emulator)
- [Step 8: Build a signed release APK](#step-8-build-a-signed-release-apk)
- [Point the app at your own server](#point-the-app-at-your-own-server)
- [Optional: Work in Android Studio](#optional-work-in-android-studio)
- [Troubleshooting](#troubleshooting)
- [Command cheat sheet](#command-cheat-sheet)
- [Files you must never commit](#files-you-must-never-commit)

---

## What you need

| Item | Details |
|---|---|
| Computer | macOS, Windows 10/11 or Linux, 64-bit |
| Memory (RAM) | **16 GB recommended.** The build reserves 8 GB for Gradle. 8 GB machines may fail with "out of memory". |
| Free disk space | At least **20 GB** (Android Studio, SDK, emulator image, build caches) |
| Internet | Needed for the first build. Later builds work mostly from cache. |
| FHIR SDK library repository | A Maven repository hosting the customised Android FHIR SDK libraries, and its login if it needs one ([Step 5.2](#52-fhir-sdk-library-repository-required)) |
| Android phone (optional) | Android **10 or newer**, with a camera. An emulator works for most screens, but the screening camera works best on a real phone. |

Tools you will install:

| Tool | Version | Why |
|---|---|---|
| Git | any recent | Downloads the code |
| Java (JDK) | **17** | Runs the build. **Do not use Java 22 or newer**: this project's Gradle 8.5 cannot run on them. |
| Android Studio | any recent | Provides the Android SDK, emulator and an editor |
| Android SDK Platform | **API 35** | The app compiles against Android 15 (API 35) |
| Android SDK Build-Tools & Platform-Tools | latest | Packaging, signing, and `adb` for installing on a phone |

You do **not** need to install Gradle. The project includes a "Gradle wrapper" (`gradlew`) that downloads the right version (8.5) automatically.

> **About commands in this guide.** Lines in grey boxes are typed into a **terminal**:
> - macOS: open **Terminal** (Spotlight → "Terminal").
> - Windows: open **PowerShell** (Start menu → "PowerShell").
> - Linux: open your terminal app.
>
> When a step differs per system, it shows separate macOS/Linux and Windows versions. On Windows, type `.\gradlew.bat` wherever this guide says `./gradlew`.

---

## Step 1: Install Git

- **macOS:** run `git --version` in Terminal. If Git is missing, macOS offers to install the "Command Line Developer Tools". Click **Install**.
- **Windows:** download and run the installer from <https://git-scm.com/download/win>. The default options are fine.
- **Linux (Ubuntu/Debian):** `sudo apt install git`

**Check:**

```bash
git --version
```

You should see something like `git version 2.4x.x`.

---

## Step 2: Install Java 17

Install **Eclipse Temurin JDK 17** (free):

1. Go to <https://adoptium.net/temurin/releases/?version=17>.
2. Pick your operating system and architecture:
   - Apple Silicon Mac (M1/M2/M3/M4): **aarch64**
   - Intel Mac and most Windows/Linux PCs: **x64**
3. Download the **JDK** installer (`.pkg` on macOS, `.msi` on Windows) and run it.
   - On Windows, turn on **"Set JAVA_HOME variable"** in the installer.

Then point your terminal at Java 17:

- **macOS:** add this line to the file `~/.zshrc`, then open a **new** Terminal window:

  ```bash
  export JAVA_HOME=$(/usr/libexec/java_home -v 17)
  ```

  One way to add it: `echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc`

- **Windows:** the installer set `JAVA_HOME` if you ticked the option. Open a **new** PowerShell window.
- **Linux:** `sudo apt install openjdk-17-jdk`, then add `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64` to `~/.bashrc`.

**Check:**

```bash
java -version
```

The first line must contain **`17`**, for example `openjdk version "17.0.x"`. If it shows another number, see [Wrong Java version](#wrong-java-version).

> Java 21 also works with this project. Java 22 and newer **do not**. Android Studio's bundled Java is often newer than 21, which is why this guide installs 17 separately.

---

## Step 3: Install Android Studio and the Android SDK

1. Download Android Studio from <https://developer.android.com/studio> and install it.
2. Open it and follow the **Setup Wizard**. Choose **Standard** setup. It downloads the Android SDK, which takes a while.
3. When it finishes, open the SDK Manager:
   - On the Welcome screen: **More Actions → SDK Manager**
   - Or inside a project: **Tools → SDK Manager**
4. In the **SDK Platforms** tab, tick **Android 15.0 ("VanillaIceCream") — API Level 35**.
5. In the **SDK Tools** tab, make sure these are ticked:
   - **Android SDK Build-Tools**
   - **Android SDK Platform-Tools**
   - **Android Emulator** (only if you will use an emulator)
6. Click **Apply** and wait for the downloads.
7. Copy the **Android SDK Location** shown at the top of the SDK Manager window. You need it in [Step 5](#step-5-create-localproperties). It is usually:

| System | Typical SDK location |
|---|---|
| macOS | `/Users/<your-name>/Library/Android/sdk` |
| Windows | `C:\Users\<your-name>\AppData\Local\Android\Sdk` |
| Linux | `/home/<your-name>/Android/Sdk` |

**Check:** the folder exists and contains `platforms/android-35`.

- macOS/Linux: `ls ~/Library/Android/sdk/platforms` (on Linux, use `ls ~/Android/Sdk/platforms`)
- Windows: `dir $env:LOCALAPPDATA\Android\Sdk\platforms`

You should see `android-35` in the list.

---

## Step 4: Download the code

Choose a folder for your projects, then clone:

```bash
cd ~/Documents
git clone https://github.com/aarogya-aarohan/aarogya-aarohan-dpg-app.git
cd aarogya-aarohan-dpg-app/android
```

On Windows, use `cd $HOME\Documents` for the first line.

> **Important:** the Android project is the **`android`** folder inside the repository, not the repository root. Every command from now on runs from inside `aarogya-aarohan-dpg-app/android`, and that is the folder you open in Android Studio.

**Check:**

```bash
ls
```

You should see `gradlew`, `settings.gradle.kts`, `quest`, `engine` and `geowidget` (on Windows, `dir`).

---

## Step 5: Create `local.properties`

`local.properties` holds settings for **your machine only**: where your Android SDK is, where the build downloads the customised FHIR SDK libraries from, and which servers the app talks to. It is ignored by git, so it is never committed. The repository has a template, `local.properties.example`, that lists every setting with an explanation.

From the `android` folder, copy the template:

- macOS / Linux: `cp local.properties.example local.properties`
- Windows: `copy local.properties.example local.properties`

Then open `local.properties` in a plain-text editor (TextEdit in plain-text mode, Notepad, or VS Code) and fill it in.

### 5.1 Android SDK location (required)

Do **one** of these:

- Open the `android` folder in Android Studio once ([Optional: Work in Android Studio](#optional-work-in-android-studio)). Android Studio adds the `sdk.dir` line for you.
- Or remove the `#` in front of `sdk.dir=` and add the SDK location from Step 3, for example `sdk.dir=/Users/your-name/Library/Android/sdk`. On Windows, double the backslashes and escape the colon: `sdk.dir=C\:\\Users\\your-name\\AppData\\Local\\Android\\Sdk`.
- Or leave `sdk.dir` out and set the `ANDROID_HOME` environment variable to the SDK location instead.

### 5.2 FHIR SDK library repository (required)

The app uses customised builds of Google's [Android FHIR SDK](https://github.com/google/android-fhir): `org.smartregister:data-capture`, `engine`, `workflow`, `knowledge`, `common`, `contrib-barcode` and `contrib-locationwidget`. They are not on Maven Central, so they must be published to a Maven repository that you control (for example GitHub Packages, a Nexus or Artifactory server, or any static HTTPS folder in Maven layout). The exact versions are listed in `gradle/libs.versions.toml`.

| Setting | Set it to |
|---|---|
| `SDK_MAVEN_URL` | The repository address, for example `https://maven.pkg.github.com/<owner>/<repo>` |
| `SDK_MAVEN_USERNAME` | The login user name, or empty if the repository needs no login |
| `SDK_MAVEN_PASSWORD` | The login password or token, or empty if the repository needs no login |

For **GitHub Packages**, the user name is your GitHub **username** (not your email), and the password is a personal access token (classic) that has **only** the `read:packages` permission. Create one at <https://github.com/settings/tokens>. GitHub requires a login even for public packages.

These three settings can also be given as environment variables with the same names, which is useful on build servers.

### 5.3 Your servers (needed to use the app, not to build it)

`FHIR_BASE_URL`, `URL`, `OAUTH_BASE_URL`, `OAUTH_CLIENT_ID`, `OAUTH_SCOPE`, `SITE_LIST_URL`, `FHIR_CANONICAL_BASE_URL` and `OPENSRP_APP_ID` tell the app where your backend is. When they are empty, the build uses `example.org` placeholders: the app builds and opens, but has no sites to show and nothing to log in to. [Point the app at your own server](#point-the-app-at-your-own-server) explains each one.

### 5.4 Optional services

`MAPBOX_SDK_TOKEN`, `POSTHOG_API_KEY` and `POSTHOG_HOST` switch on maps and analytics. Leave them empty to keep those services off.

Rules that trip people up:

1. **No quotes** around URLs, user names, client IDs, scopes or keys. **The one exception** is `OPENSRP_APP_ID`, which **must** have quotes, for example `OPENSRP_APP_ID="app"`.
2. Keep the file directly inside `android/`, next to `gradlew`, not inside `quest/`.
3. **Never commit `local.properties`.** It is ignored by git; keep it that way.

**Check:** `cat local.properties` (Windows: `type local.properties`) shows your values, and `SDK_MAVEN_URL` is not empty.

---

## Step 6: Build the debug APK

A **debug APK** is for testing on your own devices. It is signed with an automatic development key, so you need no keystore.

From the `android` folder:

**macOS / Linux:**

```bash
./gradlew :quest:assembleOpensrpDebug
```

**Windows:**

```powershell
.\gradlew.bat :quest:assembleOpensrpDebug
```

On macOS/Linux, if you see `permission denied: ./gradlew`, run `chmod +x gradlew` once and try again.

What to expect:

- The **first** run downloads Gradle 8.5 and about **4 GB** of libraries. That takes **15–40 minutes**, depending on your internet speed and computer. Later builds take a few minutes.
- You will see many `warning:` lines. They are normal.
- It ends with **`BUILD SUCCESSFUL`**.

**Check:** the APK exists at:

```
quest/build/outputs/apk/opensrp/debug/quest-opensrp-debug.apk
```

The file is large, about **550 MB**. That is expected: it bundles image-processing and AI libraries for four phone CPU types.

> The project has one "flavor", called **`opensrp`**. That is why every task name contains `Opensrp` (`assembleOpensrpDebug`, `assembleOpensrpRelease`). The installed app is called **Aarogya Aarohan**, with package id `in.ac.iisc.arogyam`.

---

## Step 7: Run the app on a phone or emulator

### Option A: A real Android phone (recommended)

1. On the phone, open **Settings → About phone** and tap **Build number** seven times. This turns on **Developer options**.
2. Open **Settings → System → Developer options** and turn on **USB debugging**.
3. Connect the phone with a USB cable. On the phone, tap **Allow** on the "Allow USB debugging?" prompt.
4. Check that your computer sees the phone:

   ```bash
   adb devices
   ```

   You should see one line ending in `device`. If `adb` is "not found", see [`adb: command not found`](#adb-command-not-found).

5. Build and install in one command:

   ```bash
   ./gradlew :quest:installOpensrpDebug
   ```

   Or install the APK you already built:

   ```bash
   adb install -r quest/build/outputs/apk/opensrp/debug/quest-opensrp-debug.apk
   ```

6. Open **Aarogya Aarohan** from the phone's app drawer.

### Option B: An emulator

1. In Android Studio, open **Tools → Device Manager → Create Virtual Device** (the **+** button).
2. Pick a phone (for example **Pixel 7**), then pick a system image with **API 34 or 35**. Click **Download** next to it if needed, then **Finish**.
3. Start the emulator with the ▶ button.
4. Once it has booted, run `./gradlew :quest:installOpensrpDebug` from the terminal.

The emulator's camera is simulated, so photo capture and AI screening work best on a real phone.

### After installing: first launch

1. The app shows **"Select your site"**. This list is downloaded from `SITE_LIST_URL`, so the phone needs a connection. With the placeholder value, the list is empty.
2. Choose a site. The app then shows the **login** screen for that site's server.
3. Log in with an account on that server. **You need credentials from the server's administrator.** This guide cannot provide them.

To set up **your own** servers and site list, see [Point the app at your own server](#point-the-app-at-your-own-server).

> Debug builds log network traffic and store the local database **unencrypted**. Never use a debug build with real patient data.

---

## Step 8: Build a signed release APK

A **release APK** is the version you give to users. It must be signed with **your own private key** (a "keystore"). Android only installs an update when it is signed with the same key as the installed app, so **if you lose the keystore or its password, you can never update that app again.** Back it up somewhere safe, outside the project folder.

### 8.1 Create your keystore (once)

From the `android` folder, run the line for your system. `keytool` comes with Java 17.

macOS / Linux:

```bash
keytool -genkeypair -v -keystore quest/opensrp.jks -alias release -keyalg RSA -keysize 2048 -validity 10000
```

Windows:

```powershell
keytool -genkeypair -v -keystore quest\opensrp.jks -alias release -keyalg RSA -keysize 2048 -validity 10000
```

It asks for:

- a **keystore password**: choose a strong one and write it down;
- your name, organisation and country (any reasonable values);
- a **key password**: you can press Enter to reuse the keystore password.

The build always looks for the keystore at **`quest/opensrp.jks`**, so keep that exact name and location. Git is already set to ignore `*.jks` files.

### 8.2 Fill in `keystore.properties`

`android/keystore.properties` is in the repository with empty values (debug builds don't use them). Fill in your real values:

```properties
KEYSTORE_ALIAS=release
KEY_PASSWORD=the-key-password-you-chose
KEYSTORE_PASSWORD=the-keystore-password-you-chose
```

If you pressed Enter for the key password in 8.1, `KEY_PASSWORD` is the same as `KEYSTORE_PASSWORD`.

On build servers, you can instead set the environment variables `KEYSTORE_ALIAS`, `KEY_PASSWORD` and `KEYSTORE_PASSWORD`. They override the file.

### 8.3 AI screening (optional)

The app is built **without** an AI model by default, and the release build does not need one. To include an AI screening model, follow the [AI model integration guide](ai-model-integration-guide.md).

### 8.4 Build

macOS / Linux:

```bash
./gradlew :quest:assembleOpensrpRelease
```

Windows:

```powershell
.\gradlew.bat :quest:assembleOpensrpRelease
```

**Check:** the signed APK is at:

```
quest/build/outputs/apk/opensrp/release/quest-opensrp-release.apk
```

### 8.5 Verify the signature (recommended)

`apksigner` is in your SDK's `build-tools` folder. Replace `35.0.0` with whichever version you have; `ls ~/Library/Android/sdk/build-tools` lists them.

macOS:

```bash
~/Library/Android/sdk/build-tools/35.0.0/apksigner verify --print-certs quest/build/outputs/apk/opensrp/release/quest-opensrp-release.apk
```

Windows:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\35.0.0\apksigner.bat" verify --print-certs quest\build\outputs\apk\opensrp\release\quest-opensrp-release.apk
```

You should see `Signer #1 certificate DN: CN=...`, showing the details you entered in 8.1.

### 8.6 Install the release APK

```bash
adb install -r quest/build/outputs/apk/opensrp/release/quest-opensrp-release.apk
```

If the phone already has a **debug** build, the signatures differ and the install fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Uninstall the old one first with `adb uninstall in.ac.iisc.arogyam`. **This deletes the app's data on that phone, including cases that have not been synced.**

### Optional: Build an App Bundle for Google Play

The Play Store needs an `.aab` instead of an `.apk`:

```bash
./gradlew :quest:bundleOpensrpRelease
```

The output is `quest/build/outputs/bundle/opensrpRelease/quest-opensrp-release.aab`.

### Before you ship a release

- **Version:** each new release needs a higher `versionCode`. Both `versionCode` and `versionName` are in `buildSrc/src/main/kotlin/BuildConfigs.kt`.
- **Server values:** a release build uses the values in `local.properties`. Make sure they point to your production servers, not the `example.org` placeholders: especially `SITE_LIST_URL` and `FHIR_CANONICAL_BASE_URL` ([Point the app at your own server](#point-the-app-at-your-own-server)).
- **Test** the release APK on a real phone: site list, login, sync, a full screening with photos, submit, and sync again.

---

## Point the app at your own server

The app has no built-in server. To log in, sync and screen, you need your own backend, and must tell the app where it is. All the settings below go in `android/local.properties` ([Step 5](#step-5-create-localproperties)). They are read **at build time**, so **rebuild and reinstall** the app after changing any of them.

### 1. What you need

| Service | What it does |
|---|---|
| [HAPI FHIR](https://hapifhir.io/) server | Stores all data as FHIR resources, plus the app's configuration (Composition, Questionnaires, StructureMaps, feature flags) |
| [Keycloak](https://www.keycloak.org/) | User accounts and login (OAuth 2.0 / OpenID Connect) |
| A web host for one JSON file | Serves the site list. Any HTTPS static hosting works, such as an object-storage bucket or GitHub Pages |

Loading the app's configuration onto the FHIR server is covered in the [OpenSRP FHIR Core documentation](https://docs.smartregister.org/), which this app is built on.

### 2. Publish a site list

On first launch, the app downloads a list of servers and sites from `SITE_LIST_URL` and asks the user to pick one. Choosing a site sets the FHIR and login servers for that phone.

Host a JSON file like this one and set `SITE_LIST_URL` to its address:

```json
{
  "my-org": {
    "name": "My organisation",
    "multi_tenant": false,
    "tenants": [
      {
        "code": "district-a",
        "name": "District A",
        "fhirBaseUrl": "https://fhir.example.org/fhir/",
        "authBaseUrl": "https://auth.example.org/realms/example/"
      }
    ]
  }
}
```

| Field | Meaning |
|---|---|
| top-level key (`my-org`) | An id for the server |
| `name` | Server name |
| `multi_tenant` | `true` if one FHIR server holds several sites (tenants) side by side. The app then reads that tenant's feature flags from the `Basic` resource with id `<code>-feature-flags` instead of `feature-flags` |
| `tenants[].code` | Short id of the site. Also used in the feature-flags id when `multi_tenant` is `true` |
| `tenants[].name` | Site name shown in the list |
| `tenants[].fhirBaseUrl` | The site's FHIR server address, ending in `/` |
| `tenants[].authBaseUrl` | The site's Keycloak realm address, ending in `/` |

Add as many servers and tenants as you need. The file is fetched again later to check the phone's saved site, so keep the URLs of sites in use up to date.

### 3. Set the server settings

| Setting | Set it to |
|---|---|
| `FHIR_BASE_URL` | Your default FHIR server, ending in `/`. The site list overrides it once the user picks a site |
| `URL` | The same value as `FHIR_BASE_URL` |
| `OAUTH_BASE_URL` | Your default Keycloak realm, ending in `/` (for example `https://auth.example.org/realms/my-realm/`) |
| `OAUTH_CLIENT_ID` | The Keycloak client the app logs in with. It must be a **public** client with the "Direct access grants" (password) flow enabled |
| `OAUTH_SCOPE` | Usually `openid` |
| `OPENSRP_APP_ID` | The id of the app configuration on the FHIR server, **in double quotes**, for example `"app"` |
| `SITE_LIST_URL` | The address of your site list file ([section 2](#2-publish-a-site-list)) |
| `FHIR_CANONICAL_BASE_URL` | The base of the app's own FHIR URLs ([section 4](#4-choose-your-fhir-canonical-base-url)) |

### 4. Choose your FHIR canonical base URL

The app reads and writes some FHIR extensions, code systems and identifiers of its own. They are not standard HL7 ones, so each needs a URL that you own. The app builds them all from `FHIR_CANONICAL_BASE_URL`:

| Built URL | What it is |
|---|---|
| `<base>/fhir/StructureDefinition/ai-inference-enabled` | Feature flag: switch AI screening on for a tenant |
| `<base>/fhir/StructureDefinition/ai-model-id` | Feature flag: which AI model to use |
| `<base>/fhir/StructureDefinition/app-update` | Feature flag: app-update prompts (minimum and latest version) |
| `<base>/fhir/StructureDefinition/last-sync-date`, `device-id`, `flw-id`, `pending-images` | Sync status the app reports for each user |
| `<base>/fhir/StructureDefinition/img-upload-error`, `img-upload-failed-permanently`, `time-taken-to-upload-img` | Photo upload status |
| `<base>/CodeSystem/ai-result-value`, `ai-result-confidence`, `case-level-ai-result` | AI screening results |
| `<base>/CodeSystem/refer-case` | Marks a case for referral |
| `<base>/fhir/identifier/patient-id` | The identifier system of the patient id |

Pick a base that your organisation controls, for example `https://fhir.your-org.org`, with no trailing slash. It only names things; the app never opens it.

> **Important:** your server content must use **exactly** the same URLs. That means the feature-flags `Basic` resource, and every StructureMap and Questionnaire that reads or writes these extensions. If they don't match, the app silently ignores the values: for example, AI never switches on. The sample StructureMaps in `quest/src/main/assets/configs/app/structuremap/` use the default base, `https://fhir.example.org`; change it there too if you start from them.

### 5. Rebuild and check

1. Rebuild and install: `./gradlew :quest:installOpensrpDebug`
2. Open the app. Your sites should appear in **"Select your site"**.
3. Choose one and log in with a Keycloak user of that realm.
4. Wait for the first sync to finish, then register a test case.

---

## Optional: Work in Android Studio

Everything above works from the terminal. To browse and edit the code in Android Studio:

1. **File → Open** and select the **`android`** folder. Do not select the repository root.
2. **Set the Gradle JDK to 17.** This step matters, because Android Studio's built-in Java is too new for this project:
   - macOS: **Android Studio → Settings → Build, Execution, Deployment → Build Tools → Gradle**
   - Windows/Linux: **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**
   - Set **Gradle JDK** to the Temurin 17 you installed. If it's not listed, choose **Add JDK…** and select its folder.
3. Click **Sync Project with Gradle Files** (the elephant icon in the toolbar). The first sync is slow.
4. In **Build → Select Build Variant**, set `quest` to **`opensrpDebug`**.
5. Choose your phone or emulator in the device dropdown, then press the green ▶ **Run** button.

To build a release APK from the menu instead, use **Build → Generate Signed App Bundle / APK…**, choose **APK**, point it at `quest/opensrp.jks`, and pick the **opensrpRelease** variant. The Gradle command in Step 8 does the same thing with less clicking.

---

## Troubleshooting

Find your error message below. If it isn't here, scroll up in the build output to the **first** line containing `What went wrong:`. That line usually names the real problem.

### `SDK location not found` / `The SDK directory '...' does not exist`

The build can't find the Android SDK. Fix `sdk.dir` in `local.properties`, or set `ANDROID_HOME` ([Step 5.1](#51-android-sdk-location-required)). Copy the exact path from **SDK Manager → Android SDK Location**. On Windows, remember the `C\:\\...` escaping.

### `Could not resolve org.smartregister:...` mentioning `maven.example.org`

`SDK_MAVEN_URL` is not set, so the build used the placeholder address. Set it in `local.properties` ([Step 5.2](#52-fhir-sdk-library-repository-required)).

### `Could not find org.smartregister:...` (searched in your repository)

Your repository doesn't have that library at that exact version. Compare the versions in `gradle/libs.versions.toml` with what is published, then run the build again with `--refresh-dependencies`.

### `Received status code 401 from server: Unauthorized` or `403 Forbidden`

The login for `SDK_MAVEN_URL` is missing, wrong or expired. Check `SDK_MAVEN_USERNAME` and `SDK_MAVEN_PASSWORD` in `local.properties`. For GitHub Packages, the user name must be your GitHub **username**, not your email, and the token needs the `read:packages` permission.

### Wrong Java version

Typical messages:

- The build fails within seconds, and `What went wrong:` is followed by **nothing but a version number**, such as `25.0.3`. That is Gradle 8.5's (unhelpful) way of saying "this Java is too new". It is what you get with Android Studio's bundled Java.
- `Unsupported class file major version 66` (or any number above 65)
- `Gradle requires JVM 17 or later`

To fix it:

1. Run `java -version` in the same terminal. It must say 17 (21 also works).
2. On macOS, check that `echo $JAVA_HOME` prints a path containing `17`. If not, redo the `~/.zshrc` line in [Step 2](#step-2-install-java-17) and open a **new** Terminal window.
3. In Android Studio, set the **Gradle JDK** to 17 (see [Optional: Work in Android Studio](#optional-work-in-android-studio)).
4. Then run `./gradlew --stop` and build again.

### `OutOfMemoryError: Java heap space` or `Gradle build daemon disappeared unexpectedly`

The build needs about 8 GB of free memory. Close other apps (browsers, emulators) and run `./gradlew --stop`, then build again. On a machine with 8 GB of RAM, lower `org.gradle.jvmargs=-Xmx8192m` to `-Xmx4096m` in `android/gradle.properties`. **Don't commit that change.**

### `error: cannot find symbol` pointing at `OPENSRP_APP_ID` in `BuildConfig.java`

The quotes are missing. The line must be exactly `OPENSRP_APP_ID="app"`.

### `Keystore file '.../quest/opensrp.jks' not found for signing config 'release'`

The keystore must be at `android/quest/opensrp.jks`. See [8.1](#81-create-your-keystore-once).

### `Keystore was tampered with, or password was incorrect` / `Cannot recover key`

A password in `keystore.properties` doesn't match the keystore, or `KEYSTORE_ALIAS` is wrong. To list the aliases inside your keystore, run `keytool -list -keystore quest/opensrp.jks`.

### `INSTALL_FAILED_UPDATE_INCOMPATIBLE`

A copy of the app signed with a different key is already installed. See [8.6](#86-install-the-release-apk).

### `adb: command not found`

`adb` is in the SDK's `platform-tools` folder. Either run it with its full path (`~/Library/Android/sdk/platform-tools/adb devices` on macOS, `& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices` on Windows), or add that folder to your `PATH`.

### `adb devices` shows `unauthorized` or nothing at all

Unplug the cable and plug it back in, then accept the "Allow USB debugging?" prompt on the phone. Try a different cable: some cables only charge. On Windows, you may need your phone maker's USB driver.

### The app opens, but the site list is empty or shows an error

The site list is downloaded from `SITE_LIST_URL`. Check that it points to your site list file (the placeholder value has no sites), that the file is valid JSON in the [expected format](#2-publish-a-site-list), and that the phone is online. You need to rebuild after changing `local.properties`.

### Anything else: a clean retry

```bash
./gradlew --stop
./gradlew clean
./gradlew :quest:assembleOpensrpDebug --stacktrace
```

`--stacktrace` prints more detail. Include that output if you ask the maintainers for help, but **remove any passwords, tokens or keys from anything you paste**.

---

## Command cheat sheet

Run these from the `android` folder. On Windows, use `.\gradlew.bat` instead of `./gradlew`.

| Goal | Command |
|---|---|
| Build debug APK | `./gradlew :quest:assembleOpensrpDebug` |
| Build and install debug on a connected phone | `./gradlew :quest:installOpensrpDebug` |
| Build signed release APK | `./gradlew :quest:assembleOpensrpRelease` |
| Build Play Store bundle | `./gradlew :quest:bundleOpensrpRelease` |
| Run app unit tests | `./gradlew :quest:testOpensrpDebugUnitTest` |
| Run engine unit tests | `./gradlew :engine:testDebugUnitTest` |
| Check code formatting | `./gradlew spotlessCheck` |
| Stop background Gradle processes | `./gradlew --stop` |
| Delete build outputs | `./gradlew clean` |
| List connected devices | `adb devices` |
| Uninstall the app (deletes its data) | `adb uninstall in.ac.iisc.arogyam` |

Output locations:

| Build | File |
|---|---|
| Debug APK | `quest/build/outputs/apk/opensrp/debug/quest-opensrp-debug.apk` |
| Release APK | `quest/build/outputs/apk/opensrp/release/quest-opensrp-release.apk` |
| Release bundle | `quest/build/outputs/bundle/opensrpRelease/quest-opensrp-release.aab` |

---

## Files you must never commit

These files contain secrets, or content we are not allowed to publish. Always check `git status` before committing.

`local.properties` is already ignored by git. `keystore.properties` is tracked with empty values (the build works without real values). Once you fill in real passwords, tell git to stop showing your local edits:

```bash
git update-index --skip-worktree keystore.properties
```

| File | Why |
|---|---|
| `android/local.properties` | Your repository login, server addresses, service keys and machine paths. Share `local.properties.example` instead |
| `android/keystore.properties` | Your keystore passwords |
| `android/quest/opensrp.jks` (or any `*.jks`) | Your release signing key. Anyone who has it can publish fake updates of your app. |
| AI model weights (`*.pt`, `*.onnx`, …) | Not licensed for publication. See the [AI model integration guide](ai-model-integration-guide.md#where-the-model-file-goes). |
| APK / AAB files | Build outputs; share them through your release channel, not git |

---

*Last verified 2026-10-22 on macOS (Apple Silicon) with Temurin JDK 17, using a fresh clone and empty build caches: the debug build, the release build, and the `apksigner` check all passed. The Windows and Linux commands follow standard tooling, but have not been run end to end.*
