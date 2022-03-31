/****************************************************************
 *    SERVICIOS DE INTERNET
 *    EE TELECOMUNICACIÓN
 *    UNIVERSIDAD DE VIGO
 *
 *    Checker de la práctica de TVML
 *
 *    Autor: Alberto Gil Solla
 *    Curso : 2019-2020
 ****************************************************************/


// variables y métodos comunes a toda la aplicación


package docencia.sint.TVML.checker;

import java.util.ArrayList;
import java.util.Scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.UnavailableException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.SAXException;

import docencia.sint.Common.CheckerFailure;
import docencia.sint.Common.CommonSINT;
import docencia.sint.Common.ErrorHandlerSINT;
import docencia.sint.Common.ExcepcionChecker;
import docencia.sint.Common.ExcepcionSINT;
import docencia.sint.TVML.CommonTVML;
import docencia.sint.TVML.MsgsTVML;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


class CommonTVChecker {

	public static ServletContext servletContextSintProf;

	public static int esProfesor=0;

	public static final String MSG_TITLE = "Checker de "+MsgsTVML.LANGUAGE;
	public static final String SERVICE_NAME = "/P2TV";
	public static final String CHECKER_NAME  = MsgsTVML.LANGUAGE+" Checker";
 public static final String CREATED = "2019";
	public static final String PROF_CONTEXT  = "/sintprof";

	public static String server_port;
	public static String servicioProf;

	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	static final String SCHEMA_TVMLAUTO = "_rules/tvmlAuto.xsd";
    	static final String CSS_FILE = "css/tvmlChecker.css";

	public static DocumentBuilderFactory dbf;
	public static DocumentBuilder db;
	public static ErrorHandlerSINT errorHandler;

	private static Logger logger = null;  // el objeto Logger

	// para inicializar el objeto Logger

	public static void initLoggerTVChecker (Class c) {
		logger = LogManager.getLogger(c);
	}

	 // para imprimir con el Logger en el sintprof.log

	 public static void logTVChecker (String msg) {
		logger.info("## TVML Checker ## "+msg);
	 }



	public static void printHead(PrintWriter out)
	{
		out.println("<head><meta charset='utf-8'/>");
		out.println("<title>"+CommonTVChecker.MSG_TITLE+"</title>");
		out.println("<link rel='stylesheet'  type='text/css' href='"+CommonTVChecker.CSS_FILE+"'/></head>");
	}


	public static void printBodyHeader(PrintWriter out)
	{
		out.println("<body>");
		out.println("<div id='asignatura'>Servicios de Internet </div><br>");
		out.println("<div id='grado'>EE Telecomunicación (Universidad de Vigo) </div><br>");
		out.println("<div id='servicio'>Comprobación de servicios sobre "+MsgsTVML.LANGUAGE+"<br> Curso "+MsgsTVML.CURSO+"<hr></div>");
	}



	// para crear el DocumentBuilder, se invoca desde el init del servlet

	public static void createDocumentBuilder() throws  UnavailableException
	{
	    dbf = DocumentBuilderFactory.newInstance();
	    dbf.setValidating(true);
	    dbf.setNamespaceAware(true);
	    dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

	    String pathSchema = CommonTVChecker.servletContextSintProf.getRealPath(SCHEMA_TVMLAUTO);
	    File fileSchema = new File(pathSchema);

	    dbf.setAttribute(JAXP_SCHEMA_SOURCE, fileSchema);    // se valida con el schema del modo auto

	    try {
		    db = dbf.newDocumentBuilder();
	    }
	    catch  (ParserConfigurationException e) {
		    throw new UnavailableException("Error creando el analizador de respuestas del modo auto: "+e);
	    }

	    errorHandler = new ErrorHandlerSINT();
	    db.setErrorHandler(CommonTVChecker.errorHandler);
	}




	// para comprobar las listas de errores de profesor y alumno (sólo se compara los nombres de los ficheros, no la explicación del error)
	// no devuelve nada si son iguales
	// levanta una excepcion ExcepcionChecker en caso contrario

