// Genera las ilustraciones de las pautas (ventanas, puertas y mamparas) que sirve el backend en /pautas/...
//
//   node herramientas/imagenes-pautas/generar.mjs
//
// Escribe los SVG en herramientas/imagenes-pautas/svg/ y los PNG (800x800) en src/main/resources/static/pautas/,
// junto con imagenes.json (la lista que usa el selector de imágenes). Los PNG se exportan con Chrome o Edge sin
// ventana; otra ruta se indica con la variable CHROME.
//
// Los nombres de archivo importan: la app deduce la forma del dibujo por palabras del nombre (granero, baño,
// mampara, batiente, puerta, arco, monorriel, corredera, fija) y las hojas por "<n>hojas".

import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const AQUI = dirname(fileURLToPath(import.meta.url));
const RAIZ = resolve(AQUI, '..', '..');
const DIR_SVG = join(AQUI, 'svg');
const DIR_PNG = join(RAIZ, 'src', 'main', 'resources', 'static', 'pautas');

const L = 800;
const C = {
    fondo: '#F2F4F7',
    piso: '#E1E5EA',
    muro: '#D9DEE4',
    marco: '#C3C9D1',
    hoja: '#D5DAE0',
    borde: '#7E8792',
    vidrio: '#CFE7F6',
    templado: '#D4EFEA',
    bordeTemplado: '#8FC7BA',
    reflejo: '#FFFFFF',
    acento: '#2B4C7E',
    herraje: '#4A525C',
    malla: '#9AA3AD',
};

let idClip = 0;

const rect = (x, y, w, h, fill, stroke = C.borde, sw = 3, extra = '') =>
    `<rect x="${x}" y="${y}" width="${w}" height="${h}" fill="${fill}" stroke="${stroke}" stroke-width="${sw}" ${extra}/>`;

/** Dos brillos diagonales dentro de un rectángulo de vidrio. */
function reflejos(x, y, w, h) {
    const s = Math.min(w, h);
    const l1 = [x + w * 0.14, y + h * 0.14 + s * 0.30, x + w * 0.14 + s * 0.30, y + h * 0.14];
    const l2 = [x + w * 0.14, y + h * 0.14 + s * 0.48, x + w * 0.14 + s * 0.48, y + h * 0.14];
    return [l1, l2]
        .map(([a, b, c, d], i) => `<line x1="${a}" y1="${b}" x2="${c}" y2="${d}" stroke="${C.reflejo}" stroke-width="${i ? 5 : 8}" stroke-linecap="round" opacity="0.75"/>`)
        .join('');
}

function vidrio(x, y, w, h, color = C.vidrio, borde = C.borde) {
    return rect(x, y, w, h, color, borde, 2) + reflejos(x, y, w, h);
}

/** Vidrio con forma libre (círculo, arco, trapecio): se recortan los brillos a la forma. */
function vidrioForma(d, caja, color = C.vidrio) {
    const id = `f${++idClip}`;
    const [x, y, w, h] = caja;
    return `<clipPath id="${id}"><path d="${d}"/></clipPath>` +
        `<path d="${d}" fill="${color}" stroke="${C.borde}" stroke-width="2"/>` +
        `<g clip-path="url(#${id})">${reflejos(x, y, w, h)}</g>`;
}

/** Marco fijo: el perfil exterior y el borde interior. */
const marco = (x, y, w, h, t = 22) => rect(x, y, w, h, C.marco, C.borde, 3) + rect(x + t, y + t, w - 2 * t, h - 2 * t, 'none', C.borde, 2);

/** Hoja: perfil de la hoja con su vidrio. */
const hoja = (x, y, w, h, t = 16) => rect(x, y, w, h, C.hoja, C.borde, 2.5) + vidrio(x + t, y + t, w - 2 * t, h - 2 * t);

/** Flecha de deslizamiento, horizontal, centrada en (cx, cy); dir = 1 a la derecha, -1 a la izquierda. */
function flecha(cx, cy, dir, largo = 90) {
    const x1 = cx - (dir * largo) / 2;
    const x2 = cx + (dir * largo) / 2;
    const p = x2 - dir * 22;
    return `<line x1="${x1}" y1="${cy}" x2="${p}" y2="${cy}" stroke="${C.acento}" stroke-width="7" stroke-linecap="round"/>` +
        `<polygon points="${x2},${cy} ${p},${cy - 15} ${p},${cy + 15}" fill="${C.acento}"/>`;
}

