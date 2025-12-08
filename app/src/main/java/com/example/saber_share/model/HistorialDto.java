package com.example.saber_share.model;

public class HistorialDto {
    private Integer idHistorial;
    private String fechapago;
    private Double pago;
    private Integer usuario_idUsuario;
    private Integer servicioId;
    private Integer cursoId;

    public Integer getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Integer idHistorial) { this.idHistorial = idHistorial; }

    public String getFechapago() { return fechapago; }
    public void setFechapago(String fechapago) { this.fechapago = fechapago; }

    public Double getPago() { return pago; }
    public void setPago(Double pago) { this.pago = pago; }

    public Integer getUsuario_idUsuario() { return usuario_idUsuario; }
    public void setUsuario_idUsuario(Integer usuario_idUsuario) { this.usuario_idUsuario = usuario_idUsuario; }

    public Integer getServicioId() { return servicioId; }
    public void setServicioId(Integer servicioId) { this.servicioId = servicioId; }

    public Integer getCursoId() { return cursoId; }
    public void setCursoId(Integer cursoId) { this.cursoId = cursoId; }
}
