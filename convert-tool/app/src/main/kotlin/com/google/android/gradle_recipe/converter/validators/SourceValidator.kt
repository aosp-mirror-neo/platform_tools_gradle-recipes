/*
 * Copyright 2022 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gradle_recipe.converter.validators

import com.google.android.gradle_recipe.converter.context.Context
import com.google.android.gradle_recipe.converter.converters.FullAgpVersion
import com.google.android.gradle_recipe.converter.converters.RecipeConverter
import com.google.android.gradle_recipe.converter.converters.RecipeConverter.Mode
import com.google.android.gradle_recipe.converter.converters.ResultMode
import com.google.android.gradle_recipe.converter.recipe.RecipeData
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.name

/**
 * Validates recipe from source mode.
 *
 * If [agpVersion] is not null, that version is used for validation. Otherwise, the recipe is
 * validated using both the min and the current/max AGP versions.
 *
 * This creates release copies of the recipe with the specific versions of AGP(s), and tests these copies.
 */
class SourceValidator(
    private val context: Context,
    private val agpVersion: FullAgpVersion? = null,
) {

    fun validate(recipeFolder: Path, name: String? = null): ResultMode {
        val finalName = name ?: recipeFolder.name
        val recipeData = RecipeData.loadFrom(recipeFolder, Mode.RELEASE)

        return if (agpVersion != null) {
            validateRecipeFromSource(finalName, recipeFolder, agpVersion)
        } else {
            validateRecipeFromSource(finalName, recipeFolder, recipeData.minAgpVersion.toFull())

            val max = if (recipeData.maxAgpVersion == null) {
                context.maxPublishedAgp
            } else {
                context.getPublishedAgp(recipeData.maxAgpVersion)
            }

            validateRecipeFromSource(finalName, recipeFolder, max)
        }
    }

    private fun validateRecipeFromSource(
        name: String,
        from: Path,
        agpVersion: FullAgpVersion,
    ): ResultMode {
        val gradleVersion = if (context.gradlePath == null) {
            context.getGradleVersion(agpVersion.toShort())
        } else {
            null
        }

        val recipeConverter = RecipeConverter(
            context = context,
            agpVersion = agpVersion,
            gradleVersion = gradleVersion,
            mode = Mode.RELEASE,
            // normally strict, unless we're running on our CI, because we validate against all AGP versions on
            // purpose and we want to just skip the incompatible ones.
            strictVersionCheck = !context.ci
        )

        val destinationFolder = createTempDirectory().also { it.toFile().deleteOnExit() }

        val conversionResult = recipeConverter.convert(
            source = from, destination = destinationFolder
        )

        // the recipe destination is inside destinationFolder
        val recipeFolder = destinationFolder.resolve(conversionResult.recipeData.destinationFolder)

        if (conversionResult.resultMode == ResultMode.SUCCESS) {
            println("Validating: Recipe $name ($recipeFolder) with AGP: $agpVersion and Gradle: $gradleVersion")
            val tasksExecutor = GradleTasksExecutor(recipeFolder, context)
            tasksExecutor.executeTasks(conversionResult.recipeData.tasks)

            if (conversionResult.recipeData.validationTasks != null) {
                tasksExecutor.executeTasks(conversionResult.recipeData.validationTasks)
            }
        }

        return conversionResult.resultMode
    }
}