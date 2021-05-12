package com.github.evgenyrsk.propertytyperevealer

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtTypeReference

/**
 * Fix which adds a property type.
 */
class PropertyTypeReferenceQuickFix(
    private val type: String,
) : LocalQuickFix {

    override fun getName(): String = "Insert the type: $type"

    override fun getFamilyName(): String = KotlinPropertyTypeReferenceInspection.INSPECTION_FAMILY_NAME

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val elementToFix: KtProperty = descriptor.psiElement as KtProperty

        try {
            val type: KtTypeReference = KtPsiFactory(elementToFix).createType(type)
            elementToFix.typeReference = type
        } catch (ignored: Exception) {
            // ignored
        }
    }
}