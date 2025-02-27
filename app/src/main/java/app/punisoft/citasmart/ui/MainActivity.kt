package app.punisoft.citasmart.ui
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import app.punisoft.citasmart.databinding.ActivityMainBinding
import app.punisoft.citasmart.util.NotificationUtils


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Crear canal de notificaciones (para Android Oreo+)
        NotificationUtils.createNotificationChannel(this)

        // Animación de carga
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.tvSplash.startAnimation(fadeIn)

        // Verificar si el usuario está autenticado
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Escucha en tiempo real para obtener el documento del usuario actualizado.
        db.collection("usuarios").document(currentUser.uid)
            .addSnapshotListener { document, error ->
                if (error != null || document == null || !document.exists()) {
                    // Si hay error o el documento no existe, redirige a Dashboard (modo paciente)
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    // Normalizamos el valor para evitar problemas de espacios o mayúsculas
                    val rol = document.getString("rol")?.trim()?.lowercase() ?: "paciente"
                    if (rol == "admin" || rol == "recepcionista") {
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, DashboardActivity::class.java))
                    }
                    finish()
                }
            }
    }
}
