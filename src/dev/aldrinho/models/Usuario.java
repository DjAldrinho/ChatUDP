package dev.aldrinho.models;

import dev.aldrinho.enums.Estados;

import java.io.Serializable;


public class Usuario implements Serializable {

    private String usuario, logo;
    private Estados estado;


    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Estados getEstado() {
        return estado;
    }

    public void setEstado(Estados estado) {
        this.estado = estado;
    }
}
