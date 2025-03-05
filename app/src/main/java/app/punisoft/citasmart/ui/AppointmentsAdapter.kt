package app.punisoft.citasmart.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.punisoft.citasmart.databinding.ItemAppointmentBinding
import app.punisoft.citasmart.model.Appointment

class AppointmentsAdapter(
    appointments: List<Appointment>,
    private val onCancel: (Appointment) -> Unit,
    private val onEdit: (Appointment) -> Unit  // Lambda para editar
) : RecyclerView.Adapter<AppointmentsAdapter.AppointmentViewHolder>() {

    // Convertimos la lista a mutable para poder eliminar elementos
    private var appointments: MutableList<Appointment> = appointments.toMutableList()

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
            // Muestra la hora de la cita
            tvAppointmentTime.text = "🕒 ${appointment.hora}"

            // Muestra los detalles de la cita
            tvAppointmentDetails.text = "📝 ${appointment.detalles} ?: 'No especificado'"

            // Muestra la información del doctor:
            // Si doctorName está presente, se muestra; de lo contrario, se muestra el medicoId.
            tvDoctorName.text = "👨‍⚕️ Doctor: ${appointment.doctorName ?: appointment.medicoId}"

            // Muestra la especialidad y el consultorio (dispensario)
            tvEspecialidad.text = "🩺 Especialidad: ${appointment.especialidad ?: "No especificada"}"
            tvDispensario.text = "🏥 Consultorio: ${appointment.dispensario ?: "No especificado"}"

            // Muestra la información del paciente (si se dispone, por ejemplo en el panel de administración)
            if (!appointment.patientName.isNullOrEmpty()) {
                tvPatientName.text = "👤 Paciente: ${appointment.patientName}"
                tvPatientName.visibility = View.VISIBLE
            } else {
                tvPatientName.visibility = View.GONE
            }

            // Si la cita está cancelada, aplica estilo (color rojo y tachado)
            if (appointment.estado == "cancelada") {
                val redColor = ContextCompat.getColor(root.context, android.R.color.holo_red_dark)
                tvAppointmentTime.setTextColor(redColor)
                tvAppointmentDetails.setTextColor(redColor)
                tvDoctorName.setTextColor(redColor)
                tvEspecialidad.setTextColor(redColor)
                tvDispensario.setTextColor(redColor)
                tvAppointmentTime.paintFlags = tvAppointmentTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvAppointmentDetails.paintFlags = tvAppointmentDetails.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvDoctorName.paintFlags = tvDoctorName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvEspecialidad.paintFlags = tvEspecialidad.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvDispensario.paintFlags = tvDispensario.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                val blackColor = ContextCompat.getColor(root.context, android.R.color.black)
                tvAppointmentTime.setTextColor(blackColor)
                tvAppointmentDetails.setTextColor(blackColor)
                tvDoctorName.setTextColor(blackColor)
                tvEspecialidad.setTextColor(blackColor)
                tvDispensario.setTextColor(blackColor)
                tvAppointmentTime.paintFlags = tvAppointmentTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvAppointmentDetails.paintFlags = tvAppointmentDetails.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvDoctorName.paintFlags = tvDoctorName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvEspecialidad.paintFlags = tvEspecialidad.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvDispensario.paintFlags = tvDispensario.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // Configura el botón "Cancelar"
            btnCancel.setOnClickListener {
                onCancel(appointment)
            }

            // Configura el botón "Editar"
            btnEdit.setOnClickListener {
                onEdit(appointment)
            }
        }
    }

    override fun getItemCount(): Int = appointments.size

    // Método para obtener un item por su posición
    fun getItem(position: Int): Appointment = appointments[position]

    // Actualiza la lista y notifica los cambios
    fun updateData(newAppointments: List<Appointment>) {
        appointments = newAppointments.toMutableList()
        notifyDataSetChanged()
    }

    // Elimina un item de la lista y notifica el cambio
    fun removeItem(position: Int) {
        appointments.removeAt(position)
        notifyItemRemoved(position)
    }
}
