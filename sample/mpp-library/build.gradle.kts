/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("multiplatform-library-convention")
    id("dev.icerock.mobile.multiplatform.apple-framework")
    id("dev.icerock.mobile.multiplatform-resources")
    id("detekt-convention")
}

android {
    namespace = "com.icerockdev.library"
    lint {
        disable += "ImpliedQuantity"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    sourceSets{
        this.getByName("main"){
            res.srcDir(File(buildDir, "generated/moko/androidMain/res"))
        }
    }
}

kotlin {
    explicitApi()

    val xcFramework = XCFramework("MultiPlatformLibrary")
    targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class)
        .matching { it.konanTarget.family == org.jetbrains.kotlin.konan.target.Family.IOS }
        .configureEach {
            binaries.withType(org.jetbrains.kotlin.gradle.plugin.mpp.Framework::class)
                .configureEach { xcFramework.add(this) }
        }
}

listOf("iosX64Test","iosSimulatorArm64Test").forEach {
    tasks.getByName<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest>(it){
        deviceId = "iPhone 14"
    }
}

dependencies {
    commonMainApi(projects.resources)
    commonMainApi(libs.mokoGraphics)
    commonMainImplementation(projects.sample.mppLibrary.nestedModule)
    commonMainImplementation(projects.sample.mppLibrary.emptyModule)

    commonTestImplementation(projects.resourcesTest)
    commonTestImplementation(projects.sample.testUtils)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
}

framework {
    export(projects.resources)
    export(libs.mokoGraphics)
}

tasks.register("debugFatFramework", org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask::class) {
    baseName = "multiplatform"

    val targets = mapOf(
        "iosX64" to kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>(
            "iosX64"
        ),
        "iosArm64" to kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>(
            "iosArm64"
        )
    )

    from(
        targets.toList().map {
            it.second.binaries.getFramework(
                "MultiPlatformLibrary",
                org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG
            )
        }
    )
}
