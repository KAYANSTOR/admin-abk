import re

filepath = "/app/applet/app/src/main/AndroidManifest.xml"
with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

permissions = """
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
"""

service = """
        <service
            android:name="com.example.ui.MyFirebaseMessagingService"
            android:exported="true">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
"""

if "<uses-permission" not in content:
    content = content.replace("<application", permissions + "\n    <application")

if "MyFirebaseMessagingService" not in content:
    content = content.replace("</application>", service + "\n    </application>")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

