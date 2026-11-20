package com.mockify.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.mockify.app.service.LocationHelper
import com.mockify.app.service.MockLocationService
import com.mockify.app.service.VibratorService
import com.mockify.app.storage.StorageManager
import com.mockify.app.ui.models.User
import com.mockify.app.ui.screens.MapScreen
import com.mockify.app.ui.theme.MockGpsTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().getReference().child("Users")

    private var mockLocationService: MockLocationService? = null
        private set(value) {
            field = value
            MockLocationService.instance = value
        }

    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MockLocationService.MockLocationBinder
            mockLocationService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            isBound = false
        }
    }

    fun toggleMocking(): Boolean {

        if (isBound && LocationHelper.hasPermission(this)) {
            if (LocationHelper.isLocationEnabled(this)) {
                mockLocationService?.toggleMocking()
                if (mockLocationService?.isMocking == true) {
                    Toast.makeText(this, "Mocking location...", Toast.LENGTH_SHORT).show()
                    VibratorService.vibrate()
                    return true
                } else if (mockLocationService?.isMocking == false) {
                    Toast.makeText(this, "Stopped mocking location...", Toast.LENGTH_SHORT).show()
                    VibratorService.vibrate()
                    return false
                }
            } else {
                Toast.makeText(this, "Please turn on the location!", Toast.LENGTH_SHORT).show()
            }
        } else if (!isBound && LocationHelper.hasPermission(this))
            Toast.makeText(this, "Service not bound!", Toast.LENGTH_SHORT).show()
        else {
            Toast.makeText(this, "Location permission not granted!", Toast.LENGTH_SHORT).show()
            LocationHelper.requestPermissions(this)
        }


        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // To check if the user's valid days to login are completed or not
        // If completed then go back to the login screen and false the remember me
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.child(currentUser.uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // User exists
                    val user = snapshot.getValue(User::class.java)

                    if (user != null) {
                        val currentTime = System.currentTimeMillis()
                        val expirationTime =
                            user.loginTime + (user.validDays * 24 * 60 * 60 * 1000L)

                        if (currentTime > expirationTime) {
                            Toast.makeText(
                                this,
                                "Login expired: Your access period of ${user.validDays} days has ended.",
                                Toast.LENGTH_LONG
                            ).show()
                            // Set isRemember/login status to false
                            StorageManager.setLogin(false)
                            // Move back to SignIn Activity
                            val intent = Intent(this, SignInActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Oops! Something went wrong.", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    // User does not exist
                    Toast.makeText(this, "Oops! Something went wrong.", Toast.LENGTH_LONG)
                        .show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Oops! Something went wrong.", Toast.LENGTH_LONG).show()
            }
        }

        setContent {
            MockGpsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen(activity = this)
                }
            }
        }

        // Start the service
        val serviceIntent = Intent(this, MockLocationService::class.java)
        startService(serviceIntent)

        // Bind to the service
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unbind from the service
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MockGpsTheme {
    }
}