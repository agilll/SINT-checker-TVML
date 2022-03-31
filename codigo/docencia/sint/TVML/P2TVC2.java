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

// consulta 2
// Canales con películas en un idioma en un día

package docencia.sint.TVML;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import docencia.sint.Common.CommonSINT;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;


   // MÉTODOS PARA LA SEGUNDA CONSULTA

public class P2TVC2 {

    // F21: método que imprime o devuelve la lista de langs

    public static void doGetF21Langs (HttpServletRequest request, HttpServletResponse response) throws IOException {

	    	ArrayList<String> langs = P2TVC2.getC2Langs();   // se pide la lista de langs

	    	if (langs.size() == 0) {
	    		CommonSINT.doBadRequest("no hay idiomas", request, response);
	    		return;
	    	}

	    	String auto = request.getParameter("auto");
        String fe = request.getParameter("fe");

        if (auto == null) auto="no";
        if (fe == null) fe="html";

        if (auto.equals("si")) {
            response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            P2TVC2FE.printF21XML(out, langs);
        }
        else {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            P2TVC2FE.printF21HTML(out, fe, langs);
	    	}
    }


    // método auxiliar del anterior, que devuelve la lista de idiomas

    private static ArrayList<String> getC2Langs () {
	    if (CommonTVML.real == 0)
	    		return new ArrayList<String>(Arrays.asList("us", "uk","es","de"));
	    else {
	    	ArrayList<String> listaLangs = new ArrayList<String>();

	        String targetLang = "/Programacion/Canal/@lang";            // langs por defecto de cada canal
	        String targetLangs = "/Programacion/Canal/Programa/@langs";    // langs en los atributos langs de los programas
			Document doc;
			NodeList nlLangs=null;
			Attr attrLang, attrLangs;
			String idioma, listaIdiomas;

			Collection<Document> collectionDocs = CommonTVML.mapDocs.values();
			Iterator<Document> iter = collectionDocs.iterator();

			while (iter.hasNext()) {   // iteramos sobre todos los días

				doc = iter.next();

		    	try {  // obtenemos los atributos lang
		    			nlLangs = (NodeList)CommonTVML.xpath.evaluate(targetLang, doc, XPathConstants.NODESET);
		    		}
				catch (XPathExpressionException ex) {CommonTVML.logTVML(ex.toString()); return null;}

				for (int z=0; z < nlLangs.getLength(); z++) {
					attrLang = (Attr)nlLangs.item(z);   // estudiamos cada lang
					idioma = attrLang.getValue();
					if (!listaLangs.contains(idioma)) listaLangs.add(idioma);
				}


		    	try {  // obtenemos los atributos langs
		    			nlLangs = (NodeList)CommonTVML.xpath.evaluate(targetLangs, doc, XPathConstants.NODESET);
		    		}
				catch (XPathExpressionException ex) {CommonTVML.logTVML(ex.toString()); return null;}

				for (int z=0; z < nlLangs.getLength(); z++) {
					attrLangs = (Attr)nlLangs.item(z);   // estudiamos cada lang
					listaIdiomas = attrLangs.getValue();
					String[] idiomas = listaIdiomas.split(" ");

					for (int i = 0; i < idiomas.length; i++){
						if (!listaLangs.contains(idiomas[i])) listaLangs.add(idiomas[i]);
					}
				}
			}

	    	Collections.sort(listaLangs);  // alfabéticamente
	    	return listaLangs;
	    }
    }







    // F22: método que imprime o devuelve la lista de programas infantiles en un idioma

