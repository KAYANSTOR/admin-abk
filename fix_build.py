with open("/app/applet/app/build.gradle.kts", "r", encoding="utf-8") as f:
    content = f.read()

if "firebase.messaging" not in content:
    content = content.replace(
        "implementation(libs.firebase.firestore)",
        "implementation(libs.firebase.firestore)\n  implementation(\"com.google.firebase:firebase-messaging-ktx\")"
    )

with open("/app/applet/app/build.gradle.kts", "w", encoding="utf-8") as f:
    f.write(content)
