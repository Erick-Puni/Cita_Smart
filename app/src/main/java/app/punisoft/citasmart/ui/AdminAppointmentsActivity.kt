package app.punisoft.citasmart.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.punisoft.citasmart.databinding.ActivityAdminAppointmentsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdminAppointmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAppointmentsBinding
    private lateinit var adapter: AppointmentsAdapter
    private val db = FirebaseFirestore.getInstance()

    // Variable para la fecha seleccionada; se obtiene del intent o se usa la fecha de hoy.
    private var selectedDate: String = getTodayDate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAppointmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Citas del Día"

        // Inicializamos el RecyclerView y el adapter con funciones para cancelar y editar citas
        adapter = AppointmentsAdapter(emptyList(), onCancel = { appointment ->
            cancelAppointment(appointment)
        }, onEdit = { appointment ->
            // Al pulsar "Editar", se abre EditAppointmentActivity pasando los datos de la cita
            val intent = Intent(this, EditAppointmentActivity::class.java).apply {
                putExtra("id", appointment.id)
                putExtra("userId", appointment.userId)
                putExtra("medicoId", appointment.medicoId)
                putExtra("fecha", appointment.fecha)
                putExtra("hora", appointment.hora)
                putExtra("estado", appointment.estado)
                putExtra("detalles", appointment.detalles)
            }
            startActivity(intent)
        })

        binding.rvAdminAppointments.layoutManager = LinearLayoutManager(this)
        binding.rvAdminAppointments.adapter = adapter

        // Obtener la fecha seleccionada del intent; si no se pasa, usar la fecha actual.
        selectedDate = intent.getStringExtra("selectedDate") ?: getTodayDate()

        // Iniciar el snapshot listener para obtener citas en tiempo real para la fecha seleccionada.
        listenForAppointments(selectedDate)

        // Habilitar swipe-to-delete para eliminar en Firebase si la cita está cancelada
        enableSwipeToDelete()
    }

    // Función para obtener la fecha actual en formato "dd/MM/yyyy"
    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    // Snapshot listener para obtener citas en tiempo real para la fecha seleccionada
    private fun listenForAppointments(selectedDate: String) {
        db.collection("citas")
            .whereEqualTo("fecha", selectedDate)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(this, "Error al cargar citas: ${exception.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val appointmentsList = mutableListOf<app.punisoft.citasmart.model.Appointment>()
                    for (document in snapshot.documents) {
                        val appointment = document.toObject(app.punisoft.citasmart.model.Appointment::class.java)
                        appointment?.let {
                            it.id = document.id // Asigna el ID del documento al modelo
                            appointmentsList.add(it)
                        }
                    }
                    // Mostrar el mensaje de lista vacía si no hay citas
                    binding.tvEmptyMessage.visibility = if (appointmentsList.isEmpty()) View.VISIBLE else View.GONE
                    adapter.updateData(appointmentsList)
                }
            }
    }

    // Función para cancelar una cita: actualiza el estado a "cancelada"
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

    // Función para habilitar swipe-to-delete que elimina la cita en Firebase si está cancelada, con confirmación
    private fun enableSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // No permitimos mover ítems
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                // Obtener la cita usando un método getItem() del adapter
                val appointment = adapter.getItem(position)
                if (appointment.estado == "cancelada") {
                    // Mostrar un diálogo de confirmación antes de borrar
                    AlertDialog.Builder(this@AdminAppointmentsActivity)
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Está seguro de que desea borrar esta cita? Esta acción es irreversible.")
                        .setPositiveButton("Sí") { dialog, _ ->
                            db.collection("citas").document(appointment.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this@AdminAppointmentsActivity, "Cita eliminada", Toast.LENGTH_SHORT).show()
                                    // El snapshot listener actualizará la lista automáticamente
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this@AdminAppointmentsActivity, "Error al eliminar cita: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    adapter.notifyItemChanged(position)
                                }
                        }
                        .setNegativeButton("Cancelar") { dialog, _ ->
                            adapter.notifyItemChanged(position)
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    Toast.makeText(this@AdminAppointmentsActivity, "Solo se pueden eliminar citas canceladas.", Toast.LENGTH_SHORT).show()
                    adapter.notifyItemChanged(position)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvAdminAppointments)
    }
}