	public static void comparaErrores (String usuario, String servicioAluP, String passwdAlu)
			throws ExcepcionChecker
	{
		CheckerFailure cf;

		// pedimos la lista de errores de sintprof

		Element pErrores;
		try {
			pErrores = requestErrores("sintprof", CommonTVChecker.servicioProf, CommonSINT.PPWD);
		}
		catch (ExcepcionChecker ex) {
			cf = ex.getCheckerFailure();
			cf.addMotivo("comparaErrores: ExcepcionChecker solicitando el informe de errores de sintprof");
			throw new ExcepcionChecker(cf);
		}


		// pedimos la lista de errores del sintX

		Element xErrores;
		try {
			xErrores = requestErrores(usuario, servicioAluP, passwdAlu);
		}
		catch (ExcepcionChecker ex) {
			cf = ex.getCheckerFailure();
			cf.addMotivo("comparaErrores: ExcepcionChecker solicitando el informe de errores");
			throw new ExcepcionChecker(cf);
		}


		ArrayList<String> pList = new ArrayList<String>();  // para crear la lista de ficheros del profesor
		ArrayList<String> xList = new ArrayList<String>();  // para crear la lista de ficheros del alumno



		// vamos a comparar los ficheros que dan warning

		NodeList pnlWarnings = pErrores.getElementsByTagName("warning");
		NodeList xnlWarnings = xErrores.getElementsByTagName("warning");

		// extraemos los warnings del profesor

		for (int x=0; x < pnlWarnings.getLength(); x++) {
			Element pelemWarning = (Element)pnlWarnings.item(x);     // cogemos uno de los warnings

			String pnombreWarningFile = CommonSINT.getTextContentOfChild (pelemWarning, "file");  //cogemos el contenido de su elemento file

			// vamos a comparar sólo el nombre, no el path completo

			int pos = pnombreWarningFile.lastIndexOf('/');
			if (pos != -1) pnombreWarningFile = pnombreWarningFile.substring(pos+1);

			pList.add(pnombreWarningFile);
		}

		// extraemos los warnings del alumno

		for (int x=0; x < xnlWarnings.getLength(); x++) {
			Element xelemWarning = (Element)xnlWarnings.item(x);    // cogemos uno de los warnings

			String xnombreWarningFile = CommonSINT.getTextContentOfChild (xelemWarning, "file");  //cogemos el contenido de su elemento file

			int pos = xnombreWarningFile.lastIndexOf('/');
			if (pos != -1) xnombreWarningFile = xnombreWarningFile.substring(pos+1);

			xList.add(xnombreWarningFile);
		}

		// comprobamos que las listas sean de igual tamaño

		if (pList.size() != xList.size()) {
			cf = new CheckerFailure("", "", "comparaErrores: La lista de warnings tiene "+xList.size()+" elementos, pero debería tener "+pList.size());
			throw new ExcepcionChecker(cf);
		}

		// comprobamos que todos los ficheros del alumno están en la lista del profesor

		for (int x=0; x < xList.size(); x++)
			if (!pList.contains(xList.get(x))) {
				cf = new CheckerFailure("", "", "comparaErrores: El warning número "+x+" ("+xList.get(x)+") no es correcto");
				throw new ExcepcionChecker(cf);
			}



		// vamos a comparar los ficheros que dan error

		pList.clear();
		xList.clear();
		NodeList pnlErrors = pErrores.getElementsByTagName("error");
		NodeList xnlErrors = xErrores.getElementsByTagName("error");

		// extraemos los errors del profesor

		for (int x=0; x < pnlErrors.getLength(); x++) {
			Element pelemError = (Element)pnlErrors.item(x);   // cogemos uno de los errors

			String pnombreErrorFile = CommonSINT.getTextContentOfChild (pelemError, "file");  //cogemos el contenido de su elemento file

			int pos = pnombreErrorFile.lastIndexOf('/');
			if (pos != -1) pnombreErrorFile = pnombreErrorFile.substring(pos+1);

			pList.add(pnombreErrorFile);
		}

		// extraemos los errors del alumno

		for (int x=0; x < xnlErrors.getLength(); x++) {
			Element xelemError = (Element)xnlErrors.item(x);  // cogemos uno de los errors

			String xnombreErrorFile = CommonSINT.getTextContentOfChild (xelemError, "file");  //cogemos el contenido de su elemento file

			int pos = xnombreErrorFile.lastIndexOf('/');
			if (pos != -1) xnombreErrorFile = xnombreErrorFile.substring(pos+1);

			xList.add(xnombreErrorFile);
		}

		// comprobamos que las listas sean de igual tamaño

		String errorFalta = "";

		if (xList.size() < pList.size()) {

		    for (int x=0; x < pList.size(); x++)
		    	if (!xList.contains(pList.get(x)))
		    		errorFalta = pList.get(x);

			cf = new CheckerFailure("", "", "comparaErrores: Faltan errors: hay "+xList.size()+" elementos, pero debería haber "+pList.size()+" (falta "+errorFalta+")");
			throw new ExcepcionChecker(cf);
		}

		if (pList.size() < xList.size()) {

		    for (int x=0; x < xList.size(); x++)
		    	if (!pList.contains(xList.get(x)))
		    		errorFalta = xList.get(x);

			cf = new CheckerFailure("", "", "comparaErrores: Sobran errors: hay "+xList.size()+" elementos, pero debería haber "+pList.size()+" (sobra "+errorFalta+")");
			throw new ExcepcionChecker(cf);
		}

		// comprobamos que todos los ficheros del alumno están en la lista del profesor


		for (int x=0; x < xList.size(); x++)
			if (!pList.contains(xList.get(x))) {
				cf = new CheckerFailure("", "", "comparaErrores: El error número "+x+" ("+xList.get(x)+") no es correcto");
				throw new ExcepcionChecker(cf);
			}


		// vamos a comparar los ficheros que dan fatalerror

		pList.clear();
		xList.clear();
		NodeList pnlFatalErrors = pErrores.getElementsByTagName("fatalerror");
		NodeList xnlFatalErrors = xErrores.getElementsByTagName("fatalerror");

		// extraemos los fatalerrors del profesor

		for (int x=0; x < pnlFatalErrors.getLength(); x++) {
			Element pelemFatalError = (Element)pnlFatalErrors.item(x);   // cogemos uno de los fatalerrors

			String pnombreFatalErrorFile = CommonSINT.getTextContentOfChild (pelemFatalError, "file");  //cogemos el contenido de su elemento file

			int pos = pnombreFatalErrorFile.lastIndexOf('/');
			if (pos != -1) pnombreFatalErrorFile = pnombreFatalErrorFile.substring(pos+1);

			pList.add(pnombreFatalErrorFile);
		}

		// extraemos los fatalerrors del alumno

		for (int x=0; x < xnlFatalErrors.getLength(); x++) {
			Element xelemFatalError = (Element)xnlFatalErrors.item(x);  // cogemos uno de los fatalerrors

			String xnombreFatalErrorFile = CommonSINT.getTextContentOfChild (xelemFatalError, "file");  //cogemos el contenido de su elemento file

			int pos = xnombreFatalErrorFile.lastIndexOf('/');
			if (pos != -1) xnombreFatalErrorFile = xnombreFatalErrorFile.substring(pos+1);

			xList.add(xnombreFatalErrorFile);
		}

		// comprobamos que las listas sean de igual tamaño

		if (pList.size() != xList.size()) {

			cf = new CheckerFailure("", "", "comparaErrores: La lista de fatal errors tiene "+xList.size()+" elementos, pero debería tener "+pList.size());
			throw new ExcepcionChecker(cf);
		}

		// comprobamos que todos los ficheros del alumno están en la lista del profesor

		for (int x=0; x < xList.size(); x++)
			if (!pList.contains(xList.get(x))) {
				cf = new CheckerFailure("", "", "comparaErrores: El fatal error número "+x+" ("+xList.get(x)+") no es correcto");
				throw new ExcepcionChecker(cf);
			}

                System.out.println("comparaErrores: "+ usuario +" OK");

		return;  // todo fue igual
	}




