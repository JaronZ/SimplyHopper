import java.text.SimpleDateFormat
import java.util.Date

plugins {
    eclipse
    idea
//    `maven-publish`
    id("net.minecraftforge.gradle") version "[6.0.16,6.2)"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
}

// gradle.properties
val minecraftVersion: String by extra
val minecraftVersionRange: String by extra
val forgeVersion: String by extra
val forgeVersionRange: String by extra
val loaderVersionRange: String by extra
val parchmentMappingChannel: String by extra
val parchmentMappingVersion: String by extra

val modId: String by extra
val modName: String by extra
val modLicense: String by extra
val modVersion: String by extra
val modGroupId: String by extra
val modAuthors: String by extra
val modDescription: String by extra

version = modVersion
group = modGroupId

base {
    archivesName = modId
}

// Mojang ships Java 17 to end users in 1.18+, so your mod should target Java 17.
java.toolchain.languageVersion = JavaLanguageVersion.of(17)

println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")
minecraft {
    // The mappings can be changed at any time and must be in the following format.
    // Channel:   Version:
    // official   MCVersion             Official field/method names from Mojang mapping files
    // parchment  YYYY.MM.DD-MCVersion  Open community-sourced parameter names and javadocs layered on top of official
    //
    // You must be aware of the Mojang license when using the 'official' or 'parchment' mappings.
    // See more information here: https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md
    //
    // Parchment is an unofficial project maintained by ParchmentMC, separate from MinecraftForge
    // Additional setup is needed to use their mappings: https://parchmentmc.org/docs/getting-started
    //
    // Use non-default mappings at your own risk. They may not always work.
    // Simply re-run your setup task after changing the mappings to update your workspace.
    mappings(mapOf("channel" to parchmentMappingChannel, "version" to parchmentMappingVersion))

    // When true, this property will have all Eclipse/IntelliJ IDEA run configurations run the "prepareX" task for the given run configuration before launching the game.
    // In most cases, it is not necessary to enable.
    // enableEclipsePrepareRuns = true
    // enableIdeaPrepareRuns = true

    // This property allows configuring Gradle's ProcessResources task(s) to run on IDE output locations before launching the game.
    // It is REQUIRED to be set to true for this template to function.
    // See https://docs.gradle.org/current/dsl/org.gradle.language.jvm.tasks.ProcessResources.html
    copyIdeResources.set(true)

    // When true, this property will add the folder name of all declared run configurations to generated IDE run configurations.
    // The folder name can be set on a run configuration using the "folderName" property.
    // By default, the folder name of a run configuration is the name of the Gradle project containing it.
     generateRunFolders = true

    // This property enables access transformers for use in development.
    // They will be applied to the Minecraft artifact.
    // The access transformer file can be anywhere in the project.
    // However, it must be at "META-INF/accesstransformer.cfg" in the final mod jar to be loaded by Forge.
    // This default location is a best practice to automatically put the file in the right place in the final jar.
    // See https://docs.minecraftforge.net/en/latest/advanced/accesstransformers/ for more information.
    // accessTransformer = file('src/main/resources/META-INF/accesstransformer.cfg')

    // Default run configurations.
    // These can be tweaked, removed, or duplicated as needed.
    runs {
        // applies to all the run configs below
        configureEach {
            workingDirectory = "run"

            // Recommended logging data for a userdev environment
            // The markers can be added/remove as needed separated by commas.
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            property("forge.logging.markers", "REGISTRIES")

            // Recommended logging level for the console
            // You can set various levels here.
            // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
            property("forge.logging.console.level", "debug")

            mods {
                create(modId) {
                    source(sourceSets["main"])
                }
            }
        }

        create("client") {
            workingDirectory = "run/client"

            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            property("forge.enabledGameTestNamespaces", modId)
        }

        create("server") {
            workingDirectory = "run/server"

            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            property("forge.enabledGameTestNamespaces", modId)
            args("--nogui")
        }

        // This run config launches GameTestServer and runs all registered gametests, then exits.
        // By default, the server will crash when no gametests are provided.
        // The gametest system is also enabled by default for other run configs under the /test command.
        create("gameTestServer") {
            workingDirectory = "run/gameTestServer"

            property("forge.enabledGameTestNamespaces", modId)
        }

        create("data") {
            workingDirectory = "run-data"
            args("--mod", modId, "--all", "--output", file("src/generated/resources/"), "--existing", file("src/main/resources/"))
        }
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

repositories {
    // Put repositories for dependencies here
    // ForgeGradle automatically adds the Forge maven and Maven Central for you

    // If you have mod jar dependencies in ./libs, you can declare them as a repository like so.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html#sub:flat_dir_resolver
    // flatDir {
    //     dir("libs")
    // }
}

dependencies {
    // Specify the version of Minecraft to use.
    // Any artifact can be supplied so long as it has a "userdev" classifier artifact and is a compatible patcher artifact.
    // The "userdev" classifier will be requested and setup by ForgeGradle.
    // If the group id is "net.minecraft" and the artifact id is one of ["client", "server", "joined"],
    // then special handling is done to allow a setup of a vanilla dependency without the use of an external repository.
    "minecraft"("net.minecraftforge:forge:${minecraftVersion}-${forgeVersion}")

    // Example mod dependency with JEI - using fg.deobf() ensures the dependency is remapped to your development mappings
    // The JEI API is declared for compile time use, while the full JEI artifact is used at runtime
    // compileOnly(fg.deobf("mezz.jei:jei-${mc_version}-common-api:${jei_version}"))
    // compileOnly(fg.deobf("mezz.jei:jei-${mc_version}-forge-api:${jei_version}"))
    // runtimeOnly(fg.deobf("mezz.jei:jei-${mc_version}-forge:${jei_version}"))

    // Example mod dependency using a mod jar from ./libs with a flat dir repository
    // This maps to ./libs/coolmod-${mc_version}-${coolmod_version}.jar
    // The group id is ignored when searching -- in this case, it is "blank"
    // implementation(fg.deobf("blank:coolmod-${mc_version}:${coolmod_version}"))

    // For more info:
    // http://www.gradle.org/docs/current/userguide/artifact_dependencies_tutorial.html
    // http://www.gradle.org/docs/current/userguide/dependency_management.html
}

// This block of code expands all declared replace properties in the specified resource targets.
// A missing property will result in an error. Properties are expanded using ${} Groovy notation.
// When "copyIdeResources" is enabled, this will also run before the game launches in IDE environments.
// See https://docs.gradle.org/current/dsl/org.gradle.language.jvm.tasks.ProcessResources.html
tasks.named<ProcessResources>("processResources") {
    val replaceProperties = mapOf(
        "minecraft_version" to minecraftVersion, "minecraft_version_range" to minecraftVersionRange,
        "forge_version" to forgeVersion, "forge_version_range" to forgeVersionRange,
        "loader_version_range" to loaderVersionRange,
        "mod_id" to modId, "mod_name" to modName, "mod_license" to modLicense, "mod_version" to modVersion,
        "mod_authors" to modAuthors, "mod_description" to modDescription,
    )
    inputs.properties(replaceProperties)

    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(replaceProperties + mapOf("project" to project))
    }
}

// Example for how to get properties into the manifest for reading at runtime.
tasks.named<Jar>("jar") {
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to modId,
                "Specification-Vendor" to modAuthors,
                "Specification-Version" to "1", // We are version 1 of ourselves
                "Implementation-Title" to project.name,
                "Implementation-Version" to archiveVersion.get(),
                "Implementation-Vendor" to modAuthors,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
            )
        )
    }

    // This is the preferred method to reobfuscate your jar file
    finalizedBy("reobfJar")
}

// Uncomment if using `publish` in a multi-project setup
// tasks.named("publish") {
//     dependsOn("reobfJar")
// }

// Example configuration to allow publishing using the maven-publish plugin
//publishing {
//    publications {
//        create<MavenPublication>("mavenJava") {
//            artifact(tasks["jar"])
//        }
//    }
//    repositories {
//        maven {
//            url = uri("file://${project.projectDir}/mcmodsrepo")
//        }
//    }
//}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
}

tasks.withType<JavaExec>().configureEach {

    if (name == "data") {
        outputs.upToDateWhen { false } // always regenerate data
    }
}

tasks.withType<Copy>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}