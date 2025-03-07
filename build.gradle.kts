@file:Suppress("SpellCheckingInspection")

import groovy.util.Node
import groovy.util.NodeList
import org.apache.tools.ant.filters.ReplaceTokens
import org.sonatype.gradle.plugins.scan.ossindex.OutputFormat
import java.util.*

plugins {
    id("java")
    jacoco
    checkstyle
    signing
    `maven-publish`
    alias(libs.plugins.lombok)
    alias(libs.plugins.versioner)
    alias(libs.plugins.index.scan)
    alias(libs.plugins.owasp.dependencycheck)
    alias(libs.plugins.cyclonedx.bom)
    alias(libs.plugins.licensee.plugin)
    alias(libs.plugins.nexus.publish.plugin)
}

group = "com.github.nagyesta"

apply("config/ossindex/ossIndexAudit.gradle.kts")

buildscript {
    fun optionalPropertyString(name: String): String {
        return if (project.hasProperty(name)) {
            project.property(name) as String
        } else {
            ""
        }
    }

    fun dockerAbortGroups(name: String): String {
        return if (project.hasProperty(name)) {
            "all"
        } else {
            ""
        }
    }

    extra.apply {
        set("gitToken", optionalPropertyString("githubToken"))
        set("gitUser", optionalPropertyString("githubUser"))
        set("ossrhUser", optionalPropertyString("ossrhUsername"))
        set("ossrhPass", optionalPropertyString("ossrhPassword"))
        set("ossIndexUser", optionalPropertyString("ossIndexUsername"))
        set("ossIndexPass", optionalPropertyString("ossIndexPassword"))
        set("artifactDisplayName", "Cache-Only")
        set(
            "artifactDescription",
            "Cache-Only is a minimal library augmenting the Spring cache abstraction with a way for caching bulk API calls."
        )
        set("repoUrl", "https://github.com/nagyesta/cache-only")
        set("licenseName", "MIT License")
        set("licenseUrl", "https://raw.githubusercontent.com/nagyesta/cache-only/main/LICENSE")
        set("maintainerId", "nagyesta")
        set("maintainerName", "Istvan Zoltan Nagy")
        set("maintainerUrl", "https://github.com/nagyesta/")
        set("scmConnection", "scm:git:https://github.com/nagyesta/cache-only.git")
        set("scmProjectUrl", "https://github.com/nagyesta/cache-only/")
        set("githubMavenRepoUrl", "https://maven.pkg.github.com/nagyesta/cache-only")
        set("ossrhMavenRepoUrl", "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    }
}

versioner {
    startFrom {
        major = 0
        minor = 0
        patch = 1
    }
    match {
        major = "{major}"
        minor = "{minor}"
        patch = "{patch}"
    }
    pattern {
        pattern = "%M.%m.%p"
    }
    git {
        authentication {
            https {
                token = project.extra.get("gitToken").toString()
            }
        }
    }
    tag {
        prefix = "v"
        useCommitMessage = true
    }
}

versioner.apply()

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(libs.lombok)
    compileOnly(libs.jetbrains.annotations)
    testCompileOnly(libs.jetbrains.annotations)
    implementation(libs.slf4j.api)
    implementation(libs.commons.collections4)
    implementation(libs.spring.context.support)
    testImplementation(libs.jupiter.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.mockito.core)
    testImplementation(libs.spring.test)
    testImplementation(libs.spring.context.support)
    testImplementation(libs.logback.classic)
}

licensee {
    allow("Apache-2.0")
    allow("MIT")
    allowUrl("https://opensource.org/license/mit")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withJavadocJar()
    withSourcesJar()
}

val copyLegalDocs = tasks.register<Copy>("copyLegalDocs") {
    from(file("${project.projectDir}/LICENSE"))
    from(layout.buildDirectory.file("reports/licensee/artifacts.json").get().asFile)
    from(layout.buildDirectory.file("reports/bom.json").get().asFile)
    into(layout.buildDirectory.dir("resources/main/META-INF").get().asFile)
    rename("artifacts.json", "dependency-licenses.json")
    rename("bom.json", "SBOM.json")
}.get()
copyLegalDocs.dependsOn(tasks.licensee)
copyLegalDocs.dependsOn(tasks.cyclonedxBom)
tasks.javadoc.get().dependsOn(copyLegalDocs)
tasks.compileJava.get().dependsOn(copyLegalDocs)
tasks.processResources.get().finalizedBy(copyLegalDocs)

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.getByName("jacocoTestReport"))
}

project.tasks.processResources {
    val tokens = mapOf("version" to project.version)
    filesMatching("**/application.properties") {
        filter<ReplaceTokens>("tokens" to tokens)
    }
}

tasks.javadoc.configure {
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:-missing", true)
}

jacoco {
    toolVersion = project.libs.versions.jacoco.get()
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/report.xml"))
        csv.required.set(false)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
    }
    dependsOn(tasks.test)
    finalizedBy(tasks.getByName("jacocoTestCoverageVerification"))
}

