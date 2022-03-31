/****************************************************************
 *    SERVICIOS DE INTERNET
 *    EE TELECOMUNICACIÓN
 *    UNIVERSIDAD DE VIGO
 *
 *    Práctica TVML
 *
 *    Autor: Alberto Gil Solla
 *    Curso : 2019-2020
 ****************************************************************/

// puede trabajar con datos reales o inventados (parámetro 'real=no' en web.xml)
// se puede especificar en el web.xml el directorio base  de los documentos (parámetro dirBase), el directorio base de los Schemas (dirRulesBase), y el fichero inicial (urlInicial)

package docencia.sint.TVML;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import docencia.sint.Common.CommonSINT;
import docencia.sint.Common.ErrorHandlerSINT;
import docencia.sint.Common.WrongFile;

import javax.xml.xpath.XPathConstants;



public class P2TV extends HttpServlet {

    private static final long serialVersionUID = 1L;

	// todas estas son variables de clase, compartidas por todos los usuarios

    final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    final String TVML_SCHEMA = "_rules/tvml.xsd";

    // los valores por defecto, si no hay parámetros de configuración
    String dirBaseDefault = "http://localhost:7000/sintprof/ficheros_prueba/19-20_TVML/";
    String urlInicialDefault = "tvml-2004-12-01.xml";
    // String urlInicialDefault = "T1.xml";

    // los parámetros de configuración o los valores por defecto
    String dirBase;
    String urlInicial;

    ArrayList<String> listaFicherosProcesados = new ArrayList<String>();

    ArrayList<WrongFile> listWarnings = new ArrayList<WrongFile>();
    ArrayList<WrongFile> listErrores= new ArrayList<WrongFile>();
    ArrayList<WrongFile> listErroresfatales = new ArrayList<WrongFile>();


    // El init se ejecuta al cargar el servlet la primera vez

    public void init (ServletConfig servletConfig) throws ServletException {

        CommonTVML.initLoggerTVML(P2TV.class);
        CommonTVML.logTVML("\nInit...");



		/*  para el examen

		     DocumentBuilderFactory dbfe;    // los terminados en e son para el examen
	    DocumentBuilder dbe;


		dbfe = DocumentBuilderFactory.newInstance();

	    	try {
	    		dbe = dbfe.newDocumentBuilder();
	    	}
	    	catch  (ParserConfigurationException e) {
	    			throw new UnavailableException("Error creando el builder para el examen: "+e);
	    	}

		Document doce;

	    	try {
	    		doce = dbe.parse("http://gssi.det.uvigo.es/users/agil/public_html/ex1.xml");
	    	}
	    	catch (Exception e) {
	    		return;
	    	}


		Element examen = doce.getDocumentElement();

		// esto será distinto para cada examen

		NodeList nlcalle = examen.getElementsByTagName("calle");
		Element calle = (Element)nlcalle.item(0);
		excalle = calle.getTextContent().trim();

		exnum = calle.getAttribute("numeros");

		NodeList nlhijos = examen.getChildNodes();

		for (int j=0; j < nlhijos.getLength(); j++) {
			Node e = (Node)nlhijos.item(j);
	    		if (e.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
	    			extit = e.getNodeValue().trim();
				if (!extit.equals(""))
					break;
			}

		*/



    	// si hay un parámetro "real=no" se trabajará con datos inventados (real=0)
    	// de lo contrario se leerán los ficheros reales

    	String datosReales = servletConfig.getInitParameter("real");

    	if (datosReales != null)
    		if (datosReales.equals("no")) CommonTVML.real=0;

      String language = servletConfig.getInitParameter("language");

    	if (language != null)
    		if (language.equals("english")) MsgsTVML.Change2English();


    	// si hay un parámetro "dirBase", se tomará como directorio base de los ficheros
    	// de lo contrario se cogerá el especificado por defecto

    	dirBase = servletConfig.getInitParameter("dirBase");
    	if (dirBase == null) dirBase = dirBaseDefault;

    	// si hay un parámetro "urlInicial", se tomará como fichero inicial
    	// de lo contrario se cogerá el especificado por defecto

    	urlInicial = servletConfig.getInitParameter("urlInicial");
    	if (urlInicial == null)  urlInicial = urlInicialDefault;

        CommonTVML.logTVML("Leyendo ficheros...");
       	if (CommonTVML.real==1) this.buscarFicheros(dirBase, urlInicial, servletConfig);

       	Collections.sort(listWarnings);
       	Collections.sort(listErrores);
       	Collections.sort(listErroresfatales);

    }




