plugins {
    id("io.papermc.paperweight.userdev") version "1.7.5"
}
dependencies {
    api(project(":api"))
    api(project(":common"))
    api(project(":minecraft"))
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")

    implementation("io.papermc:paperlib:1.0.7")
}