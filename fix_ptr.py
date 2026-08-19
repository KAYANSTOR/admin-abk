import re
filepath = "/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Replace the manual pull to refresh logic with PullToRefreshBox
old_block = """    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val pullRefreshState = rememberPullToRefreshState()
        
        if (pullRefreshState.isRefreshing) {
            LaunchedEffect(true) {
                viewModel.refresh()
            }
        }
        LaunchedEffect(isRefreshing) {
            if (!isRefreshing) {
                pullRefreshState.endRefresh()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {"""

new_block = """    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {"""

content = content.replace(old_block, new_block)

old_end = """            
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color.White,
                contentColor = TealGradientStart
            )
        }
    }
}"""

new_end = """        }
    }
}"""

content = content.replace(old_end, new_end)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

