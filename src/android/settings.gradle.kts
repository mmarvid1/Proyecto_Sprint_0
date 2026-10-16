// ===========================================================================
//  FICHERO   : settings.gradle.kts
//  PROPOSITO : Configuracion de Gradle: repositorios y modulos del proyecto.
//  AUTOR     : Minerva Maravilla Vidaurre
//  FECHA     : 05/10/2026
// ===========================================================================
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "android"
include(":app")
 