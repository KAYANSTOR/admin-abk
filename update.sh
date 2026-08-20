sed -i '/package com.example.ui/d' fix_dashboard_metrics.kt
sed -i '/import /d' fix_dashboard_metrics.kt

awk '/class DashboardViewModel : ViewModel\(\) \{/{flag=1; next} /^@Composable/{if(flag){flag=0; system("cat fix_dashboard_metrics.kt"); print}} !flag' /app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt > temp.kt && mv temp.kt /app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt

# Wait, the previous awk already replaced the whole block! 
# If I run it again, it will append again. Let's reset the file from git!
git checkout /app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt
