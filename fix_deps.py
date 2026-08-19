with open("/app/applet/app/build.gradle.kts", "r", encoding="utf-8") as f:
    content = f.read()

if "kotlinx-coroutines-play-services" not in content:
    content = content.replace("implementation(libs.kotlinx.coroutines.core)", "implementation(libs.kotlinx.coroutines.core)\n  implementation(\"org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3\")")

with open("/app/applet/app/build.gradle.kts", "w", encoding="utf-8") as f:
    f.write(content)

print("Deps fixed")