	// pide y devuelve la lista de errores detectados de un usuario

	 public static Element requestErrores (String usuario, String url, String passwd)
			 		throws ExcepcionChecker
	 {
		Document doc;
		String qs, call;
		CheckerFailure cf;

		qs = "?"+CommonSINT.PFASE+"=02&auto=si&p="+passwd;
		call = url+qs;

		errorHandler.clear();

		try {
			doc = db.parse(call);
		}
		catch (SAXException ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo("requestErrores: SAXException al solicitar y parsear la lista de errores");
			throw new ExcepcionChecker(cf);
		}
		catch (Exception ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo("requestErrores: Exception al solicitar y parsear la lista de errores");
			throw new ExcepcionChecker(cf);
		}


		if (errorHandler.hasErrors()) {
			CommonTVChecker.logCall(call);

			ArrayList<String> errors = errorHandler.getErrors();
			String msg="";

			for (int x=0; x < errors.size(); x++)
				if (x == (errors.size()-1)) msg += "++++"+errors.get(x);
				else msg += "++++"+errors.get(x)+"<br>\n";


			cf = new CheckerFailure(call, "", msg);
			cf.addMotivo("requestErrores: La respuesta con la lista de errores es inválida, se generan 'errors' al parsearla");
			throw new ExcepcionChecker(cf);
		}

		if (errorHandler.hasFatalerrors()) {
			CommonTVChecker.logCall(call);

			ArrayList<String> fatalerrors = errorHandler.getFatalerrors();
			String msg="";

			for (int x=0; x < fatalerrors.size(); x++)
				if (x == (fatalerrors.size()-1)) msg += "++++"+fatalerrors.get(x);
				else msg += "++++"+fatalerrors.get(x)+"<br>\n";


			cf = new CheckerFailure(call, "", msg);
			cf.addMotivo("requestErrores: La respuesta con la lista de errores es inválida, se generan 'fatal errors' al parsearla");
			throw new ExcepcionChecker(cf);
		}


		if (doc == null) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", "requestErrores: Se recibe 'null' al solicitar y parsear la lista de errores");
			throw new ExcepcionChecker(cf);
		}

		Element e = doc.getDocumentElement();
		String tagName = e.getTagName();
		if (tagName.equals("wrongRequest")) {
			String reason = e.getTextContent();

			cf = new CheckerFailure(call, "", "requestErrores: Se recibe &lt;wrongRequest>"+reason+" &lt;/wrongRequest> al solicitar la lista de errores");
			throw new ExcepcionChecker(cf);
		}