    public static void doGetF22Infantiles (HttpServletRequest request, HttpServletResponse response) throws IOException {

		Programa programa;
	    ArrayList<Programa> programas;

	    String plang = request.getParameter("plang");
	    if (plang == null) {
	    	CommonSINT.doBadRequest("no param:plang", request, response);
	    	return;
	    }

	    programas = P2TVC2.getC2Infantiles(plang);  // se pide la lista de programas infantiles en el idioma seleccionado

	    if (programas == null) {
	    		CommonSINT.doBadRequest("El lang "+plang+" no existe", request, response);
	    		return;
	    }

	    String auto = request.getParameter("auto");
      String fe = request.getParameter("fe");

      if (auto == null) auto="no";
      if (fe == null) fe="html";

      if (auto.equals("si")) {
          response.setContentType("text/xml");
          PrintWriter out = response.getWriter();
          P2TVC2FE.printF22XML(out, programas);
      }
      else {
          response.setContentType("text/html");
          PrintWriter out = response.getWriter();
          P2TVC2FE.printF22HTML(out, fe, plang, programas);
      }
    }



    // método auxiliar del anterior, que calcula la lista de programas infantiles en un determinado idioma

    public static ArrayList<Programa> getC2Infantiles (String lang) {
    	if (CommonTVML.real == 0)
    		return new ArrayList<Programa>(Arrays.asList(new Programa("14",lang, "r1", "P1", "Infantiles", "10:00", ""),
    				new Programa("16",lang, "r2", "P2", "Infantiles", "14:00", ""),
    				new Programa("18",lang, "r3", "P3", "Infantiles", "16:00", "")));
    	else {
    		Document doc;
            NodeList nlPrograma;
        	Element elPrograma, elRoot;
	    	String nombreP, resumenP, diaP;

	    	ArrayList<Programa> listaProgramas = new ArrayList<Programa>();  // lista de programas a devolver

        	String target1 = "/Programacion/Canal[@lang='"+lang+"']/Programa[Categoria='Infantiles' and not(@langs)]"; // programas sin @langs de un canal con lang por defecto
        	String target2 = "/Programacion/Canal/Programa[Categoria='Infantiles' and contains(@langs,'"+lang+"')]";   // programas con lang entre sus langs

			Collection<Document> collectionDocs = CommonTVML.mapDocs.values();
			Iterator<Document> iter = collectionDocs.iterator();

			while (iter.hasNext()) {   // iteramos sobre todos los días

				doc = iter.next();
				elRoot = doc.getDocumentElement();

				diaP = CommonSINT.getTextContentOfChild(elRoot, "Fecha");  // averiguamos el día del documento

		    	try {  // obtenemos los programas que nos interesan de ese día
		    		nlPrograma = (NodeList)CommonTVML.xpath.evaluate(target1+" | "+target2, doc, XPathConstants.NODESET);
		    		}
				catch (XPathExpressionException ex) {CommonTVML.logTVML(ex.toString()); return null;}
				catch (Exception ex) {CommonTVML.logTVML(ex.toString()); return null;}

				for (int z=0; z < nlPrograma.getLength(); z++) {
					elPrograma  = (Element)nlPrograma.item(z);   // estudiamos cada programa

	    			nombreP = CommonSINT.getTextContentOfChild(elPrograma, "NombrePrograma");  // averiguamos el nombre del programa

					resumenP = CommonSINT.getTextContent(elPrograma);

					listaProgramas.add(new Programa("", lang, resumenP, nombreP, "Infantiles", "", diaP));
				}
			}

    		Collections.sort(listaProgramas, Programa.DIA_RESUMEN);  // por día de emisión, luego por tamaño de resumen yluego por alfabético de resumen
	   	    return listaProgramas;
    	}
    }






    // F23: método que imprime o devuelve la lista de canales con películas en un idioma en un día

