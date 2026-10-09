// =====================================================================
// FICHERO : ux.js
// PROPOSITO: UX del NAVEGADOR. Muestra el historial de mediciones que
//            llega de LogicaFake.recuperarMedicion() y se refresca solo,
//            sin ningun boton que pulsar.
// USO      : Sprint 0 - Proyecto Beacon. Pinta una tabla con TODOS los
//            campos de cada medicion (id, tipo_medicion, valor,
//            nombre_emisora, fecha_hora), de la mas reciente a la mas
//            antigua, y avisa de los estados cargando / vacio / error.
// REGLA    : La UX solo pinta. No hace peticiones: de eso se encarga
//            LogicaFake.
// =====================================================================

// Objeto UX: la interfaz de la web (un solo objeto).
var UX = {

    // ---- Elemento del DOM donde se pinta todo ----
    // Es el <main id="contenedorMediciones"> del index.html.
    contenedor: null,

    // ---- Cada cuanto se refresca la tabla sola, en milisegundos ----
    intervaloMs: 5000,

    // ---- Evita que se solapen peticiones ----
    // Si llega una peticion mientras la anterior sigue en vuelo, la
    // nueva se descarta en vez de saturar el servidor.
    peticionEnCurso: false,

    // =================================================================
    // Funcion: iniciar()
    // PROPOSITO: Arranca la web al cargar la pagina: pinta el estado
    //            "cargando", hace la primera peticion y programa el
    //            refresco automatico cada intervaloMs.
    // PARAM.   : ninguno.
    // RETORNA  : nada.
    // () ----------------------------------------------------------------
    iniciar: function () {
        // ---- 1. Localizamos el contenedor del HTML ----
        this.contenedor = document.getElementById('contenedorMediciones');

        // ---- 2. Primeiro estado: cargando ----
        console.log('>>>> [UX] inicio la web');
        this.pintarEstado('cargando', 'Cargando mediciones...');

        // ---- 3. Primera carga inmediata ----
        this.recargarMediciones();

        // ---- 4. Y a partir de ahi, refresco solo cada 5 segundos ----
        // No hay boton "Recargar": la tabla se actualiza sola.
        const self = this;
        this.timer = setInterval(function () {
            self.recargarMediciones();
        }, this.intervaloMs);
    },

    // =================================================================
    // Funcion: recargarMediciones()
    // PROPOSITO: Vuelve a pedir al servidor la lista de mediciones y la
    //            pinta. Si el servidor falla, pinta el estado de error.
    // PARAM.   : ninguno.
    // RETORNA  : nada (es asincrona; no se espera su resultado).
    // () ----------------------------------------------------------------
    recargarMediciones: async function () {
        // ---- 1. Si ya hay una peticion en vuelo, la ignoramos ----
        if (this.peticionEnCurso) {
            console.log('>>>> [UX] hay una peticion en curso, no lanzo otra');
            return;
        }
        this.peticionEnCurso = true;

        try {
            // ---- 2. Pedimos la lista al servidor ----
            const mediciones = await LogicaFake.recuperarMedicion();

            // ---- 3. La pintamos ----
            this.pintarMediciones(mediciones);
        } catch (e) {
            // ---- 4. Si falla, lo decimos en pantalla ----
            console.log('>>>> [UX] error al pedir las mediciones:', e.message);
            this.pintarEstado('error', e.message);
        } finally {
            this.peticionEnCurso = false;
        }
    },

        // =================================================================
    // Funcion: claseValor()
    // PROPOSITO: Decide el color del valor medido segun su gravedad.
    // PARAM.   : number valor - el valor de la medicion (campo "valor").
    // RETORNA  : string - clase CSS ('valor-alto', 'valor-medio' o '').
    // () ----------------------------------------------------------------
    claseValor: function (valor) {
        const n = Number(valor);
        if (isNaN(n)) {
            return '';
        }
        if (n >= 1000) { return ' valor-alto'; }    // rojo
        if (n >= 500)  { return ' valor-medio'; }   // amarillo
        return '';                                   // negro
    },
    
    // =================================================================
    // Funcion: pintarMediciones()
    // PROPOSITO: Pinta la tabla con todas las Mediciones recibidas, de
    //            la mas reciente a la mas antigua (el servidor ya las
    //            devuelve ordenadas asi). Si la lista esta vacia,
    //            pinta el estado "vacio".
    // PARAM.   : Array mediciones - lista de Mediciones.
    // RETORNA  : nada.
    // () ----------------------------------------------------------------
    pintarMediciones: function (mediciones) {
        // ---- 1. Sin datos: lo decimos y no pintamos tabla ----
        if (!mediciones || mediciones.length === 0) {
            console.log('>>>> [UX] no hay mediciones que pintar');
            this.pintarEstado('vacio', 'No hay mediciones todavía.');
            return;
        }

        console.log('>>>> [UX] pinto', mediciones.length, 'mediciones');

        // ---- 2. Cabecera de la tabla: TODOS los campos ----
        const columnas = ['id', 'tipo_medicion', 'valor', 'nombre_emisora', 'fecha_hora'];
        const cabeceras = {
            id: 'id',
            tipo_medicion: 'tipo_medicion',
            valor: 'valor',
            nombre_emisora: 'nombre_emisora',
            fecha_hora: 'fecha_hora'
        };

        let html = '<table class="mediciones">';
        html += '<thead><tr>';
        for (const campo of columnas) {
            html += '<th>' + cabeceras[campo] + '</th>';
        }
        html += '</tr></thead>';

        // ---- 3. Una fila por medicion ----
        html += '<tbody>';
        for (const m of mediciones) {
            html += '<tr class="medicion">';
            for (const campo of columnas) {
                // La fecha se muestra en formato espanol dd/mm/aaaa hh:mm:ss.
                const valor = campo === 'fecha_hora' ? this.formatearFecha(m.fecha_hora) : m[campo];
                // Un campo nulo (metadato opcional) se muestra como "-".
                // data-etiqueta lleva el nombre del campo: en movil el
                // CSS lo usa como etiqueta, porque las columnas se apilan.
                                // El valor lleva ademas la clase de color segun su valor.
                let clases = 'medicion-' + campo;
                if (campo === 'valor') {
                    clases += this.claseValor(m.valor);
                }

                html += '<td class="' + clases + '" data-etiqueta="'
                      + cabeceras[campo] + '">'
                      + (valor === null || valor === undefined || valor === '' ? '-' : valor)
                      + '</td>';
            }
            html += '</tr>';
        }
        html += '</tbody></table>';

        // ---- 4. Volcamos el HTML en el contenedor ----
        this.contenedor.innerHTML = html;
    },

    // =================================================================
    // Funcion: pintarEstado()
    // PROPOSITO: Pinta un aviso en lugar de la tabla cuando no hay nada
    //            que mostrar: cargando, vacio o error.
    // PARAM.   : string tipo - 'cargando' | 'vacio' | 'error'.
    //            string mensaje - texto que se muestra.
    // RETORNA  : nada.
    // () ----------------------------------------------------------------
    pintarEstado: function (tipo, mensaje) {
        // ---- 1. Solo texto y clase: el HTML va con textContent ----
        // Asi el mensaje del servidor nunca se interpreta como HTML.
        const aviso = document.createElement('p');
        aviso.className = 'estado estado-' + tipo;
        aviso.textContent = mensaje;

        // ---- 2. Reemplazamos el contenido del contenedor ----
        this.contenedor.innerHTML = '';
        this.contenedor.appendChild(aviso);
    },

    // =================================================================
    // Funcion: formatearFecha()
    // PROPOSITO: Convierte la fecha que da el servidor
    //            ("2026-09-28 09:00:00") al formato espanol
    //            ("28/09/2026 09:00:00").
    // PARAM.   : string fecha - fecha y hora del servidor.
    // RETORNA  : string - la fecha en formato espanol, o "-" si no vale.
    // () ----------------------------------------------------------------
    formatearFecha: function (fecha) {
        if (!fecha) {
            return '-';
        }
        const partes = String(fecha).split(' ');
        if (partes.length < 2) {
            return String(fecha);
        }
        const dia = partes[0].split('-');          // [aaaa, mm, dd]
        if (dia.length !== 3) {
            return String(fecha);
        }
        return dia[2] + '/' + dia[1] + '/' + dia[0] + ' ' + partes[1];
    }
};

// () Fin de UX.

// ---- Arrancamos la web en cuanto el navegador tiene el DOM listo ----
if (typeof document !== 'undefined') {
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function () { UX.iniciar(); });
    } else {
        UX.iniciar();
    }
}

// ---- Exportamos el objeto para poder probarlo desde Node ----
if (typeof module !== 'undefined' && module.exports) {
    module.exports = UX;
}