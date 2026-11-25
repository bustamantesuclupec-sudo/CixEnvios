const express = require("express");
const fs = require("fs");
const path = require("path");

const app = express();
const PORT = 3000;

app.use(express.json());

const tarifas = new Map([
  ["chiclayo", { base: 4, precioKg: 1, dias: 1 }],
  ["lambayeque", { base: 5, precioKg: 2, dias: 1 }],
  ["motupe", { base: 8, precioKg: 2, dias: 1 }],
  ["trujillo", { base: 15, precioKg: 3, dias: 2 }],
  ["lima", { base: 20, precioKg: 4, dias: 3 }]
]);

const SOLICITUDES_FILE = path.join(__dirname, "Solicitudes.json");

function registrarSolicitud(info) {
  try {
    let solicitudes = [];

    if (fs.existsSync(SOLICITUDES_FILE)) {
      const contenido = fs.readFileSync(SOLICITUDES_FILE, "utf8");
      if (contenido.trim() !== "") {
        solicitudes = JSON.parse(contenido);
      }
    }

    solicitudes.push({
      fecha: new Date().toISOString(),
      ...info
    });

    fs.writeFileSync(
      SOLICITUDES_FILE,
      JSON.stringify(solicitudes, null, 2)
    );
  } catch (err) {
    console.error("Error registrando solicitud:", err);
  }
}

app.post("/api/envios/calcular", (req, res) => {
  const datos = req.body;

  const registrar = (estado, extra) =>
    registrarSolicitud({
      pesoKg: datos && datos.pesoKg,
      ciudadDestino: datos && datos.ciudadDestino,
      estado,
      ...extra
    });

  if (
    datos === null ||
    typeof datos !== "object" ||
    Array.isArray(datos)
  ) {
    registrar(400, { error: "Debe enviar un objeto JSON." });
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
    registrar(400, {
      error: "El peso debe ser mayor a 0."
    });
    return res.status(400).json({
      error: "El peso debe ser mayor a 0."
    });
  }

  if (
    typeof ciudadDestino !== "string" ||
    ciudadDestino.trim() === ""
  ) {
    registrar(400, {
      error: "El campo ciudadDestino debe ser un texto no vacío."
    });
    return res.status(400).json({
      error: "El campo ciudadDestino debe ser un texto no vacío."
    });
  }

  const destino = ciudadDestino.trim().toLowerCase();
  const tarifa = tarifas.get(destino);

  if (!tarifa) {
    registrar(400, {
      error: "No tenemos cobertura para la ciudad indicada."
    });
    return res.status(400).json({
      error: "No tenemos cobertura para la ciudad indicada."
    });
  }

  const costo = tarifa.base + pesoKg * tarifa.precioKg;

  if (!Number.isFinite(costo)) {
    registrar(400, { error: "El peso indicado es demasiado grande." });
    return res.status(400).json({
      error: "El peso indicado es demasiado grande."
    });
  }

  const costoEnvio = Number(costo.toFixed(2));

  registrar(200, { costoEnvio, diasEstimados: tarifa.dias });

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
