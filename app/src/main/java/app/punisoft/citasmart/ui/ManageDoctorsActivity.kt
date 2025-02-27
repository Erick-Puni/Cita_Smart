package app.punisoft.citasmart.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.punisoft.citasmart.databinding.ActivityManageDoctorsBinding

import com.google.firebase.firestore.FirebaseFirestore

class ManageDoctorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageDoctorsBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageDoctorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegisterDoctor.setOnClickListener {
            val name = binding.etDoctorName.text.toString().trim()
            val speciality = binding.etSpeciality.text.toString().trim()
            val dispensary = binding.etDispensary.text.toString().trim()
            val locality = binding.etLocality.text.toString().trim()

            // Validar que todos los campos obligatorios estén llenos
            if (name.isEmpty() || speciality.isEmpty() || dispensary.isEmpty() || locality.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Preparar los datos del médico
            val newDoctor = hashMapOf(
                "nombre" to name,
                "especialidad" to speciality,
                "dispensario" to dispensary,
                "localidad" to locality
            )

            // Agregar el médico a la colección "medicos"
            db.collection("medicos")
                .add(newDoctor)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Médico registrado con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al registrar médico: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
