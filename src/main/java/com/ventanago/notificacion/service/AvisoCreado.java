package com.ventanago.notificacion.service;

/** Se publica al guardar un aviso; tras confirmar la transacción se envía como notificación push a la cuenta. */
public record AvisoCreado(Long cuentaId, Long solicitudNumero, String titulo, String mensaje) {
}
