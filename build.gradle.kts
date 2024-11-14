import com.xpdustry.toxopid.spec.ModPlatform
import com.xpdustry.toxopid.extension.anukeZelaux
import com.xpdustry.toxopid.extension.anukeJitpack
import com.xpdustry.toxopid.spec.ModMetadata

plugins {
    java
    id("com.xpdustry.toxopid") version "4.1.0"
}

project.version  = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    anukeJitpack()
    anukeZelaux()
}

val metadata = ModMetadata.fromJson(project.file("mod.json"))
version = metadata.version

val jabelVersion = "93fde537c7"

toxopid {
    compileVersion.set("v${metadata.minGameVersion}")
    platforms.set(setOf(ModPlatform.ANDROID, ModPlatform.DESKTOP))
}

allprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("--release", "8"))
    }
}

dependencies {
    compileOnly(toxopid.dependencies.mindustryCore)
    compileOnly(toxopid.dependencies.arcCore)

    annotationProcessor("com.github.Anuken:jabel:$jabelVersion")
}

//tasks.runMindustryDesktop {
//    mods.from(project.file("./libs/AdminTools-${version}.jar"))
//}

tasks {
    withType<Jar> {
        from(rootDir) {
            include("mod.json")
        }

        doLast {
            val name = project.rootDir.name
            project.file("build/libs/$name-$version-dexed.jar").renameTo(project.file("build/libs/$name.jar"))
        }
    }
}


tasks.build {
    dependsOn(tasks.mergeJar)
}