plugins {
    kotlin("js") version "1.9.21"
}

group = "com.ieum"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    
    // Kotlin/JS React Wrapper (간단 버전)
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.668")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.668")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.7.3")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
            binaries.executable()
            
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
}
