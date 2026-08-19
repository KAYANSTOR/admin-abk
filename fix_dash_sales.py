import re

filepath = "/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Replace the block for Subscriptions / Sales
# It starts at `// Subscriptions / Sales (using subscriptions collection for mock sales)`
old_block = '''// Subscriptions / Sales (using subscriptions collection for mock sales)
            db.collection("subscriptions").addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val count = snapshot.documents.size
                // Mock sales data based on count
                val todayVal = count * 1500
                val monthVal = count * 1500 * 30
                _metrics.update { 
                    it.copy(
                        todaySalesCount = "$count اشتراك",
                        todaySalesValue = "$todayVal ر.ي",
                        monthSalesCount = "${count * 30} اشتراك",
                        monthSalesValue = "$monthVal ر.ي"
                    )
                }
            }'''

new_block = '''// Sales Data (Real Backend Logic)
            db.collection("sales").addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                // Assuming SaleTransaction has amount and timestamp (or just using all for now)
                var monthVal = 0L
                var todayVal = 0L
                var monthCount = 0
                var todayCount = 0
                
                for (doc in snapshot.documents) {
                    val amountStr = doc.getString("amount") ?: "0"
                    val amount = amountStr.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                    
                    // Simple mockup for dates since there's no actual field logic standard here yet
                    // But using real collection data instead of fake numbers.
                    monthVal += amount
                    monthCount += 1
                    
                    // Let's pretend half are today for now or if we implement real timestamp check
                    // todayVal += amount 
                }
                
                _metrics.update { 
                    it.copy(
                        monthSalesCount = "$monthCount مبيعات",
                        monthSalesValue = "$monthVal ر.ي",
                        // Will display 0 until real timestamp filtering is added
                        todaySalesCount = "$todayCount مبيعات",
                        todaySalesValue = "$todayVal ر.ي"
                    )
                }
            }'''

content = content.replace(old_block, new_block)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
