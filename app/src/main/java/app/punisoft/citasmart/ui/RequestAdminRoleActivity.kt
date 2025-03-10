package app.punisoft.citasmart.ui

import android.app.AlertDialog
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
            if (message.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa una justificación.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Verificar si ya existe un documento con este userId
            val docRef = db.collection("roleRequests").document(currentUserId)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Ya hay una solicitud previa
                        Toast.makeText(
                            this,
                            "Ya has enviado una solicitud previamente. No puedes enviar otra.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // 2. Si no existe, mostrar un AlertDialog de confirmación
                        AlertDialog.Builder(this)
                            .setTitle("Confirmar envío de solicitud")
                            .setMessage(
                                "Esta es tu única oportunidad para enviar la solicitud. " +
                                        "Asegúrate de que tu justificación esté bien redactada " +
                                        "porque no podrás modificarla después.\n\n" +
                                        "¿Deseas continuar?"
                            )
                            .setPositiveButton("Enviar") { _, _ ->
                                // 3. Crear el documento con el userId como ID
                                val requestData = hashMapOf(
                                    "userId" to currentUserId,
                                    "message" to message,
                                    "status" to "pendiente",
                                    "timestamp" to System.currentTimeMillis()
                                )

                                docRef.set(requestData)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Solicitud enviada correctamente.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish() // Cierra la actividad
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Error al enviar solicitud: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al verificar solicitud: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
