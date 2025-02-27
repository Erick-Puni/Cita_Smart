package app.punisoft.citasmart.ui
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.punisoft.citasmart.databinding.ActivityNewAppointmentBinding
import app.punisoft.citasmart.model.Doctor
import app.punisoft.citasmart.util.NotificationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NewAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewAppointmentBinding
    private val db = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Lista para almacenar los médicos cargados desde Firestore
    private val doctorsList = mutableListOf<Doctor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Crear canal de notificaciones (para Android Oreo+)
        NotificationUtils.createNotificationChannel(this)

        // Cargar los médicos desde Firestore y actualizar el Spinner
        loadDoctorsIntoSpinner()

        // Configurar el campo de fecha para abrir un DatePicker
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
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

        // Configurar el campo de hora para abrir un TimePicker
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

        // Botón de confirmación para agendar la cita con confirmación
        binding.btnConfirm.setOnClickListener {
            // Verificar que se haya cargado al menos un médico
            if (doctorsList.isEmpty()) {
                Toast.makeText(this, "No hay médicos disponibles, intenta más tarde", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedDoctorIndex = binding.spinnerDoctor.selectedItemPosition
            if (selectedDoctorIndex < 0 || selectedDoctorIndex >= doctorsList.size) {
                Toast.makeText(this, "No se ha seleccionado un médico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val doctorSelected = doctorsList[selectedDoctorIndex]

            val date = binding.etDate.text.toString().trim()
            val time = binding.etTime.text.toString().trim()
            val details = binding.etDetails.text.toString().trim()

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa fecha y hora", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar que la cita sea en el futuro
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            val appointmentDate = sdf.parse("$date $time")
            if (appointmentDate == null || appointmentDate.time <= System.currentTimeMillis()) {
                Toast.makeText(this, "La cita debe agendarse en una fecha y hora futuras", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Preparar los datos para la nueva cita, incluyendo la información del doctor
            val newAppointment = hashMapOf(
                "userId" to currentUserId,
                "medicoId" to doctorSelected.id,
                "doctorName" to doctorSelected.nombre,
                "especialidad" to doctorSelected.especialidad,
                "dispensario" to doctorSelected.dispensario,
                "fecha" to date,
                "hora" to time,
                "estado" to "pendiente",
                "detalles" to details
            )

            // Validar que no exista ya una cita para la misma fecha, hora y para el mismo médico
            db.collection("citas")
                .whereEqualTo("fecha", date)
                .whereEqualTo("hora", time)
                .whereEqualTo("medicoId", doctorSelected.id)
                .whereNotEqualTo("estado", "cancelada")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        Toast.makeText(this, "El médico ya tiene una cita a esa fecha y hora", Toast.LENGTH_LONG).show()
                    } else {
                        // Mostrar un diálogo de confirmación antes de agendar la cita
                        AlertDialog.Builder(this)
                            .setTitle("Confirmar cita")
                            .setMessage("¿Estás seguro de que deseas agendar la cita? Revisa bien la información antes de aceptar, ya que no podrás editarla después.")
                            .setPositiveButton("Aceptar") { _, _ ->
                                // Agregar la cita a Firestore
                                db.collection("citas")
                                    .add(newAppointment)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Cita agendada correctamente", Toast.LENGTH_SHORT).show()

                                        // Programar notificaciones recordatorias
                                        val currentTime = System.currentTimeMillis()
                                        // Notificación 1 día antes, solo si la cita es al menos 24 horas en el futuro
                                        if (appointmentDate.time > currentTime + (24 * 60 * 60 * 1000)) {
                                            val dayBeforeMillis = appointmentDate.time - (24 * 60 * 60 * 1000)
                                            NotificationUtils.scheduleNotification(
                                                context = this,
                                                triggerAtMillis = dayBeforeMillis,
                                                title = "Recordatorio de Cita",
                                                message = "Tienes una cita mañana.",
                                                requestCode = 0
                                            )
                                        }
                                        // Notificación 1 hora antes, solo si la cita es al menos 1 hora en el futuro
                                        if (appointmentDate.time > currentTime + (60 * 60 * 1000)) {
                                            val hourBeforeMillis = appointmentDate.time - (60 * 60 * 1000)
                                            NotificationUtils.scheduleNotification(
                                                context = this,
                                                triggerAtMillis = hourBeforeMillis,
                                                title = "Recordatorio de Cita",
                                                message = "Tienes una cita en 1 hora.",
                                                requestCode = 1
                                            )
                                        }
                                        finish() // Regresa a la pantalla anterior
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this, "Error al agendar cita: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error en la validación: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Cargar médicos desde Firestore y actualizar el Spinner
    private fun loadDoctorsIntoSpinner() {
        db.collection("medicos")
            .get()
            .addOnSuccessListener { querySnapshot ->
                doctorsList.clear()
                for (document in querySnapshot.documents) {
                    val doctor = document.toObject(Doctor::class.java)
                    doctor?.let {
                        it.id = document.id
                        doctorsList.add(it)
                    }
                }
                val doctorDisplayList = doctorsList.map { it.toString() }
                val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, doctorDisplayList)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerDoctor.adapter = spinnerAdapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar médicos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
