package Frontend;

import backend.RegistroDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegistroMetricasApp extends Application {

    private ComboBox<RegistroDAO.Item> comboSensor;
    private ComboBox<RegistroDAO.Item> comboParametro;
    private TextField txtValor;
    private Label lblEstado;
    private RegistroDAO dao;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        dao = new RegistroDAO();
        primaryStage.setTitle("Simulador Sensor IoT - Monitoreo Piscícola");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        comboSensor = new ComboBox<>();
        comboSensor.setPromptText("Seleccione el Sensor");

        comboParametro = new ComboBox<>();
        comboParametro.setPromptText("Seleccione el Parámetro");

        txtValor = new TextField();
        txtValor.setPromptText("Valor de la lectura (ej. 6.5)");

        Button btnRegistrar = new Button("Registrar Lectura");
        Button btnVerHistorial = new Button("Ver Gráficas de Adaptación (CU-03)");
        Button btnMantenimiento = new Button("Gestionar Mantenimiento (CU-05)");
        lblEstado = new Label();

        cargarDatosDesplegables();
        btnRegistrar.setOnAction(e -> procesarRegistro());
        btnVerHistorial.setOnAction(e -> abrirVentanaHistorial());
        btnMantenimiento.setOnAction(e -> abrirVentanaMantenimiento());

        root.getChildren().addAll(
                new Label("Ubicación del Sensor:"), comboSensor,
                new Label("Parámetro Medido:"), comboParametro,
                new Label("Valor Registrado:"), txtValor,
                btnRegistrar, btnVerHistorial, btnMantenimiento, lblEstado
        );

        primaryStage.setScene(new Scene(root, 400, 380));
        primaryStage.show();
    }

    private void cargarDatosDesplegables() {
        try {
            comboSensor.getItems().clear();
            comboParametro.getItems().clear();
            comboSensor.getItems().addAll(dao.obtenerSensores());
            comboParametro.getItems().addAll(dao.obtenerParametros());
        } catch (SQLException e) {
            lblEstado.setText("Error de conexión BD: " + e.getMessage());
            lblEstado.setStyle("-fx-text-fill: red;");
        }
    }

    private void procesarRegistro() {
        RegistroDAO.Item sensor = comboSensor.getValue();
        RegistroDAO.Item parametro = comboParametro.getValue();
        String valorTexto = txtValor.getText();

        if (sensor == null || parametro == null || valorTexto == null || valorTexto.trim().isEmpty()) {
            lblEstado.setText("Por favor, complete todos los campos.");
            lblEstado.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            double valor = Double.parseDouble(valorTexto.trim());

            int idLectura = dao.registrarLectura(sensor.getId(), parametro.getId(), valor);
            if (idLectura > 0) {
                lblEstado.setText("Lectura registrada exitosamente.");
                lblEstado.setStyle("-fx-text-fill: green;");
                txtValor.clear();
                comboSensor.setValue(null);
                comboParametro.setValue(null);

                String mensajeAlerta;
                try {
                    mensajeAlerta = dao.evaluarYGenerarAlerta(
                            idLectura, sensor.getId(), parametro.getId(), valor
                    );
                } catch (SQLException e) {
                    lblEstado.setText("Lectura registrada, pero no se pudo evaluar la alerta: " + e.getMessage());
                    lblEstado.setStyle("-fx-text-fill: red;");
                    return;
                }

                if (mensajeAlerta != null) {
                    Alert alertaUI = new Alert(Alert.AlertType.WARNING);
                    alertaUI.setTitle("Sistema de Monitoreo Acuícola");
                    alertaUI.setHeaderText("Notificación de Alerta Temprana");
                    alertaUI.setContentText(mensajeAlerta);
                    alertaUI.showAndWait();
                }
            } else {
                lblEstado.setText("No se pudo registrar la lectura.");
                lblEstado.setStyle("-fx-text-fill: orange;");
            }
        } catch (NumberFormatException e) {
            lblEstado.setText("El valor debe ser un número válido.");
            lblEstado.setStyle("-fx-text-fill: red;");
        } catch (SQLException e) {
            lblEstado.setText("Error al guardar en BD: " + e.getMessage());
            lblEstado.setStyle("-fx-text-fill: red;");
        }
    }

    private void abrirVentanaHistorial() {
        Stage stageHistorial = new Stage();
        stageHistorial.setTitle("Historial y Gráficas de Adaptación");

        DatePicker dpInicio = new DatePicker(LocalDate.now().minusDays(7));
        DatePicker dpFin = new DatePicker(LocalDate.now());

        ComboBox<RegistroDAO.Item> comboParamGrafica = new ComboBox<>();
        comboParamGrafica.getItems().addAll(comboParametro.getItems());
        comboParamGrafica.setPromptText("Seleccione Parámetro");

        Button btnGenerar = new Button("Generar Gráfica");
        Label lblEstadoHistorial = new Label();
        HBox panelFiltros = new HBox(
                10,
                new Label("Desde:"), dpInicio,
                new Label("Hasta:"), dpFin,
                comboParamGrafica,
                btnGenerar
        );
        panelFiltros.setPadding(new Insets(10));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Fecha y Hora");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Valor de la Métrica");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Evolución del Parámetro");
        lineChart.setAnimated(false);

        btnGenerar.setOnAction(e -> {
            RegistroDAO.Item parametroSeleccionado = comboParamGrafica.getValue();
            LocalDate fechaInicio = dpInicio.getValue();
            LocalDate fechaFin = dpFin.getValue();

            if (parametroSeleccionado == null || fechaInicio == null || fechaFin == null) {
                lblEstadoHistorial.setText("Seleccione un parámetro y ambas fechas.");
                lblEstadoHistorial.setStyle("-fx-text-fill: red;");
                return;
            }
            if (fechaInicio.isAfter(fechaFin)) {
                lblEstadoHistorial.setText("La fecha inicial no puede ser posterior a la fecha final.");
                lblEstadoHistorial.setStyle("-fx-text-fill: red;");
                return;
            }

            try {
                List<RegistroDAO.DatoGrafica> datos = dao.obtenerHistorialPorFechas(
                        parametroSeleccionado.getId(), fechaInicio, fechaFin
                );
                lineChart.getData().clear();

                if (datos.isEmpty()) {
                    lblEstadoHistorial.setText("No hay lecturas para el parámetro y rango seleccionados.");
                    lblEstadoHistorial.setStyle("-fx-text-fill: orange;");
                    return;
                }

                lblEstadoHistorial.setText("");
                Map<String, List<RegistroDAO.DatoGrafica>> datosPorSensor = datos.stream()
                        .collect(Collectors.groupingBy(
                                RegistroDAO.DatoGrafica::getUbicacionSensor,
                                java.util.LinkedHashMap::new,
                                Collectors.toList()
                        ));

                for (Map.Entry<String, List<RegistroDAO.DatoGrafica>> entry : datosPorSensor.entrySet()) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Sensor: " + entry.getKey());
                    for (RegistroDAO.DatoGrafica dato : entry.getValue()) {
                        series.getData().add(new XYChart.Data<>(dato.getFechaHora(), dato.getValor()));
                    }
                    lineChart.getData().add(series);
                }
            } catch (SQLException ex) {
                lblEstadoHistorial.setText("Error al consultar el historial: " + ex.getMessage());
                lblEstadoHistorial.setStyle("-fx-text-fill: red;");
            }
        });

        VBox layoutGrafica = new VBox(10, panelFiltros, lblEstadoHistorial, lineChart);
        Scene scene = new Scene(layoutGrafica, 900, 550);
        stageHistorial.setScene(scene);
        stageHistorial.show();
    }

    private void abrirVentanaMantenimiento() {
        Stage stageMto = new Stage();
        stageMto.setTitle("Gestión de Mantenimiento del Fondo (CU-05)");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        Label lblReporte = new Label("Reporte Reciente de Amoniaco:");
        ListView<String> listaAmoniaco = new ListView<>();
        Label lblEstadoReporte = new Label();
        try {
            List<String> reporte = dao.obtenerReporteAmoniaco();
            if (reporte.isEmpty()) {
                listaAmoniaco.getItems().add("No hay registros de amoniaco recientes.");
            } else {
                listaAmoniaco.getItems().addAll(reporte);
            }
        } catch (SQLException e) {
            lblEstadoReporte.setText("Error al cargar el reporte: " + e.getMessage());
            lblEstadoReporte.setStyle("-fx-text-fill: red;");
        }
        listaAmoniaco.setPrefHeight(150);

        ComboBox<String> comboActividad = new ComboBox<>();
        comboActividad.getItems().addAll("Limpieza", "Aplicación de abono");
        comboActividad.setPromptText("Tipo de Actividad");

        DatePicker dpFecha = new DatePicker();
        dpFecha.setPromptText("Fecha Programada");

        TextField txtObservaciones = new TextField();
        txtObservaciones.setPromptText("Observaciones");

        Button btnGuardarMto = new Button("Registrar Mantenimiento");
        Label lblEstadoMto = new Label();

        btnGuardarMto.setOnAction(e -> {
            String tipoActividad = comboActividad.getValue();
            LocalDate fechaProgramada = dpFecha.getValue();
            if (tipoActividad == null || fechaProgramada == null) {
                lblEstadoMto.setText("Seleccione el tipo de actividad y la fecha.");
                lblEstadoMto.setStyle("-fx-text-fill: red;");
                return;
            }

            try {
                boolean exito = dao.registrarMantenimiento(
                        tipoActividad, fechaProgramada, txtObservaciones.getText().trim()
                );
                if (exito) {
                    lblEstadoMto.setText("Mantenimiento programado con éxito.");
                    lblEstadoMto.setStyle("-fx-text-fill: green;");
                    comboActividad.getSelectionModel().clearSelection();
                    dpFecha.setValue(null);
                    txtObservaciones.clear();
                } else {
                    lblEstadoMto.setText("No se pudo registrar el mantenimiento.");
                    lblEstadoMto.setStyle("-fx-text-fill: red;");
                }
            } catch (SQLException ex) {
                lblEstadoMto.setText("Error en base de datos: " + ex.getMessage());
                lblEstadoMto.setStyle("-fx-text-fill: red;");
            }
        });

        layout.getChildren().addAll(
                lblReporte, listaAmoniaco, lblEstadoReporte,
                new Label("Programar Nueva Actividad:"),
                comboActividad, dpFecha, txtObservaciones,
                btnGuardarMto, lblEstadoMto
        );

        stageMto.setScene(new Scene(layout, 450, 450));
        stageMto.show();
    }
}