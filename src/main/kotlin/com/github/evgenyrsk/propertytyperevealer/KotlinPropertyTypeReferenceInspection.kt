package com.github.evgenyrsk.propertytyperevealer

import com.intellij.codeInsight.daemon.GroupNames
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.immediateSupertypes

private const val DISPLAY_NAME = "The property is without type declaration"

/**
 * An inspection for detecting places where the property with no type.
 * The inspection highlights that place and offers to add an appropriate type [PropertyTypeReferenceQuickFix].
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

                val possibleQuickFixes: MutableList<LocalQuickFix> = mutableListOf()
                if (property.typeReference == null && (property.isTopLevel || property.isMember)) {

                    possibleQuickFixes.add(
                        quickFixFactory.create(
                            property.type().toString()
                        )
                    )
                    property.type()
                        ?.immediateSupertypes()
                        ?.forEach { kotlinType: KotlinType ->
                            possibleQuickFixes.add(
                                quickFixFactory.create(
                                    constructSupertypeWithGenericArguments(kotlinType)
                                )
                            )
                        }

                    registerProblems(
                        holder = holder,
                        whatToFix = property,
                        propertyTypeQuickFixes = possibleQuickFixes.toTypedArray()
                    )
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

    private fun registerProblems(
        holder: ProblemsHolder,
        whatToFix: PsiElement,
        propertyTypeQuickFixes: Array<LocalQuickFix>
    ) {
        holder.registerProblem(
            whatToFix,
            "You should set a type for the property",
            *propertyTypeQuickFixes
        )
    }

    companion object {
        const val INSPECTION_FAMILY_NAME = "Code style inspections"
    }
}