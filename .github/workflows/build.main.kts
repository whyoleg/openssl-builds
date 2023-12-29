#!/usr/bin/env kotlin

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.8.0")

import io.github.typesafegithub.workflows.actions.actions.*
import io.github.typesafegithub.workflows.domain.*
import io.github.typesafegithub.workflows.domain.RunnerType.*
import io.github.typesafegithub.workflows.domain.triggers.*
import io.github.typesafegithub.workflows.dsl.*
import io.github.typesafegithub.workflows.dsl.expressions.*
import io.github.typesafegithub.workflows.yaml.*

class Configuration(
    val name: String,
    val runnerType: RunnerType,
    // 'profile name' to 'supports shared'
    val profiles: List<Pair<String, Boolean>>
)

val configurations = listOf(
    Configuration(
        name = "macos",
        runnerType = MacOSLatest,
        profiles = listOf(
            "tvos-simulator-arm64" to false,
            "tvos-simulator-x64" to false,
            "tvos-device-arm64" to false,

            "watchos-simulator-arm64" to false,
            "watchos-simulator-x64" to false,
            "watchos-device-arm32" to false,
            "watchos-device-arm64" to false,
            "watchos-device-arm64_32" to false,

            "ios-device-arm64" to false,
            "ios-simulator-arm64" to false,
            "ios-simulator-x64" to false,

            "macos-x64" to true,
            "macos-arm64" to true,
        )
    ),
    Configuration(
        name = "linux",
        runnerType = Ubuntu2004,
        profiles = listOf(
            "android-arm64" to true,
            "android-arm32" to true,
            "android-x64" to true,
            "android-x86" to true,

            "linux-x64" to true,
            "linux-arm64" to true,

            "wasm" to false,
        )
    ),
    Configuration(
        name = "windows",
        runnerType = WindowsLatest,
        profiles = listOf(
            "mingw-x64" to true
        )
    )
)

fun conanCreateCommand(profile: String, version: String, shared: String): String = conanCommand(
    profile, version, shared,
    "create conan-center-index/recipes/openssl/3.x.x --build=missing"
)

fun conanInstallCommand(profile: String, version: String, shared: String): String = conanCommand(
    profile, version, shared,
    "install packages/openssl3 --output-folder build/openssl3/$profile"
)

fun conanCommand(profile: String, version: String, shared: String, command: String): String = listOf(
    "conan",
    command,
    "--version=$version",
    "-pr:b default",
    "-pr:h profiles/$profile",
    "-o \"*:shared=$shared\"",
).joinToString(" ")

workflow(
    name = "Build",
    on = listOf(
        WorkflowDispatch(
            inputs = mapOf(
                "version" to WorkflowDispatch.Input(
                    description = "version of OpenSSL 3",
                    required = true,
                    type = WorkflowDispatch.Type.String
                )
            )
        )
    ),
    sourceFile = __FILE__.toPath(),
) {
    val version = expr("inputs.version")
    val jobs = configurations.map { configuration ->
        job(
            id = configuration.name,
            name = "${configuration.name}-$version",
            runsOn = configuration.runnerType
        ) {
            uses(action = CheckoutV4(submodules = true))
            uses(action = SetupPythonV5(pythonVersion = "3.x"))
            run(command = "pip install conan")
            run(command = "conan profile detect")

            if (configuration.runnerType == Ubuntu2004) {
                run(command = "sudo apt update")
                run(command = "sudo apt install g++-8-aarch64-linux-gnu g++-8")
            }

            configuration.profiles.forEach { (profile, supportsShared) ->
                if (supportsShared) {
                    run(command = conanCreateCommand(profile, version, "True"))
                    run(command = conanInstallCommand(profile, version, "True"))
                }
                run(command = conanCreateCommand(profile, version, "False"))
                run(command = conanInstallCommand(profile, version, "False"))
            }

            uses(
                action = UploadArtifactV4(
                    name = "openssl-${configuration.name}-$version",
                    ifNoFilesFound = UploadArtifactV4.BehaviorIfNoFilesFound.Error,
                    path = listOf(
                        "build/openssl3/*/dynamicLib/*",
                        "build/openssl3/*/staticLib/*",
                        "build/openssl3/*/include/*",
                    )
                )
            )
        }
    }

    job(id = "aggregate", runsOn = UbuntuLatest, needs = jobs) {
        uses(
            action = DownloadArtifactV4(
                pattern = "openssl-*-$version",
                mergeMultiple = true,
                path = "openssl"
            )
        )
        uses(
            action = UploadArtifactV4(
                name = "openssl-$version",
                ifNoFilesFound = UploadArtifactV4.BehaviorIfNoFilesFound.Error,
                path = listOf("openssl")
            )
        )
    }
}.writeToFile(addConsistencyCheck = false)
