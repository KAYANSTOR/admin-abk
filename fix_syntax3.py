import re
filepath = "/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Fix the duplicate QuickActionCard blocks and brackets
bad_block = """        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(Modifier.weight(1f), "اشتراك جديد", Icons.Default.CreditCard, TealGradientStart, onClick = { navController?.navigate("subscriptions") })
            QuickActionCard(Modifier.weight(1f), "إضافة جهاز", Icons.Default.PhoneAndroid, TealGradientStart, onClick = { navController?.navigate("serials") })
            QuickActionCard(Modifier.weight(1f), "إضافة عميل", Icons.Default.PersonAdd, TealGradientStart, onClick = { navController?.navigate("clients") })
            QuickActionCard(Modifier.weight(1f), "إصدار ترخيص", Icons.Default.VpnKey, TealGradientStart, onClick = { navController?.navigate("licenses") })
        })
            QuickActionCard(Modifier.weight(1f), "إضافة عميل", Icons.Default.PersonAdd, TealGradientStart, onClick = { navController?.navigate("clients") })
            QuickActionCard(Modifier.weight(1f), "إضافة جهاز", Icons.Default.PhoneAndroid, TealGradientStart, onClick = { navController?.navigate("serials") })
            QuickActionCard(Modifier.weight(1f), "اشتراك جديد", Icons.Default.CardMembership, TealGradientStart, onClick = { navController?.navigate("subscriptions") })
        }
    }
}"""

good_block = """        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(Modifier.weight(1f), "اشتراك جديد", Icons.Default.CreditCard, TealGradientStart, onClick = { navController?.navigate("subscriptions") })
            QuickActionCard(Modifier.weight(1f), "إضافة جهاز", Icons.Default.PhoneAndroid, TealGradientStart, onClick = { navController?.navigate("serials") })
            QuickActionCard(Modifier.weight(1f), "إضافة عميل", Icons.Default.PersonAdd, TealGradientStart, onClick = { navController?.navigate("clients") })
            QuickActionCard(Modifier.weight(1f), "إصدار ترخيص", Icons.Default.VpnKey, TealGradientStart, onClick = { navController?.navigate("licenses") })
        }
    }
}"""

content = content.replace(bad_block, good_block)

# Remove that extra brace before @Composable fun SalesCardMin
content = content.replace("    }\n}\n\n@Composable\nfun SalesCardMin", "    }\n}\n\n@Composable\nfun SalesCardMin")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
