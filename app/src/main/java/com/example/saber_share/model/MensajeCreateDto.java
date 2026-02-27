package com.example.saber_share.model;

public class MensajeCreateDto {
    public int emisorId;
    public int receptorId;
    public String contenido;

    public MensajeCreateDto(int emisorId, int receptorId, String contenido) {
        this.emisorId = emisorId;
        this.receptorId = receptorId;
        this.contenido = contenido;
    }
}
