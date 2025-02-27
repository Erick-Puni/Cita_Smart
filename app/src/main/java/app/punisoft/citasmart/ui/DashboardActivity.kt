package app.punisoft.citasmart.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import app.punisoft.citasmart.databinding.ActivityDashboardBinding
import app.punisoft.citasmart.util.NotificationUtils


class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private var selectedDate: String? = null  // Guardará la fecha seleccionada en el CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solicitar permiso para notificaciones en Android 13+ (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Verificar permiso para alarmas exactas en Android 12 (API 31) y superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Mostrar un diálogo explicativo para que el usuario habilite "Exact Alarms"
                AlertDialog.Builder(this)
                    .setTitle("Permiso de Alarmas Exactas")
                    .setMessage("Para que la app pueda programar recordatorios correctamente, es necesario habilitar el permiso de alarmas exactas. ¿Deseas ir a la configuración para habilitarlo?")
                    .setPositiveButton("Aceptar") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancelar") { _, _ ->
                        Toast.makeText(
                            this,
                            "No se habilitaron las alarmas exactas. Algunas notificaciones pueden no funcionar.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .show()
            }
        }

        // Crear el canal de notificaciones (importante para Android Oreo+)
        NotificationUtils.createNotificationChannel(this)

        // Listener para el CalendarView: captura la fecha seleccionada
        binding.calendarViewDashboard.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // El mes se devuelve en formato 0-indexado, por lo que se suma 1
            selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
        }

        // Configura el botón "Ver Citas": redirige a la Activity de citas con la fecha seleccionada
        binding.btnViewAppointments.setOnClickListener {
            if (selectedDate != null) {
                val intent = Intent(this, AppointmentsActivity::class.java)
                intent.putExtra("selectedDate", selectedDate)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Por favor, selecciona una fecha", Toast.LENGTH_SHORT).show()
            }
        }

        // Configura el botón "Solicitar Rol de Administrador": redirige a RequestAdminRoleActivity
        binding.btnRequestAdminRole.setOnClickListener {
            val intent = Intent(this, RequestAdminRoleActivity::class.java)
            startActivity(intent)
        }
    }
}
