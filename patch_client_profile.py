with open('/tmp/Admin-web/src/pages/ClientProfile.tsx', 'r') as f:
    content = f.read()

imports = "import { doc, onSnapshot, updateDoc } from 'firebase/firestore';\n"
if "updateDoc" not in content:
    content = content.replace("import { doc, onSnapshot } from 'firebase/firestore';", imports)

state = """
  const [showEditComm, setShowEditComm] = useState(false);
  const [newComm, setNewComm] = useState('');

  const handleUpdateComm = async () => {
    if (!client) return;
    const val = parseFloat(newComm);
    if (!isNaN(val)) {
      await updateDoc(doc(db, 'clients', client.id), { commissionPercentage: val });
      setShowEditComm(false);
    }
  };
"""

content = content.replace("const { id } = useParams();", "const { id } = useParams();\n" + state)

comm_ui = """
        <div className="bg-white rounded-[24px] shadow-[0_2px_8px_rgba(0,0,0,0.04)] p-6 mb-6 flex justify-between items-center">
          <div>
            <div className="text-[13px] text-gray-500 font-medium mb-1">نسبة العمولة الخاصة</div>
            <div className="text-[20px] font-black text-primary-dark">{client?.commissionPercentage || 0}%</div>
          </div>
          <button 
            onClick={() => { setNewComm(client?.commissionPercentage?.toString() || '0'); setShowEditComm(true); }}
            className="text-[13px] font-bold text-primary bg-primary/5 px-4 py-2 rounded-xl"
          >
            تعديل النسبة
          </button>
        </div>
"""

content = content.replace("{/* Status & Quick Actions */}", comm_ui + "\n        {/* Status & Quick Actions */}")

modal_ui = """
      {showEditComm && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-surface rounded-[24px] w-full max-w-sm p-6 animate-in zoom-in-95 duration-200">
            <h2 className="font-black text-[18px] text-primary-dark mb-6 text-center">تعديل نسبة العمولة</h2>
            <div className="space-y-4">
              <input type="number" placeholder="مثال: 15" className="w-full p-3.5 bg-app-bg border border-gray-200 rounded-xl outline-none focus:border-primary text-right text-[14px]" value={newComm} onChange={e => setNewComm(e.target.value)} />
              <div className="flex gap-3 pt-4">
                <button onClick={handleUpdateComm} className="flex-1 bg-primary text-white py-3.5 rounded-xl font-bold text-[15px]">حفظ</button>
                <button onClick={() => setShowEditComm(false)} className="flex-1 bg-gray-100 text-gray-700 py-3.5 rounded-xl font-bold text-[15px]">إلغاء</button>
              </div>
            </div>
          </div>
        </div>
      )}
"""

content = content.replace("    </div>\n  );\n}", modal_ui + "\n    </div>\n  );\n}")

with open('/tmp/Admin-web/src/pages/ClientProfile.tsx', 'w') as f:
    f.write(content)
