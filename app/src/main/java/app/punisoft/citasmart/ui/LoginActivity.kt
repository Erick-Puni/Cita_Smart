package app.punisoft.citasmart.ui
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import app.punisoft.citasmart.R
import app.punisoft.citasmart.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Configuración de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Asegúrate de que este valor coincida con el client_id de tipo 3 en Firebase
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Registra el launcher para el resultado de Google Sign-In
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                task.addOnCompleteListener { taskResult ->
                    if (taskResult.isSuccessful) {
                        val account = taskResult.result
                        firebaseAuthWithGoogle(account)
                    } else {
                        Log.w("LoginActivity", "Google sign in failed", taskResult.exception)
                        Toast.makeText(this, "Google sign in failed: ${taskResult.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Google sign in cancelado", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener para el botón de inicio de sesión con email/password
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener para el botón de registro: redirige a RegisterActivity
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        // Listener para el botón "Continuar con Google"
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account == null) return
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Autenticación exitosa, registra el usuario en Firestore
                    val user = firebaseAuth.currentUser
                    registerUserInFirestore(user)
                    Toast.makeText(this, "Inicio de sesión con Google exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun registerUserInFirestore(user: FirebaseUser?) {
        if (user == null) {
            Log.w("LoginActivity", "registerUserInFirestore: user es null")
            return
        }
        val userDocRef = db.collection("usuarios").document(user.uid)
        userDocRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val userData = hashMapOf(
                    "nombre" to (user.displayName ?: "Sin nombre"),
                    "correo" to (user.email ?: "Sin correo"),
                    "rol" to "paciente"  // Rol predeterminado
                )
                userDocRef.set(userData)
                    .addOnSuccessListener {
                        Log.d("LoginActivity", "Usuario registrado en Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.w("LoginActivity", "Error registrando usuario en Firestore", e)
                    }
            } else {
                Log.d("LoginActivity", "El usuario ya existe en Firestore")
            }
        }.addOnFailureListener { e ->
            Log.w("LoginActivity", "Error al obtener documento de usuario", e)
        }
    }
}
