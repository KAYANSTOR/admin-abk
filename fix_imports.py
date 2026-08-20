import re
with open('/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Find the last occurrence of 'data class DashboardMetrics'
metrics_idx = content.find('data class DashboardMetrics')

# Keep the content until the end of DashboardMetrics
if metrics_idx != -1:
    end_of_metrics = content.find(')', metrics_idx) + 1
    
    # We want to keep everything up to here.
    part1 = content[:end_of_metrics]
    
    # The rest has duplicate imports and package. We need to find 'class DashboardViewModel'
    class_idx = content.find('class DashboardViewModel')
    if class_idx != -1:
        part2 = content[class_idx:]
        
        # Merge
        new_content = part1 + '\n\n' + part2
        
        with open('/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as out:
            out.write(new_content)
