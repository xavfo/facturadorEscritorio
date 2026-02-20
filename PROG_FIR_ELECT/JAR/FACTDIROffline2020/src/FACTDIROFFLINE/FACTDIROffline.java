/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FACTDIROFFLINE;

import Email.JCMail;
import Facturacion.Facturacion;
import FirmaElectronica.FirmaElectronica;
import JasperRide.JasperRide;
import Respuesta.MensajeGenerado;
import Respuesta.RespuestaInterna;
import Ride.Ride;
import Util.Util;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.transform.OutputKeys;
import org.xml.sax.InputSource;

/**
 *
 * @author Yoelvys
 */
public class FACTDIROffline {

    /**
     * @param args the command line arguments
     */
    private static Properties configuracion;
    private static Facturacion facturacion;
    private static JCMail jcmail;
    private static String tipoComprobante;
    private static String razonSocial;
    private static String razonSocialEmisor;
    private static String identificacionCliente;
    private static String emailCliente;
    private static String fechaEmision;
    private static Principal principal;
    private static FirmaElectronica firma;
    
    private static String DIR_IREPORT = "C:\\Directorio\\Plantillaspdf\\IReport";

    public static void main(String[] args) {
        try {
            Inicio inicio = new Inicio();
            inicio.setLocationRelativeTo(null);
            inicio.setVisible(true);

            Animacion animacion = new Animacion();
            animacion.fade(inicio, true);
            Thread.sleep(4000);
            animacion.fade(inicio, false);
            Thread.sleep(3000);
            inicio.setVisible(false);
            jcmail = new JCMail();
            configuracion = new Properties();
            configuracion.load(FACTDIROffline.class.getResourceAsStream("/Util/configuracion.properties"));
            firma = new FirmaElectronica(configuracion);
            facturacion = new Facturacion(configuracion);

            Path dir = Paths.get(configuracion.getProperty("dirGenerado"));
            principal = new Principal(dir);
            principal.setLocationRelativeTo(null);
            principal.setVisible(true);

            //String mod = modulo11("271220140117922072660011001001000054810006606691");
            WatchService watcher = FileSystems.getDefault().newWatchService();

            principal.ActualizarText("Bienvenido al Sistema de Facturacion Electronica");
            principal.ActualizarText("OSANSTORE.NET email: osanstore@gmail.com cel: 593 984558290 PRODUCCIÓN OFF-LINE 5.0");
            principal.ActualizarText("");
            ProcesarComprobantesPendientes(dir);
            ProcesarComprobantesPendientesSRI();
            ProcesarComprobantesPendientesAutorizacion();

            dir.register(watcher, ENTRY_CREATE);
            while (true) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException ex) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();
                    Path fullPath = dir.resolve(fileName);
                    String extension = "";
                    int i = fileName.toString().lastIndexOf('.');
                    if (i > 0) {
                        extension = fileName.toString().substring(i + 1);
                    }
                    if (kind == ENTRY_CREATE) {

                        Thread.sleep(1500);
                        File file = new File(fullPath.toString());
                        if (!extension.equals("") && extension.toLowerCase().equals("xml") && file.exists()) {
                            ProcesarComprobante(fullPath, fileName);
                            ProcesarComprobantesPendientes(dir);
                            ProcesarComprobantesPendientesSRI();
                            ProcesarComprobantesPendientesAutorizacion();

                        }
                    }

                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (Exception ex) {
            principal.ActualizarText(ex.getMessage());
        }
    }

