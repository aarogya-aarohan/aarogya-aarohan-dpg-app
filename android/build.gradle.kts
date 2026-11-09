import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration


// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.util.Properties

buildscript {
  dependencies {
    classpath(libs.kotlin.gradle.plugin)
    classpath(libs.coveralls.gradle.plugin)
    classpath(libs.gradle)
    classpath(libs.dokka.base)
  }
}

plugins {
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  alias(libs.plugins.kt3k.coveralls)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.dagger.hilt.android) apply false
  alias(libs.plugins.androidx.navigation.safeargs) apply false
  alias(libs.plugins.org.jetbrains.dokka)
  alias(libs.plugins.org.owasp.dependencycheck)
  alias(libs.plugins.com.diffplug.spotless) apply false
  alias(libs.plugins.android.junit5) apply false
  alias(libs.plugins.jetbrainsKotlinAndroid) apply false

}

tasks.dokkaHtmlMultiModule {
  moduleName.set("OpenSRP")
  moduleVersion.set(project.version.toString())
  outputDirectory.set(file(buildDir.resolve("dokka")))
  pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
    footerMessage = "(c) Ona Systems Inc"
  }
}

// The customised Android FHIR SDK libraries (org.smartregister:data-capture, engine, workflow, …)
// are not on a public repository: host them on a Maven repository you control and set its address
// and login in local.properties (see local.properties.example and docs/developer-setup-guide.md).
val sdkRepoProperties =
  Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) file.inputStream().use { load(it) }
  }

fun sdkRepoSetting(name: String): String? =
  sdkRepoProperties.getProperty(name)?.trim()?.takeIf { it.isNotEmpty() }
    ?: System.getenv(name)?.trim()?.takeIf { it.isNotEmpty() }

val sdkMavenUrl: String = sdkRepoSetting("SDK_MAVEN_URL") ?: "https://maven.example.org/android-fhir-sdk"
val sdkMavenUsername: String? = sdkRepoSetting("SDK_MAVEN_USERNAME")
val sdkMavenPassword: String? = sdkRepoSetting("SDK_MAVEN_PASSWORD")

allprojects {
  repositories {
    // The customised Android FHIR SDK libraries come only from SDK_MAVEN_URL (see above).
    exclusiveContent {
      forRepository {
        maven {
          name = "androidFhirSdk"
          url = uri(sdkMavenUrl)
          if (sdkMavenUsername != null || sdkMavenPassword != null) {
            credentials {
              username = sdkMavenUsername
              password = sdkMavenPassword
            }
          }
        }
      }
      filter {
        includeModuleByRegex(
          "org\\.smartregister",
          "common|contrib-barcode|contrib-locationwidget|data-capture|engine|knowledge|workflow",
        )
      }
    }
    gradlePluginPortal()
    mavenLocal()
    google()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://jcenter.bintray.com/")
    apply(plugin = "org.owasp.dependencycheck")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots")
    //maven(url = "https://repo.spring.io/plugins-release")
    maven(url = "https://central.sonatype.com/repository/maven-snapshots")
    maven(url = "https://repository.liferay.com/nexus/content/repositories/public")
    maven { url = uri("https://jitpack.io") }
    tasks.dependencyCheckAggregate{
      dependencyCheck.formats.add("XML")
    }
    configurations.all {
      resolutionStrategy.cacheChangingModulesFor(0, "seconds")
      resolutionStrategy.dependencySubstitution {
        substitute(module("org.smartregister:fhir-common-utils")).using(module("com.github.opensrp:fhir-common-utils:v1.0.3-SNAPSHOT"))
      }
    }
  }
}

subprojects {
  apply {
    plugin("com.diffplug.spotless")
    plugin("org.jetbrains.dokka")
    plugin(  "jacoco")
  }

  configure<SpotlessExtension> {

    kotlin {
      target("**/*.kt")
      ktlint(BuildConfigs.ktLintVersion)
      ktfmt().googleStyle()
      licenseHeaderFile("${project.rootProject.projectDir}/license-header.txt")
    }

    kotlinGradle {
      target("*.gradle.kts")
      ktlint(BuildConfigs.ktLintVersion)
      ktfmt().googleStyle()
    }

    format("xml") {
      target("**/*.xml")
      indentWithSpaces()
      trimTrailingWhitespace()
      endWithNewline()
    }

    format("json") {
      target("**/*.json")
      indentWithSpaces(2)
      trimTrailingWhitespace()
    }
  }

  configurations.all {
    resolutionStrategy {
      eachDependency {
        when (requested.group) {
          "org.jacoco" -> useVersion(BuildConfigs.jacocoVersion)
        }
      }
    }
    // guava already bundles ListenableFuture; the standalone artifact causes a
    // capability conflict on Android builds.
    exclude(group = "com.google.guava", module = "listenablefuture")
  }

  tasks.withType<Test> {
    configure<JacocoTaskExtension> {
      isIncludeNoLocationClasses = true
      excludes = listOf("jdk.internal.*", "**org.hl7*")
    }
  }
}
