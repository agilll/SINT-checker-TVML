/****************************************************************
 *    SERVICIOS DE INTERNET
 *    EE TELECOMUNICACIÓN
 *    UNIVERSIDAD DE VIGO
 *
 *    Checker de la práctica de EAML
 *
 *    Autor: Alberto Gil Solla
 *    Curso : 2020-2021
 ****************************************************************/

// en esta clase van variables y métodos usados por toda la aplicación

package docencia.sint.EAML.checker;

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
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.SAXException;

import docencia.sint.Common.Msgs;
import docencia.sint.Common.CheckerFailure;
import docencia.sint.Common.CommonSINT;
import docencia.sint.Common.ErrorHandlerSINT;
import docencia.sint.Common.ExcepcionChecker;
import docencia.sint.EAML.CommonEAML;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


class CommonEAChecker {

	public static ServletContext servletContextSintProf;

	public static int esProfesor=0;

	public static final String SERVICE_NAME = "/P2EA";
	public static final String PROF_CONTEXT  = "/sintprof";

	public static String server_port;
	public static String servicioProf;

	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	static final String SCHEMA_EAMLAUTO = "_rules/eamlAuto.xsd";
  static final String CSS_FILE = "css/eamlChecker.css";

	public static DocumentBuilderFactory dbf;
	public static DocumentBuilder db;
	public static ErrorHandlerSINT errorHandler;

	private static Logger logger = null;  // el objeto Logger

	// para inicializar el objeto Logger

	public static void initLoggerEAChecker (Class c) {
		logger = LogManager.getLogger(c);
	}

	 // para imprimir con el Logger en el sintprof.log

	 public static void logEAChecker (String msg) {
		logger.info("## EAML Checker ## "+msg);
	 }



	public static void printHead(PrintWriter out, String lang)
	{
		out.println("<head><meta charset='utf-8'/>");
		out.println("<title>"+MsgsEAMLChecker.getMsg("0", lang)+"</title>");  // 0 = "Corrector de EAML"
		out.println("<link rel='stylesheet'  type='text/css' href='"+CommonEAChecker.CSS_FILE+"'/></head>");
	}


	public static void printBodyHeader(PrintWriter out, String lang)
	{
		out.println("<body>");
		out.println("<div id='asignatura'>"+Msgs.getMsg(Msgs.CC00,lang)+"</div><br>");  // CC00 = Servicios de Internet
		out.println("<div id='grado'>"+Msgs.getMsg(Msgs.CC01,lang)+"</div><br>");  // CC01 = EE Telecomunicación
		// 3 = "Comprobación de servicios sobre EAML"        "4" = CURSO xx-yy
		out.println("<div id='servicio'>"+MsgsEAMLChecker.getMsg("3",lang)+"<br>"+MsgsEAMLChecker.getMsg("4",lang)+"<hr></div>");
	}



	// para crear el DocumentBuilder, se invoca desde el init del servlet

	public static void createDocumentBuilder() throws  UnavailableException
	{
	    dbf = DocumentBuilderFactory.newInstance();
	    dbf.setValidating(true);
	    dbf.setNamespaceAware(true);
	    dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

	    String pathSchema = CommonEAChecker.servletContextSintProf.getRealPath(SCHEMA_EAMLAUTO);
	    File fileSchema = new File(pathSchema);

	    dbf.setAttribute(JAXP_SCHEMA_SOURCE, fileSchema);    // se valida con el schema del modo auto

	    try {
		    db = dbf.newDocumentBuilder();
	    }
	    catch  (ParserConfigurationException e) {
		    throw new UnavailableException("Error creando el analizador de respuestas del modo auto: "+e);
	    }

	    errorHandler = new ErrorHandlerSINT();
	    db.setErrorHandler(CommonEAChecker.errorHandler);
	}




	// para comprobar las listas de errores de profesor y alumno (sólo se compara los nombres de los ficheros, no la explicación del error)
	// no devuelve nada si son iguales
	// levanta una excepcion ExcepcionChecker en caso contrario

	public static void comparaErrores (String usuario, String servicioAluP, String passwdAlu, String lang)
			throws ExcepcionChecker
	{
		CheckerFailure cf;
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();

		// pedimos la lista de errores de sintprof

		Element pErrores;
		try {
			pErrores = requestErrores("sintprof", CommonEAChecker.servicioProf, CommonSINT.PPWD, lang);
		}
		catch (ExcepcionChecker ex) {
			cf = ex.getCheckerFailure();
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+Msgs.getMsg(Msgs.CC44,lang)+"sintprof");  // CC44 = Solicitando el informe de errores de
			throw new ExcepcionChecker(cf);
		}


		// pedimos la lista de errores del sintX

