package com.newsme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.newsme.app.data.remote.ApiClient
import kotlinx.coroutines.launch

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
    var loggedEmail by remember { mutableStateOf("") }

    MaterialTheme {
        if (!isLoggedIn) {
            when (authScreen) {
                "login" -> LoginScreen(onLoginSuccess = { email -> loggedEmail = email; isLoggedIn = true }, onGoRegister = { authScreen = "register" }, onGoVerify = { email -> loggedEmail = email; authScreen = "verify" })
                "register" -> RegisterScreen(onRegistered = { email -> loggedEmail = email; authScreen = "verify" }, onBack = { authScreen = "login" })
                "verify" -> VerifyScreen(email = loggedEmail, onVerified = { isLoggedIn = true }, onBack = { authScreen = "login" })
            }
        } else {
            MainWithBottomNav(loggedEmail) { isLoggedIn = false }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit, onGoRegister: () -> Unit, onGoVerify: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { TopAppBar(title = { Text("تسجيل الدخول - مربوط بالسيرفر") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(24.dp), Arrangement.Center) {
            OutlinedTextField(email, { email = it }, label = { Text("الايميل") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(password, { password = it }, label = { Text("كلمة السر") }, modifier = Modifier.fillMaxWidth())
            if (msg.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text(msg, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                loading = true; msg = ""
                scope.launch {
                    val res = ApiClient.login(email, password)
                    loading = false
                    if (res.success) onLoginSuccess(email) else msg = res.message
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = !loading) { Text(if (loading) "جاري الدخول..." else "دخول حقيقي للسيرفر") }
            TextButton(onClick = onGoRegister, modifier = Modifier.fillMaxWidth()) { Text("انشاء حساب جديد - register.php") }
            TextButton(onClick = { if(email.isNotEmpty()) onGoVerify(email) }, modifier = Modifier.fillMaxWidth()) { Text("عندي كود تحقق") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegistered: (String) -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { TopAppBar(title = { Text("انشاء حساب - register.php") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(24.dp), Arrangement.Center) {
            OutlinedTextField(name, { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("الايميل") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password, { password = it }, label = { Text("كلمة السر") }, modifier = Modifier.fillMaxWidth())
            if (msg.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text(msg) }
            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                loading = true
                scope.launch {
                    val reg = ApiClient.register(name, email, password)
                    if (reg.success) {
                        ApiClient.sendOtp(email)
                        loading = false
                        onRegistered(email)
                    } else {
                        loading = false; msg = reg.message
                    }
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = !loading) { Text("تسجيل وارسال كود OTP") }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("رجوع") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyScreen(email: String, onVerified: () -> Unit, onBack: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { TopAppBar(title = { Text("كود التحقق - $email") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text("الكود وصل على $email من send_email.php")
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(code, { if(it.length <= 6) code = it }, label = { Text("الكود") })
            if (msg.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text(msg) }
            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                loading = true
                scope.launch {
                    val res = ApiClient.verify(email, code)
                    loading = false
                    if (res.success) onVerified() else msg = res.message
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = !loading) { Text("تأكيد الكود - verification.php") }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("رجوع") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWithBottomNav(email: String, onLogout: () -> Unit) {
    val navController = rememberNavController()
    var selectedRoute by remember { mutableStateOf("home") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("نيوز مي - $email") }) },
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
            composable("home") { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("مرحبا $email - الرئيسية مربوطة بالسيرفر") } }
            composable("search") { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("البحث") } }
            composable("add") { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("اضافة خبر - هيتخزن في uploads") } }
            composable("notifications") { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("الاشعارات") } }
            composable("settings") { Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) { Button(onClick = onLogout) { Text("خروج") } } }
        }
    }
}