    public static void ProcesarComprobantesPendientesAutorizacion() {

        try {
            File archivosSinProcesar = new File(configuracion.getProperty("dirProcesando"));
            File[] archivos = archivosSinProcesar.listFiles();
            if (archivos != null) {
                for (int x = 0; x < archivos.length; x++) {
                    Path fullPath = Paths.get(archivos[x].getAbsolutePath());
                    Path fileName = Paths.get(archivos[x].getName());
                    File file = new File(fullPath.toString());

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document xmlComprobante = dBuilder.parse(new InputSource(fullPath.toString()));

                    principal.ActualizarText("Procesando comprobantes con clave de acceso en proceso");
                    String claveAcceso = xmlComprobante.getElementsByTagName("claveAcceso").item(0).getTextContent();
                    Thread.sleep(1500);
                    principal.ActualizarText("Procesando Comprobante " + fileName + "..");
                    RespuestaInterna respuestaInterna = facturacion.procesarComprobantesPendientesAutorizacion(claveAcceso, xmlComprobante);
                    principal.ActualizarText("Comprobante " + fileName + " procesado. Estado: " + respuestaInterna.getEstadoComprobante());
                    if (!respuestaInterna.getEstadoComprobante().equals("AUTORIZADO") && !respuestaInterna.getMensajes().isEmpty()) {
                        principal.ActualizarText("Mensaje " + respuestaInterna.getMensajes().get(0).getMensaje() + ". " + respuestaInterna.getMensajes().get(0).getInformacionAdicional());
                    }
                    String ruc = xmlComprobante.getElementsByTagName("ruc").item(0).getTextContent();
                    ProcesarRespuesta(fullPath, fileName, respuestaInterna, ruc, true);

                }
            }
        } catch (Exception e) {
            principal.ActualizarText("Error procesando los comprobantes con clave de acceso en proceso");
        }

    }

    public static void ProcesarComprobantesPendientesSRI() {
        try {
            File archivosSinProcesar = new File(configuracion.getProperty("dirPendienteSRI"));
            File[] archivos = archivosSinProcesar.listFiles();
            if (archivos != null) {
                for (int x = 0; x < archivos.length; x++) {
                    Path fullPath = Paths.get(archivos[x].getAbsolutePath());
                    Path fileName = Paths.get(archivos[x].getName());
                    File file = new File(fullPath.toString());

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document xmlComprobante = dBuilder.parse(new InputSource(fullPath.toString()));

                    principal.ActualizarText("Procesando comprobantes pendiente de Autorizacion SRI");
                    principal.ActualizarText("Procesando comprobante " + fileName + "...");
                    Thread.sleep(1500);
                    RespuestaInterna respuestaInterna = facturacion.ProcesarComprobante(xmlComprobante);
                    principal.ActualizarText("Comprobante " + fileName + " procesado. Estado: " + respuestaInterna.getEstadoComprobante());
                    String ruc = xmlComprobante.getElementsByTagName("ruc").item(0).getTextContent();
                    ProcesarRespuesta(fullPath, fileName, respuestaInterna, ruc, false);

                }
            }
        } catch (Exception e) {
            principal.ActualizarText("Error procesando los comprobantes pendiente de Autorizacion SRI" + e.getMessage());
        }

    }

    public static void ProcesarComprobantesPendientes(Path dirGenerados) throws ParserConfigurationException, SAXException, IOException, TransformerException, TransformerConfigurationException, InterruptedException {
        File archivosSinProcesar = new File(dirGenerados.toString());
        File[] archivos = archivosSinProcesar.listFiles();
        if (archivos != null) {
            for (int x = 0; x < archivos.length; x++) {
                Path fullPath = Paths.get(archivos[x].getAbsolutePath());
                Path fileName = Paths.get(archivos[x].getName());
                ProcesarComprobante(fullPath, fileName);
            }
        }
        principal.ActualizarText("Todos los comprobantes procesados. Esperando nuevo(s) comprobante(s)...");
        principal.ActualizarText("");

    }

    private static void ProcesarComprobante(Path fullPath, Path fileName) {
        File file = new File(fullPath.toString());
        DocumentBuilder dBuilder = null;
        try {
            if (file.exists()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                dBuilder = dbFactory.newDocumentBuilder();
                Document xmlComprobante = dBuilder.parse(new InputSource(fullPath.toString()));

                principal.ActualizarText("Procesando comprobante: " + fileName + " ...");
                Thread.sleep(1500);
                RespuestaInterna respuestaInterna = firma.FirmarComprobante(xmlComprobante);
                principal.ActualizarText("Comprobante " + fileName + " procesado. Estado: " + respuestaInterna.getEstadoComprobante());
                String ruc = xmlComprobante.getElementsByTagName("ruc").item(0).getTextContent();
                ProcesarRespuesta(fullPath, fileName, respuestaInterna, ruc, false);
            }
        } catch (Exception ex) {
            String dirErrorProcesando = configuracion.getProperty("errorProcensando");
            try {
                Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(dirErrorProcesando).resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                principal.ActualizarText("Error copiando xml sin procesar");
            }
            file.delete();
            principal.ActualizarText("Error procesando comprobante: " + fileName + " fue copiado a: " + dirErrorProcesando);
        }

    }