    public static void doGetF23Canales (HttpServletRequest request, HttpServletResponse response) throws IOException {

    		Canal canal;
    		ArrayList<Canal> canales;

	    	String plang = request.getParameter("plang");

	    	if (plang == null) {
	    		CommonSINT.doBadRequest("no param:plang", request, response);
	    		return;
	    	}

	    	String pdia = request.getParameter("pdia");

	    	if (pdia == null) {
	    		CommonSINT.doBadRequest("no param:pdia", request, response);
	    		return;
	    	}


	    	canales = P2TVC2.getC2Canales(plang, pdia);   // pedimos la lista de canales que emiten películas en un idioma en un día

	    	if (canales == null) {
	    		CommonSINT.doBadRequest("No hay canales en el día "+pdia+" que emitan programas en el idioma "+plang, request, response);
	    		return;
	    	}

	    	String auto = request.getParameter("auto");
        String fe = request.getParameter("fe");

        if (auto == null) auto="no";
        if (fe == null) fe="html";

        if (auto.equals("si")) {
            response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            P2TVC2FE.printF23XML(out, canales);
        }
        else {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            P2TVC2FE.printF23HTML(out, fe, plang, pdia, canales);
        }
    }




    // método auxiliar del anterior, que calcula la lista de canales que emiten pelis en un día en un idioma

    private static ArrayList<Canal> getC2Canales (String plang, String pdia) {
	    if (CommonTVML.real == 0)
	    	return new ArrayList<Canal>(Arrays.asList(new Canal("1","es", "A3","A3Media"), new Canal("2","gl", "TVG1","CRTVG"),
	    		   new Canal ("3","ca", "TV3","CCMA")));
	    else {
	    		Document doc = CommonTVML.mapDocs.get(pdia);
	    		if (doc == null) return null;  // no existe ese día

	    		ArrayList<Canal> listaCanales = new ArrayList<Canal>();

	        	String target1 = "/Programacion/Canal[@lang='"+plang+"' and Programa[Categoria='Cine' and not(@langs)]]"; // canales con lang y con películas sin @langs
	        	String target2 = "/Programacion/Canal[Programa[Categoria='Cine' and contains(@langs,'"+plang+"')]]";   // canales con peliculas con langs
	        	String target = target1+" | "+target2;

	        	CommonTVML.logTVML(target);

	        	NodeList nlCanales=null;
	    		Element elemCanal;
	    		String nombreCanal, grupo, idCanal;

	    		// vamos a recopilar la información de todos los canales

	    		try {  // obtenemos los canales que nos interesan de ese día
		    		nlCanales = (NodeList)CommonTVML.xpath.evaluate(target1+" | "+target2, doc, XPathConstants.NODESET);
		    	}
				catch (XPathExpressionException ex) {CommonTVML.logTVML(ex.toString()); return null;}
				catch (Exception ex) {CommonTVML.logTVML(ex.toString()); return null;}



	    		for (int y=0; y < nlCanales.getLength(); y++) {
	    			elemCanal = (Element)nlCanales.item(y);  // estudiamos un canal

	    			nombreCanal = CommonSINT.getTextContentOfChild(elemCanal, "NombreCanal");  // obtenemos el nombre del canal
	    			grupo = CommonSINT.getTextContentOfChild(elemCanal, "Grupo");  // obtenemos el grupo al que pertenece el canal
	    			idCanal = elemCanal.getAttribute("idCanal");   // leemos el identificador del canal


	    			listaCanales.add(new Canal(idCanal, "", nombreCanal, grupo));  // creamos y añadimos el canal
	    		}

	    		Collections.sort(listaCanales, Canal.IDCANAL);  // por orden alfabético del nombre del canal

	    		return listaCanales;
	    }
    }







    /* para el examen
     *
     public static void doGetF15 (HttpServletRequest request, HttpServletResponse response) throws IOException {


		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();

		out.println("<html><head><meta charset='utf-8'/><title>"+Common.MSGTITLE+"</title></head><body>");
    		out.println("<h2>"+Common.MSGINICIAL+"</h2>");

    		out.println("<h3>Los datos son:</h3>");



    		out.println("<form>");
    		out.println("<input type='hidden' name='p' value='"+Common.PASSWD+"'>");

    		out.println("<input type='hidden' name='pphase' value='0'><br>");

    		out.println("<input class='home' type='submit' value='Inicio' onClick='document.forms[0].pphase.value=\"0\"'>");
    		out.println("</form>");

    		CommonSINT.printFoot(out);
    		out.println("</body></html>");


     }
    	*/

}
