import com.xpdustry.toxopid.spec.ModPlatform
import com.xpdustry.toxopid.extension.anukeZelaux
import com.xpdustry.toxopid.extension.anukeJitpack
import com.xpdustry.toxopid.spec.ModMetadata

plugins {
    java
    id("com.xpdustry.toxopid") version "4.1.2"
}

val metadata = ModMetadata.fromJson(project.file("mod.json"))
version = metadata.version

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

repositories {
    mavenCentral()
    anukeJitpack()
    anukeZelaux()
}

toxopid {
    compileVersion.set("v${metadata.minGameVersion}")
    platforms.set(setOf(ModPlatform.ANDROID, ModPlatform.DESKTOP))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("--release", "16"))
}

dependencies {
    compileOnly(toxopid.dependencies.mindustryCore)
    compileOnly(toxopid.dependencies.arcCore)
}

tasks {
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(rootDir) {
            include("mod.json")
        }
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