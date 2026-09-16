import com.xpdustry.toxopid.extension.anukeJitpack
import com.xpdustry.toxopid.extension.anukeZelaux
import com.xpdustry.toxopid.spec.ModMetadata
import com.xpdustry.toxopid.spec.ModPlatform

plugins {
    java
    id("com.xpdustry.toxopid") version "4.1.2"
}

val metadata = ModMetadata.fromJson(project.file("mod.json"))
version = metadata.version

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.x-core.org/releases")
    maven("https://maven.x-core.org/snapshots")
    anukeJitpack()
    anukeZelaux()
}

toxopid {
    compileVersion.set("v${metadata.minGameVersion}")
    platforms.set(setOf(ModPlatform.ANDROID, ModPlatform.DESKTOP))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("--release", "17"))
}

dependencies {
    compileOnly(toxopid.dependencies.mindustryCore)
    compileOnly(toxopid.dependencies.arcCore)
    implementation("org.xcore:xcore-protocol-java:0.6.2")
}

tasks {
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(rootDir) {
            include("mod.json")
            include("bundles/**")
        }
        from({
            configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
        })
    }

    mergeJar {
        doLast {
            val dexedJar = outputs.files.singleFile
            val finalJar = project.file("build/libs/${project.rootDir.name}.jar")

            if (dexedJar.exists()) {
                dexedJar.copyTo(finalJar, overwrite = true)
                println("✅ Android-ready JAR created: ${finalJar.name}")
            } else {
                println("⚠️ Dexed jar not found!")
            }
        }
    }

    build {
        dependsOn(mergeJar)
    }
}
