import re

with open('/tmp/Admin-web/src/pages/Clients.tsx', 'r') as f:
    content = f.read()

# Add state for commission
if "const [newCommission, setNewCommission]" not in content:
    content = content.replace(
        "const [newStore, setNewStore] = useState('');",
        "const [newStore, setNewStore] = useState('');\n  const [newCommission, setNewCommission] = useState('');"
    )

# Handle Create Client
if "commissionPercentage:" not in content:
    content = content.replace(
        "deviceLimit: 3",
        "deviceLimit: 3,\n        commissionPercentage: parseFloat(newCommission) || 0"
    )
    content = content.replace(
        "setNewName(''); setNewPhone(''); setNewStore('');",
        "setNewName(''); setNewPhone(''); setNewStore(''); setNewCommission('');"
    )

# Wait, is there an edit client feature in Web? Let's check if there is an Edit Modal.
# I'll just add the input field to Add Modal for now.
input_field = """
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-2">نسبة العمولة الخاصة بالعميل (%)</label>
                <input
                  type="number"
                  value={newCommission}
                  onChange={(e) => setNewCommission(e.target.value)}
                  className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 text-primary-dark font-bold focus:outline-none focus:ring-2 focus:ring-primary/20"
                  placeholder="مثال: 15"
                />
              </div>
"""
if "نسبة العمولة الخاصة بالعميل" not in content:
    content = content.replace(
        """              <button\n                type="submit"\n                className="w-full""",
        input_field + """              <button\n                type="submit"\n                className="w-full"""
    )

with open('/tmp/Admin-web/src/pages/Clients.tsx', 'w') as f:
    f.write(content)
