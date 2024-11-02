#!/usr/bin/env kotlin

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.8.0")
@file:Suppress("PropertyName")

import io.github.typesafegithub.workflows.actions.actions.*
import io.github.typesafegithub.workflows.domain.*
import io.github.typesafegithub.workflows.domain.RunnerType.*
import io.github.typesafegithub.workflows.domain.triggers.*
import io.github.typesafegithub.workflows.dsl.*
import io.github.typesafegithub.workflows.dsl.expressions.*
import io.github.typesafegithub.workflows.yaml.*

val LinuxRunner = Ubuntu2004
val MacosRunner = MacOSLatest
val WindowsRunner = WindowsLatest

class Configuration(
    val name: String,
    val runnerType: RunnerType,
    val profiles: List<Pair<String, BuildKind>>
)

enum class BuildKind(
    val buildDynamic: Boolean,
    val buildStatic: Boolean
) {
    Dynamic(true, false),
    Static(false, true),
    Both(true, true)
}

val configurations = listOf(
    Configuration(
        name = "macos",
        runnerType = MacosRunner,
        profiles = listOf(
            "tvos-simulator-arm64" to BuildKind.Static,
            "tvos-simulator-x64" to BuildKind.Static,
            "tvos-device-arm64" to BuildKind.Static,

            "watchos-simulator-arm64" to BuildKind.Static,
            "watchos-simulator-x64" to BuildKind.Static,
            "watchos-device-arm32" to BuildKind.Static,
            "watchos-device-arm64" to BuildKind.Static,
            "watchos-device-arm64_32" to BuildKind.Static,

            "ios-device-arm64" to BuildKind.Static,
            "ios-simulator-arm64" to BuildKind.Static,
            "ios-simulator-x64" to BuildKind.Static,

            "macos-x64" to BuildKind.Both,
            "macos-arm64" to BuildKind.Both,
        )
    ),
    Configuration(
        name = "linux",
        runnerType = LinuxRunner,
        profiles = listOf(
            "android-arm64" to BuildKind.Both,
            "android-arm32" to BuildKind.Both,
            "android-x64" to BuildKind.Both,
            "android-x86" to BuildKind.Both,

            "linux-x64" to BuildKind.Both,
            "linux-arm64" to BuildKind.Both,

            "wasm" to BuildKind.Static,
        )
    ),
    Configuration(
        name = "windows",
        runnerType = WindowsRunner,
        profiles = listOf(
            "mingw-x64" to BuildKind.Both,
            "windows-x64" to BuildKind.Dynamic,
        )
    )
)

fun conanCreateCommand(profile: String, version: String, shared: String): String = conanCommand(
    profile, version, shared,
    "create conan-center-index/recipes/libcurl/all --build=missing"
)

fun conanInstallCommand(profile: String, version: String, shared: String): String = conanCommand(
    profile, version, shared,
    "install packages/libcurl --output-folder build/libcurl/$profile"
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
//        Push(),
        WorkflowDispatch(
            inputs = mapOf(
                "version" to WorkflowDispatch.Input(
                    description = "version of libcurl",
                    required = true,
                    type = WorkflowDispatch.Type.String
                )
            )
        )
    ),
    _customArguments = mapOf(
        "defaults" to mapOf(
            "run" to mapOf(
                "shell" to "bash"
            )
        )
    ),
    sourceFile = __FILE__.toPath(),
) {
    //val version = "3.2.0"
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

            if (configuration.runnerType == LinuxRunner) {
                run(command = "sudo apt update")
                run(command = "sudo apt install g++-8-aarch64-linux-gnu g++-8")
            }

            configuration.profiles.forEach { (profile, buildKind) ->
                if (buildKind.buildDynamic) {
                    run(command = conanCreateCommand(profile, version, "True"), continueOnError = true)
                    run(command = conanInstallCommand(profile, version, "True"), continueOnError = true)
                }
                if (buildKind.buildStatic) {
                    run(command = conanCreateCommand(profile, version, "False"), continueOnError = true)
                    run(command = conanInstallCommand(profile, version, "False"), continueOnError = true)
                }
            }

            when (configuration.runnerType) {
                WindowsRunner -> listOf("lib", "include", "bin")
                else -> listOf("lib", "include")
            }.forEach { folder ->
                run(command = "tar -rvf ${configuration.name}.tar build/libcurl/*/$folder", continueOnError = true)
            }

            uses(
                action = UploadArtifactV4(
                    name = "libcurl-${configuration.name}-$version",
                    ifNoFilesFound = UploadArtifactV4.BehaviorIfNoFilesFound.Error,
                    path = listOf("${configuration.name}.tar")
                ),
                continueOnError = true
            )
        }
    }

    job(
        id = "aggregate",
        runsOn = UbuntuLatest,
        needs = jobs
    ) {
        uses(
            action = DownloadArtifactV4(
                pattern = "libcurl-*-$version",
                mergeMultiple = true
            )
        )

        configurations.forEach {
            run(command = "tar -xvf ${it.name}.tar")
        }

        run(
            command = "tar -czvf ../../libcurl-$version.tar.gz *",
            workingDirectory = "build/libcurl"
        )

        run(
            command = "zip --symlinks -r ../../libcurl-$version.zip *",
            workingDirectory = "build/libcurl"
        )

        uses(
            action = UploadArtifactV4(
                name = "libcurl-$version",
                ifNoFilesFound = UploadArtifactV4.BehaviorIfNoFilesFound.Error,
                path = listOf(
                    "libcurl-$version.tar.gz",
                    "libcurl-$version.zip",
                )
            )
        )
    }
}.writeToFile(addConsistencyCheck = false)
