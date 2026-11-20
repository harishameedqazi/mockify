package com.mockify.app.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mockify.app.MainActivity
import com.mockify.app.storage.StorageManager
import com.mockify.app.ui.models.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignInViewModel() : ViewModel() {

    val isSignIn = MutableLiveData(false)

    private val database = FirebaseDatabase.getInstance().getReference().child("Users")

    fun handleEmailSignIn(
        email: String,
        password: String,
        isRemember: Boolean,
        context: Context
    ) {
        viewModelScope.launch {
            isSignIn.value = true

            emailSignIn(email, password).collect { result ->
                result.fold(
                    onSuccess = { authResult ->
                        // Handle success
                        val currentUser = authResult.user
                        if (currentUser != null) {
                            validateUserAndLogin(
                                currentUser.uid,
                                getDeviceId(context)
                            ) { success, message ->
                                if (success) {
                                    isSignIn.value = false

                                    // User exists and is allowed to log in
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                                    // Store login status to SharedPreferences
                                    StorageManager.setLogin(isRemember)

                                    // Navigate to MainActivity
                                    val intent =
                                        Intent(context, MainActivity::class.java)
                                    context.startActivity(intent)
                                    // Finish the current activity
                                    (context as? android.app.Activity)?.finish()
                                } else {
                                    if (message.contains("does not exist")) {
                                        // First-time login: Store user data
                                        storeUserData(
                                            context,
                                            currentUser.uid,
                                            email,
                                            password,
                                            getDeviceId(context),
                                            30,
                                            isRemember
                                        )
                                    } else {
                                        // Error message for mismatch or expired validity
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        isSignIn.value = false
                                    }
                                }
                            }
                        } else {
                            // Error message for mismatch or expired validity
                            Toast.makeText(
                                context,
                                "Oops! Something went wrong.",
                                Toast.LENGTH_SHORT
                            ).show()
                            isSignIn.value = false
                        }
                    },
                    onFailure = { e ->
                        // Handle error
                        Toast.makeText(context, "Login failed: ${e.message}", Toast.LENGTH_LONG)
                            .show()
                        isSignIn.value = false
                    }
                )
            }
        }

    }

    private suspend fun emailSignIn(email: String, password: String): Flow<Result<AuthResult>> {
        val firebaseAuth = FirebaseAuth.getInstance()
        return callbackFlow {
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                trySend(Result.success(authResult))
            } catch (e: Exception) {
                trySend(Result.failure(e))
            }

            awaitClose { }
        }
    }

    private fun storeUserData(
        context: Context,
        userId: String,
        email: String,
        password: String,
        deviceId: String,
        validDays: Int,
        isRemember: Boolean
    ) {
        // Get the current date in format e.g May 23, 2024 (login date)
        val loginDate = getCurrentDate()

        // Get the current time in milliseconds (login time)
        val loginTime = System.currentTimeMillis()

        // Create the user object
        val user = User(userId, email, deviceId, loginDate, loginTime, validDays)

        // Store the data under the user's ID
        database.child(userId).setValue(user)
            .addOnSuccessListener {

                isSignIn.value = false

                Toast.makeText(
                    context,
                    "First login successful!",
                    Toast.LENGTH_SHORT
                ).show()

                // Store login status to SharedPreferences
                StorageManager.setLogin(isRemember)

                // Navigate to MainActivity
                val intent =
                    Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                // Finish the current activity
                (context as? android.app.Activity)?.finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Something went wrong: ${exception.message}!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun validateUserAndLogin(
        userId: String,
        deviceId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        database.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // User exists
                val user = snapshot.getValue(User::class.java)

                if (user != null) {
                    val currentTime = System.currentTimeMillis()
                    val expirationTime = user.loginTime + (user.validDays * 24 * 60 * 60 * 1000L)

                    when {
                        user.deviceId != deviceId -> {
                            onResult(false, "Device mismatch. Login denied.")
                        }

                        currentTime > expirationTime -> {
                            onResult(
                                false,
                                "Login expired: Your access period of ${user.validDays} days has ended."
                            )
                        }

                        else -> {
                            val remainingDays =
                                ((expirationTime - currentTime) / (24 * 60 * 60 * 1000)).toInt()
                            onResult(
                                true,
                                "Login successful! You have $remainingDays valid days left."
                            )
                        }
                    }
                } else {
                    onResult(false, "Error: User data is invalid.")
                }
            } else {
                // User does not exist
                onResult(false, "User does not exist. Proceed with first-time login.")
            }
        }.addOnFailureListener { exception ->
            onResult(false, "Error accessing database: ${exception.message}")
        }
    }


    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

}