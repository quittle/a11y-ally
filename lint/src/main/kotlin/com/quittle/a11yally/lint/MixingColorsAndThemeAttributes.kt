package com.quittle.a11yally.lint

import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScannerConstants
import org.w3c.dom.Element

/**
 * Sample detector showing how to analyze Kotlin/Java code.
 * This example flags all string literals in the code that contain
 * the word "lint".
 */
@Suppress("UnstableApiUsage")
class MixingColorsAndThemeAttributes : ResourceXmlDetector() {
    override fun appliesTo(folderType: ResourceFolderType): Boolean {
        return folderType === ResourceFolderType.DRAWABLE
    }

    override fun getApplicableElements(): Collection<String> = XmlScannerConstants.ALL

    /**
     * Visit the given element.
     * @param context information about the document being analyzed
     * @param element the element to examine
     */
    override fun visitElement(context: XmlContext, element: Element) {
        val attributes = element.attributes

        val containsAttribute = mutableListOf<String>()
        val containsReferenceOrLiteral = mutableListOf<String>()

        for (i in 0 until attributes.length) {
            val attribute = attributes.item(i)
            val key = attribute.nodeName
            val value = attribute.nodeValue

            if (isAttributeReference(value)) {
                containsAttribute.add(key)
            } else if (isColorLiteral(value) or isColorReference(value)) {
                containsReferenceOrLiteral.add(key)
            }
        }

        if (containsAttribute.isNotEmpty() and containsReferenceOrLiteral.isNotEmpty()) {
            context.report(
                ISSUE, element, context.getLocation(element),
                "$containsAttribute uses attribute references while $containsReferenceOrLiteral " +
                    "uses color literals or references."
            )
        }
    }

    override fun getApplicableAttributes(): Collection<String> = XmlScannerConstants.ALL

    companion object {
        /** Issue describing the problem and pointing to the detector implementation */
        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "MixingColorsAndThemeAttributes",
            briefDescription = "Mixing colors and theme attributes in drawables",
            explanation = """
                    Some Android versions do not support mixing color references or literals with
                    theme attributes.
                    
                    On these platforms, loading the styles results in the following error at runtime.
                    ```
                    java.lang.IllegalArgumentException: color and position arrays must be of equal length
                    ```
                    """,
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.WARNING,
            implementation = Implementation(
                MixingColorsAndThemeAttributes::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )

        private fun isColorLiteral(attributeValue: String): Boolean {
            return Regex("#[a-fA-F0-9]{3,8}").matches(attributeValue)
        }

        private fun isColorReference(attributeValue: String): Boolean {
            return Regex("@[\\w]*:?color/.+").matches(attributeValue)
        }

        private fun isAttributeReference(attributeValue: String): Boolean {
            return Regex("\\?[\\w]*:?attr/.+").matches(attributeValue)
        }
    }
}
