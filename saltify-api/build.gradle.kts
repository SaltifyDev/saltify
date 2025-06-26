plugins {
    id("buildsrc.convention.kotlin-jvm")
    `java-library`
    `maven-publish`
}

dependencies {
    api(libs.kotlinxDatetime)
    api(libs.kotlinxCoroutines)
    api(libs.ktorm)
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "org.ntqqrev.saltify"
            version = "0.1.0"
            artifactId = "saltify-api"

            pom {
                name = "saltify-api"
                description = "API for Saltify plugins"
                url = "https://github.com/SaltifyDev/saltify"
                inceptionYear = "2025"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "Wesley-Young"
                        name = "Wesley F. Young"
                        email = "wesley.f.young@outlook.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/SaltifyDev/saltify.git"
                    developerConnection = "scm:git:ssh://github.com/SaltifyDev/saltify.git"
                    url = "https://github.com/SaltifyDev/saltify"
                }
            }

            from(components["java"])
        }
    }

    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}