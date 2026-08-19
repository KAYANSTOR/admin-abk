import re

filepath = "/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Replace QuickActions with proper order matching RTL image
quick_actions_replacement = """        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(Modifier.weight(1f), "اشتراك جديد", Icons.Default.CreditCard, TealGradientStart, onClick = { navController?.navigate("subscriptions") })
            QuickActionCard(Modifier.weight(1f), "إضافة جهاز", Icons.Default.PhoneAndroid, TealGradientStart, onClick = { navController?.navigate("serials") })
            QuickActionCard(Modifier.weight(1f), "إضافة عميل", Icons.Default.PersonAdd, TealGradientStart, onClick = { navController?.navigate("clients") })
            QuickActionCard(Modifier.weight(1f), "إصدار ترخيص", Icons.Default.VpnKey, TealGradientStart, onClick = { navController?.navigate("licenses") })
        }"""
        
content = re.sub(r'Row\(modifier = Modifier.fillMaxWidth\(\), horizontalArrangement = Arrangement.spacedBy\(12.dp\)\) \{.*?\}', quick_actions_replacement, content, flags=re.DOTALL, count=1)

# Replace Sales section
sales_replacement = """        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SalesCardMin(Modifier.weight(1f), "مبيعات اليوم", metrics.todaySalesValue)
            SalesCardMin(Modifier.weight(1f), "مبيعات الشهر", metrics.monthSalesValue)
        }"""
content = re.sub(r'Row\(modifier = Modifier.fillMaxWidth\(\), horizontalArrangement = Arrangement.spacedBy\(16.dp\)\) \{.*?\}', sales_replacement, content, flags=re.DOTALL, count=1)

# Ensure Date and Header layout perfectly match
header_replacement = """@Composable
fun DashboardHeader(userName: String, date: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TealGradientStart, modifier = Modifier.size(16.dp))
                Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = PrimaryDark)
                    Box(modifier = Modifier.align(Alignment.TopEnd).offset((-2).dp, 2.dp).size(18.dp).clip(CircleShape).background(Color.Red), contentAlignment = Alignment.Center) {
                        Text("3", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryDark)
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text("مرحباً بك", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TealGradientStart)
                Text(userName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray)) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(8.dp), tint = Color.DarkGray)
            }
        }
    }
}"""
content = re.sub(r'@Composable\s*fun DashboardHeader.*?\}\s*\}', header_replacement, content, flags=re.DOTALL)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
