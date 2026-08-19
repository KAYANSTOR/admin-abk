import re

filepath = "/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Add isRefreshing state and refresh() method to ViewModel
viewModel_add = '''
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            updateDate()
            try {
                // Fetch directly from server to satisfy manual refresh requirement
                val clientsSnapshot = db.collection("clients").get(com.google.firebase.firestore.Source.SERVER).kotlinx.coroutines.tasks.await()
                val activeCount = clientsSnapshot.documents.count { it.getBoolean("isActive") == true }
                val trialCount = clientsSnapshot.documents.count { it.getBoolean("isActive") == false }
                _metrics.update { it.copy(accountsCount = activeCount.toString(), trialAccountsCount = trialCount.toString()) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            kotlinx.coroutines.delay(1000) // visual feedback
            _isRefreshing.value = false
        }
    }
'''

content = re.sub(
    r'(init\s*\{.*?\})',
    r'\1\n' + viewModel_add,
    content,
    flags=re.DOTALL
)

# Replace DashboardScreen signature and body to add PullToRefreshBox
screen_pattern = r'(@Composable\s*fun\s*DashboardScreen\s*\([^)]*\)\s*\{)(.*?)(val\s*metrics\s*=\s*viewModel\.metrics\.collectAsState\(\)\.value)'
screen_replace = r'''@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
\1
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    \3

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() }
    ) {
'''

content = re.sub(screen_pattern, screen_replace, content, flags=re.DOTALL)

# Add closing brace for PullToRefreshBox at the end of the DashboardScreen composable
# The DashboardScreen composable ends before `fun HeaderSection`
content = re.sub(
    r'(LazyColumn\(\s*modifier = Modifier\..*?}\n\s*})',
    r'\1\n    }',
    content,
    flags=re.DOTALL
)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
print("Dashboard updated successfully.")
