# CixEnvios

API Node.js con Express para calcular el costo y el tiempo estimado de envío
de paquetes desde Chiclayo hacia diferentes ciudades del Perú.

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
- El formulario muestra errores y la API vuelve a validar los datos recibidos.

## Ejecución

En Windows, desde la raíz del repositorio:

```powershell
cd .\cixenvios-node
npm install
npm start
```

El servidor escucha en `http://localhost:3000`. Mantén abierta esa terminal
mientras usas la API.

Abre `http://localhost:3000` en el navegador para usar el formulario. La página
envía los datos a la API de Node.

## Probar la API

Envía una solicitud `POST` a `http://localhost:3000/api/envios/calcular`
con un cuerpo JSON:

```json
{
  "pesoKg": 2.5,
  "ciudadDestino": "Motupe"
}
```

En PowerShell puedes probarla desde otra terminal:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:3000/api/envios/calcular `
  -ContentType "application/json" `
  -Body '{"pesoKg":2.5,"ciudadDestino":"Motupe"}'
```

## Nota

Las tarifas y los tiempos de entrega son ficticios.

## Autores

- Bustamante Suclupe Carlos
- Lozano Vasquez Heidy
- Miñan Gonzalez Danna
- Zamora Segura Marcos