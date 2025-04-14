package com.example.myapplication
import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.saveable.rememberSaveable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            var screenState by rememberSaveable { mutableStateOf("login") }
            var selectedRoute by rememberSaveable { mutableStateOf("") }
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            val lightColors = lightColorScheme()
            val darkColors = darkColorScheme(
                background = Color(0xFF121212) // sötétszürke háttér
            )

            val colorScheme = if (isDarkTheme) darkColors else lightColors

            MaterialTheme(colorScheme = colorScheme) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background) // 💡 Itt van a lényeg
                ) {

                    when (screenState) {
                        "login" -> LoginScreen(
                            onSwitchToRegister = { screenState = "register" },
                            onLoginSuccess = { screenState = "main" }
                        )

                        "register" -> RegisterScreen(
                            onSwitchToLogin = { screenState = "login" },
                            onRegisterSuccess = { screenState = "main" }
                        )

                        "main" -> MainScreen(
                            onRouteClick = {
                                selectedRoute = it
                                screenState = "details"
                            },
                            onLogout = {
                                Firebase.auth.signOut()
                                screenState = "login"
                            }
                        )

                        "details" -> DetailsScreen(
                            route = selectedRoute,
                            onBack = { screenState = "main" }
                        )
                    }

                    // 🌗 Dark mode váltó gomb
                    IconButton(
                        onClick = { isDarkTheme = !isDarkTheme },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                            contentDescription = "Téma váltás",
                            tint = MaterialTheme.colorScheme.onBackground // ✅ téma alapú szín
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun LoginScreen(onSwitchToRegister: () -> Unit, onLoginSuccess: () -> Unit) {
        val context = LocalContext.current
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Bejelentkezés",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Jelszó") },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val label = if (showPassword) "🙈" else "👁️"
                    Text(label, modifier = Modifier.clickable { showPassword = !showPassword })
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedButton(onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Töltsd ki az összes mezőt!", Toast.LENGTH_SHORT).show()
                    return@AnimatedButton
                }
                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Sikeres bejelentkezés", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "Hiba: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }) {
                Text("Bejelentkezés")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Nincs fiókod? Regisztrálj!",
                modifier = Modifier.clickable { onSwitchToRegister() },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

@Composable
fun RegisterScreen(onSwitchToLogin: () -> Unit, onRegisterSuccess: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Regisztráció",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Jelszó") },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val label = if (showPassword) "🙈" else "👁️"
                Text(label, modifier = Modifier.clickable { showPassword = !showPassword })
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedButton(onClick = {
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Töltsd ki az összes mezőt!", Toast.LENGTH_SHORT).show()
                return@AnimatedButton
            }
            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Sikeres regisztráció", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess()
                    } else {
                        Toast.makeText(context, "Hiba: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }) {
            Text("Regisztráció")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Van már fiókod? Jelentkezz be!",
            modifier = Modifier.clickable { onSwitchToLogin() },
            color = MaterialTheme.colorScheme.primary
        )
    }
}

    @Composable
    fun MainScreen(onRouteClick: (String) -> Unit, onLogout: () -> Unit) {
        val allRoutes = listOf(
            "4-es metró – Kelenföld → Keleti",
            "7-es busz – Albertfalva → Újpalota",
            "49-es villamos – Kelenföld → Deák tér",
            "M3 metró – Kőbánya-Kispest → Újpest-Központ"
        )

        val user = Firebase.auth.currentUser
        val email = user?.email ?: "Ismeretlen felhasználó"
        var search by remember { mutableStateOf("") }

        val filteredRoutes = allRoutes.filter {
            it.contains(search, ignoreCase = true)
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            val isTablet = maxWidth > 600.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isTablet) 64.dp else 0.dp),
            ) {
                Text(
                    text = "Menetrend",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Bejelentkezve: $email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Keresés...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredRoutes) { jarat ->
                        val interactionSource = remember { MutableInteractionSource() }
                        var pressed by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(if (pressed) 1.1f else 1f, label = "")

                        Card(
                            modifier = Modifier
                                .scale(scale)
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    onRouteClick(jarat)
                                }
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            pressed = event.changes.any { it.pressed }
                                        }
                                    }
                                }
                        ) {
                            Text(text = jarat, modifier = Modifier.padding(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedButton(onClick = { onLogout() }) {
                    Text("Kijelentkezés")
                }
            }
        }
    }

    @Composable
    fun DetailsScreen(route: String, onBack: () -> Unit) {
        var reversed by remember { mutableStateOf(false) }

        val megallok = when (route) {
            "4-es metró – Kelenföld → Keleti" -> listOf("Kelenföld", "Bikás park", "Móricz", "Fővám", "Keleti")
            "7-es busz – Albertfalva → Újpalota" -> listOf("Albertfalva", "Móricz", "Astoria", "Keleti", "Újpalota")
            "49-es villamos – Kelenföld → Deák tér" -> listOf("Kelenföld", "Fehérvári út", "Móricz", "Fővám", "Deák")
            "M3 metró – Kőbánya-Kispest → Újpest-Központ" -> listOf("Kőbánya", "Népliget", "Corvin", "Deák", "Újpest")
            else -> listOf("Nincs adat")
        }

        val times = if (!reversed) {
            megallok.mapIndexed { index, m -> "$m (${8 + index}:${(index * 5) % 60})" }
        } else {
            megallok.reversed().mapIndexed { index, m -> "$m (${8 + index}:${(index * 6 + 3) % 60})" }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                route,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground // 💡 ezt add hozzá
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(times) { megallo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = megallo, modifier = Modifier.padding(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedButton(onClick = { reversed = !reversed }) {
                Text("Irány megfordítása")
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedButton(onClick = { onBack() }) {
                Text("Vissza")
            }
        }
    }

@Composable
fun AnimatedButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 1.1f else 1f, label = "")

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .scale(scale)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        pressed = event.changes.any { it.pressed }
                    }
                }
            }
    ) {
        content()
    }
}}
