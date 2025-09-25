load("//tools/base/bazel:kotlin.bzl", "kotlin_library", "kotlin_test")
load("//tools/base/bazel:maven.bzl", "maven_repository")
load("//tools/base/build-system/integration-test:common-dependencies.bzl", "KGP_1_8_10", "KGP_1_9_22", "KGP_2_1_20")
load(":recipes.bzl", "recipe_test_suite")

kotlin_library(
    name = "convert_tool",
    srcs = glob([
        "convert-tool/app/src/main/kotlin/**/*.kt",
    ]),
    lint_baseline = "lint_baseline.xml",
    deps = [
        "@maven//:com.google.guava.guava",
        "@maven//:com.squareup.okhttp3.okhttp",
        "@maven//:org.gradle.gradle-tooling-api",
        "@maven//:org.jetbrains.kotlinx.kotlinx-cli-jvm",
        "@maven//:org.tomlj.tomlj",
    ],
)

kotlin_test(
    name = "convert_tool_tests",
    srcs = glob([
        "convert-tool/app/src/test/kotlin/**/*.kt",
    ]),
    jvm_flags = ["-Dtest.suite.jar=convert_tool_tests.jar"],
    test_class = "com.android.testutils.JarTestSuite",
    deps = [
        ":convert_tool",
        "//tools/base/testutils:tools.testutils",
        "@maven//:com.google.truth.truth",
        "@maven//:junit.junit",
    ],
)

kotlin_library(
    name = "gradle_recipe_tester",
    testonly = 1,
    srcs = glob(["convert-tool/integTest/src/main/kotlin/**/*.kt"]),
    visibility = ["//visibility:public"],
    deps = [
        ":convert_tool",
        "//tools/base/bazel:gradle",
        "//tools/base/common:tools.common",
        "//tools/base/testutils:tools.testutils",
        "@maven//:com.google.code.gson.gson",
        "@maven//:junit.junit",
    ],
)

# for testing against older KGP
maven_repository(
    name = "kotlin_2_1_20",
    artifacts = KGP_2_1_20,
    visibility = [":__subpackages__"],
)

maven_repository(
    name = "kotlin_1_9_22",
    artifacts = KGP_1_9_22,
    visibility = [":__subpackages__"],
)

maven_repository(
    name = "kotlin_1_8_10",
    artifacts = KGP_1_8_10,
    visibility = [":__subpackages__"],
)

recipe_test_suite(
    name = "recipe_tests",
    recipes = glob(["recipes/**/recipe_metadata.toml"]),
)