tasks.withType<JacocoCoverageVerification>().configureEach {
    inputs.file(layout.buildDirectory.file("reports/jacoco/report.xml"))
    outputs.file(layout.buildDirectory.file("reports/jacoco/jacocoTestCoverageVerification"))

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = BigDecimal.valueOf(0.8)
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = BigDecimal.valueOf(0.8)
            }
            excludes = listOf()
        }
        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = BigDecimal.valueOf(0.5)
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = BigDecimal.valueOf(0.5)
            }
            excludes = mutableListOf(
                "com.github.nagyesta.cacheonly.raw.BatchServiceCaller",
                "com.github.nagyesta.cacheonly.transform.NoOpPartialCacheSupport"
            )
        }
    }
    doLast {
        layout.buildDirectory.file("reports/jacoco/jacocoTestCoverageVerification").get().asFile.writeText("Passed")
    }
}

tasks.jar.configure {
    dependsOn(tasks.check)
}

tasks.withType<Checkstyle>().configureEach {
    configProperties = mutableMapOf<String, Any>(
        "base_dir" to rootDir.absolutePath.toString(),
        "cache_file" to layout.buildDirectory.file("checkstyle/cacheFile").get().asFile.absolutePath.toString()
    )
    checkstyle.toolVersion = project.libs.versions.checkstyle.get()
    checkstyle.configFile = project.file("config/checkstyle/checkstyle.xml")
    reports {
        xml.required.set(false)
        html.required.set(true)
        html.stylesheet = project.resources.text.fromFile("config/checkstyle/checkstyle-stylesheet.xsl")
    }
}

//Disable metadata publishing and rely on Maven only
tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}

ossIndexAudit {
    username = project.extra.get("ossIndexUser").toString()
    password = project.extra.get("ossIndexPass").toString()
    isPrintBanner = false
    isColorEnabled = true
    isShowAll = false
    outputFormat = OutputFormat.DEFAULT
    @Suppress("UNCHECKED_CAST")
    excludeVulnerabilityIds = project.extra.get("ossIndexExclusions") as MutableSet<String>
}

tasks.cyclonedxBom {
    setProjectType("library")
    setIncludeConfigs(listOf("runtimeClasspath"))
    setSkipConfigs(listOf("compileClasspath", "testCompileClasspath"))
    setSkipProjects(listOf())
    setSchemaVersion("1.5")
    setDestination(file("build/reports"))
    setOutputName("bom")
    setOutputFormat("json")
    //noinspection UnnecessaryQualifiedReference
    val attachmentText = org.cyclonedx.model.AttachmentText()
    attachmentText.text = Base64.getEncoder().encodeToString(
        file("${project.project.projectDir}/LICENSE").readBytes()
    )
    attachmentText.encoding = "base64"
    attachmentText.contentType = "text/plain"
    //noinspection UnnecessaryQualifiedReference
    val license = org.cyclonedx.model.License()
    license.name = "MIT License"
    license.setLicenseText(attachmentText)
    license.url = "https://raw.githubusercontent.com/nagyesta/cache-only/main/LICENSE"
    setLicenseChoice {
        it.addLicense(license)
    }
}

checkstyle {
    toolVersion = project.libs.versions.checkstyle.get()
}

nexusPublishing {
    repositories {
        sonatype {
            username = project.extra.get("ossrhUser").toString()
            password = project.extra.get("ossrhPass").toString()
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri(project.extra.get("githubMavenRepoUrl").toString())
            credentials {
                username = project.extra.get("gitUser").toString()
                password = project.extra.get("gitToken").toString()
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = project.name
            pom {
                name.set(project.extra.get("artifactDisplayName").toString())
                description.set(project.extra.get("artifactDescription").toString())
                url.set(project.extra.get("repoUrl").toString())
                packaging = "jar"
                licenses {
                    license {
                        name.set(project.extra.get("licenseName").toString())
                        url.set(project.extra.get("licenseUrl").toString())
                    }
                }
                developers {
                    developer {
                        id.set(project.extra.get("maintainerId").toString())
                        name.set(project.extra.get("maintainerName").toString())
                        email.set(project.extra.get("maintainerUrl").toString())
                    }
                }
                scm {
                    connection.set(project.extra.get("scmConnection").toString())
                    developerConnection.set(project.extra.get("scmConnection").toString())
                    url.set(project.extra.get("scmProjectUrl").toString())
                }
            }
            pom.withXml {
                asNode().apply {
                    (get("dependencies") as NodeList).forEach { depsNode ->
                        ((depsNode as Node).get("dependency") as NodeList).forEach { depNode ->
                            ((depNode as Node).get("scope") as NodeList).forEach { scope ->
                                if (scope is Node && "runtime" == scope.text()) {
                                    scope.setValue("compile")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
