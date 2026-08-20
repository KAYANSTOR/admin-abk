sed -i 's/@Composable//g' /app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/fun DashboardScreen/@Composable\nfun DashboardScreen/g' /app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/fun DashboardHeader/@Composable\nfun DashboardHeader/g' /app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/fun HeroRevenueCard/@Composable\nfun HeroRevenueCard/g' /app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/fun KpiCard/@Composable\nfun KpiCard/g' /app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/fun QuickActionCard/@Composable\nfun QuickActionCard/g' /app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/fun LatestClientsSection/@Composable\nfun LatestClientsSection/g' /app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/fun SalesOverviewSection/@Composable\nfun SalesOverviewSection/g' /app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/fun SalesCardMin/@Composable\nfun SalesCardMin/g' /app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt

cat << 'INNEREOF' > add_refresh.py
with open('/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    content = f.read()

refresh_fun = """
    fun refresh() {
        _isRefreshing.value = true
        fetchMetrics()
        fetchLatestClients()
        // Simulate network delay
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            _isRefreshing.value = false
        }
    }
"""

if "fun refresh()" not in content:
    content = content.replace("private fun fetchLatestClients() {", refresh_fun + "\n    private fun fetchLatestClients() {")
    with open('/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as out:
        out.write(content)

INNEREOF
python3 add_refresh.py
