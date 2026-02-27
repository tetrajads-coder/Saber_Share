package com.example.saber_share.model;

public class OpinionesCursoDto {
    private Integer id;
    private String comentario;
    private Integer calificacion;
    private Integer usuarioId;
    private Integer cursoId;

    public OpinionesCursoDto() {}

    public OpinionesCursoDto(Integer usuarioId, Integer cursoId, Integer calificacion, String comentario) {
        this.usuarioId = usuarioId;
        this.cursoId = cursoId;
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
    public Integer getCursoId() { return cursoId; }
    public void setCursoId(Integer cursoId) { this.cursoId = cursoId; }
}