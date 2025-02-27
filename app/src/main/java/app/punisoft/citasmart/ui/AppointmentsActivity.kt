package app.punisoft.citasmart.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import app.punisoft.citasmart.databinding.ActivityAppointmentsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class AppointmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointmentsBinding
    private lateinit var adapter: AppointmentsAdapterForPatients

    // Instancias de Firestore y FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar con el título "Mis Citas"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Mis Citas"

        // Inicializar el RecyclerView con una lista vacía y función de cancelación
        adapter = AppointmentsAdapterForPatients(emptyList()) { appointment ->
            cancelAppointment(appointment)
        }
        binding.recyclerViewAppointments.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAppointments.adapter = adapter

        // Obtener la fecha seleccionada pasada desde el Dashboard; de lo contrario, usar la fecha de hoy
        val selectedDate = intent.getStringExtra("selectedDate") ?: getTodayDate()
        Log.d("AppointmentsActivity", "Fecha seleccionada: $selectedDate")

        // Iniciar el snapshot listener para obtener las citas en tiempo real
        listenForAppointments(selectedDate)

        // Listener para el FAB que agrega una nueva cita
        binding.fabAddAppointment.setOnClickListener {
            startActivity(Intent(this, NewAppointmentActivity::class.java))
        }
    }

    // Función que retorna la fecha actual en formato "dd/MM/yyyy"
    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    // Snapshot listener para obtener las citas en tiempo real
    // Filtra por fecha, por el ID del usuario actual y excluye las citas canceladas.
    private fun listenForAppointments(selectedDate: String) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("citas")
            .whereEqualTo("fecha", selectedDate)
            .whereEqualTo("userId", currentUserId)
            .whereNotEqualTo("estado", "cancelada")
            .orderBy("hora", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(this, "Error al cargar citas: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AppointmentsActivity", "Error en snapshot: ${exception.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val appointmentsList = mutableListOf<app.punisoft.citasmart.model.Appointment>()
                    for (document in snapshot.documents) {
                        val appointment = document.toObject(app.punisoft.citasmart.model.Appointment::class.java)
                        appointment?.let {
                            it.id = document.id  // Asigna el ID del documento al objeto
                            appointmentsList.add(it)
                        }
                    }
                    // Mostrar mensaje de "sin citas" si la lista está vacía
                    binding.tvEmptyMessage.visibility = if (appointmentsList.isEmpty()) View.VISIBLE else View.GONE
                    adapter.updateData(appointmentsList)
                }
            }
    }

    // Función para cancelar una cita, actualizando su estado a "cancelada"
    private fun cancelAppointment(appointment: app.punisoft.citasmart.model.Appointment) {
        if (appointment.id.isEmpty()) {
            Toast.makeText(this, "Error: ID de cita no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("citas").document(appointment.id)
            .update("estado", "cancelada")
            .addOnSuccessListener {
                Toast.makeText(this, "Cita cancelada", Toast.LENGTH_SHORT).show()
                // La actualización se reflejará automáticamente en el snapshot listener.
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cancelar la cita: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