/**
 * Líneas de apertura (convención de planos): el vértice apunta al lado de las bisagras. lado: izq, der, arriba, abajo.
 */
function apertura(x, y, w, h, lado) {
    const pts = {
        izq: [[x + w, y], [x, y + h / 2], [x + w, y + h]],
        der: [[x, y], [x + w, y + h / 2], [x, y + h]],
        arriba: [[x, y + h], [x + w / 2, y], [x + w, y + h]],
        abajo: [[x, y], [x + w / 2, y + h], [x + w, y]],
    }[lado];
    return `<polyline points="${pts.map((p) => p.join(',')).join(' ')}" fill="none" stroke="${C.acento}" stroke-width="4" stroke-dasharray="16 11" stroke-linejoin="round" opacity="0.85"/>`;
}

const manillaV = (cx, cy, alto = 56) => rect(cx - 6, cy - alto / 2, 12, alto, C.herraje, C.herraje, 1, 'rx="6"');
const manillaH = (cx, cy, ancho = 56) => rect(cx - ancho / 2, cy - 6, ancho, 12, C.herraje, C.herraje, 1, 'rx="6"');

/** Escena de ventana: el antepecho bajo la ventana. */
const alfeizar = (x, y, w) => rect(x - 24, y, w + 48, 20, C.muro, C.borde, 2, 'rx="3"');
/** Escena de puerta o mampara: el piso. */
const piso = (y = 720) => rect(0, y, L, L - y, C.piso, 'none', 0) + `<line x1="0" y1="${y}" x2="${L}" y2="${y}" stroke="${C.borde}" stroke-width="2"/>`;

function svg(contenido) {
    return `<svg xmlns="http://www.w3.org/2000/svg" width="${L}" height="${L}" viewBox="0 0 ${L} ${L}">` +
        `<rect width="${L}" height="${L}" fill="${C.fondo}"/>${contenido}</svg>`;
}

// ---------------------------------------------------------------------------------------------------------------
// Ventanas

/** Corredera de n hojas; fijas: cuántas hojas de cada extremo no se mueven. */
function corredera(n, fijas = 0) {
    const x = 90, y = 170, w = 620, h = 440, t = 22;
    const ox = x + t, oy = y + t, ow = w - 2 * t, oh = h - 2 * t;
    const solape = 14;
    const ancho = ow / n;
    let atras = '', delante = '', marcas = '';
    for (let i = 0; i < n; i++) {
        const hx = ox + i * ancho - (i > 0 ? solape / 2 : 0);
        const hw = ancho + (i > 0 && i < n - 1 ? solape : solape / 2);
        const dibujo = hoja(hx, oy, hw, oh);
        if (i % 2 === 0) atras += dibujo; else delante += dibujo;
        const fija = i < fijas || i >= n - fijas;
        if (!fija) {
            const dir = i < n / 2 ? 1 : -1;
            marcas += flecha(hx + hw / 2, oy + oh * 0.62, dir, Math.min(110, hw * 0.55));
            const borde = dir === 1 ? hx + hw - 9 : hx + 9;
            marcas += manillaV(borde, oy + oh / 2);
        }
    }
    return svg(alfeizar(x, y + h, w) + marco(x, y, w, h, t) + atras + delante + marcas);
}

function monorriel() {
    const x = 110, y = 210, w = 580, h = 400, t = 22;
    const ox = x + t, oy = y + t, ow = w - 2 * t, oh = h - 2 * t;
    const riel = rect(x - 20, y - 46, w + 40, 26, C.herraje, C.herraje, 1, 'rx="5"');
    const fijo = vidrio(ox, oy, ow / 2, oh);
    // La hoja corre por fuera del marco, colgada del riel.
    const hx = x + w / 2 - 40, hw = w / 2 + 20;
    const colgadores = [hx + 40, hx + hw - 40].map((cx) => `<line x1="${cx}" y1="${y - 20}" x2="${cx}" y2="${y - 4}" stroke="${C.herraje}" stroke-width="6"/>`).join('');
    return svg(alfeizar(x, y + h, w) + marco(x, y, w, h, t) + fijo + riel + colgadores +
        hoja(hx, y - 4, hw, h + 8) + flecha(hx + hw / 2, oy + oh * 0.62, -1, 120) + manillaV(hx + 12, oy + oh / 2));
}

