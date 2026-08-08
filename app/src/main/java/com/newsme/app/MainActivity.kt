package com.newsme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsMeApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsMeApp() {
    val navController = rememberNavController()
    var selectedRoute by remember { mutableStateOf("home") }
    var hasNotification by remember { mutableStateOf(true) }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("نيوز مي") },
                    actions = {
                        BadgedBox(
                            badge = {
                                if (hasNotification) Badge()
                            }
                        ) {
                            IconButton(onClick = { 
                                selectedRoute = "notifications"
                                navController.navigate("notifications")
                                hasNotification = false
                            }) {
                                Icon(Icons.Default.Notifications, contentDescription = "اشعارات")
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedRoute == "home",
                        onClick = { selectedRoute = "home"; navController.navigate("home") },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("الرئيسية") }
                    )
                    NavigationBarItem(
                        selected = selectedRoute == "search",
                        onClick = { selectedRoute = "search"; navController.navigate("search") },
                        icon = { Icon(Icons.Default.Search, null) },
                        label = { Text("بحث") }
                    )
                    NavigationBarItem(
                        selected = selectedRoute == "add",
                        onClick = { selectedRoute = "add"; navController.navigate("add") },
                        icon = { Icon(Icons.Default.AddCircle, null) },
                        label = { Text("اضافة") }
                    )
                    NavigationBarItem(
                        selected = selectedRoute == "notifications",
                        onClick = { selectedRoute = "notifications"; navController.navigate("notifications") },
                        icon = { Icon(Icons.Default.Notifications, null) },
                        label = { Text("اشعارات") }
                    )
                    NavigationBarItem(
                        selected = selectedRoute == "settings",
                        onClick = { selectedRoute = "settings"; navController.navigate("settings") },
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("اعدادات") }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(padding)
            ) {
                composable("home") { ScreenBox("الرئيسية - هنا الاخبار هتظهر") }
                composable("search") { ScreenBox("البحث عن الاخبار") }
                composable("add") { ScreenBox("اضافة خبر جديد بصورة وفيديو وصوت") }
                composable("notifications") { ScreenBox("الاشعارات بتوصل اول باول") }
                composable("settings") { ScreenBox("الاعدادات - ليلي ونهاري وعربي وانجليزي") }
            }
        }
    }
}

@Composable
fun ScreenBox(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(24.dp))
    }
}
