package com.example.appgenda;

public class Evento {

    String Titulo;
    String Descripcion;
    String fechaDesde;
    String fechaHasta;

    public Evento() {

    }

    public Evento(String titulo, String descripcion, String fechaDesde, String fechaHasta) {
        Titulo = titulo;
        Descripcion = descripcion;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }



    public String getTitulos() {
        return Titulo;
    }

    public void setTitulos(String titulo) {
        Titulo = titulo;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public String getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(String fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public String getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(String fechaHasta) {
        this.fechaHasta = fechaHasta;
    }
}