		if (!tagName.equals("errores")) {
			CommonTVChecker.logTVChecker("Elemento resultado = "+tagName);

			cf = new CheckerFailure(call, "", "requestErrores: Tipo de respuesta incorrecta (&lt;"+tagName+">) al solicitar la lista de errores");
			throw new ExcepcionChecker(cf);
		}

		NodeList nlErrores = doc.getElementsByTagName("errores");
		Element elemErrores = (Element)nlErrores.item(0);

		return elemErrores;
	}





	// método para comprobar si un servicio está operativo, pidiendo el estado
	// no devuelve nada si está operativo,
	// levanta una excepción  ExcepcionChecker si falla algo

	public static void doOneCheckUpStatus (HttpServletRequest request, String user, String passwd)
	throws ExcepcionChecker
	{
		CheckerFailure cf;
		Document doc;
		Element e;
		String qs;

		ServletContext scPropio, scUser;
		scPropio = request.getServletContext();  // contexto del que ejecuta el checker (sintprof)
		scUser = scPropio.getContext("/"+user);  // contexto del User que vamos a comprobar

		CommonTVChecker.logTVChecker("Vamos a comprobar el estado de "+user);

		if (scUser == null)  {
			CommonTVChecker.logTVChecker("01_NOCONTEXT: null "+user);
			throw new ExcepcionChecker(new CheckerFailure("", "01_NOCONTEXT", "doOneCheckUpStatus: No existe el contexto "+user));
		}

		String cpUser = scUser.getContextPath();        // context path del User que vamos a comprobar

		if (cpUser.equals("")) {
			CommonTVChecker.logTVChecker("01_NOCONTEXT:"+cpUser+"**"+user);
			throw new ExcepcionChecker(new CheckerFailure("", "01_NOCONTEXT", "doOneCheckUpStatus: No existe el contexto "+user));
		}


		// vamos a pedir el estado sin passwd, para comprobar que responde con error

		String url = "http://"+server_port+cpUser+CommonTVChecker.SERVICE_NAME;
		String call;

		errorHandler.clear();

		qs = "?auto=si";
		call = url+qs;

		try {
			doc = db.parse(call);  // petición del estado sin passwd
		}
		catch (ConnectException ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			cf = new CheckerFailure(call, "00_DOWN", ex.toString());
			cf.addMotivo("doOneCheckUpStatus: (ConnectException) El servidor no responde");
			throw new ExcepcionChecker(cf);    // No debería pasar, ya que es el mismo servidor que está ejecutando el checker
		}
		catch (FileNotFoundException ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			cf = new CheckerFailure(call, "02_FILENOTFOUND", ex.toString());
			cf.addMotivo("doOneCheckUpStatus: (FileNotFoundException) Ese servlet no está declarado");
			throw new ExcepcionChecker(cf);   // el servlet no está declarado en el web.xml de ese usuario
		}
		catch (com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			cf = new CheckerFailure(call, "03_ENCODING", ex.toString());
			cf.addMotivo("doOneCheckUpStatus: (MalformedByteSequenceException) La codificación de caracteres recibida es incorrecta (no UTF-8)");
			throw new ExcepcionChecker(cf);
		}
		catch (IOException ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			cf = new CheckerFailure(call, "04_IOEXCEPTION", ex.toString());
			cf.addMotivo("doOneCheckUpStatus: (IOException) No se ha encontrado la clase del servlet o ésta devolvió una excepción al pedir el estado sin passwd");
			throw new ExcepcionChecker(cf);
		}
		catch (SAXException ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "05_BF", ex.toString());
			cf.addMotivo("doOneCheckUpStatus: (SAXException) La respuesta al pedir el estado sin passwd está mal construida");
			throw new ExcepcionChecker(cf);
		}
		catch (Exception ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call,"07_ERRORUNKNOWN", ex.toString());
			cf.addMotivo("doOneCheckUpStatus: (Exception) Error desconocido al realizar la solicitud de estado sin passwd");
			throw new ExcepcionChecker(cf);
		}

		if (errorHandler.hasErrors()) {
			CommonTVChecker.logCall(call);

			ArrayList<String> errors = errorHandler.getErrors();
			String msg="";

			for (int x=0; x < errors.size(); x++)
				if (x == (errors.size()-1)) msg += "++++"+errors.get(x);
				else msg += "++++"+errors.get(x)+"<br>\n";

			CommonTVChecker.logTVChecker(msg);

			cf = new CheckerFailure(call,"06_INVALID", msg);
			cf.addMotivo("doOneCheckUpStatus: La respuesta al pedir el estado sin passwd es inválida, tiene errors");
			throw new ExcepcionChecker(cf);
		}

		if (errorHandler.hasFatalerrors()) {
			CommonTVChecker.logCall(call);

			ArrayList<String> fatalerrors = errorHandler.getFatalerrors();
			String msg="";

			for (int x=0; x < fatalerrors.size(); x++)
				if (x == (fatalerrors.size()-1)) msg += "++++"+fatalerrors.get(x);
				else msg += "++++"+fatalerrors.get(x)+"<br>\n";


			CommonTVChecker.logTVChecker(msg);
			cf = new CheckerFailure(call,"06_INVALID", msg);
			cf.addMotivo("doOneCheckUpStatus: La respuesta al pedir el estado sin passwd es inválida, tiene fatal errors");
			throw new ExcepcionChecker(cf);
		}


		e = doc.getDocumentElement();
		String tagName = e.getTagName();

		if (tagName.equals("service"))
			throw new ExcepcionChecker(new CheckerFailure(call, "08_OKNOPASSWD", "doOneCheckUpStatus: No ha requerido passwd"));

		if (tagName.equals("wrongRequest")) {
			String reason = e.getTextContent().trim().toLowerCase();
			if (!reason.equals("no passwd"))
				throw new ExcepcionChecker(new CheckerFailure(call, "10_BADANSWER", "doOneCheckUpStatus: Responde con &lt;wrongRequest> pero no por 'no passwd', sino por: "+reason));
		}
		else throw new ExcepcionChecker(new CheckerFailure(call, "10_BADANSWER", "doOneCheckUpStatus: No ha contestado con &lt;wrongRequest> al no enviar passwd, sino con &lt;"+tagName+">"));


		// si ha llegado hasta aquí es que respondió correctamente con <wrongRequest>no passwd</wrongRequest>


		// vamos ahora a pedir el estado incluyendo la passwd

		errorHandler.clear();

		qs = "?auto=si&p="+passwd;
		call = url+qs;

		try {
			doc = db.parse(call);  // petición del estado
		}
		catch (ConnectException ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			cf = new CheckerFailure(call, "00_DOWN", ex.toString());
			cf.addMotivo("doOneCheckUpStatus: (ConnectException) El servidor no responde");
			throw new ExcepcionChecker(cf);    // No debería pasar, ya que es el mismo servidor que está ejecutando el checker
		}
		catch (FileNotFoundException ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			cf = new CheckerFailure(call, "02_FILENOTFOUND", ex.toString());
			cf.addMotivo("doOneCheckUpStatus: (FileNotFoundException) Ese servlet no está declarado");
			throw new ExcepcionChecker(cf);   // el servlet no está declarado en el web.xml de ese usuario
		}
		catch (com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			cf = new CheckerFailure(call, "03_ENCODING", ex.toString());
			cf.addMotivo("doOneCheckUpStatus: (MalformedByteSequenceException) La codificación de caracteres recibida es incorrecta (no UTF-8)");
			throw new ExcepcionChecker(cf);
		}
		catch (IOException ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			cf = new CheckerFailure(call, "04_IOEXCEPTION", ex.toString());
			cf.addMotivo("doOneCheckUpStatus: (IOException) No se ha encontrado la clase del servlet o ésta devolvió una excepción al pedir el estado");
			throw new ExcepcionChecker(cf);
		}
		catch (SAXException ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "05_BF", ex.toString());
			cf.addMotivo("doOneCheckUpStatus: (SAXException) La respuesta al pedir el estado está mal construida");
			throw new ExcepcionChecker(cf);
		}
		catch (Exception ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call,"07_ERRORUNKNOWN", ex.toString());
			cf.addMotivo("doOneCheckUpStatus: (Exception) Error desconocido al realizar la solicitud de estado");
			throw new ExcepcionChecker(cf);
		}


		if (errorHandler.hasErrors()) {
			CommonTVChecker.logCall(call);

			ArrayList<String> errors = errorHandler.getErrors();
			String msg="";

			for (int x=0; x < errors.size(); x++)
				if (x == (errors.size()-1)) msg += "++++"+errors.get(x);
				else msg += "++++"+errors.get(x)+"<br>\n";


			CommonTVChecker.logTVChecker(msg);
			cf = new CheckerFailure(call,"06_INVALID", msg);
			cf.addMotivo("doOneCheckUpStatus: La respuesta al pedir el estado con passwd es inválida, tiene errors");
			throw new ExcepcionChecker(cf);
		}

		if (errorHandler.hasFatalerrors()) {
			CommonTVChecker.logCall(call);

			ArrayList<String> fatalerrors = errorHandler.getFatalerrors();
			String msg="";

			for (int x=0; x < fatalerrors.size(); x++)
				if (x == (fatalerrors.size()-1)) msg += "++++"+fatalerrors.get(x);
				else msg += "++++"+fatalerrors.get(x)+"<br>\n";


			CommonTVChecker.logTVChecker(msg);
			cf = new CheckerFailure(call,"06_INVALID", msg);
			cf.addMotivo("doOneCheckUpStatus: La respuesta al pedir el estado con passwd es inválida, tiene fatal errors");
			throw new ExcepcionChecker(cf);
		}


		e = doc.getDocumentElement();
		tagName = e.getTagName();

		if (tagName.equals("wrongRequest")) {
			String reason = e.getTextContent().trim();
			if (reason.equals("bad passwd")) {
				throw new ExcepcionChecker(new CheckerFailure(call, "09_BADPASSWD", "doOneCheckUpStatus: Dice que la passwd enviada es incorrecta"));
			}
			else throw new ExcepcionChecker(new CheckerFailure(call, "10_BADANSWER", "doOneCheckUpStatus: contesta con &lt;wrongRequest> con razón desconocida ("+reason+") al pedir el estado"));
		}

		if (!tagName.equals("service"))
			throw new ExcepcionChecker(new CheckerFailure(call, "10_BADANSWER", "doOneCheckUpStatus: contesta con un tag inadecuado (&lt;"+tagName+">) al pedir el estado"));

                System.out.println("doOneCheckUpStatus: "+user+" OK");

		return;
	}






	// Envío del fichero de un resultado negativo
	// el nombre del fichero se recibe en el parámetro 'file'

	public static void doGetRequestResultFile(HttpServletRequest request, PrintWriter out)
						throws IOException, ServletException
	{
		File file;
		BufferedReader br;
		String linea;

		out.println("<html>");
		CommonTVChecker.printHead(out);
		CommonTVChecker.printBodyHeader(out);

		String resultFile = request.getParameter("file");
		if (resultFile == null) {
			out.println("<h4>Error: no se ha recibido el parámetro con el nombre del fichero solicitado</h4>");
		}
		else {
			file = new File(resultFile);
			br = new BufferedReader(new FileReader(file));

			while ((linea = br.readLine()) != null) {
				out.println(linea+"<BR>");
			}

			br.close();
		}

		out.println("<form>");
		out.println("<p><input type='hidden' name='p' value='si'>");
		out.println("<input class='home' type='submit' value='Inicio'>");
		out.println("</form>");

		CommonSINT.printFoot(out, CommonTVChecker.CREATED);

		out.println("</body></html>");
	}



	// para obtener la passwd de un alumno que está en el server.xml

	public static String getAluPasswd (String alu) throws ExcepcionSINT
	{
		ServletContext scAlu;

		scAlu = CommonTVChecker.servletContextSintProf.getContext("/"+alu);

		if (scAlu == null)
			throw new ExcepcionSINT("NOCONTEXT");

		String cpUser = scAlu.getContextPath();

		if (cpUser.equals(""))
			throw new ExcepcionSINT("NOCONTEXT");

        String passwdAlu = scAlu.getInitParameter("passwd");
        if (passwdAlu == null)
        	throw new ExcepcionSINT("NOPASSWD");

        if (passwdAlu.equals(""))
        	throw new ExcepcionSINT("NOPASSWD");

        return passwdAlu;
	}


	// para comprobar que un alumno tiene sus ficheros en regla

	public static void doOneCheckUpFiles (String alu_num,  String consulta)
			throws ExcepcionChecker
	{
	  String SINT_HOME = "/home/eetlabs.local/sint/sint";
	  String aluPublic = SINT_HOME+alu_num+"/public_html/";
	  String aluClasses = aluPublic+"webapps/WEB-INF/classes/";
	  String f ;
	  File fd;
		int veces;

	  f = aluPublic+"p2/tvml.xsd";
	  fd = new File(f);
	  if (!fd.isFile())     throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: No existe o no se puede acceder: "+f));

	  f = aluPublic+"p2/Sint"+alu_num+"P2.java";
	  fd = new File(f);
	  if (!fd.isFile())    throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: No existe o no se puede acceder: "+f));

		veces = CommonTVChecker.isInFile("history.back()", f);
	  if (veces > 0)
	      throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: El fichero "+f+" contiene la cadena prohibida 'history.back()'"));

		veces = CommonTVChecker.isInFile("localhost", f);
	  if ( veces > 0)
	      throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: El fichero "+f+" contiene la cadena prohibida 'localhost'"));



		if (consulta.equals("2")) {
			      veces = CommonTVChecker.isInFile("getC2Idiomas", f);
			      if (veces != 2)
						    throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: En el fichero "+f+", debe aparecer la cadena 'getC2Idiomas' 2 (y sólo 2) veces. Aparece "+veces));

						veces = CommonTVChecker.isInFile("getC2Infantiles", f);
			      if (veces != 2)
						    throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: En el fichero "+f+", debe aparecer la cadena 'getC2Infantiles' 2 (y sólo 2) veces. Aparece "+veces));

		        veces = CommonTVChecker.isInFile("getC2Canales", f);
				    if (veces != 2)
						    throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: En el fichero "+f+", debe aparecer la cadena 'getC2Canales' 2 (y sólo 2) veces. Aparece "+veces));

		}
	  else {
			      veces = CommonTVChecker.isInFile("getC1Fechas", f);
			      if (veces != 2)
						    throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: En el fichero "+f+", debe aparecer la cadena 'getC1Fechas' 2 (y sólo 2) veces. Aparece "+veces));

						veces = CommonTVChecker.isInFile("getC1Canales", f);
			      if (veces != 2)
						    throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: En el fichero "+f+", debe aparecer la cadena 'getC1Canales' 2 (y sólo 2) veces. Aparece "+veces));

		        veces = CommonTVChecker.isInFile("getC1Peliculas", f);
				    if (veces != 2)
						    throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: En el fichero "+f+", debe aparecer la cadena 'getC1Peliculas' 2 (y sólo 2) veces. Aparece "+veces));
		}


    veces = CommonTVChecker.isInFile("newDocumentBuilder", f);
		if (veces > 1)
			 throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: En el fichero "+f+", sólo puede aparecer una vez la cadena'newDocumentBuilder'. Aparece "+veces));

	  f = aluPublic+"webapps/p2/tvml.xsd";
	  fd = new File(f);
	  if (!fd.isFile())    throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: No existe o no se puede acceder: "+f));

    veces = CommonTVChecker.isInFile("mixed", f);
		if (veces != 1)
				throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: En el fichero "+f+", sólo puede aparecer una vez la cadena 'mixed'. Aparece "+veces));

	  f = aluClasses+"p2/Sint"+alu_num+"P2.class";
	  fd = new File(f);
	  if (!fd.isFile())    throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: No existe o no se puede acceder: "+f));

	  f = aluClasses+"Sint"+alu_num+"P2.class";
	  fd = new File(f);
	  if (fd.isFile())    throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", "doOneCheckUpFiles: El fichero "+f+" no tiene ningún objeto en esa ubicación"));

          System.out.println("doOneCheckUpFiles: "+ alu_num +" OK");

	  return;
	}


    public static int isInFile(String cad, String f) {
        String line;
        Scanner input = null;
        int veces = 0;
	File file = new File(f);

	try {
	    input = new Scanner(file);
	}
	catch (FileNotFoundException e) {System.out.println("Excepcion scanner"); return 0;}

	while(input.hasNextLine()) {
	    line = input.nextLine();
			line = line.trim();
			if (line.startsWith("//")) continue;
			if (line.startsWith("System.out.print")) continue;
	    if (line.contains(cad))  veces++;
	}

	return veces;

    }

	// para hacer una llamada y hacer log con el resultado

	public static void logCall (String call)
	{
		try {
		     String urlContents = CommonSINT.getURLContents(call);
		     CommonTVChecker.logTVChecker(urlContents);
		}
		catch (ExcepcionSINT es) {CommonTVChecker.logTVChecker(es.toString());}
	}


	// OJO SOLO HAY 2 PARAMETROS

	// pide la lista de elementos resultado sin algún parámetro (recibe los nombres y valores de los parámetros)
	// comprueba que recibe del usuario las correspondientes notificaciones de error
	// levanta ExcepcionChecker si algo va mal

	public static void checkLackParam (String url, String passwd, String VPF, String NP1, String VP1, String NP2,  String VP2)
						throws ExcepcionChecker
	{
		CheckerFailure cf;
		Document doc;
		Element el;
		String tagName, reason, call, qs, qs1, qs2;

		try {
            VP1 = URLEncoder.encode(VP1, "utf-8");
            VP2 = URLEncoder.encode(VP2, "utf-8");
         }
          catch (UnsupportedEncodingException ex) {
                 cf = new CheckerFailure("", "", "utf-8 no soportado");
		throw new ExcepcionChecker(cf);
            }


		// no hace falta URL-encoded en ninguna
		qs = "?auto=si&"+CommonSINT.PFASE+"="+VPF+"&p="+passwd;     // falta NP1, NP2
		qs1 = qs+"&"+NP2+"="+VP2;     // falta NP1
		qs2 = qs+"&"+NP1+"="+VP1;   // falta NP2


		// probando qs1, donde falta NP1

		call = url+qs1;

		CommonTVChecker.errorHandler.clear();

		try {
			doc = CommonTVChecker.db.parse(call);
		}
		catch (SAXException e) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo("checkLackParam: SAXException al solicitar y parsear la lista de resultados sin '"+NP1+"'");
			throw new ExcepcionChecker(cf);
		}
		catch (Exception e) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo("checkLackParam: Exception al solicitar y parsear la lista de resultados sin '"+NP1+"'");
			throw new ExcepcionChecker(cf);
		}

		if (CommonTVChecker.errorHandler.hasErrors()) {
			CommonTVChecker.logCall(call);

			ArrayList<String> errors = CommonTVChecker.errorHandler.getErrors();
			String msg="";

			for (int x=0; x < errors.size(); x++)
				if (x == (errors.size()-1)) msg += "++++"+errors.get(x);
				else msg += "++++"+errors.get(x)+"<br>\n";


			cf = new CheckerFailure(call, "", msg);
			cf.addMotivo("checkLackParam: La respuesta (al solicitar y parsear la lista de resultados sin '"+NP1+"') es inválida, tiene errors");
			throw new ExcepcionChecker(cf);
		}

		if (CommonTVChecker.errorHandler.hasFatalerrors()) {
			CommonTVChecker.logCall(call);

			ArrayList<String> fatalerrors = CommonTVChecker.errorHandler.getFatalerrors();
			String msg="";

			for (int x=0; x < fatalerrors.size(); x++)
				if (x == (fatalerrors.size()-1)) msg += "++++"+fatalerrors.get(x);
				else msg += "++++"+fatalerrors.get(x)+"<br>\n";


			cf = new CheckerFailure(call, "", msg);
			cf.addMotivo("checkLackParam: La respuesta (al solicitar y parsear la lista de resultados sin '"+NP1+"') es inválida, tiene fatal errors");
			throw new ExcepcionChecker(cf);
		}

		if (doc == null) {
			cf = new CheckerFailure(call, "", "checkLackParam: Se recibe 'null' al solicitar la lista de resultados sin '"+NP1+"'");
			throw new ExcepcionChecker(cf);
		}

		el = doc.getDocumentElement();
		tagName = el.getTagName();
		if (tagName.equals("wrongRequest")) {
			reason = el.getTextContent();
			if (!reason.equals("no param:"+NP1)) {
				cf = new CheckerFailure(call, "", "checkLackParam: probando llamada sin '"+NP1+"'. Se recibe &lt;wrongRequest> pero con '"+reason+"'");
				throw new ExcepcionChecker(cf);
			}
		}
		else {
			cf = new CheckerFailure(call, "", "checkLackParam: No se recibe &lt;wrongRequest> al solicitar la lista de resultados sin '"+NP1+"'");
			throw new ExcepcionChecker(cf);
		}



		// probando qs2, donde falta NP2

		call = url+qs2;

		CommonTVChecker.errorHandler.clear();

		try {
			doc = CommonTVChecker.db.parse(call);
		}
		catch (SAXException e) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo("checkLackParam: SAXException al solicitar y parsear la lista de resultados sin '"+NP2+"'");
			throw new ExcepcionChecker(cf);
		}
		catch (Exception e) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo("checkLackParam: Exception al solicitar y parsear la lista de resultados sin '"+NP2+"'");
			throw new ExcepcionChecker(cf);
		}

		if (CommonTVChecker.errorHandler.hasErrors()) {
			CommonTVChecker.logCall(call);

			ArrayList<String> errors = CommonTVChecker.errorHandler.getErrors();
			String msg="";

			for (int x=0; x < errors.size(); x++)
				if (x == (errors.size()-1)) msg += "++++"+errors.get(x);
				else msg += "++++"+errors.get(x)+"<br>\n";

			cf = new CheckerFailure(call, "", msg);
			cf.addMotivo("checkLackParam: La respuesta (al solicitar y parsear la lista de resultados sin '"+NP2+"') es inválida, tiene errors");
			throw new ExcepcionChecker(cf);
		}

		if (CommonTVChecker.errorHandler.hasFatalerrors()) {
			CommonTVChecker.logCall(call);

			ArrayList<String> fatalerrors = CommonTVChecker.errorHandler.getFatalerrors();
			String msg="";

			for (int x=0; x < fatalerrors.size(); x++)
				if (x == (fatalerrors.size()-1)) msg += "++++"+fatalerrors.get(x);
				else msg += "++++"+fatalerrors.get(x)+"<br>\n";

			cf = new CheckerFailure(call, "", msg);
			cf.addMotivo("checkLackParam: La respuesta (al solicitar y parsear la lista de resultados sin '"+NP2+"') es inválida, tiene fatal errors");
			throw new ExcepcionChecker(cf);
		}

		if (doc == null) {
			cf = new CheckerFailure(call, "", "checkLackParam: Se recibe 'null' al solicitar la lista de resultados sin '"+NP2+"'");
			throw new ExcepcionChecker(cf);
		}

		el = doc.getDocumentElement();
		tagName = el.getTagName();
		if (tagName.equals("wrongRequest")) {
			reason = el.getTextContent();
			if (!reason.equals("no param:"+NP2)) {
				cf = new CheckerFailure(call, "", "checkLackParam: probando llamada sin '"+NP2+"'. Se recibe &lt;wrongRequest> pero con '"+reason+"'");
				throw new ExcepcionChecker(cf);
			}
		}
		else {
			cf = new CheckerFailure(call, "", "checkLackParam: No se recibe &lt;wrongRequest> al solicitar la lista de resultados sin '"+NP2+"'");
			throw new ExcepcionChecker(cf);
		}


		// todo bien
		return;
	}



}
