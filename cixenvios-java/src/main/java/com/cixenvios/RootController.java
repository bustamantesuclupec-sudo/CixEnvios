package com.cixenvios;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /api/envios/calcular no calcula: el cálculo es POST.
 * La página de inicio se sirve desde static/index.html.
 */
@RestController
public class RootController {

    private static final String MENSAJE_RUTA =
        "Ruta no encontrada. Use POST /api/envios/calcular.";

    @GetMapping("/api/envios/calcular")
    public ResponseEntity<Map<String, String>> calcularSoloPost() {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", MENSAJE_RUTA));
    }
}
