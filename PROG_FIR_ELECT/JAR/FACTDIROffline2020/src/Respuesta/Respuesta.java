/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Respuesta;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yoelvys
 */
public class Respuesta {

    private String estadoComprobante;
    private List<MensajeGenerado> mensajes;
    private String comprobante;

    public Respuesta() {
        mensajes = new ArrayList<MensajeGenerado>();
    }

    /**
     * @return the mensajes
     */
    public List<MensajeGenerado> getMensajes() {
        return mensajes;
    }

    /**
     * @param mensajes the mensajes to set
     */
    public void setMensajes(List<MensajeGenerado> mensajes) {
        this.mensajes = mensajes;
    }

    public void addMensaje(MensajeGenerado mensaje) {
        mensajes.add(mensaje);
    }

    /**
     * @return the estadoComprobante
     */
    public String getEstadoComprobante() {
        return estadoComprobante;
    }

    /**
     * @param estadoComprobante the estadoComprobante to set
     */
    public void setEstadoComprobante(String estadoComprobante) {
        this.estadoComprobante = estadoComprobante;
    }

    /**
     * @return the comprobante
     */
    public String getComprobante() {
        return comprobante;
    }

    /**
     * @param comprobante the comprobante to set
     */
    public void setComprobante(String comprobante) {
        this.comprobante = comprobante;
    }

  
    
}
