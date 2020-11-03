package com.github.evgenyrsk.propertytyperevealer

import com.intellij.codeInsight.daemon.GroupNames
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.immediateSupertypes

private const val DISPLAY_NAME = "The property is without type declaration"

/**
 * An inspection for detecting places where there is no type for the property.
 * Highlights and offers to add a type if there is none [PropertyTypeReferenceQuickFix].
 */
class KotlinPropertyTypeReferenceInspection : AbstractKotlinInspection() {

    private val quickFixFactory: PropertyTypeQuickFixFactory = PropertyTypeQuickFixFactory {
        PropertyTypeReferenceQuickFix(it)
    }

    override fun getDisplayName(): String = DISPLAY_NAME

    override fun getGroupDisplayName(): String = GroupNames.STYLE_GROUP_NAME

    override fun isEnabledByDefault(): Boolean = true

    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean,
            session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return object : KtVisitorVoid() {

            override fun visitProperty(property: KtProperty) {
                super.visitProperty(property)
                if (property.typeReference == null) {
                    registerProblem(
                            holder,
                            property,
                            quickFixFactory.create(property.type().toString())
                    )
                    property.type()
                            ?.immediateSupertypes()
                            ?.forEach { kotlinType: KotlinType ->
                                registerProblem(
                                        holder,
                                        property,
                                        quickFixFactory.create(
                                                constructSupertypeWithGenericArguments(kotlinType)
                                        )
                                )
                            }
                }
            }
        }
    }

    private fun constructSupertypeWithGenericArguments(type: KotlinType): String {
        val typeBuilder: StringBuilder = StringBuilder(type.constructor.toString())
        val isArgumentsNotEmpty: Boolean = !type.arguments.isNullOrEmpty()
        if (isArgumentsNotEmpty) {
            typeBuilder.append("<")
            type.arguments.forEachIndexed { index, typeProjection ->
                typeBuilder.append("${typeProjection.type.constructor}")
                if (index + 1 < type.arguments.size) {
                    typeBuilder.append(", ")
                }
            }
            typeBuilder.append(">")
        }
        return typeBuilder.toString()
    }

    private fun registerProblem(
            holder: ProblemsHolder,
            whatToFix: PsiElement,
            propertyTypeQuickFix: LocalQuickFix
    ) {
        holder.registerProblem(
                whatToFix,
                "There's no type reference for the property",
                propertyTypeQuickFix
        )
    }

    companion object {
        const val INSPECTION_FAMILY_NAME = "Add property type"
    }
}