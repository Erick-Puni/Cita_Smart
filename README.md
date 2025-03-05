<!-- README.md -->
# Cita Smart

Cita Smart es una aplicación Android desarrollada en **Kotlin** para la gestión de citas médicas en un centro de salud. Utiliza **Firebase** para autenticación, almacenamiento en Firestore y notificaciones locales para recordar a los usuarios sus citas.

## 📌 Funcionalidades

### 🔑 Autenticación de Usuarios
- Inicio de sesión con email/contraseña.
- Inicio de sesión con Google (usando Firebase Authentication).
- Registro de nuevos usuarios.

### 📅 Gestión de Citas
- Los pacientes pueden agendar citas con médicos disponibles.
- Validación para evitar agendar citas en fechas/horas pasadas.
- Verificación de disponibilidad del médico (se evita duplicidad de citas para el mismo horario).
- Los pacientes pueden cancelar sus citas (pero no editarlas).
- Los administradores pueden ver, editar y cancelar todas las citas.

### 📊 Dashboard y Roles
- Redirección basada en roles: pacientes y administradores (o recepcionistas).
- Los usuarios se registran en la colección **usuarios** en Firestore, y su rol se determina por el campo `rol`.
- Flujo para solicitar el rol de administrador mediante la colección **roleRequests**.

### 🔔 Notificaciones Locales
- **Recordatorios programados**: notificación **1 día antes** y otra **1 hora antes** de la cita.

### 🛠️ Otros
- Integración con **Firestore** para obtener datos en tiempo real.
- Uso de **Material Design** y **MVVM** (con ViewModel y LiveData) para una experiencia de usuario moderna y reactiva.

## 🚀 Tecnologías Utilizadas

| Tecnología  | Descripción |
|------------|------------|
| **Lenguaje** | Kotlin |
| **IDE** | Android Studio |
| **Firebase** | Autenticación, Firestore |

## 📜 Instalación y Uso

1. **Clona el repositorio:**
   ```bash
   git clone https://github.com/usuario/cita-smart.git
   ```
2. **Abre el proyecto en Android Studio.**
3. **Configura Firebase:**
   - Crea un proyecto en Firebase.
   - Descarga el archivo `google-services.json` y colócalo en `app/`.
   - Habilita Authentication, Firestore y Cloud Messaging.
4. **Ejecuta la aplicación en un emulador o dispositivo físico.**

## 📌 Contribuciones
¡Las contribuciones son bienvenidas! Si deseas contribuir, por favor abre un **Pull Request** o un **Issue** en el repositorio.

## 📄 Licencia
Este proyecto está bajo la licencia **MIT**. Puedes ver más detalles en el archivo `LICENSE`.

---
<p align="center">Hecho con ❤️ por <strong>PuniSoft</strong></p>
