plugins {
    org.jetbrains.kotlin.plugin.compose
    id("ivy.module")
}

android {
    // Compose
    buildFeatures {
        compose = true
    }

    lint {
        disable += "MissingTranslation"
        disable += "ComposeViewModelInjection"
        abortOnError = false
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

composeCompiler {
    // Kotlin 2.2.10's report printer crashes on some legacy composable default expressions
    // (IrSourcePrinterVisitor.printReceiver). Reports are diagnostics, not compilation output.
    if (providers.gradleProperty("composeCompilerReports").orNull == "true") {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
    }
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    implementation(libs.bundles.compose)

    lintChecks(libs.slack.lint.compose)
}
