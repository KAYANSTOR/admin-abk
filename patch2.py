with open('/tmp/Admin-web/src/pages/Clients.tsx', 'r') as f:
    content = f.read()

input_field = """
              <div>
                <input
                  type="number"
                  value={newCommission}
                  onChange={(e) => setNewCommission(e.target.value)}
                  className="w-full p-3.5 bg-app-bg border border-gray-200 rounded-xl outline-none focus:border-primary text-right text-[14px]"
                  placeholder="نسبة العمولة الخاصة بالعميل (%)"
                />
              </div>
"""

content = content.replace(
    '<div className="flex gap-3 pt-4">',
    input_field + '              <div className="flex gap-3 pt-4">'
)

with open('/tmp/Admin-web/src/pages/Clients.tsx', 'w') as f:
    f.write(content)
