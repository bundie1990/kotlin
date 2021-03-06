/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.tasks.KOTLIN_BUILD_DIR_NAME
import java.io.File

open class KotlinWithJavaTarget<KotlinOptionsType : KotlinCommonOptions>(
    project: Project,
    override val platformType: KotlinPlatformType,
    override val targetName: String,
    kotlinOptionsFactory: () -> KotlinOptionsType
) : AbstractKotlinTarget(project) {
    override var disambiguationClassifier: String? = null
        internal set

    override val defaultConfigurationName: String
        get() = Dependency.DEFAULT_CONFIGURATION

    override val apiElementsConfigurationName: String
        get() = JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME

    override val runtimeElementsConfigurationName: String
        get() = JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME

    override val artifactsTaskName: String
        get() = JavaPlugin.JAR_TASK_NAME

    override val compilations: NamedDomainObjectContainer<KotlinWithJavaCompilation<KotlinOptionsType>> =
        @Suppress("UNCHECKED_CAST")
        project.container(
            KotlinWithJavaCompilation::class.java as Class<KotlinWithJavaCompilation<KotlinOptionsType>>,
            KotlinWithJavaCompilationFactory(project, this, kotlinOptionsFactory)
        )

    internal val defaultArtifactClassesListFile: File
        get() {
            val jarTask = project.tasks.getByName(artifactsTaskName) as Jar
            return File(File(project.buildDir, KOTLIN_BUILD_DIR_NAME), "${sanitizeFileName(jarTask.archiveFileName.get())}-classes.txt")
        }
}

private fun sanitizeFileName(candidate: String): String = candidate.filter { it.isLetterOrDigit() }
