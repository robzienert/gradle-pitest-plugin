/* Copyright (c) 2012 Marcin Zajączkowski
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.solidsoft.gradle.pitest

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet
import org.gradle.api.internal.tasks.DefaultSourceSet
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.tasks.TaskInstantiationException
import com.google.common.annotations.VisibleForTesting

/**
 * The main class for Pitest plugin.
 */
class PitestPlugin implements Plugin<Project> {
    final static DEFAULT_PITEST_VERSION = '0.29'
    final static PITEST_TASK_GROUP = "Report"
    final static PITEST_TASK_NAME = "pitest"
    final static PITEST_CONFIGURATION_NAME = 'pitest'

    @VisibleForTesting
    final static String PIT_HISTORY_DEFAULT_FILE_NAME = 'pitHistory.txt'

    Project project
    PitestPluginExtension extension

    void apply(Project project) {
        this.project = project
        verifyThatRequiredPluginsWereAlreadyApplied()
        createConfigurations()
        createExtension(project)
        project.plugins.withType(JavaBasePlugin) {
            PitestTask task = project.tasks.add(PITEST_TASK_NAME, PitestTask)
            task.with {
                description = "Run Pitest analysis for java classes"
                group = PITEST_TASK_GROUP
            }
            //TODO: MZA: Is it used internally by Gradle?
            task.setSource(project.sourceSets.main.java.srcDirs)
            task.dependsOn("cleanTest", "test")
            configureTaskDefault(task)
        }
    }

    private void verifyThatRequiredPluginsWereAlreadyApplied() {
        //MZA: Pitest currently works only with Java project and it is required to applied Java plugin first.
        // This clarify the error message (is better than "missing sourceSets property")
        if (!project.plugins.hasPlugin("java")) {
            throw new TaskInstantiationException("Java plugin has to be applied before Pitest plugin")
        }
    }

    private void createConfigurations() {
        project.configurations.add(PITEST_CONFIGURATION_NAME).with {
            visible = false
            transitive = true
            description = "The Pitest libraries to be used for this project."
            //TODO: MZA:excludes?
        }
    }

    //TODO: MZA: Maybe move it to the constructor of an extension class?
    private void createExtension(Project project) {
        extension = project.extensions.create("pitest", PitestPluginExtension)
        //TODO: MZA: Set target classed based on project group and name?
//        extension.targetClasses = ...
        extension.reportDir = new File("${project.reporting.baseDir.path}/pitest")
        extension.sourceDirsAsFiles = project.sourceSets.main.java.srcDirs
        extension.pitestVersion = DEFAULT_PITEST_VERSION
    }

    private void configureTaskDefault(PitestTask task) {
        task.conventionMapping.with {
            //TODO: MZA: Setting pitestClasspath is not needed, but there was a problem with too early resolving
            // $extension.pitestVersion when not put into closure
            pitestClasspath = {
                def config = project.configurations[PITEST_CONFIGURATION_NAME]
                if (config.dependencies.empty) {
                    project.dependencies {
                        println "pitestVersion $extension.pitestVersion"
                        pitest "org.pitest:pitest:$extension.pitestVersion"
                    }
                }
                config
            }
            taskClasspath = {
                List<FileCollection> testRuntimeClasspath = project.sourceSets.findAll { SourceSet sourceSet ->
                    //MZA: Test classpath already contains main classpath - maybe it can be simplified
                    sourceSet.getName() == DefaultSourceSet.TEST_SOURCE_SET_NAME
                }*.runtimeClasspath
                FileCollection taskClasspath = new UnionFileCollection(testRuntimeClasspath)
                taskClasspath += project.configurations[PITEST_CONFIGURATION_NAME]
                taskClasspath
            }
            mutableCodePaths = {
                Set<File> mainClassesOutputDirs = project.sourceSets.findAll { SourceSet sourceSet ->
                    sourceSet.getName() == DefaultSourceSet.MAIN_SOURCE_SET_NAME
                }*.output.classesDir
                mainClassesOutputDirs
            }
            sourceDirs = { extension.sourceDirs }

            reportDir = { extension.reportDir }
            targetClasses = { extension.targetClasses }
            targetTests = { extension.targetTests }
            dependencyDistance = { extension.dependencyDistance }
            threads = { extension.threads }
            mutateStaticInits = { extension.mutateStaticInits }
            includeJarFiles = { extension.includeJarFiles }
            mutators = { extension.mutators }
            excludedMethods = { extension.excludedMethods }
            excludedClasses = { extension.excludedClasses }
            avoidCallsTo = { extension.avoidCallsTo }
            verbose = { extension.verbose }
            timeoutFactor = { extension.timeoutFactor }
            timeoutConstInMillis = { extension.timeoutConstInMillis }
            maxMutationsPerClass = { extension.maxMutationsPerClass }
            jvmArgs = { extension.jvmArgs }
            outputFormats = { extension.outputFormats }
            failWhenNoMutations = { extension.failWhenNoMutations }
            includedTestNGGroups = { extension.includedTestNGGroups }
            excludedTestNGGroups = { extension.excludedTestNGGroups }
            configFile = { extension.configFile }
            detectInlinedCode = { extension.detectInlinedCode }
            timestampedReports = { extension.timestampedReports }
            historyInputLocation = { extension.historyInputLocation }
            historyOutputLocation = { extension.historyOutputLocation }
            enableDefaultIncrementalAnalysis = { extension.enableDefaultIncrementalAnalysis }
            defaultFileForHistoryDate = { new File(project.buildDir, PIT_HISTORY_DEFAULT_FILE_NAME) }
        }
    }
}