    private static void ProcesarRespuesta(Path fullPath, Path fileName, RespuestaInterna respuestaInterna, String ruc, boolean envioEmail) throws ParserConfigurationException, TransformerConfigurationException, TransformerException, SAXException, IOException {
        try {
            Util util = new Util();
            Document xmlComprobante = respuestaInterna.getComprobante();
            Document xmlOriginal = respuestaInterna.getDocAutorizado() != null ? util.convertirStringToXML(respuestaInterna.getDocAutorizado()) : xmlComprobante;
            String dirGuardarDoc = "";

            File file = new File(fullPath.toString());
            File fileErrorProcesando = new File(Paths.get(configuracion.getProperty("errorProcensando")).resolve(fileName).toString());
            if (fileErrorProcesando.exists()) {
                fileErrorProcesando.delete();
            }
            if (xmlComprobante != null && respuestaInterna.getEstadoComprobante().equals("DEVUELTA")) {
                dirGuardarDoc = Paths.get(configuracion.getProperty("dirRechazados" + ruc)).resolve(fileName).toString();
            } else if (xmlComprobante != null && respuestaInterna.getEstadoComprobante().equals("NO AUTORIZADO")) {
                dirGuardarDoc = Paths.get(configuracion.getProperty("dirNoAutorizado" + ruc)).resolve(fileName).toString();
            } else if (xmlComprobante != null && respuestaInterna.getEstadoComprobante().equals("FIRMADO")) {
                dirGuardarDoc = Paths.get(configuracion.getProperty("dirPendienteSRI")).resolve(fileName).toString();
            } else if (xmlComprobante != null && respuestaInterna.getEstadoComprobante().equals("AUTORIZADO")) {
                dirGuardarDoc = Paths.get(configuracion.getProperty("dirAutorizados" + ruc)).resolve(fileName).toString();

            } else if (respuestaInterna.getEstadoComprobante().equals("PROCESANDOSE")) {
                dirGuardarDoc = Paths.get(configuracion.getProperty("dirProcesando")).resolve(fileName).toString();
                file.delete();
            } else {
                dirGuardarDoc = Paths.get(configuracion.getProperty("dirError" + ruc)).resolve(fileName).toString();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                DOMImplementation implementation = builder.getDOMImplementation();
                xmlComprobante = implementation.createDocument(null, "informacion", null);
                xmlComprobante.setXmlVersion("1.0");
                Element tagRoot = xmlComprobante.getDocumentElement();

                Element tagError = xmlComprobante.createElement("error");
                for (Iterator<MensajeGenerado> it = respuestaInterna.getMensajes().iterator(); it.hasNext();) {
                    MensajeGenerado mensajeGenerado = it.next();
                    Element tagIdentificador = xmlComprobante.createElement("identificador");
                    tagIdentificador.setTextContent(mensajeGenerado.getIdentificador());

                    Element tagMensaje = xmlComprobante.createElement("mensaje");
                    tagMensaje.setTextContent(mensajeGenerado.getMensaje());

                    Element tagInformacionAdicional = xmlComprobante.createElement("informacionAdicional");
                    tagInformacionAdicional.setTextContent(mensajeGenerado.getInformacionAdicional());

                    tagError.appendChild(tagIdentificador);
                    tagError.appendChild(tagMensaje);
                    tagError.appendChild(tagInformacionAdicional);
                }
                tagRoot.appendChild(tagError);
            }

            Source source = null;
            Result result = null;
            Transformer transformer = null;
            if (xmlOriginal != null && !respuestaInterna.getEstadoComprobante().equals("AUTORIZADO")) {
                source = new DOMSource(xmlComprobante);
                result = new StreamResult(new File(dirGuardarDoc));
                transformer = TransformerFactory.newInstance().newTransformer();
                if (!respuestaInterna.getEstadoComprobante().equals("FIRMADO")) {
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                }
                transformer.transform(source, result);
                principal.ActualizarText("Comprobante: " + fileName + " guardado en: " + dirGuardarDoc);
            }
            if (respuestaInterna.getEstadoComprobante() != null && !respuestaInterna.getEstadoComprobante().equals("ERROR") && !respuestaInterna.getEstadoComprobante().equals("PROCESANDOSE")) {
                String dirArchivoError = Paths.get(configuracion.getProperty("dirError" + ruc)).resolve(fileName).toString();
                File enError = new File(dirArchivoError);
                if (enError.exists()) {
                    enError.delete();
                }
                file.delete();
            }

            String numAutorizacion = "";
            String mensajeCorreo = crearMensajeCorreo(xmlOriginal);
            if (respuestaInterna.getEstadoComprobante().equals("FIRMADO") || respuestaInterna.getEstadoComprobante().equals("AUTORIZADO")) {
                String directorioRaiz = Paths.get(configuracion.getProperty("dirAutorizados" + ruc)).resolve(crearNombreCarpeta(xmlOriginal)).toString();
                File directorio = new File(directorioRaiz);
                if (!directorio.exists()) {
                    directorio.mkdir();
                }

                String dirDocAutorizado = Paths.get(directorioRaiz).resolve(fileName).toString();

                source = new DOMSource(xmlOriginal);
                result = new StreamResult(new File(dirDocAutorizado));
                transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(source, result);

                //Ride ride = new Ride();
                numAutorizacion = xmlOriginal.getElementsByTagName("claveAcceso").item(0).getTextContent();
                //ride.CrearRide(xmlOriginal, numAutorizacion, fileName, dirDocAutorizado);
                JasperRide jasperRide = new JasperRide();
                String logo = configuracion.getProperty("dirLogo" + ruc);

                try {
                    jasperRide.CrearRide(xmlOriginal, numAutorizacion, dirDocAutorizado, logo, DIR_IREPORT);
                } catch (Exception ex) {
                    // System.out.println(ex.getMessage());
                    principal.ActualizarText(ex.getMessage());
                }

                String dirArchivoRechazado = Paths.get(configuracion.getProperty("dirRechazados" + ruc)).resolve(fileName).toString();
                File enRechazado = new File(dirArchivoRechazado);
                if (enRechazado.exists()) {
                    enRechazado.delete();
                }
                String dirArchivoNoAutorizado = Paths.get(configuracion.getProperty("dirNoAutorizado" + ruc)).resolve(fileName).toString();
                File enNoAutorizado = new File(dirArchivoNoAutorizado);
                if (enNoAutorizado.exists()) {
                    enNoAutorizado.delete();
                }

                String destinatarios = obtenerDestinatarios(xmlOriginal);
                boolean enviarEmail = false;
                if (configuracion.getProperty("correoSiAutorizado" + ruc).equals("si") && respuestaInterna.getEstadoComprobante().equals("AUTORIZADO")) {
                    enviarEmail = true;
                } else if (configuracion.getProperty("correoSiAutorizado" + ruc).equals("no") && respuestaInterna.getEstadoComprobante().equals("FIRMADO")) {
                    enviarEmail = true;
                }
                try {
                    if (!destinatarios.equals("") && (enviarEmail || envioEmail)) {
                        jcmail.setHost(configuracion.getProperty("servidorCorreo" + ruc));
                        jcmail.setPort(configuracion.getProperty("puertoCorreo" + ruc));
                        jcmail.setFrom(configuracion.getProperty("correoRemitente" + ruc));
                        jcmail.setPassword(configuracion.getProperty("correoPass" + ruc).toCharArray());
                        jcmail.setSubject(configuracion.getProperty("correoAsunto" + ruc));

                        jcmail.setMessage(mensajeCorreo);
                        jcmail.setTo(destinatarios);
                        principal.ActualizarText("Enviando Correo al cliente");
                        principal.ActualizarText(jcmail.SEND(fileName.toString(), dirDocAutorizado));
                    }
                } catch (Exception ex) {
                    principal.ActualizarText("Error enviando el correo" + ex.getMessage());
                }

            }
            String mensaje = "";
            String mensajeAdicional = "";
            if (!respuestaInterna.getEstadoComprobante().equals("AUTORIZADO") && !respuestaInterna.getEstadoComprobante().equals("FIRMADO")) {
                mensaje = respuestaInterna.getMensajes().get(0).getMensaje().replaceAll("'", "");
                mensajeAdicional = respuestaInterna.getMensajes().get(0).getInformacionAdicional() != null ? respuestaInterna.getMensajes().get(0).getInformacionAdicional().replaceAll("'", "") : "";
            }
            principal.ActualizarText("Procesamiento de " + fileName + " completado.");
        } catch (Exception e) {
            principal.ActualizarText("Error procesando la respuesta del comprobante: " + fileName);
            principal.ActualizarText("Error: " + e.getMessage());
        }

        principal.ActualizarText("");
    }

