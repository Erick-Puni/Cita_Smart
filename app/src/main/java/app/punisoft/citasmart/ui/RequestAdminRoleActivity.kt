package app.punisoft.citasmart.ui
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.punisoft.citasmart.databinding.ActivityRequestAdminRoleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RequestAdminRoleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestAdminRoleBinding
    private val db = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestAdminRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmitRequest.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isEmpty()){
                Toast.makeText(this, "Por favor, ingresa una justificación.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null){
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Preparar la solicitud
            val request = hashMapOf(
                "userId" to currentUserId,
                "message" to message,
                "status" to "pendiente",  // Estado inicial de la solicitud
                "timestamp" to System.currentTimeMillis()
            )
            // Guardar la solicitud en Firestore en la colección "roleRequests"
            db.collection("roleRequests")
                .add(request)
                .addOnSuccessListener {
                    Toast.makeText(this, "Solicitud enviada correctamente.", Toast.LENGTH_SHORT).show()
                    finish() // Cierra la actividad
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al enviar solicitud: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
