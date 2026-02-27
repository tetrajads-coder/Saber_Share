package com.example.saber_share.model;

public class OpinionServicioDto {
    private Integer id;
    private String comentario;
    private Integer calificacion; // 1 a 5
    private Integer usuarioId;
    private Integer servicioId;

    public OpinionServicioDto() {}

    public OpinionServicioDto(Integer usuarioId, Integer servicioId, Integer calificacion, String comentario) {
        this.usuarioId = usuarioId;
        this.servicioId = servicioId;
        this.calificacion = calificacion;
        this.comentario = comentario;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
    public Integer getServicioId() { return servicioId; }
    public void setServicioId(Integer servicioId) { this.servicioId = servicioId; }
}