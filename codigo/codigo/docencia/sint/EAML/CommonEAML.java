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

// cosas comunes para varias clases

package docencia.sint.EAML;

import java.io.PrintWriter;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import docencia.sint.Common.WrongFile;

public class CommonEAML {

    public static final String EAMLCSS = "css/eaml.css";

		static ArrayList<String> listaFicherosProcesados = new ArrayList<String>();
    static ArrayList<WrongFile> listWarnings = new ArrayList<WrongFile>();
    static ArrayList<WrongFile> listErrores= new ArrayList<WrongFile>();
    static ArrayList<WrongFile> listErroresfatales = new ArrayList<WrongFile>();

    static HashMap<String,Document> mapDocs = new HashMap<String,Document>();   // el hashmap de documentos

    static XPathFactory xpathFactory = XPathFactory.newInstance();
    static XPath xpath = xpathFactory.newXPath();

    static int real=1; // para indicar si los resultados son reales (real=1) o inventados (real=0)

		private static Logger logger = null;  // el objeto Logger

		// para inicializar el objeto Logger

		public static void initLoggerEAML (Class c) {
			logger = LogManager.getLogger(c);
		}

		// para imprimir con el Logger en el sintprof.log

		public static void logEAML (String msg) {
			logger.info("## EAML ## "+msg);
		}



		// para imprimir la cabecera de cada respuesta HTML

		public static void printHead(String language, PrintWriter out) {
			out.println("<head><meta charset='utf-8'/>");
			// 001 = "Servicio de consulta de expedientes académicos"
			out.println("<title>"+MsgsEAML.getMsg("001", language)+"</title>");
			out.println("<link rel='stylesheet'  type='text/css'  href='"+CommonEAML.EAMLCSS+"'/></head>");
		}

}
