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


// pide los resultados de la práctica de un alumno y los compara con los que obtiene del profesor

// Query1 y Query2 emplean las clases Subject y Student de la propia implementación de la práctica

package docencia.sint.EAML.checker;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import docencia.sint.Common.CommonSINT;
import docencia.sint.Common.Msgs;
import docencia.sint.EAML.CommonEAML;



public class P2EAChecker extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String ENGLISH_URI = "P2EAeCheck";  // called URI for English version

	public int esProfesor = 0;  // 1 -> es el profesor,    0 -> es un alumno

	// en el init sólo se ordena crear el DocumentBuilder del CommonEAChecker, que será usado para llamar al servicio del alumno y analizar su respuesta

	public void init (ServletConfig servletConfig)   throws ServletException
	{
      CommonEAChecker.initLoggerEAChecker(P2EAChecker.class);
      CommonEAChecker.logEAChecker("Init...");

	    CommonEAChecker.servletContextSintProf = servletConfig.getServletContext();

	    // si hay parámetro 'passwd' en el contexto (server.xml), se coge esa
	    // de lo contrario queda la fija que hay en CommonSINT

	    String passwdSintProf = CommonEAChecker.servletContextSintProf.getInitParameter("passwd");
	    if (passwdSintProf != null) CommonSINT.PPWD = passwdSintProf;

	    CommonEAChecker.createDocumentBuilder();
	}





	// procesa todas las solicitudes a este servlet

	public void doGet(HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException
	{

			// se averigua el idioma (y se asigna a lang) según la URI invocada
		  String lang="es";
		  if (request.getRequestURI().endsWith(P2EAChecker.ENGLISH_URI))
				  lang = "en";

	    // si el parámetro p=si, es el profesor el que está ejecutando, de lo contrario es un alumno
	    // lo reflejamos en la variable CommonEAChecker.esProfesor

	    esProfesor = 0;

	    String profesor = request.getParameter("p");
	    if (profesor != null) {
    			if (profesor.equals("si")) esProfesor = 1;
	    }


		// inicializamos un par de variables comunes

	    CommonEAChecker.server_port = request.getServerName()+":"+request.getServerPort();
	    CommonEAChecker.servicioProf = "http://"+CommonEAChecker.server_port+CommonEAChecker.PROF_CONTEXT+CommonEAChecker.SERVICE_NAME;

	    response.setCharacterEncoding("utf-8");

	    // en 'screenP' se recibe la pantalla que se está solicitando

	    String screenP;
	    screenP = request.getParameter("screenP");

	    if (screenP == null) screenP="0"; // si screenP no existe, se está pidiendo la pantalla inicial (igual a screenP=0)

	    switch (screenP) {
			case "0":		// screenP=0, se está pidiendo la pantalla inicial del checker
				this.doGetHome(request, response, lang);
				break;

			case "4":		// screenP=4, se está pidiendo un fichero de resultados de una corrección
				CommonEAChecker.doGetRequestResultFile(request, response, lang);
				break;



			// CONSULTA 1: Alumnos de una asignatura de una titulación

			case "11":  	// screenP=11, se está pidiendo comprobar todas las llamadas del profesor
				Query1.doGetC1CheckSintprofCalls(request, response, lang);
				break;

			case "12":    // screenP=12, se está pidiendo la pantalla para ordenar corregir un único servicio
				Query1.doGetC1CorrectOneForm(request, response, lang, esProfesor);
				break;
			case "121":	  // screenP=121, se pide el informe de la corrección de un servicio
				Query1.doGetC1CorrectOneReport(request, response, lang, esProfesor);
				break;

			// los siguientes 2 bloques hacen lo mismo de distinta forma

			// este bloque gestiona la corrección de todos los servicios, devolviendo una respuesta al terminar
			case "13":    // screenP=13, se está pidiendo la pantalla para ordenar corregir todos los servicios informando al final
				Query1.doGetC1CorrectAllForm(request, response, lang);
				break;
			case "131":		// screenP=131, se pide el informe de la corrección de todos los servicios informando al final
				Query1.doGetC1CorrectAllReport(request, response, lang);
				break;

			// este bloque gestiona la corrección de todos los servicios, devolviendo una respuesta cada vez que se corrige uno
			// utiliza la página jSP "/InformeResultadosCorreccionC1.jsp" que está en webapps
			case "14":    // screenP=14, se está pidiendo la pantalla para ordenar corregir todos los servicios informando uno a uno
				Query1.doGetC1CorrectAllForm2(request, response, lang);
				break;
			case "141":		// screenP=141, se pide el informe de la corrección de todos los servicios informando uno a uno
				Query1.doGetC1CorrectAllReport2(request, response, lang);
				break;
			case "142":		// screenP=142, se envía el informe de la corrección de todos los servicios uno a uno
				Query1.doGetC1CorrectAllReport2Run(request, response, lang);
				break;



			// CONSULTA 2: notas de un alumno en una titulación

			case "21":  	// screenP=21, se está pidiendo comprobar todas las llamadas del profesor
				Query2.doGetC2CheckSintprofCalls(request, response, lang);
				break;

			case "22":     // screenP=22, se está pidiendo la pantalla para ordenar corregir un único servicio
				Query2.doGetC2CorrectOneForm(request,response,lang,esProfesor);
				break;
			case "221":	   // screenP=221, se pide el informe de la corrección de un servicio
				Query2.doGetC2CorrectOneReport(request,response,lang,esProfesor);
				break;

			// los siguientes 2 bloques hacen lo mismo de distinta forma

			// este bloque gestiona la corrección de todos los servicios, devolviendo una respuesta al terminar
			case "23":    // screenP=23, se está pidiendo la pantalla para ordenar corregir todos los servicios informando al final
				Query2.doGetC2CorrectAllForm(request,response,lang);
				break;
			case "231":	  // screenP=231, se pide el informe de la corrección de todos los servicios informando al final
				Query2.doGetC2CorrectAllReport(request,response,lang);
				break;

			// este bloque gestiona la corrección de todos los servicios, devolviendo una respuesta cada vez que se corrige uno
			// utiliza la página jSP "/InformeResultadosCorreccionC2.jsp" que está en webapps
			case "24":    // screenP=24, se está pidiendo la pantalla para ordenar corregir todos los servicios informando uno a uno
				Query2.doGetC2CorrectAllForm2(request,response,lang);
				break;
			case "241":		// screenP=241, se pide el informe de la corrección de todos los servicios informando uno a uno
				Query2.doGetC2CorrectAllReport2(request,response,lang);
				break;
			case "242":		// screenP=242, se envía el informe de la corrección de todos los servicios uno a uno
				Query2.doGetC2CorrectAllReport2Run(request,response,lang);
				break;

	    }

	}



	// pantalla inicial para seleccionar acción

	public void doGetHome (HttpServletRequest request, HttpServletResponse response, String lang)
				throws IOException
	{

		PrintWriter out = response.getWriter();

		out.println("<html>");
		CommonEAChecker.printHead(out, lang);
		CommonEAChecker.printBodyHeader(out, lang);

		out.println("<h2>"+MsgsEAMLChecker.getMsg("5",lang)+"</h2>");  // CONSULTA 1 (enero)...

		out.println("<form>");
		out.println("<input type='hidden' name='screenP' value='12'>");
		if (esProfesor == 1)
			   out.println("<input type='hidden' name='p' value='si'>");
		out.println("<input class='menu' type='submit'  value='"+Msgs.getMsg(Msgs.CC02,lang)+"'>");  // CC02=corregir un servicio
		out.println("</form>");

		// estas opciones sólo se le muestran al profesor, por tanto no es necesario traducirlas

		if (esProfesor == 1) {
			out.println("<form>");
			out.println("<input type='hidden' name='screenP' value='13'>");
			out.println("<input type='hidden' name='p' value='si'>");
			out.println("<input class='menu'  type='submit'  value='Corregir todos los servicios'>");
			out.println("</form>");

			out.println("<form>");
			out.println("<input type='hidden' name='screenP' value='14'>");
			out.println("<input type='hidden' name='p' value='si'>");
			out.println("<input class='menu'  type='submit'  value='Corregir todos los servicios (uno a uno)'>");
			out.println("</form>");

			out.println("<form>");
			out.println("<input type='hidden' name='screenP' value='11'>");
			out.println("<input type='hidden' name='p' value='si'>");
			out.println("<input class='menu'  type='submit'  value='Comprobar las llamadas a sintprof'>");
			out.println("</form>");
		}

		out.println("<br><br><hr>");

		out.println("<h2>"+MsgsEAMLChecker.getMsg("6",lang)+"</h2>");  // CONSULTA 2 (junio)...

		out.println("<form>");
		out.println("<input type='hidden' name='screenP' value='22'>");
		if (esProfesor == 1)
			out.println("<input type='hidden' name='p' value='si'>");
		out.println("<input class='menu' type='submit'  value='"+Msgs.getMsg(Msgs.CC02,lang)+"'>");  // CC02=corregir un servicio
		out.println("</form>");

		// estas opciones sólo se le muestran al profesor, por tanto no es necesario traducirlas

		if (esProfesor == 1) {
			out.println("<form>");
			out.println("<input type='hidden' name='screenP' value='23'>");
			out.println("<input type='hidden' name='p' value='si'>");
			out.println("<input class='menu'  type='submit'  value='Corregir todos los servicios'>");
			out.println("</form>");

			out.println("<form>");
			out.println("<input type='hidden' name='screenP' value='24'>");
			out.println("<input type='hidden' name='p' value='si'>");
			out.println("<input class='menu'  type='submit'  value='Corregir todos los servicios (uno a uno)'>");
			out.println("</form>");

			out.println("<form>");
			out.println("<input type='hidden' name='screenP' value='21'>");
			out.println("<input type='hidden' name='p' value='si'>");
			out.println("<input class='menu' type='submit'  value='Comprobar las llamadas a sintprof'>");
			out.println("</form>");
		}


		CommonSINT.printFoot(out, MsgsEAMLChecker.CREATED);
		out.println("</body></html>");
	}

}