		Element xErrores;
		try {
			xErrores = requestErrores(usuario, servicioAluP, passwdAlu, lang);
		}
		catch (ExcepcionChecker ex) {
			cf = ex.getCheckerFailure();
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+Msgs.getMsg(Msgs.CC44,lang)+usuario); // CC44 = Solicitando el informe de errores de
			throw new ExcepcionChecker(cf);
		}


		ArrayList<String> pList = new ArrayList<String>();  // para crear la lista de ficheros del profesor
		ArrayList<String> xList = new ArrayList<String>();  // para crear la lista de ficheros del alumno



		// vamos a comparar los ficheros que dan WARNING

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
			cf = new CheckerFailure("", "", currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC45,lang), pList.size(), xList.size())); // CC45 = Deberían haberse recibido x warnings, pero se reciben y
			throw new ExcepcionChecker(cf);
		}

		// comprobamos que todos los ficheros del alumno están en la lista del profesor

		for (int x=0; x < xList.size(); x++)
			if (!pList.contains(xList.get(x))) {
				cf = new CheckerFailure("", "", currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC46,lang), x, xList.get(x)));  // CC46 = el warning x no es correcto
				throw new ExcepcionChecker(cf);
			}



		// vamos a comparar los ficheros que dan ERROR

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

			cf = new CheckerFailure("", "", currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC47,lang), errorFalta)); // CC47 = Falta un 'error': x
			throw new ExcepcionChecker(cf);
		}

		String errorSobra = "";

		if (pList.size() < xList.size()) {

		    for (int x=0; x < xList.size(); x++)
		    	if (!pList.contains(xList.get(x)))
		    		errorSobra = xList.get(x);

			cf = new CheckerFailure("", "", currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC48,lang), errorSobra)); // CC48 = Se recibe un 'error' incorrecto: x
			throw new ExcepcionChecker(cf);
		}

		// comprobamos que todos los ficheros del alumno están en la lista del profesor

		for (int x=0; x < xList.size(); x++)
			if (!pList.contains(xList.get(x))) {
				cf = new CheckerFailure("", "", currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC48,lang), xList.get(x))); // CC48 = Se recibe un 'error' incorrecto: x
				throw new ExcepcionChecker(cf);
			}


		// vamos a comparar los ficheros que dan FATAL ERROR

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

			cf = new CheckerFailure("", "", currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC49,lang), pList.size(), xList.size())); // CC49 = Deberían haberse recibido x fatal errors, pero se reciben y
			throw new ExcepcionChecker(cf);
		}

		// comprobamos que todos los ficheros del alumno están en la lista del profesor

		for (int x=0; x < xList.size(); x++)
			if (!pList.contains(xList.get(x))) {
				cf = new CheckerFailure("", "", currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC50,lang), x, xList.get(x)));  // CC46 = el fatal error x no es correcto. File: y
				throw new ExcepcionChecker(cf);
			}

		return;  // todo fue bien, los errores son los mismos
	}




	// pide y devuelve la lista de errores detectados de un usuario

	 public static Element requestErrores (String usuario, String url, String passwd, String lang)
			 		throws ExcepcionChecker
	 {
		Document doc;
		String qs, call;
		CheckerFailure cf;
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();

		qs = "?"+CommonSINT.PFASE+"=02&auto=true&p="+passwd;
		call = url+qs;

		errorHandler.clear();

		try {
			doc = db.parse(call);
		}
		catch (SAXException ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString().replace("<", "&lt;"));
			cf.addMotivo(currentMethod+": SAXException: "+Msgs.getMsg(Msgs.CC51,lang)); // CC51 = Error al solicitar/parsear la lista de ficheros erróneos"
			throw new ExcepcionChecker(cf);
		}
		catch (Exception ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo(currentMethod+": Exception: "+Msgs.getMsg(Msgs.CC51,lang)); // CC51 = Error al solicitar/parsear la lista de ficheros erróneos"
			throw new ExcepcionChecker(cf);
		}


		if (errorHandler.hasErrors()) {
			CommonEAChecker.logCall(call);

			ArrayList<String> errors = errorHandler.getErrors();
			String msg="";

			for (int x=0; x < errors.size(); x++)
				if (x == (errors.size()-1)) msg += "++++"+errors.get(x);
				else msg += "++++"+errors.get(x)+"<br>\n";


			cf = new CheckerFailure(call, "", msg);
			cf.addMotivo(currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC52,lang), "<errors>"));  // CC52 Resultado inválido, <errors> al parsear la lista de ficheros erróneos
			throw new ExcepcionChecker(cf);
		}

		if (errorHandler.hasFatalerrors()) {
			CommonEAChecker.logCall(call);

			ArrayList<String> fatalerrors = errorHandler.getFatalerrors();
			String msg="";

			for (int x=0; x < fatalerrors.size(); x++)
				if (x == (fatalerrors.size()-1)) msg += "++++"+fatalerrors.get(x);
				else msg += "++++"+fatalerrors.get(x)+"<br>\n";


			cf = new CheckerFailure(call, "", msg);
			cf.addMotivo(currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC52,lang), "<fatalerrors>"));  // CC52 Resultado inválido, <fatalerrors> al parsear la lista de ficheros erróneos
			throw new ExcepcionChecker(cf);
		}


		if (doc == null) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", currentMethod+": "+Msgs.getMsg(Msgs.CC53,lang));    // CC53 = El parser devuelve 'null' al parsear la lista de ficheros erróneos
			throw new ExcepcionChecker(cf);
		}

		Element e = doc.getDocumentElement();
		String tagName = e.getTagName();

		if (tagName.equals("wrongRequest")) {
			String reason = e.getTextContent();

			cf = new CheckerFailure(call, "", currentMethod+": "+Msgs.getMsg(Msgs.CC54,lang)+reason); // CC54 = Al solicitar la lista de ficheros erróneos se recibe <wrongRequest>reason
			throw new ExcepcionChecker(cf);
		}

		if (!tagName.equals("wrongDocs")) {
			CommonEAChecker.logEAChecker("Elemento resultado = "+tagName);

			cf = new CheckerFailure(call, "", currentMethod+": "+Msgs.getMsg(Msgs.CC55,lang)+"(<"+tagName+">)"); // CC55 = Al solicitar la lista de ficheros erróneos se recibe una respuesta incorrecta:
			throw new ExcepcionChecker(cf);
		}

		NodeList nlErrores = doc.getElementsByTagName("wrongDocs");
		Element elemErrores = (Element)nlErrores.item(0);

		return elemErrores;
	}





	// método para comprobar si un servicio está operativo, pidiendo el estado
	// no devuelve nada si está operativo,
	// levanta una excepción  ExcepcionChecker si falla algo

	public static void doOneCheckUpStatus (HttpServletRequest request, String user, String passwd, String lang)
	throws ExcepcionChecker
	{
		CheckerFailure cf;
		Document doc;
		Element e;
		String qs;
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();

		ServletContext scPropio, scUser;
		scPropio = request.getServletContext();  // contexto del que ejecuta el checker (sintprof)
		scUser = scPropio.getContext("/"+user);  // contexto del User que vamos a comprobar

		CommonEAChecker.logEAChecker("Vamos a comprobar el estado de "+user);

		if (scUser == null)  {
			CommonEAChecker.logEAChecker("01_NOCONTEXT: null "+user);
			throw new ExcepcionChecker(new CheckerFailure("", "01_NOCONTEXT", currentMethod+": No existe el contexto "+user));
		}

		String cpUser = scUser.getContextPath();        // context path del User que vamos a comprobar

		if (cpUser.equals("")) {
			CommonEAChecker.logEAChecker("01_NOCONTEXT:"+cpUser+"**"+user);
			throw new ExcepcionChecker(new CheckerFailure("", "01_NOCONTEXT", currentMethod+": No existe el contexto "+user));
		}


		// vamos a pedir el estado sin passwd, para comprobar que responde con error

		String url = "http://"+server_port+cpUser+CommonEAChecker.SERVICE_NAME;
		String call;

		errorHandler.clear();

		qs = "?auto=true";
		call = url+qs;

		try {
			doc = db.parse(call);  // petición del estado sin passwd
		}
		catch (ConnectException ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			cf = new CheckerFailure(call, "00_DOWN", ex.toString());
			cf.addMotivo(currentMethod+": (ConnectException): "+Msgs.getMsg(Msgs.CC23,lang)); // CC23 = el servidor no responde
			throw new ExcepcionChecker(cf);    // No debería pasar, ya que es el mismo servidor que está ejecutando el checker
		}
		catch (FileNotFoundException ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			cf = new CheckerFailure(call, "02_FILENOTFOUND", ex.toString());
			cf.addMotivo(currentMethod+": (FileNotFoundException):"+Msgs.getMsg(Msgs.CC24,lang)); // CC24 = el servlet no está declarado
			throw new ExcepcionChecker(cf);   // el servlet no está declarado en el web.xml de ese usuario
		}
		catch (com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			cf = new CheckerFailure(call, "03_ENCODING", ex.toString());
			cf.addMotivo(currentMethod+": (MalformedByteSequenceException): "+Msgs.getMsg(Msgs.CC25,lang));  // CC25 = La codificación de caracteres recibida es incorrecta (no UTF-8)
			throw new ExcepcionChecker(cf);
		}
		catch (IOException ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			cf = new CheckerFailure(call, "04_IOEXCEPTION", ex.toString());
			cf.addMotivo(currentMethod+": (IOException): "+Msgs.getMsg(Msgs.CC26,lang)); // CC26 = No se ha encontrado la clase del servlet
			throw new ExcepcionChecker(cf);
		}
		catch (SAXException ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "05_BF", ex.toString());
			cf.addMotivo(currentMethod+": (SAXException): "+Msgs.getMsg(Msgs.CC27,lang));  // CC27 = La respuesta al pedir el estado sin passwd está mal construida
			throw new ExcepcionChecker(cf);
		}
		catch (Exception ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call,"07_ERRORUNKNOWN", ex.toString());
			cf.addMotivo(currentMethod+": (Exception): "+Msgs.getMsg(Msgs.CC28,lang));  // CC28 = Error desconocido al realizar la solicitud de estado sin passwd
			throw new ExcepcionChecker(cf);
		}

		if (errorHandler.hasErrors()) {
			CommonEAChecker.logCall(call);

			ArrayList<String> errors = errorHandler.getErrors();
			String msg="";

			for (int x=0; x < errors.size(); x++)
				if (x == (errors.size()-1)) msg += "++++"+errors.get(x);
				else msg += "++++"+errors.get(x)+"<br>\n";

			CommonEAChecker.logEAChecker(msg);

			cf = new CheckerFailure(call,"06_INVALID", msg);
			cf.addMotivo(currentMethod+": "+Msgs.getMsg(Msgs.CC29,lang)+"errors");  // CC29 = La respuesta al pedir el estado sin passwd es inválida, tiene
			throw new ExcepcionChecker(cf);
		}

		if (errorHandler.hasFatalerrors()) {
			CommonEAChecker.logCall(call);

			ArrayList<String> fatalerrors = errorHandler.getFatalerrors();
			String msg="";

			for (int x=0; x < fatalerrors.size(); x++)
				if (x == (fatalerrors.size()-1)) msg += "++++"+fatalerrors.get(x);
				else msg += "++++"+fatalerrors.get(x)+"<br>\n";

			CommonEAChecker.logEAChecker(msg);
			cf = new CheckerFailure(call,"06_INVALID", msg);
			cf.addMotivo(currentMethod+": "+Msgs.getMsg(Msgs.CC29,lang)+"fatal errors");  // CC29 = La respuesta al pedir el estado sin passwd es inválida, tiene
			throw new ExcepcionChecker(cf);
		}

		e = doc.getDocumentElement();
		String tagName = e.getTagName();

		if (tagName.equals("service"))
			throw new ExcepcionChecker(new CheckerFailure(call, "08_OKNOPASSWD", currentMethod+": "+Msgs.getMsg(Msgs.CC30,lang)));   // CC30 = No ha requerido passwd

		if (tagName.equals("wrongRequest")) {
			String reason = e.getTextContent().trim().toLowerCase();
			if (!reason.equals("no passwd"))
				throw new ExcepcionChecker(new CheckerFailure(call, "10_BADANSWER", currentMethod+": "+Msgs.getMsg(Msgs.CC31,lang)+reason));  // CC31 = Responde con &lt;wrongRequest> pero no por 'no passwd', sino por:
		}
		else throw new ExcepcionChecker(new CheckerFailure(call, "10_BADANSWER", currentMethod+": "+Msgs.getMsg(Msgs.CC32,lang)+"&lt;"+tagName+">"));   // CC32 = No ha contestado con &lt;wrongRequest> al no enviar passwd, sino con


		// si ha llegado hasta aquí es que respondió correctamente con <wrongRequest>no passwd</wrongRequest>


		// vamos ahora a pedir el estado incluyendo la passwd

		errorHandler.clear();

		qs = "?auto=true&p="+passwd;
		call = url+qs;

		try {
			doc = db.parse(call);  // petición del estado
		}
		catch (ConnectException ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			cf = new CheckerFailure(call, "00_DOWN", ex.toString());
			cf.addMotivo(currentMethod+": (ConnectException): "+Msgs.getMsg(Msgs.CC23,lang)); // CC23 = el servidor no responde
			throw new ExcepcionChecker(cf);    // No debería pasar, ya que es el mismo servidor que está ejecutando el checker
		}
		catch (FileNotFoundException ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			cf = new CheckerFailure(call, "02_FILENOTFOUND", ex.toString());
			cf.addMotivo(currentMethod+": (FileNotFoundException):"+Msgs.getMsg(Msgs.CC24,lang)); // CC24 = el servlet no está declarado
			throw new ExcepcionChecker(cf);   // el servlet no está declarado en el web.xml de ese usuario
		}
		catch (com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			cf = new CheckerFailure(call, "03_ENCODING", ex.toString());
			cf.addMotivo(currentMethod+": (MalformedByteSequenceException): "+Msgs.getMsg(Msgs.CC25,lang));  // CC25 = La codificación de caracteres recibida es incorrecta (no UTF-8)
			throw new ExcepcionChecker(cf);
		}
		catch (IOException ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			cf = new CheckerFailure(call, "04_IOEXCEPTION", ex.toString());
			cf.addMotivo(currentMethod+": (IOException): "+Msgs.getMsg(Msgs.CC26,lang)); // CC26 = No se ha encontrado la clase del servlet
			throw new ExcepcionChecker(cf);
		}
		catch (SAXException ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "05_BF", ex.toString());
			cf.addMotivo(currentMethod+": (SAXException): "+Msgs.getMsg(Msgs.CC33,lang));  // CC33 = La respuesta al pedir el estado con passwd está mal construida
			throw new ExcepcionChecker(cf);
		}
		catch (Exception ex) {
			CommonEAChecker.logEAChecker(ex.toString());
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call,"07_ERRORUNKNOWN", ex.toString());
			cf.addMotivo(currentMethod+": (Exception): "+Msgs.getMsg(Msgs.CC34,lang));  // CC34 = Error desconocido al realizar la solicitud de estado con passwd
			throw new ExcepcionChecker(cf);
		}


		if (errorHandler.hasErrors()) {
			CommonEAChecker.logCall(call);

			ArrayList<String> errors = errorHandler.getErrors();
			String msg="";

			for (int x=0; x < errors.size(); x++)
				if (x == (errors.size()-1)) msg += "++++"+errors.get(x);
				else msg += "++++"+errors.get(x)+"<br>\n";

			CommonEAChecker.logEAChecker(msg);
			cf = new CheckerFailure(call,"06_INVALID", msg);
			cf.addMotivo(currentMethod+": "+Msgs.getMsg(Msgs.CC35,lang)+"errors");  // CC35 = La respuesta al pedir el estado con passwd es inválida, tiene
			throw new ExcepcionChecker(cf);
		}

		if (errorHandler.hasFatalerrors()) {
			CommonEAChecker.logCall(call);

			ArrayList<String> fatalerrors = errorHandler.getFatalerrors();
			String msg="";

			for (int x=0; x < fatalerrors.size(); x++)
				if (x == (fatalerrors.size()-1)) msg += "++++"+fatalerrors.get(x);
				else msg += "++++"+fatalerrors.get(x)+"<br>\n";

			CommonEAChecker.logEAChecker(msg);
			cf = new CheckerFailure(call,"06_INVALID", msg);
			cf.addMotivo(currentMethod+": "+Msgs.getMsg(Msgs.CC35,lang)+"fatal errors");  // CC35 = La respuesta al pedir el estado con passwd es inválida, tiene
			throw new ExcepcionChecker(cf);
		}


		e = doc.getDocumentElement();
		tagName = e.getTagName();

		if (tagName.equals("wrongRequest")) {
			String reason = e.getTextContent().trim();
			if (reason.equals("bad passwd")) {
				throw new ExcepcionChecker(new CheckerFailure(call, "09_BADPASSWD", currentMethod+": "+Msgs.getMsg(Msgs.CC36,lang))); // CC36 = el servicio dice que la passwd enviada es incorrecta"
			}
			else throw new ExcepcionChecker(new CheckerFailure(call, "10_BADANSWER", currentMethod+": "+Msgs.getMsg(Msgs.CC37,lang)+reason)); //  CC37 = al pedir el estado, el servicio contesta con &lt;wrongRequest> con razón desconocida
		}

		if (!tagName.equals("service"))
			throw new ExcepcionChecker(new CheckerFailure(call, "10_BADANSWER", currentMethod+": "+Msgs.getMsg(Msgs.CC38,lang)+"(&lt;"+tagName+">)"));   //  al pedir el estado, el servicio contesta con un tag incorrecto

		System.out.println(currentMethod+": "+user+" OK");
		return;
	}






	// Envío del fichero de un resultado negativo
	// el nombre del fichero se recibe en el parámetro 'file'
	// es sólo para el profesor, no e snecesario traducirlo

	public static void doGetRequestResultFile(HttpServletRequest request, HttpServletResponse response, String lang)
						throws IOException, ServletException
	{
		File file;
		BufferedReader br;
		String linea;

		PrintWriter out = response.getWriter();

		out.println("<html>");
		CommonEAChecker.printHead(out, lang);
		CommonEAChecker.printBodyHeader(out, lang);

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

		CommonSINT.printFoot(out, MsgsEAMLChecker.CREATED);

		out.println("</body></html>");
	}



	// para obtener la passwd de un alumno que está en el server.xml

	public static String getAluPasswd (String alu) throws ExcepcionChecker
	{
		ServletContext scAlu;

		scAlu = CommonEAChecker.servletContextSintProf.getContext("/"+alu);

		if (scAlu == null) {
			CheckerFailure cf = new CheckerFailure("", "NOCONTEXT", "");
			throw new ExcepcionChecker(cf);
		}

		String cpUser = scAlu.getContextPath();

		if (cpUser.equals("")) {
			CheckerFailure cf = new CheckerFailure("", "NOCONTEXT", "");
			throw new ExcepcionChecker(cf);
		}

        String passwdAlu = scAlu.getInitParameter("passwd");
        if (passwdAlu == null) {
			    CheckerFailure cf = new CheckerFailure("", "NOPASSWD", "");
			    throw new ExcepcionChecker(cf);
				}

        if (passwdAlu.equals("")) {
					CheckerFailure cf = new CheckerFailure("", "NOPASSWD", "");
			    throw new ExcepcionChecker(cf);
				}

        return passwdAlu;
	}


	// para comprobar que un alumno tiene sus ficheros en regla

	public static void doOneCheckUpFiles (String alu_num,  String consulta, String lang)
			throws ExcepcionChecker
	{
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();
	  String SINT_HOME = "/home/eetlabs.local/sint/sint";
	  String aluPublic = SINT_HOME+alu_num+"/public_html/";
	  String aluClasses = aluPublic+"webapps/WEB-INF/classes/";
	  String f ;
	  File fd;
		int veces, veces_call1, veces_call2, veces_call3;
		String call1, call2, call3;
		int veces_Builder=0;

		if (consulta.equals("1")) {
			call1 = "getC1Degrees";
			call2 = "getC1Subjects";
			call3 = "getC1Students";
		}
		else {
			call1 = "getC2Degrees";
			call2 = "getC2Students";
			call3 = "getC2Subjects";
		}

		SearchInFile sif = new SearchInFile();

		// comprobamos que eaml.xsd esté en public_html/p2
	  f = aluPublic+"p2/eaml.xsd";
	  fd = new File(f);
	  if (!fd.isFile())
				// CC15 = "No existe o no se puede acceder al fichero "
				throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+Msgs.getMsg(Msgs.CC15,lang)+f));

		// comprobamos que eaml.xsd esté en public_html/p2
			f = aluPublic+"p2/FrontEnd.java";
			fd = new File(f);
			if (!fd.isFile())
				// CC15 = "No existe o no se puede acceder al fichero "
				throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+Msgs.getMsg(Msgs.CC15,lang)+f));

		// comprobamos que DataModel.java esté en public_html/p2
		f = aluPublic+"p2/DataModel.java";
		fd = new File(f);
		if (!fd.isFile())
				// CC15 = "No existe o no se puede acceder al fichero "
				throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+Msgs.getMsg(Msgs.CC15,lang)+f));

		// comprobamos que DataModel.java contiene las llamadas getC*
		veces = sif.isInFile(call1, fd);
		if (veces != 1)
			// CC20 = "debe aparecer una vez, y sólo una, en el fichero "
			throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+call1+" "+Msgs.getMsg(Msgs.CC20,lang)+f));

		veces = sif.isInFile(call2, fd);
		if (veces != 1)
			// CC20 = "debe aparecer una vez, y sólo una, en el fichero "
			throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+call2+" "+Msgs.getMsg(Msgs.CC20,lang)+f));

		veces = sif.isInFile(call3, fd);
		if (veces != 1)
			// CC20 = "debe aparecer una vez, y sólo una, en el fichero "
			throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+call3+" "+Msgs.getMsg(Msgs.CC20,lang)+f));


		// revisamos todos los ficheros java de public_html/p2

		File carpeta = new File(aluPublic+"p2");
		veces_Builder = veces_call1 = veces_call2 = veces_call3 = 0;

		for (File ficheroEntrada : carpeta.listFiles()) {
			if (ficheroEntrada.isFile()) {
					String fileName = ficheroEntrada.getName();

					if (fileName.endsWith(".java")) {
								 veces_Builder += sif.isInFile("newDocumentBuilder", ficheroEntrada);
								 veces_call1 += sif.isInFile(call1, ficheroEntrada);
								 veces_call2 += sif.isInFile(call2, ficheroEntrada);
								 veces_call3 += sif.isInFile(call3, ficheroEntrada);
					}
				}
    }

		if (veces_Builder != 1)
				// CC21 = "debe aparecer una vez, y sólo una, en el código, pero aparece "
			 	throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": newDocumentBuilder "+Msgs.getMsg(Msgs.CC21,lang)+veces_Builder));

		if (veces_call1 != 2)
			 // CC22 = "debe aparecer dos veces, y sólo dos, en el código, pero aparece "
			 throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+call1+" "+String.format(Msgs.getMsg(Msgs.CC22,lang), call1)+veces_call1));

		if (veces_call2 != 2)
			 // CC22 = "debe aparecer dos veces, y sólo dos, en el código, pero aparece "
			 throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+call2+" "+String.format(Msgs.getMsg(Msgs.CC22,lang), call2)+veces_call2));

		if (veces_call3 != 2)
			 // CC22 = "debe aparecer dos veces, y sólo dos, en el código, pero aparece "
			 throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+call3+" "+String.format(Msgs.getMsg(Msgs.CC22,lang), call3)+veces_call3));


		// revisamos el fichero public_html/p2/SintXP2.java, que debe estar en public_html/p2
	  f = aluPublic+"p2/Sint"+alu_num+"P2.java";
	  fd = new File(f);
	  if (!fd.isFile())
				throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+Msgs.getMsg(Msgs.CC15,lang)+f));  // CC15 = "No existe o no se puede acceder al fichero "


		// revisamos el fichero  webapps/p2/eaml.xsd
	  f = aluPublic+"webapps/p2/eaml.xsd";
	  fd = new File(f);
	  if (!fd.isFile())
				throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+Msgs.getMsg(Msgs.CC15,lang)+f));  // CC15 = "No existe o no se puede acceder al fichero "

    veces = sif.isInFile("mixed", fd);
		if (veces != 1)
				// CC20 = "debe aparecer sólo una vez en el fichero "
				throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": mixed "+Msgs.getMsg(Msgs.CC20,lang)+f));

		// comprobamos que SintXP2.class sólo esté dentro de classes/p2
	  f = aluClasses+"p2/Sint"+alu_num+"P2.class";
	  fd = new File(f);
	  if (!fd.isFile())
				throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+Msgs.getMsg(Msgs.CC15,lang)+f));  // CC15 = "No existe o no se puede acceder al fichero "

	  f = aluClasses+"Sint"+alu_num+"P2.class";
	  fd = new File(f);
	  if (fd.isFile())
				throw new ExcepcionChecker(new CheckerFailure("", "12_FILES", currentMethod+": "+Msgs.getMsg(Msgs.CC19,lang)+f));  // CC19 = "Fichero prohibido en esa ubicación: "

	  return;
	}


	// para hacer una llamada y hacer log con el resultado

	public static void logCall (String call)
	{
		try {
		     String urlContents = CommonSINT.getURLContents(call);
		     CommonEAChecker.logEAChecker(urlContents);
		}
		catch (ExcepcionChecker ex) {
			String motivos = ex.getCheckerFailure().toString();
			CommonEAChecker.logEAChecker(motivos);
		}
	}


	// OJO SOLO HAY 2 PARAMETROS

	// pide la lista de elementos resultado sin algún parámetro (recibe los nombres y valores de los parámetros)
	// comprueba que recibe del usuario las correspondientes notificaciones de error
	// levanta ExcepcionChecker si algo va mal

	public static void checkLackParam (String url, String passwd, String VPF, String NP1, String VP1, String NP2,  String VP2, String lang)
						throws ExcepcionChecker
	{
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();
		CheckerFailure cf;
		Document doc;
		Element el;
		String tagName, reason, call, qs, qs1, qs2;

		try {
            VP1 = URLEncoder.encode(VP1, "utf-8");
         }
    catch (UnsupportedEncodingException ex) {
        cf = new CheckerFailure(url, "", currentMethod+": "+Msgs.getMsg(Msgs.CC58,lang)+VP1);  // CC58 = "utf-8 no soportado para parametro"
		    throw new ExcepcionChecker(cf);
    }

		try {
            VP2 = URLEncoder.encode(VP2, "utf-8");
         }
    catch (UnsupportedEncodingException ex) {
        cf = new CheckerFailure(url, "", currentMethod+": "+Msgs.getMsg(Msgs.CC58,lang)+VP2);  // CC58 = "utf-8 no soportado para parametro"
		    throw new ExcepcionChecker(cf);
    }

		qs = "?auto=true&"+CommonSINT.PFASE+"="+VPF+"&p="+passwd;     // falta NP1, NP2
		qs1 = qs+"&"+NP2+"="+VP2;     // falta NP1
		qs2 = qs+"&"+NP1+"="+VP1;   // falta NP2


		// probando qs1, donde falta NP1

		call = url+qs1;

		CommonEAChecker.errorHandler.clear();

		try {
			doc = CommonEAChecker.db.parse(call);
		}
		catch (SAXException e) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo(currentMethod+": SAXException: "+Msgs.getMsg(Msgs.CC59,lang)+"'"+NP1+"'"); // CC59 = al solicitar y parsear la lista de resultados sin el parámetro obligatorio x
			throw new ExcepcionChecker(cf);
		}
		catch (Exception e) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo(currentMethod+": Exception: "+Msgs.getMsg(Msgs.CC59,lang)+"'"+NP1+"'");  // CC59 = al solicitar y parsear la lista de resultados sin el parámetro obligatorio x
			throw new ExcepcionChecker(cf);
		}

		if (CommonEAChecker.errorHandler.hasErrors()) {
			CommonEAChecker.logCall(call);

			ArrayList<String> errors = CommonEAChecker.errorHandler.getErrors();
			String msg="";

			for (int x=0; x < errors.size(); x++)
				if (x == (errors.size()-1)) msg += "++++"+errors.get(x);
				else msg += "++++"+errors.get(x)+"<br>\n";


			cf = new CheckerFailure(call, "", msg);
			cf.addMotivo(currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC60,lang), "errors")+"'"+NP1+"'");  // CC60 = Respuesta inválida (tiene %s) al solicitar y parsear la lista de resultados sin el parámetro obligatorio x
			throw new ExcepcionChecker(cf);
		}

		if (CommonEAChecker.errorHandler.hasFatalerrors()) {
			CommonEAChecker.logCall(call);

			ArrayList<String> fatalerrors = CommonEAChecker.errorHandler.getFatalerrors();
			String msg="";

			for (int x=0; x < fatalerrors.size(); x++)
				if (x == (fatalerrors.size()-1)) msg += "++++"+fatalerrors.get(x);
				else msg += "++++"+fatalerrors.get(x)+"<br>\n";


			cf = new CheckerFailure(call, "", msg);
			cf.addMotivo(currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC60,lang), "fatal errors")+"'"+NP1+"'");  // CC60 = Respuesta inválida (tiene %s) al solicitar y parsear la lista de resultados sin el parámetro obligatorio x
			throw new ExcepcionChecker(cf);
		}

		if (doc == null) {
			cf = new CheckerFailure(call, "", currentMethod+": "+Msgs.getMsg(Msgs.CC61,lang)+"'"+NP1+"'");  // CC61 = Se recibe 'null' al solicitar la lista de resultados sin el parámetro obligatorio x
			throw new ExcepcionChecker(cf);
		}

		el = doc.getDocumentElement();
		tagName = el.getTagName();
		if (tagName.equals("wrongRequest")) {
			reason = el.getTextContent().trim();

			if (!reason.equals("no param:"+NP1)) {
				cf = new CheckerFailure(call, "", currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC62,lang), NP1)+"'"+reason+"'"); // CC62 = "Responde con &lt;wrongRequest> pero no por 'no param:%s', sino por: " reason
				throw new ExcepcionChecker(cf);
			}
		}
		else {
			cf = new CheckerFailure(call, "", currentMethod+": "+Msgs.getMsg(Msgs.CC63,lang)+"'"+NP1+"'"); // CC63 = "No se recibe &lt;wrongRequest> al solicitar la lista de resultados sin el parámetro obligatorio "
			throw new ExcepcionChecker(cf);
		}



		// probando qs2, donde falta NP2

		call = url+qs2;

		CommonEAChecker.errorHandler.clear();

		try {
			doc = CommonEAChecker.db.parse(call);
		}
		catch (SAXException e) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo(currentMethod+": SAXException: "+Msgs.getMsg(Msgs.CC59,lang)+"'"+NP2+"'"); // CC59 = al solicitar y parsear la lista de resultados sin el parámetro obligatorio x
			throw new ExcepcionChecker(cf);
		}
		catch (Exception e) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo(currentMethod+": Exception: "+Msgs.getMsg(Msgs.CC59,lang)+"'"+NP2+"'"); // CC59 = al solicitar y parsear la lista de resultados sin el parámetro obligatorio x
			throw new ExcepcionChecker(cf);
		}

		if (CommonEAChecker.errorHandler.hasErrors()) {
			CommonEAChecker.logCall(call);

			ArrayList<String> errors = CommonEAChecker.errorHandler.getErrors();
			String msg="";

			for (int x=0; x < errors.size(); x++)
				if (x == (errors.size()-1)) msg += "++++"+errors.get(x);
				else msg += "++++"+errors.get(x)+"<br>\n";

			cf = new CheckerFailure(call, "", msg);
			cf.addMotivo(currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC60,lang), "errors")+"'"+NP2+"'");  // CC60 = Respuesta inválida (tiene %s) al solicitar y parsear la lista de resultados sin el parámetro obligatorio x
			throw new ExcepcionChecker(cf);
		}

		if (CommonEAChecker.errorHandler.hasFatalerrors()) {
			CommonEAChecker.logCall(call);

			ArrayList<String> fatalerrors = CommonEAChecker.errorHandler.getFatalerrors();
			String msg="";

			for (int x=0; x < fatalerrors.size(); x++)
				if (x == (fatalerrors.size()-1)) msg += "++++"+fatalerrors.get(x);
				else msg += "++++"+fatalerrors.get(x)+"<br>\n";

			cf = new CheckerFailure(call, "", msg);
			cf.addMotivo(currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC60,lang), "fatal errors")+"'"+NP2+"'");  // CC60 = Respuesta inválida (tiene %s) al solicitar y parsear la lista de resultados sin el parámetro obligatorio x
			throw new ExcepcionChecker(cf);
		}

		if (doc == null) {
			cf = new CheckerFailure(call, "", currentMethod+": "+Msgs.getMsg(Msgs.CC61,lang)+"'"+NP2+"'");  // CC61 = Se recibe 'null' al solicitar la lista de resultados sin el parámetro obligatorio
			throw new ExcepcionChecker(cf);
		}

		el = doc.getDocumentElement();
		tagName = el.getTagName();
		if (tagName.equals("wrongRequest")) {
			reason = el.getTextContent().trim();
			if (!reason.equals("no param:"+NP2)) {
				cf = new CheckerFailure(call, "", currentMethod+": "+String.format(Msgs.getMsg(Msgs.CC62,lang), NP2)+"'"+reason+"'"); // CC62 = "Responde con &lt;wrongRequest> pero no por 'no param:%s', sino por: " reason
				throw new ExcepcionChecker(cf);
			}
		}
		else {
			cf = new CheckerFailure(call, "", currentMethod+": "+Msgs.getMsg(Msgs.CC63,lang)+"'"+NP2+"'"); // CC63 = "No se recibe &lt;wrongRequest> al solicitar la lista de resultados sin el parámetro obligatorio "
			throw new ExcepcionChecker(cf);
		}


		// todo bien
		return;
	}



}
