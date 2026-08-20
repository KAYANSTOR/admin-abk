with open('/tmp/Admin-web/src/pages/Settings.tsx', 'r') as f:
    content = f.read()

# Replace imports
imports = "import React, { useState, useEffect } from 'react';\nimport { doc, onSnapshot, setDoc } from 'firebase/firestore';\nimport { db } from '../lib/firebase';\n"
content = content.replace("import React from 'react';", imports)
content = content.replace("Shield, Bell, Lock,   HelpCircle, LogOut, ChevronLeft", "Shield, Bell, Lock,   HelpCircle, LogOut, ChevronLeft, Percent, X")

# Insert states and logic
logic = """
  const [commission, setCommission] = useState(0);
  const [showCommModal, setShowCommModal] = useState(false);
  const [commInput, setCommInput] = useState('');

  useEffect(() => {
    const unsub = onSnapshot(doc(db, 'settings', 'general'), (docSnap) => {
      if (docSnap.exists()) {
        setCommission(docSnap.data().commissionPercentage || 0);
      }
    });
    return () => unsub();
  }, []);

  const saveCommission = async () => {
    const val = parseFloat(commInput);
    if (!isNaN(val)) {
      await setDoc(doc(db, 'settings', 'general'), { commissionPercentage: val }, { merge: true });
      setShowCommModal(false);
    }
  };
"""

content = content.replace("const handleLogout = async () => {", logic + "\n  const handleLogout = async () => {")

# Insert Setting Row for Admin
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
content = content.replace(
    "{user?.role === 'ADMIN' && (\n            <SettingRow \n               icon={Shield}",
    comm_row + "          {user?.role === 'ADMIN' && (\n            <SettingRow \n               icon={Shield}"
)

modal_html = """
      {/* Modal */}
      {showCommModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white w-full max-w-sm rounded-[24px] p-6 shadow-xl animate-fade-in-up">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-black text-primary-dark">تعديل نسبة العمولة</h2>
              <button onClick={() => setShowCommModal(false)} className="p-2 bg-gray-100 rounded-full text-gray-500 hover:bg-gray-200">
                <X className="w-5 h-5" />
              </button>
            </div>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-2">النسبة المئوية (%)</label>
                <input
                  type="number"
                  value={commInput}
                  onChange={(e) => setCommInput(e.target.value)}
                  className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 text-primary-dark font-bold focus:outline-none focus:ring-2 focus:ring-primary/20"
                  placeholder="مثال: 20"
                  dir="ltr"
                />
              </div>
              
              <button
                onClick={saveCommission}
                className="w-full bg-primary text-white font-bold py-3.5 rounded-xl hover:bg-primary-dark transition-colors mt-2"
              >
                حفظ
              </button>
            </div>
          </div>
        </div>
      )}
"""

content = content.replace(
    "    </div>\n  );\n}",
    modal_html + "\n    </div>\n  );\n}"
)

with open('/tmp/Admin-web/src/pages/Settings.tsx', 'w') as f:
    f.write(content)
