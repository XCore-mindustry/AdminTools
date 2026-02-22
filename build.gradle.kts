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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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
    options.compilerArgs.addAll(listOf("--release", "17"))
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

        doLast {
            val modName = project.rootDir.name
            val dexedJar = project.file("build/libs/$modName-$version-dexed.jar")
            val finalJar = project.file("build/libs/$modName.jar")

            if (dexedJar.exists()) {
                dexedJar.renameTo(finalJar)
            }
        }
    }

    build {
        dependsOn(mergeJar)
    }
}