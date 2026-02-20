/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Respuesta;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;

/**
 *
 * @author Yoelvys
 */
public class RespuestaInterna {

    private String estadoComprobante;
    private List<MensajeGenerado> mensajes;
    private Document comprobante;
    private String docAutorizado;
    private String fechaAutorizacion;
     public RespuestaInterna() {
        mensajes = new ArrayList<MensajeGenerado>();
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
     * @return the mensajes
     */
    public List<MensajeGenerado> getMensajes() {
        return mensajes;
    }

    public void addMensaje(MensajeGenerado mensaje) {
        getMensajes().add(mensaje);
    }

    /**
     * @param mensajes the mensajes to set
     */
    public void setMensajes(List<MensajeGenerado> mensajes) {
        this.mensajes = mensajes;
    }

    /**
     * @return the comprobante
     */
    public Document getComprobante() {
        return comprobante;
    }

    /**
     * @param comprobante the comprobante to set
     */
    public void setComprobante(Document comprobante) {
        this.comprobante = comprobante;
    }

    /**
     * @return the docAutorizado
     */
    public String getDocAutorizado() {
        return docAutorizado;
    }

    /**
     * @param docAutorizado the docAutorizado to set
     */
    public void setDocAutorizado(String docAutorizado) {
        this.docAutorizado = docAutorizado;
    }

    /**
     * @return the fechaAutorizacion
     */
    public String getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    /**
     * @param fechaAutorizacion the fechaAutorizacion to set
     */
    public void setFechaAutorizacion(String fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }
}
