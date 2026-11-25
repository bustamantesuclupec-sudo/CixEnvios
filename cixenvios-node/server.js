const path = require("path");
const express = require("express");

const app = express();
const PORT = 3000;

app.use(express.json());
app.use(express.static(path.join(__dirname, "public")));

const tarifas = new Map([
  ["chiclayo", { base: 4, precioKg: 1, dias: 1 }],
  ["lambayeque", { base: 5, precioKg: 2, dias: 1 }],
  ["motupe", { base: 8, precioKg: 2, dias: 1 }],
  ["trujillo", { base: 15, precioKg: 3, dias: 2 }],
  ["lima", { base: 20, precioKg: 4, dias: 3 }]
]);

app.post("/api/envios/calcular", (req, res) => {
  const datos = req.body;

  if (
    datos === null ||
    typeof datos !== "object" ||
    Array.isArray(datos)
  ) {
    return res.status(400).json({
      error: "Debe enviar un objeto JSON."
    });
  }

  const { pesoKg, ciudadDestino } = datos;

  if (
    typeof pesoKg !== "number" ||
    !Number.isFinite(pesoKg) ||
    pesoKg <= 0
  ) {
    return res.status(400).json({
      error: "El campo pesoKg debe ser un número mayor que cero."
    });
  }

  if (
    typeof ciudadDestino !== "string" ||
    ciudadDestino.trim() === ""
  ) {
    return res.status(400).json({
      error: "El campo ciudadDestino debe ser un texto no vacío."
    });
  }

  const destino = ciudadDestino.trim().toLowerCase();
  const tarifa = tarifas.get(destino);

  if (!tarifa) {
    return res.status(400).json({
      error: "No tenemos cobertura para la ciudad indicada."
    });
  }

  const costo = tarifa.base + pesoKg * tarifa.precioKg;

  if (!Number.isFinite(costo)) {
    return res.status(400).json({
      error: "El peso indicado es demasiado grande."
    });
  }

  const costoEnvio = Number(costo.toFixed(2));

  return res.status(200).json({
    costoEnvio,
    diasEstimados: tarifa.dias
  });
});

app.use((req, res) => {
  res.status(404).json({
    error: "Ruta no encontrada. Use POST /api/envios/calcular."
  });
});

app.use((err, req, res, next) => {
  if (err.type === "entity.parse.failed") {
    return res.status(400).json({
      error: "El cuerpo debe ser un objeto JSON válido."
    });
  }

  const status = err.status || 500;

  return res.status(status).json({
    error:
      status === 500
        ? "Ocurrió un error interno en el servidor."
        : "No se pudo procesar la solicitud."
  });
});

app.listen(PORT, () => {
  console.log(
    `CixEnvios disponible en http://localhost:${PORT}`
  );
});
