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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * SignUpActivity handles user registration functionality
 * This activity allows users to create new accounts using Firebase Authentication
 * and stores additional user data in Firebase Realtime Database
 */
class SignUpActivity : AppCompatActivity() {
    // Firebase Authentication instance for user management
    private lateinit var auth: FirebaseAuth
    // Firebase Realtime Database reference for storing user data
    private lateinit var db: DatabaseReference
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display for modern Android UI
        enableEdgeToEdge()
        // Set the layout for this activity
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Realtime Database reference
        db = FirebaseDatabase.getInstance().reference

        // Set up click listener for the Sign Up button
        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            // Get user input from EditText fields and trim whitespace
            val username = findViewById<EditText>(R.id.etSignUpUsername).text.toString().trim()
            val email = findViewById<EditText>(R.id.etSignUpEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etSignUpPassword).text.toString().trim()

            // Validate that all fields are filled
            if(username.isEmpty() || email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                // Return early to prevent further execution
                return@setOnClickListener
            }

            // Validate password length (Firebase requires minimum 6 characters)
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable the signup button to prevent multiple submissions
            findViewById<Button>(R.id.btnSignUp).isEnabled = false

            // Create new user account with Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // User account created successfully in Firebase Auth
                        val user = auth.currentUser
                        
                        // user?.let is a safe call operator with let scope function
                        // It only executes the block if 'user' is not null
                        // If user is null, the entire block is skipped safely
                        // This prevents null pointer exceptions
                        user?.let { firebaseUser ->
                            // Inside this block, 'firebaseUser' is guaranteed to be non-null
                            // This is the same user object but with a different name for clarity
                            
                            // Create user data object to store in Firebase Database
                            val userData = hashMapOf(
                                "userId" to firebaseUser.uid,  // Firebase Auth UID as unique identifier
                                "username" to username,        // User's chosen username
                                "email" to email              // User's email address
                            )

                            // Save user data to Realtime Database
                            // Using the UID as the key ensures each user has a unique entry
                            db.child("users").child(firebaseUser.uid).setValue(userData)
                                .addOnSuccessListener {
                                    // Database save successful
                                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                    // Navigate to MainActivity
                                    val intent = Intent(this, MainActivity::class.java)
                                    // Clear activity stack so user can't go back to signup screen
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    // Close this activity
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    // Database save failed
                                    Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                    // Re-enable button so user can try again
                                    findViewById<Button>(R.id.btnSignUp).isEnabled = true
                                }
                        }
                        // If user is null, this point is reached without any error
                        // The app continues normally without crashing
                    } else {
                        // Firebase Authentication failed
                        // Get the error message or use default message
                        val errorMessage = task.exception?.message ?: "Authentication failed"
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        // Re-enable button so user can try again
                        findViewById<Button>(R.id.btnSignUp).isEnabled = true
                    }
                }
        }

        // Set up click listener for "SignIn" text to navigate to SignInActivity
        val signInText = findViewById<TextView>(R.id.tvSignIn)
        signInText.setOnClickListener {
            // Create intent to start SignInActivity
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}