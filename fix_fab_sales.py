import re
filepath = "/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Make the SalesOverviewSection have the large teal FAB on the bottom edge matching the image
sales_new = """@Composable
fun SalesOverviewSection(metrics: DashboardMetrics, navController: NavController?) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("عرض الكل", color = TealGradientStart, fontSize = 14.sp, modifier = Modifier.clickable { navController?.navigate("sales") })
                Text("نظرة سريعة على المبيعات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryDark)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SalesCardMin(Modifier.weight(1f), "مبيعات اليوم", metrics.todaySalesValue)
                SalesCardMin(Modifier.weight(1f), "مبيعات الشهر", metrics.monthSalesValue)
            }
        }
        
        // Teal FAB overlapping the "مبيعات اليوم" card
        FloatingActionButton(
            onClick = { navController?.navigate("sales") },
            containerColor = TealGradientStart,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 12.dp, y = 20.dp)
                .size(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Sales")
        }
    }
}"""
content = re.sub(r'@Composable\s*fun SalesOverviewSection.*?\}\s*\}', sales_new, content, flags=re.DOTALL)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
