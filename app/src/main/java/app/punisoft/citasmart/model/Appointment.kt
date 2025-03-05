package app.punisoft.citasmart.model

data class Appointment(
    var id: String = "",
    var userId: String = "",
    var medicoId: String = "",
    var fecha: String = "",   
    var hora: String = "",
    var estado: String = "",
    var detalles: String = "",
    var doctorName: String? = null,
    var especialidad: String? = null,
    var dispensario: String? = null,
    var patientName: String? = null
)
