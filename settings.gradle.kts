pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

        // در صورت نیاز به مخزن ایران‌سرور
        maven("https://maven.iranserver.com/maven2")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // در صورت نیاز به مخزن ایران‌سرور
        maven("https://maven.iranserver.com/maven2")
    }
}

rootProject.name = "khoshhesab"
include(":app")