    private static String obtenerDestinatarios(Document xmlComprobante) {
        emailCliente = "";
        NodeList campoAdicional = xmlComprobante.getElementsByTagName("campoAdicional");
        int cantidad = campoAdicional.getLength();
        String destinatarios = "";
        for (int i = 0; i < cantidad; i++) {
            Node node = campoAdicional.item(i);
            String nombre = node.getAttributes().item(0).getNodeValue();
            if (nombre.toLowerCase().equals("email")) {
                if (node.getTextContent() == null || node.getTextContent().isEmpty()) {
                    continue;
                }
                if (!destinatarios.equals("")) {
                    destinatarios = destinatarios + ",";
                }
                destinatarios = destinatarios + node.getTextContent();
                emailCliente = node.getTextContent();
            } else if (nombre.equals("Email2")) {
                if (node.getTextContent() == null || node.getTextContent().isEmpty()) {
                    continue;
                }
                if (!destinatarios.equals("")) {
                    destinatarios = destinatarios + ",";
                }
                destinatarios = destinatarios + node.getTextContent();
            } else if (nombre.equals("Email3")) {
                if (node.getTextContent() == null || node.getTextContent().isEmpty()) {
                    continue;
                }
                if (!destinatarios.equals("")) {
                    destinatarios = destinatarios + ",";
                }
                destinatarios = destinatarios + node.getTextContent();
            }

        }
        return destinatarios;
    }