    public void doGet(HttpServletRequest request, HttpServletResponse response)
    		throws IOException, ServletException
    {
        // previamente se ha comprobado la passwd con un filtro

	    String fase = request.getParameter(CommonSINT.PFASE);
	    if (fase == null) fase = "01";

	    CommonTVML.logTVML("Solicitud fase "+fase);

        response.setCharacterEncoding("utf-8");

	    switch (fase) {
		case "01":
			this.doGetHome(request,response);
			break;
		case "02":
			this.doGetErrors(request,response);
			break;

		// consulta 1, canciones de un interprete que duran menos que una dada

		case "11": // se pide el listado de fechas
			P2TVC1.doGetF11Dias(request, response);
			break;
		case "12": // se pide los canales de un día
			P2TVC1.doGetF12Canales(request, response);
			break;
		case "13": // se pide las películas de un canal de un día
			P2TVC1.doGetF13Peliculas(request, response);
			break;


			// consulta 2, Canales con películas en un idioma en un día

		case "21":  // se pide el listado de idiomas
			P2TVC2.doGetF21Langs(request, response);
			break;
		case "22":  // se pide los programas infantiles en un idioma
			P2TVC2.doGetF22Infantiles(request, response);
			break;
		case "23":  // se piden los canales que emiten películas en un idioma en un día
			P2TVC2.doGetF23Canales(request, response);
			break;

		default:
			CommonSINT.doBadRequest("el parámetro '"+CommonSINT.PFASE+"' tiene un valor incorrecto ("+fase+")", request, response);
			break;
	}
}







    // la pantalla inicial

    public void doGetHome (HttpServletRequest request, HttpServletResponse response)
    		throws IOException
    {
    	String auto = request.getParameter("auto");
      String fe = request.getParameter("fe");

      if (fe == null)  fe = "html";
      if (auto == null)  auto = "no";

      if (auto.equals("si")) {
          CommonTVML.logTVML("HOME auto=si");
          response.setContentType("text/xml");
          PrintWriter out = response.getWriter();
          P2TVFE.printHomeXML(out);
      }
      else
          if (fe.equals("html")) {
              CommonTVML.logTVML("HOME HTML auto=no");
              response.setContentType("text/html");
              PrintWriter out = response.getWriter();
              P2TVFE.printHomeHTML("html", out);
          }
          else {
              CommonTVML.logTVML("HOME AJAX auto=no");
              response.setContentType("text/html");
              PrintWriter out = response.getWriter();
              P2TVFE.printHomeHTML("ajax", out);
          }
    }


    // método que imprime o devuelve la lista de errores

    public void doGetErrors (HttpServletRequest request, HttpServletResponse response)
    		throws IOException
    {
    	String auto = request.getParameter("auto");
      String fe = request.getParameter("fe");

      if (fe == null)  fe = "html";
      if (auto == null)  auto = "no";

      if (auto.equals("si")) {
          CommonTVML.logTVML("HOME auto=si");
          response.setContentType("text/xml");
          PrintWriter out = response.getWriter();
          P2TVFE.printErrorsXML(out, listWarnings, listErrores, listErroresfatales);
      }
      else {
          response.setContentType("text/html");
          PrintWriter out = response.getWriter();

          if (fe.equals("html")) {
              CommonTVML.logTVML("HOME HTML auto=no");
              P2TVFE.printErrorsHTML(out, "html", listWarnings, listErrores, listErroresfatales);
          }
          else {
              CommonTVML.logTVML("HOME AJAX auto=no");
              P2TVFE.printErrorsHTML(out, "ajax", listWarnings, listErrores, listErroresfatales);
          }
      }

    }







    // MÉTODOS AUXILIARES


    // Zona de búsqueda de ficheros, llamado la primera vez que se invoca el doGet

