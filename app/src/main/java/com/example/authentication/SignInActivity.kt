package com.example.authentication

// Import statements for Android components and Firebase
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

/**
 * SignInActivity handles user authentication functionality
 * This activity allows existing users to sign in using Firebase Authentication
 * with email and password credentials
 */
class SignInActivity : AppCompatActivity() {
    // Firebase Authentication instance for user management
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display for modern Android UI
        enableEdgeToEdge()
        // Set the layout for this activity
        setContentView(R.layout.activity_sign_in)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Set up click listener for the Sign In button
        findViewById<Button>(R.id.btnSignIn).setOnClickListener {
            // Get user input from EditText fields and trim whitespace
            val email = findViewById<EditText>(R.id.etSignInEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etSignInPassword).text.toString().trim()

            // Validate that email and password fields are filled
            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                // Return early to prevent further execution
                return@setOnClickListener
            }

            // Validate email format (basic validation)
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate password is not too short
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable the signin button to prevent multiple submissions
            findViewById<Button>(R.id.btnSignIn).isEnabled = false

            // Sign in user with Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    // Sign in successful
                    val user = authResult.user
                    
                    // user?.let ensures the user object is not null
                    user?.let { firebaseUser ->
                        // User is successfully signed in
                        Toast.makeText(this, "Welcome back, ${firebaseUser.email}!", Toast.LENGTH_SHORT).show()
                        
                        // Navigate to MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        // Clear activity stack so user can't go back to signin screen
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        // Close this activity
                        finish()
                    }
                }
                .addOnFailureListener { exception ->
                    // Sign in failed
                    val errorMessage = when {
                        exception.message?.contains("no user record") == true -> 
                            "No account found with this email address"
                        exception.message?.contains("password is invalid") == true -> 
                            "Incorrect password"
                        exception.message?.contains("network") == true -> 
                            "Network error. Please check your internet connection"
                        else -> exception.message ?: "Sign in failed. Please try again"
                    }
                    
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    // Re-enable button so user can try again
                    findViewById<Button>(R.id.btnSignIn).isEnabled = true
                }
        }

        // Set up click listener for "SignUp" text to navigate to SignUpActivity
        val signUpText = findViewById<TextView>(R.id.tvSignUp)
        signUpText.setOnClickListener {
            // Create intent to start SignUpActivity
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}