function fija() {
    const x = 130, y = 150, w = 540, h = 460, t = 24;
    return svg(alfeizar(x, y + h, w) + marco(x, y, w, h, t) + vidrio(x + t, y + t, w - 2 * t, h - 2 * t));
}

function fijaCircular() {
    const cx = 400, cy = 390, r = 270, t = 26;
    const anillo = `<circle cx="${cx}" cy="${cy}" r="${r}" fill="${C.marco}" stroke="${C.borde}" stroke-width="3"/>`;
    const ri = r - t;
    const d = `M ${cx - ri} ${cy} A ${ri} ${ri} 0 1 0 ${cx + ri} ${cy} A ${ri} ${ri} 0 1 0 ${cx - ri} ${cy} Z`;
    return svg(anillo + vidrioForma(d, [cx - ri, cy - ri, 2 * ri, 2 * ri]));
}

function fijaMedioPunto() {
    const cx = 400, base = 560, r = 300, t = 26;
    const exterior = `M ${cx - r} ${base} A ${r} ${r} 0 0 1 ${cx + r} ${base} Z`;
    const ri = r - t;
    const interior = `M ${cx - ri} ${base - t} A ${ri} ${ri} 0 0 1 ${cx + ri} ${base - t} Z`;
    return svg(alfeizar(cx - r, base, 2 * r) +
        `<path d="${exterior}" fill="${C.marco}" stroke="${C.borde}" stroke-width="3"/>` +
        vidrioForma(interior, [cx - ri, base - t - ri, 2 * ri, ri]));
}

function fijaTrapecio() {
    const x = 120, w = 560, base = 610, altoIzq = 300, altoDer = 470, t = 24;
    const exterior = `M ${x} ${base} L ${x} ${base - altoIzq} L ${x + w} ${base - altoDer} L ${x + w} ${base} Z`;
    // Interior aproximado: el mismo trapecio contraído por el grosor del marco.
    const pend = (altoDer - altoIzq) / w;
    const yi = (xx) => base - altoIzq - pend * (xx - x) + t * 1.04;
    const interior = `M ${x + t} ${base - t} L ${x + t} ${yi(x + t)} L ${x + w - t} ${yi(x + w - t)} L ${x + w - t} ${base - t} Z`;
    return svg(alfeizar(x, base, w) +
        `<path d="${exterior}" fill="${C.marco}" stroke="${C.borde}" stroke-width="3" stroke-linejoin="round"/>` +
        vidrioForma(interior, [x + t, base - altoDer, w - 2 * t, altoDer - t]));
}

/** Ventana con arcada: medio punto arriba (fijo) y dos hojas abajo. */
function arco() {
    const x = 170, w = 460, base = 650, r = w / 2, alto = 300, t = 22;
    const cx = x + r, arranque = base - alto;
    const exterior = `M ${x} ${base} L ${x} ${arranque} A ${r} ${r} 0 0 1 ${x + w} ${arranque} L ${x + w} ${base} Z`;
    const ri = r - t;
    const arcoInterior = `M ${x + t} ${arranque} A ${ri} ${ri} 0 0 1 ${x + w - t} ${arranque} Z`;
    const travesano = rect(x + t, arranque, w - 2 * t, 18, C.marco, C.borde, 2);
    const oy = arranque + 18, oh = base - t - oy, ow = (w - 2 * t) / 2;
    return svg(alfeizar(x, base, w) +
        `<path d="${exterior}" fill="${C.marco}" stroke="${C.borde}" stroke-width="3"/>` +
        vidrioForma(arcoInterior, [x + t, arranque - ri, w - 2 * t, ri]) + travesano +
        hoja(x + t, oy, ow, oh) + hoja(x + t + ow, oy, ow, oh) +
        apertura(x + t + 16, oy + 16, ow - 32, oh - 32, 'izq') + apertura(x + t + ow + 16, oy + 16, ow - 32, oh - 32, 'der') +
        manillaV(x + t + ow - 9, oy + oh / 2, 44) + manillaV(x + t + ow + 9, oy + oh / 2, 44));
}

/** Ventana de abrir de una hoja; lados: hacia dónde quedan las bisagras (uno o dos símbolos). */
function abrirUnaHoja(lados, manilla) {
    const x = 190, y = 140, w = 420, h = 480, t = 22;
    const ox = x + t, oy = y + t, ow = w - 2 * t, oh = h - 2 * t;
    const v = 16;
    const simbolos = lados.map((l) => apertura(ox + v, oy + v, ow - 2 * v, oh - 2 * v, l)).join('');
    const m = { der: manillaV(ox + ow - 9, oy + oh / 2), arriba: manillaH(ox + ow / 2, oy + 9), abajo: manillaH(ox + ow / 2, oy + oh - 9) }[manilla];
    return svg(alfeizar(x, y + h, w) + marco(x, y, w, h, t) + hoja(ox, oy, ow, oh) + simbolos + m);
}

