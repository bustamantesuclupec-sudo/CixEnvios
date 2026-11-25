package com.cixenvios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {

    private record Tarifa(
        BigDecimal base,
        BigDecimal precioKg,
        int dias
    ) {}

    public record RespuestaEnvio(
        BigDecimal costoEnvio,
        int diasEstimados
    ) {}

    private static final Path ARCHIVO_SOLICITUDES =
        Path.of("Solicitudes.json");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, Tarifa> TARIFAS = Map.of(
        "chiclayo", new Tarifa(
            new BigDecimal("4"), new BigDecimal("1"), 1
        ),
        "lambayeque", new Tarifa(
            new BigDecimal("5"), new BigDecimal("2"), 1
        ),
        "motupe", new Tarifa(
            new BigDecimal("8"), new BigDecimal("2"), 1
        ),
        "trujillo", new Tarifa(
            new BigDecimal("15"), new BigDecimal("3"), 2
        ),
        "lima", new Tarifa(
            new BigDecimal("20"), new BigDecimal("4"), 3
        )
    );

    @PostMapping("/calcular")
    public RespuestaEnvio calcular(
        @RequestBody Map<String, Object> datos
    ) {
        try {
            RespuestaEnvio respuesta = procesar(datos);

            registrarSolicitud(datos, 200, Map.of(
                "costoEnvio", respuesta.costoEnvio(),
                "diasEstimados", respuesta.diasEstimados()
            ));

            return respuesta;
        } catch (IllegalArgumentException ex) {
            registrarSolicitud(datos, 400, Map.of(
                "error", ex.getMessage()
            ));
            throw ex;
        }
    }

    private RespuestaEnvio procesar(
        Map<String, Object> datos
    ) {
        if (datos == null) {
            throw new IllegalArgumentException(
                "Debe enviar un objeto JSON."
            );
        }

        Object pesoRecibido = datos.get("pesoKg");
        Object ciudadRecibida = datos.get("ciudadDestino");

        if (!(pesoRecibido instanceof Number)) {
            throw new IllegalArgumentException(
                "El campo pesoKg debe ser un número mayor que cero."
            );
        }

        BigDecimal peso;

        try {
            peso = new BigDecimal(pesoRecibido.toString());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                "El campo pesoKg debe ser un número válido."
            );
        }

        if (peso.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "El peso debe ser mayor a 0."
            );
        }

        if (!(ciudadRecibida instanceof String ciudad)
                || ciudad.isBlank()) {
            throw new IllegalArgumentException(
                "El campo ciudadDestino debe ser un texto no vacío."
            );
        }

        String destino = ciudad.trim().toLowerCase(Locale.ROOT);
        Tarifa tarifa = TARIFAS.get(destino);

        if (tarifa == null) {
            throw new IllegalArgumentException(
                "No tenemos cobertura para la ciudad indicada."
            );
        }

        BigDecimal costo = tarifa.base()
            .add(peso.multiply(tarifa.precioKg()))
            .setScale(2, RoundingMode.HALF_UP);

        return new RespuestaEnvio(costo, tarifa.dias());
    }

    private void registrarSolicitud(
        Map<String, Object> datos,
        int estado,
        Map<String, Object> detalle
    ) {
        try {
            List<Map<String, Object>> solicitudes =
                new ArrayList<>();

            if (Files.exists(ARCHIVO_SOLICITUDES)) {
                String contenido =
                    Files.readString(ARCHIVO_SOLICITUDES);

                if (!contenido.isBlank()) {
                    solicitudes = objectMapper.readValue(
                        contenido,
                        new TypeReference<
                            List<Map<String, Object>>
                        >() {}
                    );
                }
            }

            Map<String, Object> registro =
                new LinkedHashMap<>();
            registro.put("fecha", Instant.now().toString());

            if (datos != null) {
                registro.put("pesoKg", datos.get("pesoKg"));
                registro.put(
                    "ciudadDestino", datos.get("ciudadDestino"));
            }

            registro.put("estado", estado);
            registro.putAll(detalle);
            solicitudes.add(registro);

            Files.writeString(
                ARCHIVO_SOLICITUDES,
                objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(solicitudes)
            );
        } catch (Exception ex) {
            System.err.println(
                "Error registrando solicitud: " + ex
            );
        }
    }
}

@RestControllerAdvice
class ManejadorErrores {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(
        IllegalArgumentException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonIncorrecto(
        HttpMessageNotReadableException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "error",
                "El cuerpo debe ser un objeto JSON válido."
            ));
    }
}
