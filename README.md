# Aarogya Aarohan

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE.txt)

Aarogya Aarohan is an offline-first Android app for community health workers. They use it to
register people, run oral cancer screening (a questionnaire and photos), and sync the records to a
FHIR server. All data is stored and exchanged as standard HL7 **FHIR** resources.

Optionally, an AI screening model can classify each photo on the phone. The app ships **without**
a model: you add your own as a plugin, and switch it on per deployment.

## Getting started

To clone, build and run the app, follow the
**[Developer setup guide](android/docs/developer-setup-guide.md)**. It starts from a computer with
nothing installed and ends with the app running on your phone or emulator, and a signed release
APK. No prior Android experience is needed.

In short, you will:

1. Install Git, Java 17 and Android Studio (with Android SDK API 35)
2. Clone this repository
3. Copy `android/local.properties.example` to `android/local.properties` and fill it in: your
   Android SDK location, the Maven repository hosting the customised Android FHIR SDK libraries,
   and your servers
4. Build and install from `android/`: `./gradlew :quest:installOpensrpDebug`

To add AI screening after that, see the
[AI model integration guide](android/docs/ai-model-integration-guide.md).

## Features

- Works offline; syncs with a [HAPI FHIR](https://hapifhir.io/) server when a connection is available
- Login and access control through [Keycloak](https://www.keycloak.org/)
- Dynamic forms using FHIR [Structured Data Capture](https://hl7.org/fhir/uv/sdc/) (SDC)
- In-app camera for screening photos
- Optional on-device AI screening through a plug-in model contract (`:ai-api`)
- Local data encrypted at rest; data sent over TLS
- User interface in English and Hindi, with partial Bengali, Marathi, Tamil, Telugu and Urdu
  translations

## Documentation

| Guide | For |
| :-- | :-- |
| [Developer setup guide](android/docs/developer-setup-guide.md) | Cloning, building and running the app, and making a signed release APK |
| [AI model integration guide](android/docs/ai-model-integration-guide.md) | Adding your own AI screening model as a plugin and switching it on for a deployment |
| [AI plugins](android/ai-plugins/README.md) | The plugin folder and the sample plugin |

## Repository layout

```text
android/
├── quest/        the Android app
├── engine/       shared FHIR, sync and configuration logic
├── geowidget/    map components
├── ai-api/       the AI model contract that plugins implement
├── ai-api-testing/  ready-made contract tests for plugins
├── ai-plugins/   AI model plugins (includes a demo plugin)
└── docs/         the guides above
```

## Requirements

- Android 10 (API 29) or newer
- To log in and sync: your own HAPI FHIR server, Keycloak server and site list. See
  [Point the app at your own server](android/docs/developer-setup-guide.md#point-the-app-at-your-own-server)
  for every URL you need to set

## Built on OpenSRP FHIR Core

This app is built on [OpenSRP FHIR Core](https://github.com/opensrp/fhircore) by
[Ona](https://ona.io/), which is powered by Google's
[Android FHIR SDK](https://github.com/google/android-fhir). Files that come from FHIR Core keep
their original copyright headers.

## License

[Apache License 2.0](LICENSE.txt)