function batienteDoble() {
    const x = 110, y = 160, w = 580, h = 450, t = 22;
    const ox = x + t, oy = y + t, ow = (w - 2 * t) / 2, oh = h - 2 * t, v = 16;
    return svg(alfeizar(x, y + h, w) + marco(x, y, w, h, t) +
        hoja(ox, oy, ow, oh) + hoja(ox + ow, oy, ow, oh) +
        apertura(ox + v, oy + v, ow - 2 * v, oh - 2 * v, 'izq') + apertura(ox + ow + v, oy + v, ow - 2 * v, oh - 2 * v, 'der') +
        manillaV(ox + ow - 9, oy + oh / 2) + manillaV(ox + ow + 9, oy + oh / 2));
}

function mosquitero() {
    const x = 150, y = 150, w = 500, h = 460, t = 18;
    const malla = `<pattern id="malla" width="10" height="10" patternUnits="userSpaceOnUse">` +
        `<path d="M10 0H0V10" fill="none" stroke="${C.malla}" stroke-width="1.2"/></pattern>`;
    const ox = x + t, oy = y + t, ow = w - 2 * t, oh = h - 2 * t;
    return svg(`<defs>${malla}</defs>` + alfeizar(x, y + h, w) + rect(x, y, w, h, C.marco, C.borde, 3) +
        rect(ox, oy, ow, oh, '#E7EBEF', C.borde, 2) + rect(ox, oy, ow, oh, 'url(#malla)', 'none', 0) +
        rect(x + w / 2 - 40, y + h - 12, 80, 20, C.herraje, C.herraje, 1, 'rx="6"'));
}

// ---------------------------------------------------------------------------------------------------------------
// Puertas y mamparas

function puertaBatiente() {
    const x = 220, y = 80, w = 360, h = 640, t = 20;
    const ox = x + t, oy = y + t, ow = w - 2 * t, oh = h - t;
    const zocalo = 150;
    return svg(piso() + rect(x, y, w, h, C.marco, C.borde, 3) +
        rect(ox, oy, ow, oh, C.hoja, C.borde, 2.5) +
        vidrio(ox + 18, oy + 18, ow - 36, oh - zocalo - 36) +
        rect(ox + 18, oy + oh - zocalo, ow - 36, zocalo - 18, C.marco, C.borde, 2) +
        apertura(ox + 30, oy + 30, ow - 60, oh - 60, 'izq') +
        manillaH(ox + ow - 40, oy + oh * 0.55, 54) + `<circle cx="${ox + ow - 18}" cy="${oy + oh * 0.55}" r="10" fill="${C.herraje}"/>`);
}

function puertaBatienteDoble() {
    const x = 130, y = 90, w = 540, h = 630, t = 20;
    const ox = x + t, oy = y + t, ow = (w - 2 * t) / 2, oh = h - t, zocalo = 140;
    const ala = (hx, lado) => rect(hx, oy, ow, oh, C.hoja, C.borde, 2.5) +
        vidrio(hx + 16, oy + 16, ow - 32, oh - zocalo - 32) +
        rect(hx + 16, oy + oh - zocalo, ow - 32, zocalo - 16, C.marco, C.borde, 2) +
        apertura(hx + 26, oy + 26, ow - 52, oh - 52, lado);
    return svg(piso() + rect(x, y, w, h, C.marco, C.borde, 3) + ala(ox, 'izq') + ala(ox + ow, 'der') +
        manillaV(ox + ow - 22, oy + oh * 0.55, 70) + manillaV(ox + ow + 22, oy + oh * 0.55, 70));
}

