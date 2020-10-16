package com.github.evgenyrsk.propertytyperevealer.services

import com.intellij.openapi.project.Project
import com.github.evgenyrsk.propertytyperevealer.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
