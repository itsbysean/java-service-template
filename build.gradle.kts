import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    base
}

val javaVersion = providers.gradleProperty("javaVersion").get().toInt()
val archUnitVersion = providers.gradleProperty("archUnitVersion").get()
val junitVersion = providers.gradleProperty("junitVersion").get()

subprojects {
    val module = this

    module.pluginManager.withPlugin("java") {
        module.extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion = JavaLanguageVersion.of(javaVersion)
            }
        }

        module.repositories {
            mavenCentral()
        }

        module.dependencies {
            add("testImplementation", platform("org.junit:junit-bom:$junitVersion"))
            add("testImplementation", "org.junit.jupiter:junit-jupiter")
            add("testImplementation", "com.tngtech.archunit:archunit-junit5:$archUnitVersion")
            add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
        }

        val mainClassesDirs = module.extensions
            .getByType<SourceSetContainer>()
            .named("main")
            .get()
            .output
            .classesDirs

        module.tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            systemProperty("architectureTest.mainClassesDirs", mainClassesDirs.asPath)
            if (module.path == ":service" || module.path == ":client") {
                val contractClassesDirs = project(":core")
                    .extensions
                    .getByType<SourceSetContainer>()
                    .named("main")
                    .get()
                    .output
                    .classesDirs

                dependsOn(project(":core").tasks.named("classes"))
                systemProperty("architectureTest.contractClassesDirs", contractClassesDirs.asPath)
                inputs.files(contractClassesDirs)
                doFirst {
                    contractClassesDirs.files.forEach { it.mkdirs() }
                }
            }
            doFirst {
                mainClassesDirs.files.forEach { it.mkdirs() }
            }
        }
    }
}
