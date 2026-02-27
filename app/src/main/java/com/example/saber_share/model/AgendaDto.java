package com.example.saber_share.model;

public class AgendaDto {

    private Integer idAgenda;
    private String fecha;
    private String hora;
    private String estado;
    private Integer servicioId;
    private Integer profesorId;
    private Integer alumnoId;
    private String tituloServicio;
    private String nombreAlumno;

    public AgendaDto() {}

    public Integer getIdAgenda() { return idAgenda; }
    public void setIdAgenda(Integer idAgenda) { this.idAgenda = idAgenda; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getServicioId() { return servicioId; }
    public void setServicioId(Integer servicioId) { this.servicioId = servicioId; }

    public Integer getProfesorId() { return profesorId; }
    public void setProfesorId(Integer profesorId) { this.profesorId = profesorId; }

    public Integer getAlumnoId() { return alumnoId; }
    public void setAlumnoId(Integer alumnoId) { this.alumnoId = alumnoId; }

    public String getTituloServicio() { return tituloServicio; }
    public void setTituloServicio(String tituloServicio) { this.tituloServicio = tituloServicio; }

    public String getNombreAlumno() { return nombreAlumno; }
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }
}
