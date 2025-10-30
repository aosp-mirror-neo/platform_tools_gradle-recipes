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
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.name

/**
 * This validator takes a working copy and validate that it still works.
 *
 * It does this by converting it back to source mode in a temp folder, and then validates it with whatever AGP
 * version range is declared (either min/max or min and current AGP version)
 */
class WorkingCopyValidator(
    private val context: Context,
) {

    fun validate(recipeSource: Path): ResultMode {
        val recipeValidator = SourceValidator(context, agpVersion = null)
        return recipeValidator.validate(convertToSourceOfTruth(recipeSource), recipeSource.name)
    }

    private fun convertToSourceOfTruth(from: Path): Path {
        val destination: Path = createTempDirectory().also { it.toFile().deleteOnExit() }

        val convertToSourceTruth = RecipeConverter(
            context = context,
            agpVersion = null,
            gradleVersion = null,
            mode = Mode.SOURCE,
            strictVersionCheck = false,
        )
        val result = convertToSourceTruth.convert(source = from, destination = destination)

        return destination.resolve(result.recipeData.destinationFolder)
    }
}
