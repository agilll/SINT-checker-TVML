/****************************************************************
 *    SERVICIOS DE INTERNET
 *    EE TELECOMUNICACIÓN
 *    UNIVERSIDAD DE VIGO
 *
 *    Práctica EAML
 *
 *    Autor: Alberto Gil Solla
 *    Curso : 2020-2021
 ****************************************************************/

// puede trabajar con datos reales o inventados (parámetro 'real=no' en web.xml)
// se puede especificar en el web.xml el directorio base de los documentos (parámetro dirBase),
// el directorio base de los Schemas (dirRulesBase), y el fichero inicial (urlInicial)

package docencia.sint.EAML;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

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
import docencia.sint.Common.Msgs;
import docencia.sint.Common.ErrorHandlerSINT;
import docencia.sint.Common.WrongFile;

import javax.xml.xpath.XPathConstants;



public class P2EA extends HttpServlet {

    private static final long serialVersionUID = 1L;

		// todas estas son variables de clase, compartidas por todos los usuarios

    final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    final String EAML_SCHEMA = "/_rules/eaml.xsd";

    // los valores por defecto, si no hay parámetros de configuración
    String dirBaseDefault = "http://localhost:7000/sintprof/ficheros_prueba/20-21_EAML/";
    String urlInicialDefault = "teleco.xml";

    // los parámetros de configuración o los valores por defecto
    String dirBase;
    String urlInicial;



    // El init se ejecuta al cargar el servlet la primera vez

    public void init (ServletConfig servletConfig) throws ServletException {

      CommonEAML.initLoggerEAML(P2EA.class);
      CommonEAML.logEAML("\nInit..."+servletConfig.getServletName());

    	// si hay un parámetro "real=no" se trabajará con datos inventados (real=0)
    	// de lo contrario se leerán los ficheros reales

    	String datosReales = servletConfig.getInitParameter("real");

    	if (datosReales != null)
    		if (datosReales.equals("no")) CommonEAML.real=0;


    	// si hay un parámetro "dirBase", se tomará como directorio base de los ficheros
    	// de lo contrario se cogerá el especificado por defecto

    	dirBase = servletConfig.getInitParameter("dirBase");
    	if (dirBase == null) dirBase = dirBaseDefault;

    	// si hay un parámetro "urlInicial", se tomará como fichero inicial
    	// de lo contrario se cogerá el especificado por defecto

    	urlInicial = servletConfig.getInitParameter("urlInicial");
    	if (urlInicial == null)  urlInicial = urlInicialDefault;

      CommonEAML.logEAML("\nLeyendo ficheros...");
      if (CommonEAML.real==1) this.buscarFicheros(dirBase, urlInicial, servletConfig);

      Collections.sort(CommonEAML.listWarnings);
      Collections.sort(CommonEAML.listErrores);
      Collections.sort(CommonEAML.listErroresfatales);
    }




