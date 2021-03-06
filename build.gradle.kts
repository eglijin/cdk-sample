import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.4.31"
}

group = "org.example"
version = "1.0-SNAPSHOT"


application {
    mainClass.set("ap.Application")
}

repositories {
    mavenCentral()
}

val ktorVersion = "1.5.2"
val cdkVersion = "1.111.0"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

    implementation("software.amazon.awscdk:core:$cdkVersion")
    implementation("software.amazon.awscdk:apigateway:$cdkVersion")
    implementation("software.amazon.awscdk:lambda:$cdkVersion")
    implementation("software.amazon.awscdk:cognito:$cdkVersion")
    implementation("software.amazon.awscdk:s3:$cdkVersion")
    implementation("software.amazon.awscdk:events-targets:$cdkVersion")
    implementation("software.amazon.awscdk:lambda-event-sources:$cdkVersion")
    implementation("software.amazon.awscdk:iam:$cdkVersion")
    implementation("software.amazon.awscdk:route53:$cdkVersion")

    implementation("com.amazonaws:aws-lambda-java-runtime-interface-client:1.0.0")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation("com.amazonaws:aws-lambda-java-events:3.2.0")

    testImplementation("io.mockk:mockk:1.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("io.ktor:ktor-client-core:$ktorVersion")
    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")

}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
    withType<KotlinCompile> {
        kotlinOptions {
            useIR = true
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    clean {
        delete += setOf("build", "cdk.out", ".aws-sam")
    }
    register<Copy>("copyRuntimeDependencies") {
        from(configurations.runtimeClasspath)
        into("$buildDir/lambda/var/task/lib/")
    }
    register<Copy>("copyLambdaFunction") {
        dependsOn("jar")
        from("$buildDir/classes/kotlin/main")
        into("$buildDir/lambda/var/task/")
    }
    register<Copy>("copyDockerFile") {
        from("$rootDir/Dockerfile")
        into("$buildDir/lambda")
    }
    register<Copy>("copyResources") {
        from("$buildDir/resources")
        into("$buildDir/lambda/resources")
    }
    getByName("build").dependsOn(
        "copyResources",
        "copyLambdaFunction",
        "copyRuntimeDependencies",
        "copyDockerFile"
    )
}