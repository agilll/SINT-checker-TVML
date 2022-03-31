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

// consulta 1
// películas de un día en un canal

package docencia.sint.TVML;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import docencia.sint.Common.CommonSINT;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;


   // MÉTODOS PARA LA PRIMERA CONSULTA

public class P2TVC1 {

    // F11: método que imprime o devuelve la lista de días

    public static void doGetF11Dias (HttpServletRequest request, HttpServletResponse response) throws IOException {

	    	ArrayList<String> dias = P2TVC1.getC1Dias();   // se pide la lista de días

	    	if (dias.size() == 0) {
	    		CommonSINT.doBadRequest("no hay días", request, response);
	    		return;
	    	}

	    	String auto = request.getParameter("auto");
        String fe = request.getParameter("fe");

	    	if (auto == null) auto="no";
        if (fe == null) fe="html";

        if (auto.equals("si")) {
            response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            P2TVC1FE.printF11XML(out, dias);
        }
        else {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            P2TVC1FE.printF11HTML(out, fe, dias);
	    	}
    }


    // método auxiliar del anterior, que calcula la lista de días

    private static ArrayList<String> getC1Dias () {
	    	if (CommonTVML.real == 0)
	    		return new ArrayList<String>(Arrays.asList("2013/12/01", "2013/12/02", "2013/12/03", "2013/12/04", "2013/12/05"));
	    	else {
	    		ArrayList<String> listaDias = new ArrayList<String>();

	    		// convertimos las claves del hashmap en una lista

	    		Set<String> setDias = CommonTVML.mapDocs.keySet();
	    		listaDias.addAll(setDias);

	    		Collections.sort(listaDias);  // se ordenan alfabéticamente, que es equivalente a cronológicamente
	    		return listaDias;
	    	}
    }






    // F12: método que imprime o devuelve la lista de canales de un día

    public static void doGetF12Canales (HttpServletRequest request, HttpServletResponse response) throws IOException {

	    ArrayList<Canal> canales;

	    String pdia = request.getParameter("pdia");
	    if (pdia == null) {
	    	CommonSINT.doBadRequest("no param:pdia", request, response);
	    	return;
	    }

	    canales = P2TVC1.getC1Canales(pdia);  // se pide la lista de canales del dia seleccionado

	    if (canales == null) {
	    		CommonSINT.doBadRequest("El día "+pdia+" no existe", request, response);
	    		return;
	    }

	    String auto = request.getParameter("auto");
      String fe = request.getParameter("fe");

      if (auto == null) auto="no";
      if (fe == null) fe="html";

      if (auto.equals("si")) {
          response.setContentType("text/xml");
          PrintWriter out = response.getWriter();
          P2TVC1FE.printF12XML(out, canales);
      }
      else {
          response.setContentType("text/html");
          PrintWriter out = response.getWriter();
          P2TVC1FE.printF12HTML(out, fe, pdia, canales);
      }

    }


    // método auxiliar del anterior, que calcula la lista de canales de un año dado

    private static ArrayList<Canal> getC1Canales (String pdia) {
	    if (CommonTVML.real == 0)
	    	return new ArrayList<Canal>(Arrays.asList(new Canal("1","es", "A3","A3Media"), new Canal("2","gl", "TVG1","CRTVG"),
	    		   new Canal ("3","ca", "TV3","CCMA")));
	    else {
	    		Document doc = CommonTVML.mapDocs.get(pdia);
	    		if (doc == null) return null;  // no existe ese día

	    		ArrayList<Canal> listaCanales = new ArrayList<Canal>();

	    		NodeList nlCanales = doc.getElementsByTagName("Canal");  // pedimos el NodeList con todos los canales de ese día

	    		Element elemCanal;
	    		String nombreCanal, grupo, idCanal, lang;

	    		// vamos a recopilar la información de todos los canales

	    		for (int y=0; y < nlCanales.getLength(); y++) {
	    			elemCanal = (Element)nlCanales.item(y);  // estudiamos un canal

	    			nombreCanal = CommonSINT.getTextContentOfChild(elemCanal, "NombreCanal");  // obtenemos el nombre del canal
	    			grupo = CommonSINT.getTextContentOfChild(elemCanal, "Grupo");  // obtenemos el grupo al que pertenece el canal

	    			idCanal = elemCanal.getAttribute("idCanal");   // leemos el identificador del canal
	    			lang = elemCanal.getAttribute("lang");   // leemos el idioma del canal


	    			listaCanales.add(new Canal(idCanal, lang, nombreCanal, grupo));  // creamos y añadimos el canal
	    		}

	    		Collections.sort(listaCanales);  // por orden alfabético del nombre del canal

	    		return listaCanales;
	    }
    }







