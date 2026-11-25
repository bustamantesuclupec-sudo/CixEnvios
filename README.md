# CixEnvios

API educativa para calcular el costo y el tiempo estimado de envío
de paquetes desde Chiclayo hacia diferentes ciudades del Perú.

## Tecnologías

El repositorio contiene dos implementaciones independientes:

- Java con Spring Boot.
- Node.js con Express.

## Operación

POST /api/envios/calcular

### Entrada

```json
{
  "pesoKg": 2.5,
  "ciudadDestino": "Motupe"
}
```

### Respuesta esperada

```json
{
  "costoEnvio": 13,
  "diasEstimados": 1
}
```

## Regla de cálculo

costoEnvio = tarifaBase + (pesoKg × precioPorKg)

## Destinos definidos

- Chiclayo
- Lambayeque
- Motupe
- Trujillo
- Lima

## Validaciones

- El peso debe ser un número mayor que cero.
- La ciudad debe ser un texto no vacío.
- El destino debe encontrarse dentro de la cobertura definida.

## Ejecución

### Java

Desde la carpeta java:

Windows:
    .\mvnw.cmd spring-boot:run

Linux o macOS:
    ./mvnw spring-boot:run

Puerto: 8080.

### Node.js

Desde la carpeta node:

    npm install
    node server.js

Puerto: 3000.