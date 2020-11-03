package com.github.evgenyrsk.propertytyperevealer

fun interface PropertyTypeQuickFixFactory {
    fun create(type: String): PropertyTypeReferenceQuickFix
}