    // F13: método que imprime o devuelve la lista de películas de un canal en un día

    public static void doGetF13Peliculas (HttpServletRequest request, HttpServletResponse response) throws IOException
    {
    		ArrayList<Programa> programas;

	    	String pdia = request.getParameter("pdia");

	    	if (pdia == null) {
	    		CommonSINT.doBadRequest("no param:pdia", request, response);
	    		return;
	    	}

	    	String pcanal = request.getParameter("pcanal");

	    	if (pcanal == null) {
	    		CommonSINT.doBadRequest("no param:pcanal", request, response);
	    		return;
	    	}


	    	programas = P2TVC1.getC2Programas(pdia, pcanal, "Cine");   // pedimos la lista de películas de un canal en un día

	    	if (programas == null) {
	    		CommonSINT.doBadRequest("el 'día' ("+pdia+") o el 'canal' ("+pcanal+") no existen", request, response);
	    		return;
	    	}

	    	String auto = request.getParameter("auto");
        String fe = request.getParameter("fe");

        if (auto == null) auto="no";
        if (fe == null) fe="html";

        if (auto.equals("si")) {
            response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            P2TVC1FE.printF13XML(out, programas);
        }
        else {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            P2TVC1FE.printF13HTML(out, fe, pdia, pcanal, programas);
        }
    }



    // método auxiliar del anterior, que calcula la lista de canciones de duración menor a la indicada

    private static ArrayList<Programa> getC2Programas (String pdia, String pcanal, String categoria) {
	    	if (CommonTVML.real == 0)
	    		return new ArrayList<Programa>(Arrays.asList(new Programa("14","es", "r1", "P1", "Cine", "10:00", ""),
	    				new Programa("16","en", "r2", "P2", "Cine", "14:00", ""),
	    				new Programa("18","it", "r3", "P3", "Cine", "16:00", "")));
	    	else {

	    		Document doc = CommonTVML.mapDocs.get(pdia);
	    		if (doc == null) return null;  // no existe ese día

	    		ArrayList<Programa> listaPeliculas = new ArrayList<Programa>();  // lista de programas a devolver

	        	String xpathTarget =   "/Programacion/Canal[NombreCanal='"+pcanal+"']";    //  el canal buscado
	        	NodeList nlCanales=null;

	    		try {  // obtenemos los canales del dia (1) con ese nombre
	    			nlCanales = (NodeList)CommonTVML.xpath.evaluate(xpathTarget, doc, XPathConstants.NODESET);
	    		}
	    		catch (XPathExpressionException e) {CommonTVML.logTVML(e.toString()); return null;}

	    		if (nlCanales.getLength() == 0) {
	    			CommonTVML.logTVML("No hay el canal "+pcanal+" en el día "+pdia);
	    			return null;  // no hay tal canal ese día
	    		}

	    		Element elemCanal = (Element)nlCanales.item(0);   // cogemos el canal

	    		xpathTarget =   "Programa[Categoria='"+categoria+"']";    //  los programas de esa categoría
	    		NodeList nlPeliculas=null;

	    		try {  // cogemos todas las películas de ese canal ese día
	    			nlPeliculas = (NodeList)CommonTVML.xpath.evaluate(xpathTarget, elemCanal, XPathConstants.NODESET);
	    		}
	    		catch (XPathExpressionException e) {CommonTVML.logTVML(e.toString()); return null;}

	    		Element elemPrograma;

	    		String edadMinima, langs, resumen, nombrePrograma, horaInicio;

	    		for (int x=0; x < nlPeliculas.getLength(); x++) {
	    			elemPrograma = (Element) nlPeliculas.item(x);

	    			edadMinima = elemPrograma.getAttribute("edadminima");
	    			langs = elemPrograma.getAttribute("langs");
	    			resumen = CommonSINT.getTextContent(elemPrograma);
	    			nombrePrograma = CommonSINT.getTextContentOfChild(elemPrograma, "NombrePrograma");
	    			horaInicio = CommonSINT.getTextContentOfChild(elemPrograma, "HoraInicio");

	    			listaPeliculas.add(new Programa(edadMinima, langs, resumen, nombrePrograma, "Cine", horaInicio, ""));

	    		}



    		Collections.sort(listaPeliculas);  // ordenamos la lista por su orden natural
    		return listaPeliculas;
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