/** Hoja de puerta granero: marco con vidrio y la cruz típica. */
function hojaGranero(hx, hy, hw, hh) {
    const t = 22;
    const cruz = `<line x1="${hx + t}" y1="${hy + hh * 0.5}" x2="${hx + hw - t}" y2="${hy + hh - t}" stroke="${C.hoja}" stroke-width="20"/>` +
        `<line x1="${hx + hw - t}" y1="${hy + hh * 0.5}" x2="${hx + t}" y2="${hy + hh - t}" stroke="${C.hoja}" stroke-width="20"/>`;
    return rect(hx, hy, hw, hh, C.hoja, C.borde, 2.5) +
        vidrio(hx + t, hy + t, hw - 2 * t, hh * 0.5 - t - 10) +
        rect(hx + t, hy + hh * 0.5, hw - 2 * t, hh * 0.5 - t, C.marco, C.borde, 2) + cruz +
        rect(hx + t, hy + hh * 0.5, hw - 2 * t, hh * 0.5 - t, 'none', C.borde, 2) +
        rect(hx + t, hy + hh * 0.5 - 10, hw - 2 * t, 10, C.hoja, C.borde, 2);
}

const ruedas = (hx, hw, y) => [hx + 36, hx + hw - 36]
    .map((cx) => `<line x1="${cx}" y1="${y}" x2="${cx}" y2="${y + 34}" stroke="${C.herraje}" stroke-width="8"/><circle cx="${cx}" cy="${y}" r="14" fill="${C.fondo}" stroke="${C.herraje}" stroke-width="6"/>`)
    .join('');

function puertaGranero() {
    const vano = rect(330, 140, 300, 580, C.muro, C.borde, 2);
    const riel = rect(90, 96, 620, 18, C.herraje, C.herraje, 1, 'rx="4"');
    const hx = 170, hw = 330, hy = 136;
    return svg(piso() + vano + riel + ruedas(hx, hw, 105) + hojaGranero(hx, hy, hw, 584) +
        flecha(hx + hw / 2, 330, 1, 140) + manillaV(hx + hw - 30, 430, 90));
}

function puertaGraneroDoble() {
    const vano = rect(220, 140, 360, 580, C.muro, C.borde, 2);
    const riel = rect(40, 96, 720, 18, C.herraje, C.herraje, 1, 'rx="4"');
    const hw = 250, hy = 136;
    const izq = 100, der = 450;
    return svg(piso() + vano + riel + ruedas(izq, hw, 105) + ruedas(der, hw, 105) +
        hojaGranero(izq, hy, hw, 584) + hojaGranero(der, hy, hw, 584) +
        flecha(izq + hw / 2, 330, 1, 110) + flecha(der + hw / 2, 330, -1, 110) +
        manillaV(izq + hw - 26, 430, 80) + manillaV(der + 26, 430, 80));
}

/** Plato de ducha y muros laterales para las mamparas. */
function escenaBano() {
    return piso() + rect(60, 60, 30, 660, C.muro, C.borde, 2) + rect(710, 60, 30, 660, C.muro, C.borde, 2) +
        rect(90, 690, 620, 30, '#FFFFFF', C.borde, 2, 'rx="4"');
}

const panelTemplado = (x, y, w, h) => vidrio(x, y, w, h, C.templado, C.bordeTemplado);

function mamparaCorrediza() {
    const riel = rect(90, 110, 620, 16, C.herraje, C.herraje, 1, 'rx="4"');
    const fijo = panelTemplado(100, 126, 320, 560);
    const movil = panelTemplado(380, 132, 320, 554);
    const toallero = rect(410, 330, 14, 160, C.herraje, C.herraje, 1, 'rx="7"');
    return svg(escenaBano() + riel + fijo + ruedas(380, 320, 118).replaceAll(C.fondo, C.templado) + movil + toallero +
        flecha(560, 600, -1, 140));
}

function mamparaBatiente() {
    const fijo = panelTemplado(100, 110, 300, 576);
    const puerta = panelTemplado(408, 110, 292, 576);
    const bisagras = [170, 600].map((y) => rect(398, y, 20, 40, C.herraje, C.herraje, 1, 'rx="4"')).join('');
    const tirador = rect(660, 340, 14, 150, C.herraje, C.herraje, 1, 'rx="7"');
    return svg(escenaBano() + fijo + puerta + bisagras + tirador + apertura(440, 150, 200, 500, 'izq'));
}

function templadoPuerta() {
    const muros = rect(0, 60, 210, 660, C.muro, C.borde, 2) + rect(590, 60, 210, 660, C.muro, C.borde, 2);
    const puerta = panelTemplado(222, 80, 356, 630);
    const herrajes = rect(222, 80, 70, 40, C.herraje, C.herraje, 1, 'rx="4"') + rect(222, 670, 70, 40, C.herraje, C.herraje, 1, 'rx="4"');
    const tirador = rect(530, 260, 16, 280, C.herraje, C.herraje, 1, 'rx="8"');
    return svg(piso() + muros + puerta + herrajes + tirador + apertura(310, 140, 200, 520, 'izq'));
}

