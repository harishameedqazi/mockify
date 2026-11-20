package com.mockify.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.mockify.app.auth.SignInViewModel
import com.mockify.app.storage.StorageManager
import com.mockify.app.ui.screens.SignInScreen
import com.mockify.app.ui.theme.MockGpsTheme

class SignInActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Check login status from SharedPreferences
        if (StorageManager.getLogin()) {
            // Redirect to MainActivity if logged in
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            return // Stop further execution of this activity
        }

        setContent {
            MockGpsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val signInViewModel = SignInViewModel()
                    val context = LocalContext.current
                    SignInScreen(signInViewModel) { email, password, isRemember ->
                        signInViewModel.handleEmailSignIn(email, password, isRemember, context)
                    }
                }
            }
        }
    }
}