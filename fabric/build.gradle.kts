plugins {
    alias(libs.plugins.shadow)
}


architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentFabric: Configuration by configurations.getting

configurations {
    compileOnly.configure { extendsFrom(common) }
    runtimeClasspath.configure { extendsFrom(common) }
    developmentFabric.extendsFrom(common)
}

dependencies {
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.architectury.fabric)
    modImplementation(libs.fabric.language.kotlin)

    common(project(":common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", "transformProductionFabric")) { isTransitive = false }
}

tasks.processResources {
    val props = (rootProject.findProperty("mod_properties") as Map<String, *>) +
            mapOf("fabric_kotlin_version" to libs.versions.fabric.language.kotlin.get())
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

tasks.shadowJar {
    exclude("architectury.common.json")
    configurations = listOf(shadowCommon)
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    injectAccessWidener.set(true)
    inputFile.set(tasks.shadowJar.get().archiveFile)
    dependsOn(tasks.shadowJar)
    archiveClassifier.set(null as String?)
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.sourcesJar {
    val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
    dependsOn(commonSources)
    from(commonSources.archiveFile.map { zipTree(it) })
}

components.getByName("java") {
    this as AdhocComponentWithVariants
    this.withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
        skip()
    }
}


val maven_group: String by project
val archives_base_name: String by project
val github_repo: String by project
val mod_description = (rootProject.extra["mod_properties"] as Map<*, *>)["mod_description"].toString()
val total_version = "${libs.versions.mod.version.get()}+${libs.versions.minecraft.get()}"
val license_name: String by project



publishing {
    publications {
        create("fabric", MavenPublication::class.java) {
            groupId = maven_group
            artifactId = "$archives_base_name-fabric"
            version = total_version

            from(components["java"])

            pom {
                name = artifactId
                description = mod_description
                url = github_repo
                licenses {
                    license {
                        name.set(license_name)
                    }
                }
                developers {
                    developer {
                        id = "fudge"
                        name = "natan"
                        email = "natandestroyer100@gmail.com"
                    }
                }

                scm {
                    url = github_repo
                }
            }
        }
    }
}
