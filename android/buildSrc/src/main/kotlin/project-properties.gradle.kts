import org.gradle.kotlin.dsl.extra
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.util.Properties

fun readProperties(file: String): Properties {
  val properties = Properties()
  val localProperties = File(file)
  if (localProperties.isFile) {
    InputStreamReader(FileInputStream(localProperties), Charsets.UTF_8).use { reader
      ->
      properties.load(reader)
    }
  }
  else  throw FileNotFoundException("\u001B[34mFile $file not found\u001B[0m")

  return properties
}

// Deployment properties, read from local.properties. The defaults are placeholders that let the
// project build; set real values for your deployment (see docs/developer-setup-guide.md).
val requiredFhirProperties =
  listOf(
    "URL",
    "FHIR_BASE_URL",
    "OAUTH_BASE_URL",
    "OAUTH_CLIENT_ID",
    "OAUTH_SCOPE",
    "MAPBOX_SDK_TOKEN",
    "OPENSRP_APP_ID",
    "POSTHOG_API_KEY",
    "POSTHOG_HOST",
    // Base of this app's own FHIR canonical URLs (extensions, code systems, identifier systems).
    "FHIR_CANONICAL_BASE_URL",
    // JSON list of servers and sites shown on the "Select your site" screen.
    "SITE_LIST_URL",
  )

val placeholderDefaults =
  mapOf(
    "URL" to "https://fhir.example.org/fhir/",
    "FHIR_BASE_URL" to "https://fhir.example.org/fhir/",
    "OAUTH_BASE_URL" to "https://auth.example.org/realms/example/",
    // Used as a raw Java literal in BuildConfig, so the quotes are part of the value.
    "OPENSRP_APP_ID" to "\"app\"",
    "FHIR_CANONICAL_BASE_URL" to "https://fhir.example.org",
    "SITE_LIST_URL" to "https://fhir.example.org/sites.json",
  )

val localProperties = readProperties((project.properties["localPropertiesFile"] ?: "${rootProject.projectDir}/local.properties").toString())

requiredFhirProperties.forEach { property ->
  val configured: String? = localProperties.getProperty(property)
  val value =
    when {
      // Settings with a placeholder default also fall back to it when left empty.
      property in placeholderDefaults ->
        configured?.takeIf { it.isNotBlank() } ?: placeholderDefaults.getValue(property)
      // An empty value here is deliberate (it turns the service off), so keep it.
      configured != null -> configured
      else -> "sample_$property"
    }
  // Canonical URLs are built as "$base/fhir/StructureDefinition/...", so drop any trailing slash.
  project.extra.set(property, if (property == "FHIR_CANONICAL_BASE_URL") value.trimEnd('/') else value)
}

// Set required keystore properties
val requiredKeystoreProperties = listOf("KEYSTORE_ALIAS", "KEY_PASSWORD", "KEYSTORE_PASSWORD")
val keystoreProperties = readProperties((project.properties["keystorePropertiesFile"] ?: "${rootProject.projectDir}/keystore.properties").toString())

requiredKeystoreProperties.forEach { property ->
  project.extra.set(property, keystoreProperties.getProperty(property,"sample_" + property))
}
