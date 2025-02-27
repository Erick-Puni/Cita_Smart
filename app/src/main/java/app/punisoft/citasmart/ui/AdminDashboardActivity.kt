package app.punisoft.citasmart.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.punisoft.citasmart.databinding.ActivityAdminDashboardBinding
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    // Variable para almacenar la fecha seleccionada; se inicia con la fecha actual.
    private var selectedDate: String = getTodayDate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Listener del CalendarView: al seleccionar una fecha, se actualiza selectedDate
        binding.calendarViewAdmin.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
        }

        // Listener para el botón "Ver Citas": redirige a la pantalla que muestra las citas para la fecha seleccionada
        binding.btnViewAdminAppointments.setOnClickListener {
            if (selectedDate.isNotEmpty()) {
                val intent = Intent(this, AdminAppointmentsActivity::class.java)
                intent.putExtra("selectedDate", selectedDate)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Por favor, selecciona una fecha", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener para el botón "Añadir Médico": redirige a ManageDoctorsActivity
        binding.btnAddDoctor.setOnClickListener {
            startActivity(Intent(this, ManageDoctorsActivity::class.java))
        }
    }

    // Función para obtener la fecha actual en formato "dd/MM/yyyy"
    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
}