function templadoFijo() {
    const canalInferior = rect(110, 690, 580, 30, C.herraje, C.herraje, 1, 'rx="3"');
    const panel = panelTemplado(130, 120, 540, 580);
    const pinzas = [220, 580].map((x) => rect(x - 22, 106, 44, 30, C.herraje, C.herraje, 1, 'rx="4"')).join('');
    return svg(piso() + panel + canalInferior + pinzas);
}

function sinImagen() {
    const x = 220, y = 200, w = 360, h = 360, t = 20;
    return svg(rect(x, y, w, h, 'none', '#B9C0C9', 10, 'rx="14"') +
        `<line x1="${x + w / 2}" y1="${y + t}" x2="${x + w / 2}" y2="${y + h - t}" stroke="#B9C0C9" stroke-width="10"/>` +
        `<line x1="${x + t}" y1="${y + h / 2}" x2="${x + w - t}" y2="${y + h / 2}" stroke="#B9C0C9" stroke-width="10"/>`);
}

// ---------------------------------------------------------------------------------------------------------------

const IMAGENES = {
    'ventana-corredera-2hojas': () => corredera(2),
    'ventana-corredera-3hojas': () => corredera(3),
    'ventana-corredera-4hojas-2fijas': () => corredera(4, 1),
    'ventana-corredera-6hojas-triple-riel': () => corredera(6, 1),
    'ventana-corredera-monorriel': monorriel,
    'ventana-fija': fija,
    'ventana-fija-circular': fijaCircular,
    'ventana-fija-medio-punto': fijaMedioPunto,
    'ventana-fija-trapecio': fijaTrapecio,
    'ventana-arco': arco,
    'ventana-batiente-1hoja': () => abrirUnaHoja(['izq'], 'der'),
    'ventana-batiente-2hojas': batienteDoble,
    'ventana-proyectante': () => abrirUnaHoja(['arriba'], 'abajo'),
    'ventana-oscilante': () => abrirUnaHoja(['abajo'], 'arriba'),
    'ventana-oscilobatiente': () => abrirUnaHoja(['izq', 'abajo'], 'der'),
    'mosquitero': mosquitero,
    'puerta-batiente-1hoja': puertaBatiente,
    'puerta-batiente-2hojas': puertaBatienteDoble,
    'puerta-granero': puertaGranero,
    'puerta-granero-doble': puertaGraneroDoble,
    'mampara-bano-corrediza': mamparaCorrediza,
    'mampara-bano-batiente': mamparaBatiente,
    'templado-puerta-abatible': templadoPuerta,
    'templado-fijo': templadoFijo,
    'sin-imagen': sinImagen,
};

function buscarChrome() {
    const candidatos = [
        process.env.CHROME,
        'C:/Program Files/Google/Chrome/Application/chrome.exe',
        'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
        '/usr/bin/google-chrome',
        '/usr/bin/chromium',
        '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
    ];
    return candidatos.find((c) => c && existsSync(c));
}

mkdirSync(DIR_SVG, { recursive: true });
mkdirSync(DIR_PNG, { recursive: true });
const chrome = buscarChrome();
if (!chrome) console.warn('No se encontró Chrome ni Edge (variable CHROME): solo se generan los SVG.');

for (const [nombre, dibujar] of Object.entries(IMAGENES)) {
    const rutaSvg = join(DIR_SVG, `${nombre}.svg`);
    writeFileSync(rutaSvg, dibujar());
    if (chrome) {
        execFileSync(chrome, [
            '--headless=new', '--disable-gpu', '--hide-scrollbars', '--force-device-scale-factor=1',
            `--window-size=${L},${L}`, `--screenshot=${join(DIR_PNG, `${nombre}.png`)}`, pathToFileURL(rutaSvg).href,
        ], { stdio: 'ignore' });
    }
    console.log(nombre);
}

// Lista para el selector de imágenes (sin la imagen de reemplazo).
const lista = Object.keys(IMAGENES).filter((n) => n !== 'sin-imagen').map((n) => `pautas/${n}.png`);
writeFileSync(join(DIR_PNG, 'imagenes.json'), JSON.stringify(lista, null, 2) + '\n');
