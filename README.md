# Gradle plugin for PIT Mutation Testing

The plugin provides an ability to perform a [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) and
calculate a mutation coverage of a [Gradle](http://gradle.org/)-based project with [PIT](http://pitest.org/).

## Quick start

Add gradle-pitest-plugin and pitest itself to the buildscript dependencies in your build.gradle file:

    buildscript {
        repositories {
            mavenLocal()
            mavenCentral()
            //Needed only for Pitest SNAPSHOT versions
            maven { url "http://oss.sonatype.org/content/repositories/snapshots/" }
        }
        dependencies {
            classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:0.28.0-SNAPSHOT'
            classpath "org.pitest:pitest:0.28-SNAPSHOT"
        }
    }

Add a pitest configuration closure:

    pitest {
        targetClasses = ['our.base.package.*']
        pitestVersion = "0.28-SNAPSHOT" //not needed when a default PIT version should be used
    }

Call Gradle with pitest task:

    gradle pitest

After the measurements a report created by PIT will be placed in ${PROJECT_DIR}/build/reports/pitest directory.

## Plugin configuration

The Pitest plugin has to be configured. All the [command line options](http://pitest.org/quickstart/commandline/) are
supported. To make life easier taskClasspath, mutableCodePaths, sourceDirs, reportDir and pitestVersion are
automatically set by a plugin. In addition sourceDirs, reportDir and pitestVersion can be overridden by an user.

There is one mandatory parameter - targetClasses - which points to the classes which should be mutated. In case of using
not default PIT version the pitestVersion parameter should be used to override it.

The configuration in Gradle is the real Groovy code which makes all assignments very intuitive. All values expected by
PIT should be passed as a corresponding types. There is only one important difference. For the parameters where PIT expects
a coma separated list of strings in a Gradle configuration a list of strings should be used (see outputFormats in the 
following example).

    pitest {
        targetClasses = ['our.base.package.*']
        pitestVersion = "0.28-SNAPSHOT" //not needed when a default PIT version should be used
        threads = 4
        outputFormats = ['XML', 'HTML']
    }

Check PIT documentation for a [list](http://pitest.org/quickstart/commandline/) of all available command line parameters.


## Versions

Every gradle-pitest-plugin version by default uses a predefined PIT version. Usually this a the latest released version
of PIT available at the time of releasing a plugin version. It can be overridden by using pitestVersion parameter
in a pitest configuration closure *and* specifying *the same* version as a buildscript dependency.

Note. There could be some issues when using different PIT versions.

gradle-pitest-plugin 0.28.0-SNAPSHOT uses pitest-0.28-SNAPSHOT.

Note. 0.27 is not supported due to [issue 47](https://code.google.com/p/pitestrunner/issues/detail?id=47).

gradle-pitest-plugin 0.28.0 was tested with Gradle 1.0.

## Known issues

There are a few known issues:
 - java.lang.VerifyError: Expecting a stackmap frame when using with Java 7
 - too verbose output from PIT

## Support

gradle-pitest-plugin was written by Marcin Zajączkowski. The author can be contacted directly via email: mszpak ATT wp DOTT pl.
There is also Marcin's blog available: [Solid Soft](http://blog.solidsoft.info) - working code is not enough.

The plugin surely has some bugs and missing features. They can be reported using an [issue tracker](https://github.com/szpak/gradle-pitest-plugin/issues).
However it is often a better idea to send a questions to the [PIT mailing list](http://groups.google.com/group/pitusers).

The plugin is licensed under the terms of the Apache License, Version 2.0.