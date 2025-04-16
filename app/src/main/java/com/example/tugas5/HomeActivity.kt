package com.example.tugas5

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    private lateinit var dataStore: UserDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        logoutButton = findViewById(R.id.logout_button)
        userNameText = findViewById(R.id.user_name_text)
        userEmailText = findViewById(R.id.user_email_text)
        dataStore = UserDataStore(this)

        lifecycleScope.launch {
            dataStore.getUserData().collect { userData ->
                userNameText.text = "Name: ${userData.first}"
                userEmailText.text = "Email: ${userData.second}"
            }
        }

        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()

        lifecycleScope.launch {
            dataStore.clearUserData()
            startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
            finish()
        }
    }
}
