package backend;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RegistroDAO {
    public static class Item {
        private final int id;
        private final String nombre;

        public Item(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    public static class DatoGrafica {
        private final String fechaHora;
        private final double valor;
        private final String ubicacionSensor;

        public DatoGrafica(String fechaHora, double valor, String ubicacionSensor) {
            this.fechaHora = fechaHora;
            this.valor = valor;
            this.ubicacionSensor = ubicacionSensor;
        }

        public String getFechaHora() {
            return fechaHora;
        }

        public double getValor() {
            return valor;
        }

        public String getUbicacionSensor() {
            return ubicacionSensor;
        }
    }

    private Connection getConnection() throws SQLException {
        Properties properties = loadDatabaseConfig();
        String password = getConfig(properties, "db.password", "PISICOLA_DB_PASSWORD", "");
        if (password.isBlank()) {
            throw new SQLException(
                    "Falta db.password en config.properties o la variable PISICOLA_DB_PASSWORD."
            );
        }

        String url = getConfig(properties, "db.url", "PISICOLA_DB_URL", "");
        if (url.isBlank()) {
            String host = getConfig(properties, "db.host", "PISICOLA_DB_HOST", "localhost");
            String port = getConfig(properties, "db.port", "PISICOLA_DB_PORT", "5432");
            String database = getConfig(properties, "db.name", "PISICOLA_DB_NAME", "pisicola");
            validatePort(port);
            url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        }
        String user = getConfig(properties, "db.user", "PISICOLA_DB_USER", "postgres");
        return DriverManager.getConnection(url, user, password);
    }

    private static Properties loadDatabaseConfig() throws SQLException {
        String configuredPath = System.getProperty("pisicola.config.file");
        if (configuredPath != null && !configuredPath.isBlank()) {
            return readDatabaseConfig(Path.of(configuredPath));
        }

        Path[] candidates = {
                Path.of("config.properties"),
                getJarDirectory().resolve("config.properties"),
                Path.of("src", "main", "java", "backend", "config.properties"),
                Path.of("..", "src", "main", "java", "backend", "config.properties").normalize(),
                Path.of(System.getProperty("user.home"), ".pisicola", "config.properties")
        };
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return readDatabaseConfig(candidate);
            }
        }
        return new Properties();
    }

    private static Path getJarDirectory() throws SQLException {
        try {
            return Path.of(RegistroDAO.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI()).toAbsolutePath().getParent();
        } catch (URISyntaxException | NullPointerException e) {
            throw new SQLException("No se pudo determinar dónde está la aplicación.", e);
        }
    }

    private static Properties readDatabaseConfig(Path configPath) throws SQLException {
        Properties properties = new Properties();
        try (var input = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            properties.load(input);
        } catch (IOException e) {
            throw new SQLException("No se pudo leer el archivo de configuración: " + configPath, e);
        }
        return properties;
    }

    private static String getConfig(
            Properties properties, String key, String environmentVariable, String defaultValue
    ) {
        String value = System.getProperty("pisicola." + key);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariable);
        }
        if (value == null || value.isBlank()) {
            value = properties.getProperty(key);
        }
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static void validatePort(String port) throws SQLException {
        try {
            int portNumber = Integer.parseInt(port);
            if (portNumber < 1 || portNumber > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new SQLException("db.port debe ser un número entre 1 y 65535.", e);
        }
    }

    public List<Item> obtenerSensores() throws SQLException {
        List<Item> sensores = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_sensor, ubicacion FROM sensor")) {
            while (rs.next()) {
                sensores.add(new Item(rs.getInt("id_sensor"), rs.getString("ubicacion")));
            }
        }
        return sensores;
    }

    public List<Item> obtenerParametros() throws SQLException {
        List<Item> parametros = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_parametro, nombre FROM parametro")) {
            while (rs.next()) {
                parametros.add(new Item(rs.getInt("id_parametro"), rs.getString("nombre")));
            }
        }
        return parametros;
    }

    public List<DatoGrafica> obtenerHistorialPorFechas(
            int idParametro, LocalDate fechaInicio, LocalDate fechaFin
    ) throws SQLException {
        List<DatoGrafica> historial = new ArrayList<>();
        String sql = "SELECT l.fecha_hora, l.valor, s.ubicacion "
                + "FROM lectura l "
                + "JOIN sensor s ON l.id_sensor = s.id_sensor "
                + "WHERE l.id_parametro = ? AND DATE(l.fecha_hora) BETWEEN ? AND ? "
                + "ORDER BY l.fecha_hora ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idParametro);
            pstmt.setDate(2, java.sql.Date.valueOf(fechaInicio));
            pstmt.setDate(3, java.sql.Date.valueOf(fechaFin));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String fechaHora = rs.getTimestamp("fecha_hora")
                            .toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    historial.add(new DatoGrafica(
                            fechaHora,
                            rs.getDouble("valor"),
                            rs.getString("ubicacion")
                    ));
                }
            }
        }
        return historial;
    }

    public int registrarLectura(int idSensor, int idParametro, double valor) throws SQLException {
        String sql = "INSERT INTO lectura (id_sensor, id_parametro, valor) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, idSensor);
            pstmt.setInt(2, idParametro);
            pstmt.setDouble(3, valor);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return -1;
        }
    }

    public String evaluarYGenerarAlerta(
            int idLectura, int idSensor, int idParametro, double valorActual
    ) throws SQLException {
        String tipoAlerta = null;
        String mensajeSms = null;
        double umbralMin;
        double umbralMax;
        String nombreParametro;

        try (Connection conn = getConnection()) {
            String sqlParam = "SELECT nombre, umbral_min, umbral_max FROM parametro WHERE id_parametro = ?";
            try (PreparedStatement pst = conn.prepareStatement(sqlParam)) {
                pst.setInt(1, idParametro);
                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("No existe el parámetro con id " + idParametro + ".");
                    }
                    nombreParametro = rs.getString("nombre");
                    umbralMin = rs.getDouble("umbral_min");
                    umbralMax = rs.getDouble("umbral_max");
                }
            }

            if (valorActual < umbralMin || valorActual > umbralMax) {
                tipoAlerta = "Umbral Riesgoso";
                mensajeSms = "ALERTA SMS: El " + nombreParametro + " (" + valorActual
                        + ") está fuera del límite seguro (" + umbralMin + " - " + umbralMax + ").";
            } else {
                String sqlAnterior = "SELECT valor FROM lectura "
                        + "WHERE id_sensor = ? AND id_parametro = ? AND id_lectura != ? "
                        + "ORDER BY fecha_hora DESC LIMIT 1";
                try (PreparedStatement pst = conn.prepareStatement(sqlAnterior)) {
                    pst.setInt(1, idSensor);
                    pst.setInt(2, idParametro);
                    pst.setInt(3, idLectura);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            double valorAnterior = rs.getDouble("valor");
                            double diferencia = Math.abs(valorActual - valorAnterior);
                            if (valorAnterior > 0 && (diferencia / valorAnterior) >= 0.30) {
                                tipoAlerta = "Cambio Abrupto";
                                mensajeSms = "ALERTA SMS: Variación brusca detectada en "
                                        + nombreParametro + ". Pasó de " + valorAnterior
                                        + " a " + valorActual + ".";
                            }
                        }
                    }
                }
            }

            if (tipoAlerta != null) {
                String sqlInsertAlerta = "INSERT INTO alerta "
                        + "(id_lectura, tipo_alerta, mensaje_enviado) VALUES (?, ?, ?)";
                try (PreparedStatement pst = conn.prepareStatement(sqlInsertAlerta)) {
                    pst.setInt(1, idLectura);
                    pst.setString(2, tipoAlerta);
                    pst.setString(3, mensajeSms);
                    pst.executeUpdate();
                }
                return mensajeSms;
            }
        }
        return null;
    }

    public List<String> obtenerReporteAmoniaco() throws SQLException {
        List<String> reporte = new ArrayList<>();
        String sql = "SELECT s.ubicacion, l.valor, l.fecha_hora "
                + "FROM lectura l "
                + "JOIN sensor s ON l.id_sensor = s.id_sensor "
                + "JOIN parametro p ON l.id_parametro = p.id_parametro "
                + "WHERE LOWER(p.nombre) = LOWER(?) "
                + "ORDER BY l.fecha_hora DESC LIMIT 10";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "Amoniaco");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String fechaHora = rs.getTimestamp("fecha_hora")
                            .toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    reporte.add("Ubicación: " + rs.getString("ubicacion")
                            + " | Nivel: " + rs.getDouble("valor")
                            + " | Fecha: " + fechaHora);
                }
            }
        }
        return reporte;
    }

    public boolean registrarMantenimiento(
            String tipoActividad, LocalDate fechaProgramada, String observaciones
    ) throws SQLException {
        String sql = "INSERT INTO mantenimiento "
                + "(tipo_actividad, fecha_programada, observaciones) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tipoActividad);
            pstmt.setDate(2, java.sql.Date.valueOf(fechaProgramada));
            pstmt.setString(3, observaciones);
            return pstmt.executeUpdate() > 0;
        }
    }
}
