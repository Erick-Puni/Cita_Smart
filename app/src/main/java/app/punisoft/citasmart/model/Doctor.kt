package app.punisoft.citasmart.model

data class Doctor(
    var id: String = "",
    val nombre: String = "",
    val especialidad: String = "",
    val dispensario: String = "",
    val localidad: String = ""
) {

    override fun toString(): String {
        return "$nombre - $especialidad, $dispensario ($localidad)"
    }
}
