package com.newsme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NewsMeApp() }
    }
}

@Composable
fun NewsMeApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var authScreen by remember { mutableStateOf("login") }

    MaterialTheme {
        if (!isLoggedIn) {
            when (authScreen) {
                "login" -> LoginScreen({ isLoggedIn = true }, { authScreen = "register" }, { authScreen = "verify" })
                "register" -> RegisterScreen({ authScreen = "verify" }, { authScreen = "login" })
                "verify" -> VerifyScreen({ isLoggedIn = true }, { authScreen = "login" })
            }
        } else {
            MainWithBottomNav { isLoggedIn = false }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLogin: () -> Unit, onGoRegister: () -> Unit, onGoVerify: () -> Unit) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("تسجيل الدخول - نيوز مي") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(24.dp), Arrangement.Center) {
            Text("مرحبا بك", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(phone, { phone = it }, label = { Text("رقم الموبايل") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(password, { password = it }, label = { Text("كلمة السر") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("دخول") }
            TextButton(onClick = onGoRegister, modifier = Modifier.fillMaxWidth()) { Text("انشاء حساب جديد") }
            TextButton(onClick = onGoVerify, modifier = Modifier.fillMaxWidth()) { Text("عندي كود تحقق") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegistered: () -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("انشاء حساب") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(24.dp), Arrangement.Center) {
            OutlinedTextField(name, { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(phone, { phone = it }, label = { Text("رقم الموبايل") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRegistered, modifier = Modifier.fillMaxWidth()) { Text("تسجيل وارسال كود") }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("رجوع") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyScreen(onVerified: () -> Unit, onBack: () -> Unit) {
    var code by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("كود التحقق") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text("ادخل كود التحقق 6 ارقام")
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(code, { if (it.length <= 6) code = it }, label = { Text("الكود") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(Modifier.height(24.dp))
            Button(onClick = onVerified, modifier = Modifier.fillMaxWidth()) { Text("تأكيد") }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("رجوع") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWithBottomNav(onLogout: () -> Unit) {
    val navController = rememberNavController()
    var selectedRoute by remember { mutableStateOf("home") }
    var hasNotification by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("نيوز مي") }, actions = {
                BadgedBox(badge = { if (hasNotification) Badge() }) {
                    IconButton(onClick = { selectedRoute = "notifications"; navController.navigate("notifications"); hasNotification = false }) {
                        Icon(Icons.Default.Notifications, null)
                    }
                }
            })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedRoute == "home", onClick = { selectedRoute = "home"; navController.navigate("home") }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("الرئيسية") })
                NavigationBarItem(selected = selectedRoute == "search", onClick = { selectedRoute = "search"; navController.navigate("search") }, icon = { Icon(Icons.Default.Search, null) }, label = { Text("بحث") })
                NavigationBarItem(selected = selectedRoute == "add", onClick = { selectedRoute = "add"; navController.navigate("add") }, icon = { Icon(Icons.Default.AddCircle, null) }, label = { Text("اضافة") })
                NavigationBarItem(selected = selectedRoute == "notifications", onClick = { selectedRoute = "notifications"; navController.navigate("notifications") }, icon = { Icon(Icons.Default.Notifications, null) }, label = { Text("اشعارات") })
                NavigationBarItem(selected = selectedRoute == "settings", onClick = { selectedRoute = "settings"; navController.navigate("settings") }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("اعدادات") })
            }
        }
    ) { pad ->
        NavHost(navController, startDestination = "home", modifier = Modifier.padding(pad)) {
            composable("home") { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("الرئيسية - هنا الاخبار") } }
            composable("search") { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("البحث") } }
            composable("add") { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("اضافة خبر بصورة وفيديو وصوت") } }
            composable("notifications") { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("الاشعارات") } }
            composable("settings") { Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) { Text("الاعدادات"); Spacer(Modifier.height(16.dp)); Button(onClick = onLogout) { Text("خروج") } } }
        }
    }
}
