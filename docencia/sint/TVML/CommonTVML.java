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

// cosas comunes para varias clases

package docencia.sint.TVML;

import java.io.PrintWriter;

import org.w3c.dom.Document;

import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonTVML {

    public static final String TVMLCSS = "css/tvml.css";

    static HashMap<String,Document> mapDocs = new HashMap<String,Document>();   // el hashmap de documentos

    static XPathFactory xpathFactory = XPathFactory.newInstance();
    static XPath xpath = xpathFactory.newXPath();

    static int real=1; // para indicar si los resultados son reales (real=1) o inventados (real=0)

	private static Logger logger = null;  // el objeto Logger

	// para inicializar el objeto Logger

	public static void initLoggerTVML (Class c) {
		logger = LogManager.getLogger(c);
	}

	 // para imprimir con el Logger en el sintprof.log

	 public static void logTVML (String msg) {
		logger.info("## TVML ## "+msg);
	 }



    // para imprimir la cabecera de cada respuesta HTML

	public static void printHead(PrintWriter out) {
		out.println("<head><meta charset='utf-8'/>");
		out.println("<title>"+MsgsTVML.M1+"</title>");
		out.println("<link rel='stylesheet'  type='text/css' href='"+CommonTVML.TVMLCSS+"'/></head>");
	}




}
