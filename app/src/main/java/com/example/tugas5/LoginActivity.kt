package com.example.tugas5

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginButton: Button
    private lateinit var privacyText: TextView
    private lateinit var googleSignInOptions: GoogleSignInOptions

    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(TAG, "onCreate: LoginActivity started")

        // Setup Google Sign In
        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        Log.d(TAG, "onCreate: GoogleSignInOptions set up")

        auth = FirebaseAuth.getInstance()
        loginButton = findViewById(R.id.login_button)
        privacyText = findViewById(R.id.privacy_text)

        loginButton.setOnClickListener {
            Log.d(TAG, "Login button clicked")
            googleSignIn()
        }
    }

    private fun googleSignIn() {
        Log.d(TAG, "googleSignIn: Starting Google Sign-In")
        val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "googleSignInLauncher: Sign-in intent result OK")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.result
            Log.d(TAG, "googleSignInLauncher: Signed-in account = ${account?.email}")
            handleSignInResult(account)
        } else {
            Log.d(TAG, "googleSignInLauncher: Sign-in cancelled or failed")
            Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSignInResult(account: GoogleSignInAccount?) {
        Log.d(TAG, "handleSignInResult: Handling result for account ${account?.email}")
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "handleSignInResult: Sign in with credential SUCCESS")
                val user = auth.currentUser
                storeUserData(user)
            } else {
                Log.d(TAG, "handleSignInResult: Sign in with credential FAILED", task.exception)
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun storeUserData(user: FirebaseUser?) {
        if (user != null) {
            val userName = user.displayName
            val userEmail = user.email
            Log.d(TAG, "storeUserData: Saving user data: $userName, $userEmail")

            val dataStore = UserDataStore(this)

            lifecycleScope.launch {
                dataStore.saveUserData(userName, userEmail)
                Log.d(TAG, "storeUserData: User data saved, launching HomeActivity")

                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()
            }
        } else {
            Log.d(TAG, "storeUserData: User is null, cannot save data")
        }
    }

    private fun logout() {
        Log.d(TAG, "logout: Logging out user")
        auth.signOut()
        val dataStore = UserDataStore(this)

        lifecycleScope.launch {
            dataStore.clearUserData()
            Log.d(TAG, "logout: User data cleared, returning to LoginActivity")

            startActivity(Intent(this@LoginActivity, LoginActivity::class.java))
            finish()
        }
    }
}