    public void doGet(HttpServletRequest request, HttpServletResponse response)
    		throws IOException, ServletException
    {
      // previamente se ha comprobado la passwd con un filtro
			String language;

			String servletPath = request.getServletPath();  // es el servletPath, que será /P2EA o /P2EAe
			if (servletPath.equals("/P2EAe")) language = "en";
			else language = "es";

			String auto = request.getParameter("auto");  // auto = "true" o "false" (default)
      String fe = request.getParameter("fe");    // fe = "ajax" o "html" (default)

      if (auto == null)  auto = "false";
			else
				if (!auto.equals("true")  &&  !auto.equals("false")) {
					// CP09 = "el parámetro %s tiene un valor incorrecto (%s)"
					CommonSINT.doBadRequest(String.format(Msgs.getMsg(Msgs.CP09, language), "auto", auto), request, response);
					return;
				}

      if (fe == null)  fe = "html";
			else
				if (!fe.equals("html")  &&  !fe.equals("ajax")) {
					// CP09 = "el parámetro %s tiene un valor incorrecto (%s)"
					CommonSINT.doBadRequest(String.format(Msgs.getMsg(Msgs.CP09, language), "fe", fe), request, response);
					return;
				}


	    String fase = request.getParameter(CommonSINT.PFASE);
	    if (fase == null) fase = "01";

	    CommonEAML.logEAML("Solicitud fase "+fase);

      response.setCharacterEncoding("utf-8");

	    switch (fase) {
				case "01":
					this.doGetHome(request,response, language, auto, fe);
					break;
				case "02":
					this.doGetErrors(request,response, language, auto, fe);
					break;

					// consulta 1, alumnos en una asignatura de una titulación

				case "11": // se pide el listado de titulaciones
					P2EAC1.doGetF11Degrees(request, response, language, auto, fe);
					break;
				case "12": // se pide las asignaturas de una titulación
					P2EAC1.doGetF12Subjects(request, response, language, auto, fe);
					break;
				case "13": // se pide los alumnos de una asignatura de una titulación
					P2EAC1.doGetF13Students(request, response, language, auto, fe);
					break;

				// consulta 2, notas de las asignaturas de un alumno de una titulación

				case "21":  // se pide el listado de titulaciones
					P2EAC2.doGetF21Degrees(request, response, language, auto, fe);
					break;
				case "22":  // se pide el listado de alumnos de una titulación
					P2EAC2.doGetF22Students(request, response, language, auto, fe);
					break;
				case "23":  // se piden las notas de un alumno en una titulación
					P2EAC2.doGetF23Subjects(request, response, language, auto, fe);
					break;

				default:
					// CP09 = "el parámetro %s tiene un valor incorrecto (%s)"
					CommonSINT.doBadRequest(String.format(Msgs.getMsg(Msgs.CP09, language), CommonSINT.PFASE, fase), request, response);
					break;
	}
}







    // la pantalla inicial

    public void doGetHome (HttpServletRequest request, HttpServletResponse response, String language, String auto, String fe)
    		throws IOException
    {
      if (auto.equals("true")) {
          response.setContentType("text/xml");
          PrintWriter out = response.getWriter();
          P2EAFE.printHomeXML(out);
      }
      else {
					response.setContentType("text/html");
					PrintWriter out = response.getWriter();
					P2EAFE.printHomeHTML(language, fe, out);
			}
    }


    // método que imprime o devuelve la lista de errores

    public void doGetErrors (HttpServletRequest request, HttpServletResponse response, String language, String auto, String fe)
    		throws IOException
    {
      if (auto.equals("true")) {
          response.setContentType("text/xml");
          PrintWriter out = response.getWriter();
          P2EAFE.printErrorsXML(out);
      }
      else {
          response.setContentType("text/html");
          PrintWriter out = response.getWriter();
          P2EAFE.printErrorsHTML(language, fe, out);
      }
    }







    // MÉTODOS AUXILIARES

    // Zona de búsqueda de ficheros, llamado la primera vez que se invoca el doGet

    public void buscarFicheros (String urlBase, String fich, ServletConfig conf)
	         throws UnavailableException  {

        Document doc, doc2;
    		DocumentBuilderFactory dbf;
    		DocumentBuilder db;
    		ErrorHandlerSINT errorHandler;

        CommonEAML.logEAML("\nBuscando ficheros..."+fich);

        String url = urlBase+fich;
        if (!CommonEAML.listaFicherosProcesados.contains(url))
						CommonEAML.listaFicherosProcesados.add(url);  // damos este fichero por procesado

    		dbf = DocumentBuilderFactory.newInstance();
    		dbf.setValidating(true);
    		dbf.setNamespaceAware(true);
    		dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

    		ServletContext servCont = conf.getServletContext();
    		String pathSchema = servCont.getRealPath(EAML_SCHEMA);
    		File fileSchema = new File(pathSchema);
    		dbf.setAttribute(JAXP_SCHEMA_SOURCE, fileSchema);
                // dbf.setAttribute(JAXP_SCHEMA_SOURCE, "http://localhost:7000/sintprof/_rules/eaml.xsd");  // también funciona

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
    			throw new UnavailableException("Error creando el analizador de ficheros "+MsgsEAML.XML_LANGUAGE+": "+e);
    		}