    private static String crearMensajeCorreo(Document xmlComprobante) {
        String codDoc = xmlComprobante.getElementsByTagName("codDoc").item(0).getTextContent();
        tipoComprobante = "";

        String dirigido = "";
        String datos = "";
        String rdirigido = "";
        String campoIdentificacion = "";
        String campoFecha = "fechaEmision";

        if (codDoc.equals("01")) {
            campoIdentificacion = "identificacionComprador";
            tipoComprobante = "FACTURA";
            dirigido = "razonSocialComprador";
            rdirigido = "identificacionComprador";
            String importeTotal = xmlComprobante.getElementsByTagName("importeTotal").item(0).getTextContent();
            datos = "<strong>Valor Total: </strong>" + importeTotal + "<br /><br />";
        } else if (codDoc.equals("03")) {
            tipoComprobante = "LIQUIDACIÓN DE COMPRA DE BIENES Y PRESTACIÓN DE SERVICIOS";
            dirigido = "razonSocialProveedor";
            rdirigido = "identificacionProveedor";
            campoIdentificacion = "identificacionProveedor";
            String importeTotal = xmlComprobante.getElementsByTagName("importeTotal").item(0).getTextContent();
            datos = "<strong>Valor Total: </strong>" + importeTotal + "<br /><br />";
        } else if (codDoc.equals("04")) {
            campoIdentificacion = "identificacionComprador";
            tipoComprobante = "NOTA DE CRÉDITO";
            dirigido = "razonSocialComprador";
            rdirigido = "identificacionComprador";
        } else if (codDoc.equals("05")) {
            campoIdentificacion = "identificacionComprador";
            tipoComprobante = "NOTA DE DÉBITO";
            dirigido = "razonSocialComprador";
            rdirigido = "identificacionComprador";
        } else if (codDoc.equals("06")) {
            campoIdentificacion = "rucTransportista";
            tipoComprobante = "GUÍA DE REMISIÓN";
            dirigido = "razonSocialDestinatario";
            campoFecha = "fechaIniTransporte";
            rdirigido = "identificacionDestinatario";
        } else if (codDoc.equals("07")) {
            campoIdentificacion = "identificacionSujetoRetenido";
            tipoComprobante = "COMPROBANTE DE RETENCIÓN";
            dirigido = "razonSocialSujetoRetenido";
            rdirigido = "identificacionSujetoRetenido";

        }

        razonSocial = xmlComprobante.getElementsByTagName(dirigido).item(0).getTextContent();
        razonSocialEmisor = xmlComprobante.getElementsByTagName("razonSocial").item(0).getTextContent();
        identificacionCliente = xmlComprobante.getElementsByTagName(campoIdentificacion).item(0).getTextContent();
        fechaEmision = xmlComprobante.getElementsByTagName(campoFecha).item(0).getTextContent();
        String identificacionComprador = xmlComprobante.getElementsByTagName(rdirigido).item(0).getTextContent();
        String nombreComercial = xmlComprobante.getElementsByTagName("nombreComercial").item(0).getTextContent();
        String claveAcceso = xmlComprobante.getElementsByTagName("claveAcceso").item(0).getTextContent();

        String mensaje = "Estimado(a),";
        mensaje += "<br /><br /><strong>" + razonSocial + " RUC/C.I.: " + identificacionComprador + "</strong>";
        mensaje += "<br /><br />Esta es una notificaci&oacuten autom&aacutetica de un documento tributario electr&oacutenico  y Legalmente v&aacutelido para las declaraciones de Impuestos ante el SRI Emitido por <strong>" + razonSocialEmisor + " - " + nombreComercial + "</strong><br /><br /> ";

        String establecimiento = xmlComprobante.getElementsByTagName("estab").item(0).getTextContent();
        String ptoEmision = xmlComprobante.getElementsByTagName("ptoEmi").item(0).getTextContent();
        String secuencial = xmlComprobante.getElementsByTagName("secuencial").item(0).getTextContent();

        mensaje += "<strong>Tipo de Comprobante: </strong>" + tipoComprobante + "<br /><br />";
        mensaje += "<strong>Nro de Comprobante: </strong>" + establecimiento + "-" + ptoEmision + "-" + secuencial + "<br /><br />";
        mensaje += "<strong>Clave de Acceso: </strong>" + claveAcceso + "<br /><br />";
        mensaje += datos;
        mensaje += "Los detalles generales del comprobante pueden ser consultados en el archivo pdf y xml adjunto en este correo.<br /><br />"
                + "<strong>Atentamente,</strong><br /><br />"
                + "         <strong>" + razonSocialEmisor + "</strong><br /><br />";

        mensaje += "<strong>Contacto   :  </strong><br /><br />";
        mensaje += "<strong>Consultar Documento Electr&oacutenico en : https://srienlinea.sri.gob.ec/comprobantes-electronicos-internet/publico/validezComprobantes.jsf# </strong><br /><br />";

        return mensaje;
    }

