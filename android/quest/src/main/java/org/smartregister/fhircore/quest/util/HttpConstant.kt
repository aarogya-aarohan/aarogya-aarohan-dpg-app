package org.smartregister.fhircore.quest.util

import org.smartregister.fhircore.engine.util.CanonicalUrls

const val SUSPICIOUS_NON_SUSPICIOUS_URL = "${CanonicalUrls.CODE_SYSTEM}/ai-result-value"
const val CONFIDENCE_PERCENTAGE_URL = "${CanonicalUrls.CODE_SYSTEM}/ai-result-confidence"
const val CASE_LEVEL_AI_RESULT_URL = "${CanonicalUrls.CODE_SYSTEM}/case-level-ai-result"
const val REFER_CASE_URL = "${CanonicalUrls.CODE_SYSTEM}/refer-case"

const val AI_RESULT_SUFFIX = "-ai-result"
const val CASE_LEVEL_AI_RESULT_LINK_ID = "case-level-ai-result"

/** Hidden QR item with the model's class for the whole case (e.g. "opmd"); see QuestionnaireActivity. */
const val CASE_LEVEL_AI_CLASS_LINK_ID = "case-level-ai-class"