    		errorHandler = new ErrorHandlerSINT();
    		db.setErrorHandler(errorHandler);

      	// parsear el fichero solicitado

      	errorHandler.clear();  // resetear el ErrorHandler para borrar lo anterior

      	try {
      		doc = db.parse(url);
      	}
      	catch (SAXException ex) {
      		CommonEAML.listErroresfatales.add(new WrongFile(url, ex.toString()));
      		return;
      	}
      	catch (IOException ex) {
      		CommonEAML.listErroresfatales.add(new WrongFile(url, ex.toString()));
      		return;
      	}

      	// ver si saltó el ErrorHandler

      	if (errorHandler.hasWarnings()) {
        		CommonEAML.listWarnings.add(new WrongFile(url, errorHandler.getWarnings()));
      	}

      	if (errorHandler.hasErrors()) {
      		CommonEAML.listErrores.add(new WrongFile(url, errorHandler.getErrors()));
      		return;  // si hubo un error se termina
      	}

      	if (errorHandler.hasFatalerrors()) {
         		CommonEAML.listErroresfatales.add(new WrongFile(url, errorHandler.getFatalerrors()));
      		return;  // si hubo un fatalerror se termina
      	}

				CommonEAML.logEAML(fich+" parseado");
      	// Vamos a procesar esta titulación para ver si contiene enlaces a otros ficheros

      	String nameDegree;

      	// averiguar el nombre de la titulación del fichero que acabamos de leer
      	// la excepción no debería producirse, pero...
      	try {
      		NodeList nlNameDegrees = (NodeList)CommonEAML.xpath.evaluate("/Degree/Name", doc, XPathConstants.NODESET);
      		Element elemNameDegree = (Element)nlNameDegrees.item(0);
      		nameDegree = elemNameDegree.getTextContent().trim();
      		if (nameDegree.equals("")) throw new Exception("Nombre vacío");
      	}
      	catch (Exception ex) {
         		CommonEAML.listErrores.add(new WrongFile(url, "Problema leyendo 'Name' ("+ex+")"));
      		return;  // si se produce cualquier tipo de excepción, hay un error y se termina
      	}

        doc2 = CommonEAML.mapDocs.get(nameDegree);

        // si no lo tenemos, lo añadimos. si ya lo tenemos (imposible), no lo añadimos y volvemos

        if (doc2 == null)  CommonEAML.mapDocs.put(nameDegree,doc);  // almacenar el Document de la titulación leída
        else {
					CommonEAML.logEAML(fich+" ya fue incluido");
					return;
				}

    	  // buscar recursivamente los nuevos ficheros que hay en el que acabamos de leer

    	  // conseguir la lista de enlaces EAML

    	  NodeList nlEAMLs = doc.getElementsByTagName("EAML");

    	  for (int x=0; x < nlEAMLs.getLength(); x++) {

      		 // procesar cada uno de los encontrados

      		 Element elemEAML = (Element) nlEAMLs.item(x);

           String newURL = CommonSINT.getTextContent(elemEAML);

  			   if (newURL.equals("")) continue;

  			   String laBase, elFichero;

    			 if (newURL.startsWith("http://"))   {  // si es absoluta la dividimos entre la base y el fichero
    				    laBase = newURL.substring(0,newURL.lastIndexOf('/')+1);
    				    elFichero = newURL.substring(newURL.lastIndexOf('/')+1);
    			 }
    			 else {
    				    laBase = urlBase;
    				    elFichero = newURL;
    			 }

    			 // si ya hemos leído este fichero en el pasado lo saltamos
    			 if (CommonEAML.listaFicherosProcesados.contains(laBase+elFichero)) continue;

           // analizamos el nuevo fichero
           this.buscarFicheros(laBase, elFichero, conf);
        }
    }
}
