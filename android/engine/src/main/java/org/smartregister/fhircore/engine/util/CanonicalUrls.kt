/*
 * Copyright 2021-2024 Ona Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.smartregister.fhircore.engine.util

import org.smartregister.fhircore.engine.BuildConfig

/**
 * This app's own FHIR canonical URLs: the extensions, code systems and identifier systems it reads
 * and writes. They all start with [BASE], which comes from `FHIR_CANONICAL_BASE_URL` in
 * `local.properties`.
 *
 * Your server's StructureMaps, Questionnaires and feature-flag resources must use the same URLs, or
 * the app won't find the values it looks for. See "Point the app at your own server" in
 * docs/developer-setup-guide.md.
 */
object CanonicalUrls {
  /** For example `https://fhir.example.org`. No trailing slash. */
  const val BASE = BuildConfig.FHIR_CANONICAL_BASE_URL

  /** Extensions: `$BASE/fhir/StructureDefinition/<name>`. */
  const val STRUCTURE_DEFINITION = "$BASE/fhir/StructureDefinition"

  /** Code systems: `$BASE/CodeSystem/<name>`. */
  const val CODE_SYSTEM = "$BASE/CodeSystem"

  /** Identifier systems: `$BASE/fhir/identifier/<name>`. */
  const val IDENTIFIER = "$BASE/fhir/identifier"
}
