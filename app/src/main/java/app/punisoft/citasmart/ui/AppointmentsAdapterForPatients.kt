package app.punisoft.citasmart.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.punisoft.citasmart.databinding.ItemAppointmentBinding

class AppointmentsAdapterForPatients(
    private var appointments: List<app.punisoft.citasmart.model.Appointment>,
    private val onCancel: (app.punisoft.citasmart.model.Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentsAdapterForPatients.AppointmentViewHolder>() {

    inner class AppointmentViewHolder(val binding: ItemAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        with(holder.binding) {
            // Muestra la hora y los detalles de la cita
            tvAppointmentTime.text = "🕒 ${appointment.hora}"
            tvAppointmentDetails.text = if (!appointment.detalles.isNullOrBlank()) {
                "📝 Detalles: ${appointment.detalles}"
            } else {
                "📝 Detalles: No especificado"
            }

            // Muestra la información del doctor:
            // Si se guardó el nombre, se muestra; si no, se muestra el ID
            tvDoctorName.text = "👨‍⚕️ Doctor: ${appointment.doctorName ?: appointment.medicoId}"
            tvEspecialidad.text = "🩺 Especialidad: ${appointment.especialidad ?: "No especificada"}"
            tvDispensario.text = "🏥 Consultorio: ${appointment.dispensario ?: "No especificado"}"

            // Configura el botón "Cancelar"
            btnCancel.setOnClickListener { onCancel(appointment) }

            // Oculta el botón "Editar" para los pacientes
            btnEdit.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = appointments.size

    // Actualiza la lista de citas y notifica los cambios
    fun updateData(newAppointments: List<app.punisoft.citasmart.model.Appointment>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }
}
