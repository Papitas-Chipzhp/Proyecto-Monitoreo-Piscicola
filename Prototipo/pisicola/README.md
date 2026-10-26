# 🐟 Pisicola Monitor

### Sistema de Monitoreo de Variables Fisicoquímicas en Estanques Piscícolas

Pisicola Monitor es una aplicación de escritorio diseñada para el **registro**, **monitoreo** y **análisis histórico** de variables ambientales en estanques de producción piscícola. Permite registrar lecturas de sensores, generar **alertas tempranas** cuando un valor sale de rango y programar actividades de **mantenimiento del fondo** de los estanques.

*<ins>Variables gestionadas actualmente: pH y Temperatura.</ins>*

---

## Ficha de Acceso Rápido

| Recurso | Ubicación | Contenido e Instrucciones |
| :--- | :--- | :--- |
| 📦 **Aplicación ejecutable** | `pisicola-monitor-1_0_0.jar` | Aplicación lista para ejecutar. Incluye JavaFX y el driver de PostgreSQL. **Solo Windows.** |
| 🗄️ **Respaldo de Base de Datos** | `respaldo_utf8.sql` | Esquema completo (5 tablas) y datos de ejemplo. Se restaura con `psql` (ver [Configuración](#configuración-del-sistema)). |
| ⚙️ **Archivo de Configuración** | `config.properties` | Datos de conexión a PostgreSQL. Debe quedar **junto al `.jar`**. |
| 🚀 **Inicio rápido** | [Ir a la sección](#inicio-rápido-3-pasos) | Tres pasos para dejar el sistema funcionando. |

---

## Inicio Rápido (3 pasos)

```powershell
# 1. Crear la base de datos
psql -U postgres -c "CREATE DATABASE pisicola;"
# 2. Restaurar el respaldo
psql -U postgres -d pisicola -f respaldo_utf8.sql
# 3. Ejecutar la aplicación (con config.properties en la misma carpeta)
java -jar pisicola-monitor-1_0_0.jar
```
> Antes del paso 3, edite `config.properties` y escriba su contraseña de PostgreSQL en `db.password`.

---

## Funcionalidades

| Caso de Uso | Descripción |
| :--- | :--- |
| **Registro de lecturas** | El usuario selecciona un sensor (ubicación), un parámetro y el valor medido. La lectura se guarda en la base de datos con fecha y hora automáticas. |
| **Alertas tempranas** | Tras cada lectura el sistema la evalúa y genera una alerta si:<br>• **Umbral Riesgoso:** el valor sale del rango seguro del parámetro.<br>• **Cambio Abrupto:** el valor varía **30 % o más** respecto a la lectura anterior del mismo sensor y parámetro.<br>La alerta se muestra en pantalla y queda guardada en la tabla `alerta`. |
| **Gráficas de adaptación (CU-03)** | Gráfica de líneas con la evolución de un parámetro en un rango de fechas, con una serie por sensor. |
| **Gestión de mantenimiento (CU-05)** | Reporte de las 10 últimas lecturas de amoníaco y programación de actividades (*Limpieza* o *Aplicación de abono*) con fecha y observaciones. |

### Umbrales configurados

| Parámetro | Tipo | Mínimo seguro | Máximo seguro |
| :--- | :--- | :---: | :---: |
| pH | químico | 6.50 | 8.50 |
| Temperatura | físico | 24.00 °C | 30.00 °C |

---

## Arquitectura del Proyecto

La aplicación está organizada en dos capas, cada una en su propio paquete Java:

| Paquete | Clases | Descripción |
| :--- | :--- | :--- |
| **Frontend** | `Launcher`, `RegistroMetricasApp` | **Interfaz gráfica en JavaFX.** Ventana principal de registro, ventana de historial con gráficas y ventana de mantenimiento. `Launcher` es el punto de entrada del `.jar`. |
| **backend** | `RegistroDAO` | **Acceso a datos (patrón DAO).** Concentra toda la lógica de conexión JDBC, consultas SQL, evaluación de alertas y lectura de la configuración. |

La interfaz nunca ejecuta SQL directamente: toda operación pasa por `RegistroDAO`, y todas las consultas usan `PreparedStatement` para evitar inyección SQL.

---

## Modelo de Datos

```
sensor (id_sensor PK, ubicacion, descripcion)
parametro (id_parametro PK, nombre, tipo_medicion, umbral_min, umbral_max)
lectura (id_lectura PK, id_sensor FK, id_parametro FK, valor, fecha_hora)
alerta (id_alerta PK, id_lectura FK, tipo_alerta, mensaje_enviado, fecha_emision)
mantenimiento (id_mantenimiento PK, tipo_actividad, fecha_programada, estado, observaciones)
```

```
sensor ──┐
         ├──< lectura ──< alerta
parametro┘
mantenimiento  (independiente)
```

---

## Stack Tecnológico

- **Lenguaje:** Java (compilado con JDK 25).
- **Interfaz Gráfica:** JavaFX 17.0.20 (controles y gráficos).
- **Base de Datos:** PostgreSQL 17 o 18.
- **Conexión:** JDBC con el driver `postgresql` 42.7.3.
- **Gestor de Dependencias y Empaquetado:** Maven (`maven-shade-plugin`, genera un `.jar` único con todas las librerías).

---

## Requisitos

| Requisito | Detalle |
| :--- | :--- |
| **Sistema operativo** | Windows (el `.jar` incluye las librerías nativas de JavaFX para Windows). |
| **Java** | **Java 25 o superior.** Verifique con `java -version`. |
| **PostgreSQL** | Versión 17 o 18 (el respaldo se generó con la 18.6). Anote la contraseña del usuario `postgres` al instalar. |
| **psql** | Viene con PostgreSQL, normalmente en `C:\Program Files\PostgreSQL\18\bin`. Si el comando no se reconoce, agregue esa carpeta al `PATH`. |

---

## Configuración del Sistema

### Base de Datos

Abra PowerShell o CMD en la carpeta donde están los archivos y ejecute:

```powershell
psql -U postgres -c "CREATE DATABASE pisicola;"
psql -U postgres -d pisicola -f respaldo_utf8.sql
```

Verifique que todo quedó bien:

```powershell
psql -U postgres -d pisicola -c "SELECT * FROM sensor;"
```

Debe mostrar los sensores **Estanque 1** y **Estanque 2**.

> ⚠️ El respaldo incluye algunas **lecturas y alertas de prueba**. Si desea empezar con la base limpia:
> ```sql
> TRUNCATE alerta, lectura, mantenimiento RESTART IDENTITY;
> ```
> Esto conserva los sensores y parámetros.

### Conexión (`config.properties`)

Edite el archivo con sus datos:

```properties
db.host=localhost
db.port=5432
db.name=pisicola
db.user=postgres
db.password=TU_CONTRASEÑA
```

`db.password` es **obligatorio**. Sin él, la aplicación muestra un error de conexión.

**Orden de búsqueda del archivo** (se usa el primero que exista):

1. Archivo indicado con `-Dpisicola.config.file=ruta`
2. Carpeta desde donde se ejecuta `java -jar`
3. **Carpeta del `.jar`** ← recomendado
4. `C:\Users\TU_USUARIO\.pisicola\config.properties`

**Prioridad de los valores:** propiedad del sistema (`-Dpisicola.db.password=...`) → variable de entorno → `config.properties` → valor por defecto.

### Variables de entorno (alternativa al archivo)

| Variable | Equivale a | Valor por defecto |
| :--- | :--- | :--- |
| `PISICOLA_DB_PASSWORD` | `db.password` | *(obligatoria)* |
| `PISICOLA_DB_USER` | `db.user` | `postgres` |
| `PISICOLA_DB_HOST` | `db.host` | `localhost` |
| `PISICOLA_DB_PORT` | `db.port` | `5432` |
| `PISICOLA_DB_NAME` | `db.name` | `pisicola` |
| `PISICOLA_DB_URL` | `db.url` | *(se arma con host, puerto y nombre)* |

Ejemplo para no necesitar el archivo:

```powershell
setx PISICOLA_DB_PASSWORD "TU_CONTRASEÑA"
```

Cierre y vuelva a abrir la terminal para que tome efecto.

---

## Instalación y Ejecución

1. Instale **Java 25+** y **PostgreSQL**.
2. Cree la base de datos y restaure el respaldo (sección anterior).
3. Coloque `config.properties` **en la misma carpeta que el `.jar`** y escriba su contraseña.
4. Ejecute:
   ```powershell
   java -jar pisicola-monitor-1_0_0.jar
   ```
5. Se abrirá la ventana **"Simulador Sensor IoT - Monitoreo Piscícola"**.

### Compilar desde el código fuente (opcional)

```powershell
mvn clean package
java -jar target/pisicola-monitor-1.0.0.jar
```

---

## Solución de Problemas

| Problema | Causa probable | Solución |
| :--- | :--- | :--- |
| `Falta db.password en config.properties...` | No se encontró el archivo o no tiene la contraseña | Ponga `config.properties` junto al `.jar` y verifique `db.password` |
| `password authentication failed` | Contraseña incorrecta | Corrija `db.password` |
| `Connection refused` | PostgreSQL apagado o puerto distinto | Inicie el servicio de PostgreSQL y revise `db.port` |
| `database "pisicola" does not exist` | No se creó la base | Ejecute `CREATE DATABASE pisicola;` |
| `UnsupportedClassVersionError` | Java anterior a la versión 25 | Instale Java 25 o superior |
| Los desplegables salen vacíos | No se restauró el respaldo | Ejecute el `psql -f respaldo_utf8.sql` |
| `unrecognized backslash command \restrict` | `psql` antiguo | Actualice PostgreSQL (17.6 o superior) |
| Tildes con caracteres raros (`├í`) | Respaldo en codificación incorrecta | Use `respaldo_utf8.sql`, no el `respaldo.sql` original |

---

## Seguridad

- No suba `config.properties` con la contraseña real a repositorios; agréguelo al `.gitignore`.
- Para entregar el proyecto, comparta una copia con `db.password=` vacío.
- Todas las consultas SQL usan sentencias preparadas (`PreparedStatement`).