    private static String crearNombreCarpeta(Document xmlComprobante) {
        String nombre = "";
        String codDoc = xmlComprobante.getElementsByTagName("codDoc").item(0).getTextContent();
        if (codDoc.equals("01")) {
            nombre = xmlComprobante.getElementsByTagName("identificacionComprador").item(0).getTextContent();
        } else if (codDoc.equals("04")) {
            nombre = xmlComprobante.getElementsByTagName("identificacionComprador").item(0).getTextContent();
        } else if (codDoc.equals("05")) {
            nombre = xmlComprobante.getElementsByTagName("identificacionComprador").item(0).getTextContent();
        } else if (codDoc.equals("06")) {
            nombre = xmlComprobante.getElementsByTagName("rucTransportista").item(0).getTextContent();
        } else if (codDoc.equals("07")) {
            nombre = xmlComprobante.getElementsByTagName("identificacionSujetoRetenido").item(0).getTextContent();
        } else if (codDoc.equals("03")) {
            nombre = xmlComprobante.getElementsByTagName("identificacionProveedor").item(0).getTextContent();
        }
        return nombre;
    }

    private static String crearNombreFichero(Document xmlComprobante) {
        String nombre = "";
        String codDoc = xmlComprobante.getElementsByTagName("codDoc").item(0).getTextContent();
        String estab = xmlComprobante.getElementsByTagName("estab").item(0).getTextContent();
        String ptoEmi = xmlComprobante.getElementsByTagName("ptoEmi").item(0).getTextContent();
        String secuencial = xmlComprobante.getElementsByTagName("secuencial").item(0).getTextContent();
        if (codDoc.equals("01")) {
            nombre = "FAC";
        } else if (codDoc.equals("04")) {
            nombre = "NC";
        } else if (codDoc.equals("05")) {
            nombre = "ND";
        } else if (codDoc.equals("06")) {
            nombre = "GR";
        } else if (codDoc.equals("07")) {
            nombre = "CR";
        } else if (codDoc.equals("03")) {
            nombre = "LIQ";
        }
        return nombre + estab + "-" + ptoEmi + "-" + secuencial + ".xml";
    }

}
