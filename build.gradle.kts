import com.xpdustry.toxopid.extension.anukeJitpack
import com.xpdustry.toxopid.extension.anukeZelaux
import com.xpdustry.toxopid.spec.ModMetadata
import com.xpdustry.toxopid.spec.ModPlatform

plugins {
    java
    id("com.xpdustry.toxopid") version "4.2.0"
}

val metadata = ModMetadata.fromJson(project.file("mod.json"))
version = metadata.version
val isRelease = project.hasProperty("release")

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
    implementation("org.xcore:xcore-protocol-java:0.12.0")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(toxopid.dependencies.mindustryCore)
    testImplementation(toxopid.dependencies.arcCore)
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
        if (isRelease) {
            exclude("admintools/dev/**")
        }
    }

    named<JavaExec>("runMindustryDesktop") {
        listOf("screen", "mobile", "mobile.landscape", "screenshot", "width", "height", "delay", "exit", "dev").forEach { key ->
            val prop = "admintools.$key"
            if (System.getProperty(prop) != null) {
                systemProperty(prop, System.getProperty(prop))
            } else if (project.hasProperty(key)) {
                systemProperty(prop, project.property(key).toString())
            }
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

tasks.withType<Test>().configureEach { useJUnitPlatform() }
