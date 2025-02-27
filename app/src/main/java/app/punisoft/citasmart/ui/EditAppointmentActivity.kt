package app.punisoft.citasmart.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.punisoft.citasmart.databinding.ActivityEditAppointmentBinding

import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EditAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAppointmentBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var appointment: app.punisoft.citasmart.model.Appointment

    // Lista para almacenar los médicos cargados desde Firestore
    private val doctorsList = mutableListOf<app.punisoft.citasmart.model.Doctor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperamos los datos de la cita pasados mediante extras
        appointment = app.punisoft.citasmart.model.Appointment(
            id = intent.getStringExtra("id") ?: "",
            userId = intent.getStringExtra("userId") ?: "",
            medicoId = intent.getStringExtra("medicoId") ?: "",
            fecha = intent.getStringExtra("fecha") ?: "",
            hora = intent.getStringExtra("hora") ?: "",
            estado = intent.getStringExtra("estado") ?: "",
            detalles = intent.getStringExtra("detalles") ?: ""
        )

        // Prellenamos los campos de fecha, hora y detalles
        binding.etDate.setText(appointment.fecha)
        binding.etTime.setText(appointment.hora)
        binding.etDetails.setText(appointment.detalles)

        // Configurar DatePicker para el campo de fecha
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            // Si la fecha ya está definida, se parsea
            val parts = appointment.fecha.split("/")
            if (parts.size == 3) {
                calendar.set(parts[2].toInt(), parts[1].toInt()-1, parts[0].toInt())
            }
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    binding.etDate.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Configurar TimePicker para el campo de hora
        binding.etTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                    }
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    binding.etTime.setText(timeFormat.format(cal.time))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        // Cargar médicos desde Firestore y configurar el Spinner
        loadDoctorsIntoSpinner()

        // Guardar cambios al pulsar el botón "Guardar Cambios"
        binding.btnSave.setOnClickListener {
            val updatedDate = binding.etDate.text.toString().trim()
            val updatedTime = binding.etTime.text.toString().trim()
            val updatedDetails = binding.etDetails.text.toString().trim()

            // Obtener el médico seleccionado del Spinner
            val selectedDoctorIndex = binding.spinnerDoctor.selectedItemPosition
            if (selectedDoctorIndex < 0 || selectedDoctorIndex >= doctorsList.size) {
                Toast.makeText(this, "No se ha seleccionado un médico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val updatedDoctor = doctorsList[selectedDoctorIndex]

            if (updatedDate.isEmpty() || updatedTime.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Preparar los datos actualizados para la cita, usando la información del médico seleccionado
            val updatedData = mapOf(
                "medicoId" to updatedDoctor.id,
                "doctorName" to updatedDoctor.nombre,
                "especialidad" to updatedDoctor.especialidad,
                "dispensario" to updatedDoctor.dispensario,
                "fecha" to updatedDate,
                "hora" to updatedTime,
                "detalles" to updatedDetails
            )

            if (appointment.id.isNotEmpty()) {
                db.collection("citas").document(appointment.id)
                    .update(updatedData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cita actualizada correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al actualizar la cita: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "ID de cita no válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDoctorsIntoSpinner() {
        db.collection("medicos")
            .get()
            .addOnSuccessListener { querySnapshot ->
                doctorsList.clear()
                for (document in querySnapshot.documents) {
                    val doctor = document.toObject(app.punisoft.citasmart.model.Doctor::class.java)
                    doctor?.let {
                        it.id = document.id
                        doctorsList.add(it)
                    }
                }
                // Crear una lista de cadenas usando toString() de Doctor
                val doctorDisplayList = doctorsList.map { it.toString() }
                val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, doctorDisplayList)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerDoctor.adapter = spinnerAdapter

                // Establecer la selección predeterminada si coincide con el médico actual de la cita
                val currentDoctorId = appointment.medicoId
                val defaultIndex = doctorsList.indexOfFirst { it.id == currentDoctorId }
                if (defaultIndex >= 0) {
                    binding.spinnerDoctor.setSelection(defaultIndex)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar médicos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
