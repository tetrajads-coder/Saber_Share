package com.example.saber_share.model;

public class OpinionesCursoDto {
    private Integer idOpiniones;
    private String  comentOps;
    private Integer calOps;
    private Integer usuarioId;
    private Integer cursoId;

    public OpinionesCursoDto() {}

    public Integer getIdOpiniones()           { return idOpiniones; }
    public void    setIdOpiniones(Integer v)  { this.idOpiniones = v; }

    public String  getComentOps()             { return comentOps; }
    public void    setComentOps(String v)     { this.comentOps = v; }
    public void    setComentario(String v)    { this.comentOps = v; } // alias setter

    public Integer getCalOps()                { return calOps; }
    public void    setCalOps(Integer v)       { this.calOps = v; }
    public void    setCalificacion(Integer v) { this.calOps = v; } // alias setter

    public Integer getUsuarioId()             { return usuarioId; }
    public void    setUsuarioId(Integer v)    { this.usuarioId = v; }

    public Integer getCursoId()               { return cursoId; }
    public void    setCursoId(Integer v)      { this.cursoId = v; }
}