// =====================================================================
// FICHERO : logica_fake.test.js
// PROPOSITO: Prueba la logica fake del navegador (logica_fake.js): que
//            pide las mediciones por GET, limpia los datos y rechaza
//            cuando el servidor falla.
// USO      : Sprint 0 - Proyecto Beacon. Se ejecuta con Node puro,
//            mockeando el "fetch" global. Sin librerias externas.
// Ejecutar  : node web/tests/logica_fake.test.js
// =====================================================================

const { comprobar, igual, cierto, contiene, titulo, resumen } = require('./ayuda.js');
const LogicaFake = require('../js/logica_fake.js');

titulo('logica_fake.js - LogicaFake.recuperarMedicion()');

// ---- Guardamos el fetch real para poder devolverlo al final ----
const fetchOriginal = globalThis.fetch;

// ---- Funcion auxiliar: mockearFetch ----
// PROPOSITO: Sustituye el fetch global por una version falsa que
//            devuelve la respuesta que le pidamos y guarda la llamada.
// PARAM.   : object respuesta - { ok, status, json() }.
// RETORNA  : function obtenerLlamada - devuelve como se llamo a fetch.
function mockearFetch(respuesta) {
    const llamadas = [];
    globalThis.fetch = async function (url, opciones) {
        llamadas.push({ url: url, opciones: opciones });
        return respuesta;
    };
    return function obtenerLlamada() {
        return llamadas;
    };
}

// =====================================================================
// Prueba 1: resuelve segun lo que devuelve el servidor
// =====================================================================
comprobar('resuelve el listado y limpia los tipos de cada Medicion', async function () {
    // Respuesta tipica de MariaDB: los numeros pueden venir como texto.
    mockearFetch({
        ok: true,
        status: 200,
        json: async () => ({
            mediciones: [{
                id: 1,
                uuid: 'EPSG-GTI-PROY-3A',
                major: '2816',
                minor: '1234',
                tx_power: '4',
                nombre_emisora: 'Minerva_ELBACON',
                fecha_hora: '2026-09-28 09:00:00'
            }]
        })
    });

    return LogicaFake.recuperarMedicion().then(function (mediciones) {
        igual(mediciones.length, 1, 'numero de mediciones');
        igual(mediciones[0].minor, 1234, 'minor como numero');
        igual(mediciones[0].major, 2816, 'major como numero');
        igual(mediciones[0].tx_power, 4, 'tx_power como numero');
        igual(mediciones[0].nombre_emisora, 'Minerva_ELBACON', 'nombre_emisora');
        igual(mediciones[0].uuid, 'EPSG-GTI-PROY-3A', 'uuid');
        igual(mediciones[0].fecha_hora, '2026-09-28 09:00:00', 'fecha_hora');
    });
});

// =====================================================================
// Prueba 2: array vacio si no hay mediciones
// =====================================================================
comprobar('devuelve [] cuando la lista de mediciones esta vacia', async function () {
    mockearFetch({
        ok: true,
        status: 200,
        json: async () => ({ mediciones: [] })
    });

    return LogicaFake.recuperarMedicion().then(function (mediciones) {
        cierto(Array.isArray(mediciones), 'devuelve un array');
        igual(mediciones.length, 0, 'numero de mediciones');
    });
});

// =====================================================================
// Prueba 3: rechaza si el servidor falla
// =====================================================================
comprobar('rechaza con el error del servidor cuando la respuesta no es 200', async function () {
    mockearFetch({
        ok: false,
        status: 500,
        json: async () => ({ error: 'error interno del servidor' })
    });

    return LogicaFake.recuperarMedicion().then(function () {
        throw new Error('deberia haber rechazado y no lo hizo');
    }, function (e) {
        contiene(e.message, 'error');
    });
});

// =====================================================================
// Prueba 4: rechaza si "mediciones" no es una lista
// =====================================================================
comprobar('rechaza si la respuesta no trae una lista de mediciones', async function () {
    mockearFetch({
        ok: true,
        status: 200,
        json: async () => ({ otra_cosa: 1 })
    });

    return LogicaFake.recuperarMedicion().then(function () {
        throw new Error('deberia haber rechazado y no lo hizo');
    }, function (e) {
        contiene(e.message, 'no contiene una lista');
    });
});

// =====================================================================
// Prueba 5: llama a fetch con el verbo GET y la ruta /mediciones
// =====================================================================
comprobar('llama a fetch con el metodo GET y la ruta /mediciones', async function () {
    const obtenerLlamada = mockearFetch({
        ok: true,
        status: 200,
        json: async () => ({ mediciones: [] })
    });

    return LogicaFake.recuperarMedicion().then(function () {
        const llamadas = obtenerLlamada();
        igual(llamadas.length, 1, 'numero de llamadas a fetch');
        igual(llamadas[0].opciones.method, 'GET', 'verbo HTTP');
        contiene(llamadas[0].url, '/mediciones');
        igual(llamadas[0].url, LogicaFake.URL_MEDICIONES, 'usa URL_MEDICIONES');
    });
});

// ---- Devolvemos el fetch real ----
globalThis.fetch = fetchOriginal;

// ---- El resumen espera a que terminen los tests asincronos ----
resumen('logica_fake.test.js');