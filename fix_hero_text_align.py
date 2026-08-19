import re
filepath = "/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Make the hero text align to the RIGHT matching RTL layout
hero_replacement = """@Composable
fun HeroRevenueCard(amount: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(TealGradientStart, PurpleGradientEnd)))
    ) {
        // Chart Background on the left
        Canvas(modifier = Modifier.fillMaxWidth(0.55f).fillMaxHeight().padding(top = 40.dp).align(Alignment.CenterStart)) {
            val path = Path()
            val points = listOf(
                Offset(0f, size.height * 0.8f),
                Offset(size.width * 0.15f, size.height * 0.6f),
                Offset(size.width * 0.3f, size.height * 0.5f),
                Offset(size.width * 0.45f, size.height * 0.7f),
                Offset(size.width * 0.6f, size.height * 0.3f),
                Offset(size.width * 0.75f, size.height * 0.4f),
                Offset(size.width * 0.9f, size.height * 0.1f),
                Offset(size.width, size.height * 0.0f)
            )
            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
            drawPath(path, Color.White.copy(alpha = 0.5f), style = Stroke(width = 4f))
            
            points.forEach { point ->
                drawCircle(Color.White, radius = 6f, center = point)
            }
        }

        // Texts on the right
        Column(modifier = Modifier.padding(24.dp).align(Alignment.TopEnd), horizontalAlignment = Alignment.End) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.background(Color.White.copy(alpha=0.2f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("هذا الشهر", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text("إجمالي الإيرادات", color = Color.White, fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(amount, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text("ري", color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.background(GreenIcon.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = GreenIcon, modifier = Modifier.size(12.dp))
                        Text("12.4%", color = GreenIcon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text("مقارنة بالشهر الماضي", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}"""
content = re.sub(r'@Composable\s*fun HeroRevenueCard.*?\}\s*\}', hero_replacement, content, flags=re.DOTALL)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