    public void buscarFicheros (String urlBase, String fich, ServletConfig conf)
	throws UnavailableException  {

		DocumentBuilderFactory dbf;
		DocumentBuilder db;
		ErrorHandlerSINT errorHandler;

		dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		dbf.setNamespaceAware(true);
		dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

		ServletContext servCont = conf.getServletContext();
		String pathSchema = servCont.getRealPath(TVML_SCHEMA);
		File fileSchema = new File(pathSchema);
		dbf.setAttribute(JAXP_SCHEMA_SOURCE, fileSchema);

	/* otra forma
	 *
	 FICHERO_SCHEMA = "/eaml.xsd";
SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
StreamSource streamSource = new StreamSource(this.getServletContext().getResourceAsStream(FICHERO_SCHEMA));
Schema schema = sf.newSchema(streamSource);
dbf.setSchema(schema);

	 */


		try {
			db = dbf.newDocumentBuilder();
		}
		catch  (ParserConfigurationException e) {
			throw new UnavailableException("Error creando el analizador de ficheros "+MsgsTVML.LANGUAGE+": "+e);
		}

		errorHandler = new ErrorHandlerSINT();
		db.setErrorHandler(errorHandler);

    	Document doc;
    	String url = urlBase+fich;

    	listaFicherosProcesados.add(url);  // damos este fichero por procesado

    	// parsear el fichero solicitado

    	errorHandler.clear();  // resetear el ErrorHandler para borrar lo anterior

    	try {
    		doc = db.parse(url);
    	}
    	catch (SAXException ex) {
    		listErroresfatales.add(new WrongFile(url, ex.toString()));
    		return;
    	}
    	catch (IOException ex) {
    		listErroresfatales.add(new WrongFile(url, ex.toString()));
    		return;
    	}

    	// ver si saltó el ErrorHandler

    	if (errorHandler.hasWarnings()) {
      		listWarnings.add(new WrongFile(url, errorHandler.getWarnings()));
    	}

    	if (errorHandler.hasErrors()) {
    		listErrores.add(new WrongFile(url, errorHandler.getErrors()));
    		return;  // si hubo un error se termina
    	}

    	if (errorHandler.hasFatalerrors()) {
       		listErroresfatales.add(new WrongFile(url, errorHandler.getFatalerrors()));
    		return;  // si hubo un fatalerror se termina
    	}


    	// Vamos a procesar esta fecha para ver si contiene enlaces a otros ficheros

    	String fecha;

    	// averiguar la fecha del fichero que acabamos de leer
    	// la excepción no debería producirse, pero...
    	try {
    		NodeList nlFechas = (NodeList)CommonTVML.xpath.evaluate("/Programacion/Fecha", doc, XPathConstants.NODESET);
    		Element elemFecha = (Element)nlFechas.item(0);
    		fecha = elemFecha.getTextContent().trim();
    		if (fecha.equals("")) throw new Exception("Fecha vacía");
    	}
    	catch (Exception ex) {
       		listErrores.add(new WrongFile(url, "Problema leyendo 'Fecha' ("+ex+")"));
    		return;  // si se produce cualquier tipo de excepción, hay un error y se termina
    	}

    	CommonTVML.mapDocs.put(fecha,doc);  // almacenar el Document de la fecha leída


    	// buscar recursivamente los nuevos ficheros que hay en el que acabamos de leer

    	// conseguir la lista de OtraEmision

    	NodeList nlOtraEmision = doc.getElementsByTagName("OtraEmision");

    	for (int x=0; x < nlOtraEmision.getLength(); x++) {

    		// procesar cada uno de los encontrados

    		Element elemOtraEmision = (Element) nlOtraEmision.item(x);

    		// averiguar la fecha a la que corresponde
    		String nuevaFecha = elemOtraEmision.getAttribute("fecha");
    		if (nuevaFecha.equals("")) continue;

    		// averiguar el url de esa nueva fecha
			String nuevaUrl = CommonSINT.getTextContentOfChild(elemOtraEmision, "TVML");
			if (nuevaUrl.equals("")) continue;

			String laBase, elFichero;

			if (nuevaUrl.startsWith("http://"))   {  // si es absoluta la dividimos entre la base y el fichero
				laBase = nuevaUrl.substring(0,nuevaUrl.lastIndexOf('/')+1);
				elFichero = nuevaUrl.substring(nuevaUrl.lastIndexOf('/')+1);
			}
			else {
				laBase = urlBase;
				elFichero = nuevaUrl;
			}

			// si ya hemos leído este fichero en el pasado lo saltamos
			if (listaFicherosProcesados.contains(laBase+elFichero)) continue;

    		// mirar si este año ya lo tenemos, lo normal es que no

    		Document doc2 = CommonTVML.mapDocs.get(nuevaFecha);

    		// si no lo tenemos, aplicamos este método recursivamente sobre su fichero

    		if (doc2 == null) this.buscarFicheros(laBase, elFichero, conf);

    	}
    }
}
