pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Add repository here, e.g.
         maven {
            url = uri("https://salesforce-marketingcloud.github.io/MarketingCloudSDK-Android/repository")
         }
    }
}

rootProject.name = "MarketingCloud"
include(":marketingcloud")
