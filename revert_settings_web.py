with open('/tmp/Admin-web/src/pages/Settings.tsx', 'r') as f:
    content = f.read()

comm_row = """
          {user?.role === 'ADMIN' && (
            <SettingRow 
               icon={Percent} 
               title="نسبة العمولة" 
               subtitle={`النسبة الحالية: ${commission}%`}
               onClick={() => {
                 setCommInput(commission.toString());
                 setShowCommModal(true);
               }}
            />
          )}
"""

if comm_row in content:
    content = content.replace(comm_row, "")

with open('/tmp/Admin-web/src/pages/Settings.tsx', 'w') as f:
    f.write(content)
