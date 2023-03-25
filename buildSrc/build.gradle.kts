plugins {
  // Support convention plugins written in Kotlin. Convention plugins are build scripts in
  // 'src/main' that automatically become available as plugins in the main build.
  `kotlin-dsl`
  id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
}

repositories {
  // Use the plugin portal to apply community plugins in convention plugins.
  gradlePluginPortal()
}

dependencies { implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10") }

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
  filter { exclude("**/generated-sources/**") }
}
