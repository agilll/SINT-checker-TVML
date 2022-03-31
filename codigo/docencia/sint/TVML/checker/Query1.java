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

// Implementación de la comprobación de la consulta 1 (películas de un canal de un día)

package docencia.sint.TVML.checker;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Date;

import docencia.sint.Common.CheckerFailure;
import docencia.sint.Common.CommonSINT;
import docencia.sint.Common.ExcepcionChecker;
import docencia.sint.Common.ExcepcionSINT;
import docencia.sint.Common.SintRandom;
import docencia.sint.Common.BeanResultados;
import docencia.sint.TVML.Canal;
import docencia.sint.TVML.CommonTVML;
import docencia.sint.TVML.Programa;
import docencia.sint.TVML.MsgsTVML;


public class Query1 {


	// nombres de los parámetros de esta consulta

	static final String PDIA = "pdia";
	static final String PCANAL = "pcanal";

	// COMPROBACIÓN DE LAS LLAMADAS A SINTPROF

	public static void doGetC1CheckSintprofCalls(HttpServletRequest request, PrintWriter out)
						throws IOException, ServletException
	{
		CheckerFailure cf;
		int esProfesor = 1;  // sólo el profesor debería llegar aquí, podemos poner esto a 1

		out.println("<html>");
		CommonTVChecker.printHead(out);
		CommonTVChecker.printBodyHeader(out);

		out.println("<h2>Consulta 1</h2>");
		out.println("<h3>Comprobar las llamadas al servicio de sintprof</h3>");

		// doOneCheckUpStatus: hace una petición de estado al servicio del profesor para ver si está operativo

		try {
			CommonTVChecker.doOneCheckUpStatus(request, "sintprof", CommonSINT.PPWD);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			out.println("<h4 style='color: red'>sintprof (error al preguntar por el estado): <br>");
			out.println(cf.toHTMLString());
			out.println("</h4>");
			CommonSINT.printEndPageChecker(out,  "0", esProfesor, CommonTVChecker.CREATED);
			return;
		}

		out.println("<h4>CheckStatus OK</h4>");




		// empezamos por pedir los errores

		try {
			CommonTVChecker.requestErrores("sintprof", CommonTVChecker.servicioProf, CommonSINT.PPWD);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			out.println("<h4 style='color: red'>sintprof (ExcepcionChecker pidiendo Errores): <br>");
			out.println(cf.toHTMLString());
			out.println("</h4>");
			CommonSINT.printEndPageChecker(out,  "0", esProfesor, CommonTVChecker.CREATED);
			return;
        }
		catch (Exception ex) {
			out.println("<h4 style='color: red'>sintprof (Exception pidiendo Errores): "+ex.toString()+"</h4>");
			CommonSINT.printEndPageChecker(out,  "0", esProfesor, CommonTVChecker.CREATED);
			return;
        }

		out.println("<h4>Errores OK</h4>");


		// y ahora todas y cada una de las consultas

		// pedimos la lista de días de sintprof

		String qs = "?auto=si&"+CommonSINT.PFASE+"=11&p="+CommonSINT.PPWD;
		String call = CommonTVChecker.servicioProf+qs;

		ArrayList<String> pDias;
		try {
			pDias = Query1.requestDias(call);
			out.println("<h4>Días OK: "+pDias.size()+"</h4>");
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			out.println("<h4 style='color: red'>ExcepcionSINT: sintprof (Días): <br>");
			out.println(cf.toHTMLString());
			out.println("</h4>");
			CommonSINT.printEndPageChecker(out,  "0", esProfesor, CommonTVChecker.CREATED);
			return;
        }



		// vamos con la segunda fase, los canales de cada día
		// el bucle X recorre todos los días


		String diaActual;

		for (int x=0; x < pDias.size(); x++) {
			String indent ="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

			diaActual = pDias.get(x);

			// pedimos los canales de ese dia de sintprof

			qs = "?auto=si&"+CommonSINT.PFASE+"=12&"+PDIA+"="+diaActual+"&p="+CommonSINT.PPWD;
			call = CommonTVChecker.servicioProf+qs;

			ArrayList<Canal> pCanales;
			try {
				pCanales = requestCanalesDia(call);
				out.println("<h4>"+indent+diaActual+": "+pCanales.size()+"  OK</h4>");
			}
			catch (ExcepcionChecker e) {
				cf = e.getCheckerFailure();
				out.println("<h4 style='color: red'>sintprof (Canales): <br>");
				out.println(cf.toHTMLString());
				out.println("</h4>");
				CommonSINT.printEndPageChecker(out,  "0", esProfesor, CommonTVChecker.CREATED);
				return;
	        }




	        // vamos con la tercera fase, las películas de un canal en un día
	        // el bucle Y recorre todos los canales

	        Canal canalActual;

	        for (int y=0; y < pCanales.size(); y++) {

		        	indent = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

		        	canalActual = pCanales.get(y);

		    		// pedimos las peliculas de ese canal de ese día de sintprof

		    		qs = "?auto=si&"+CommonSINT.PFASE+"=13&"+PDIA+"="+diaActual+"&"+PCANAL+"="+canalActual.getNombreCanal()+"&p="+CommonSINT.PPWD;    // necesario URL-encoded??
		    		call = CommonTVChecker.servicioProf+qs;

		    		ArrayList<Programa> pPeliculas;
		    		try {
		    			pPeliculas = requestPeliculasCanalDia(call);
		    			out.println("<h4>"+indent+diaActual+"+"+canalActual.getNombreCanal()+": "+pPeliculas.size()+"  OK</h4>");
		    		}
		    		catch (ExcepcionChecker e) {
						cf = e.getCheckerFailure();
		    			out.println("<h4 style='color: red'>ExcepcionSINT: sintprof (Peliculas): <br>");
						out.println(cf.toHTMLString());
		    			out.println("</h4>");
		    			CommonSINT.printEndPageChecker(out,  "0", esProfesor, CommonTVChecker.CREATED);
		    			return;
		            }


	        } // for y

	    } // for x


		out.println("<h4>sintprof: Todo OK</h4>");

		CommonSINT.printEndPageChecker(out,  "0", esProfesor, CommonTVChecker.CREATED);
	}








	// COMPROBACIÓN DEL SERVICIO DE UN ÚNICO ESTUDIANTE

	// pantalla para ordenar comprobar un estudiante (se pide su número de login)
	// debería unificarse en uno sólo con el de la consulta 2, son casi iguales

	public static void doGetC1CorrectOneForm (HttpServletRequest request, PrintWriter out, int esProfesor)
						throws IOException
	{
		out.println("<html>");
		CommonTVChecker.printHead(out);

		out.println("<script>");

		out.println("function stopEnter (event) {");
		out.println("var x = event.which;");
		out.println("if (x === 13) {event.preventDefault();}");
		out.println("}");

		out.println("function hideservice () {");
		out.println("var serviceAluElem = document.getElementById('serviceAluInput');");
		out.println("serviceAluElem.style.visibility='hidden';");
		out.println("var sendButton = document.getElementById('sendButton');");
		out.println("sendButton.disabled=true;");
		out.println("}");

		out.println("function showservice () {");
		out.println("var inputSintElement = document.getElementById('inputSint');");
		out.println("if ( ! inputSintElement.validity.valid ) return;");
		out.println("var inputSint = inputSintElement.value;");
		out.println("var sendButton = document.getElementById('sendButton');");
		out.println("sendButton.disabled=false;");

		out.println("var inputServiceElem = document.getElementById('serviceAluInput');");

		out.println("inputServiceElem.value = 'http://"+CommonTVChecker.server_port+"/sint'+inputSint+'"+CommonTVChecker.SERVICE_NAME+"';");
		out.println("inputServiceElem.style.visibility='visible';");
		out.println("}");
		out.println("</script>");

		CommonTVChecker.printBodyHeader(out);

		out.println("<h2>Consulta 1</h2>");                               // consulta 1
		out.println("<h3>Corrección de un único servicio</h3>");

		out.println("<form>");
		out.println("<input type='hidden' name='screenP' value='121'>");  // conduce a doGetC1CorrectOneReport

		out.println("Introduzca el número de la cuenta SINT a comprobar: ");
		out.println("<input id='inputSint' type='text' name='alumnoP' size='3' onfocus='hideservice();' onblur='showservice();' onkeypress='stopEnter(event);' pattern='[1-9]([0-9]{1,2})?' required> <br>");

		out.println("URL del servicio del alumno:");
		out.println("<input style='visibility: hidden' id='serviceAluInput' type='text' name='servicioAluP' value='' size='40'><br>");

		if (esProfesor == 0) {
			out.println("<p>Passwd de la cuenta (10 letras o números) <input id='passwdAlu' type='text' name='passwdAlu'  pattern='[A-Za-z0-9]{10}?' required> <br><br>");
		}
		else {
			out.println("<p><input type='hidden' name='p' value='si'>");
		}

		out.println("<p><input class='enviar' id='sendButton' disabled='true' type='submit' value='Enviar'>");
		out.println("</form>");

		out.println("<form>");
		if (esProfesor == 1)
			out.println("<p><input type='hidden' name='p' value='si'>");
		out.println("<p><input class='home'  type='submit' value='Inicio'>");
		out.println("</form>");

		CommonSINT.printFoot(out, CommonTVChecker.CREATED);

		out.println("</body></html>");
	}






	// pantalla para informar de la corrección de un sintX (se recibe en 'alumnoP' su número de login X)
	// también recibe en servicioAlu el URL del servicio del alumno

	public static void doGetC1CorrectOneReport(HttpServletRequest request, PrintWriter out, int esProfesor)
						throws IOException, ServletException
	{
		out.println("<html>");
		CommonTVChecker.printHead(out);
		CommonTVChecker.printBodyHeader(out);

		out.println("<h2>Consulta 1</h2>");
		out.println("<h3>Corrección de un único servicio</h3>");

		// leemos los datos del estudiante

		String alumnoP = request.getParameter("alumnoP");
		String servicioAluP = request.getParameter("servicioAluP");

		if ((alumnoP == null) || (servicioAluP == null)) {
			out.println("<h4 style='color: red'>Falta uno de los parámetros</h4>");  // si falta algún parámetro no se hace nada
			CommonSINT.printEndPageChecker(out,  "12", esProfesor, CommonTVChecker.CREATED);
			return;
		}

		String usuario="sint"+alumnoP;
		String passwdAlu, passwdRcvd;



		try {
			passwdAlu = CommonTVChecker.getAluPasswd(usuario);
		}
		catch (ExcepcionSINT ex) {
			if (ex.getMessage().equals("NOCONTEXT"))
				out.println("<h4 style='color: red'>ExcepcionSINT: Todavía no se ha creado el contexto de "+usuario+"</h4>");
			else
				out.println("<h4 style='color: red'>"+ex.getMessage()+": Imposible recuperar la passwd del contexto de "+usuario+"</h4>");
			CommonSINT.printEndPageChecker(out,  "12", esProfesor, CommonTVChecker.CREATED);
			return;
		}

		if (esProfesor == 0) {  // lo está probando un alumno, es obligatorio que haya introducido su passwd
			passwdRcvd = request.getParameter("passwdAlu");   // leemos la passwd del alumno del parámetro

			if (passwdRcvd == null) {
				out.println("<h4 style='color: red'>No se ha recibido la passwd de "+usuario+"</h4>");
				CommonSINT.printEndPageChecker(out,  "12", esProfesor, CommonTVChecker.CREATED);
				return;
			}

			if (!passwdAlu.equals(passwdRcvd)) {
				out.println("<h4 style='color: red'>La passwd proporcionada no coincide con la almacenada en el sistema para "+usuario+"</h4>");
				CommonSINT.printEndPageChecker(out,  "12", esProfesor, CommonTVChecker.CREATED);
				return;
			}

		}

		out.println("<h3>Comprobando el servicio del usuario "+usuario+" ("+servicioAluP+")</h3>");

		// doOneCheckUpStatus: hace una petición de estado al servicio del profesor para ver si está operativo


		try {
			CommonTVChecker.doOneCheckUpStatus(request, "sintprof", CommonSINT.PPWD);
		}
		catch (ExcepcionChecker e) {
			CheckerFailure cf = e.getCheckerFailure();
			out.println("<h4 style='color: red'>Error al preguntar por el estado de la práctica del profesor: <br>");
			out.println(cf.toHTMLString());
			out.println("</h4>");
			CommonSINT.printEndPageChecker(out,  "12", esProfesor, CommonTVChecker.CREATED);
			return;
		}


		try {
			Query1.correctC1OneStudent(request, usuario, alumnoP, servicioAluP, passwdAlu);
		}
		catch (ExcepcionChecker e) {
			CheckerFailure cf = e.getCheckerFailure();
			out.println("<h4 style='color: red'>Resultado incorrecto para la práctica de "+usuario+" <br>");
			out.println(cf.toHTMLString());
			out.println("</h4>");
			CommonSINT.printEndPageChecker(out,  "12", esProfesor, CommonTVChecker.CREATED);
			return;
		}

		out.println("<h3>Resultado: OK</h3>");



		// terminamos con botones Atrás e Inicio

		CommonSINT.printEndPageChecker(out,  "12", esProfesor, CommonTVChecker.CREATED);
	}




	// método que corrige la consulta 1 de un estudiante

    private static void correctC1OneStudent (HttpServletRequest request,String usuario, String aluNum, String servicioAluP, String passwdAlu)
    			throws ExcepcionChecker
	{
    	CheckerFailure cf;

		// para la consulta directa final, vamos a escoger dia, y canal al azar y a guardarlas en esas variables

		SintRandom.init(); // inicializamos el generador de números aleatorios
		int posrandom;

		String dqDia="";
		String dqCanal="";

		// empezamos por comprobar los ficheros

		try {
			CommonTVChecker.doOneCheckUpFiles(aluNum, "1");
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.addMotivo("Error en los ficheros fuente");
			throw new ExcepcionChecker(cf);
		}


        // ahora comprobamos que el servicio está operativo

		try {
			CommonTVChecker.doOneCheckUpStatus(request, usuario, passwdAlu);
		}
		catch (ExcepcionChecker e) {
			throw e;
		}





		// ahora comprobamos los errores

		try {
			CommonTVChecker.comparaErrores(usuario, servicioAluP, passwdAlu);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("correctC1OneStudent: Diferencias en la lista de errores");
			throw new ExcepcionChecker(cf);
		}




		// y ahora todas y cada una de las consultas

		// pedimos la lista de días de sintprof

		String qs = "?auto=si&"+CommonSINT.PFASE+"=11&p="+CommonSINT.PPWD;
		String call = CommonTVChecker.servicioProf+qs;

		ArrayList<String> pDias;
		try {
			pDias = Query1.requestDias(call);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("correctC1OneStudent: ExcepcionChecker solicitando la lista de días a sintprof");
			throw new ExcepcionChecker(cf);
		}


		// pedimos la lista de día del sintX

		qs = "?auto=si&"+CommonSINT.PFASE+"=11&p="+passwdAlu;
		call = servicioAluP+qs;

		ArrayList<String> xDias;
		try {
			xDias = Query1.requestDias(call);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("correctC1OneStudent: ExcepcionChecker solicitando la lista de días a "+usuario);
			throw new ExcepcionChecker(cf);
		}


		// comparamos las listas de sintprof y sintX

		try {
		     Query1.comparaDias(usuario, pDias, xDias);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setUrl(call);
			cf.addMotivo("correctC1OneStudent: Diferencias en la lista de días ");
			throw new ExcepcionChecker(cf);
		}



		// las listas de días son iguales
		// elegimos un día al azar para la consulta directa final

		posrandom = SintRandom.getRandomNumber(0, pDias.size()-1);
		dqDia = pDias.get(posrandom);




		// vamos con la segunda fase, los canales de cada día
		// el bucle X recorre todos los días


		String diaActual;

		for (int x=0; x < pDias.size(); x++) {

			diaActual = pDias.get(x);

			// pedimos los canales de ese día de sintprof

			qs = "?auto=si&"+CommonSINT.PFASE+"=12&"+PDIA+"="+diaActual+"&p="+CommonSINT.PPWD;
			call = CommonTVChecker.servicioProf+qs;

			ArrayList<Canal> pCanales;
			try {
				pCanales = requestCanalesDia(call);
			}
			catch (ExcepcionChecker e) {
				cf = e.getCheckerFailure();
				cf.setCodigo("20_DIFS");
				cf.addMotivo("correctC1OneStudent: ExcepcionChecker solicitando la lista de canales a sintprof");
				throw new ExcepcionChecker(cf);
			}


			// pedimos los canales de ese día del sintX

			qs = "?auto=si&"+CommonSINT.PFASE+"=12&"+PDIA+"="+diaActual+"&p="+passwdAlu;
			call = servicioAluP+qs;

			ArrayList<Canal> xCanales;
			try {
				xCanales = requestCanalesDia(call);
			}
			catch (ExcepcionChecker e) {
				cf = e.getCheckerFailure();
				cf.setCodigo("20_DIFS");
				cf.addMotivo("correctC1OneStudent: Excepción solicitando la lista de canales de "+diaActual+" a "+usuario);
				throw new ExcepcionChecker(cf);
			}


			// comparamos las listas de sintprof y sintX

			try {
				Query1.comparaCanales(usuario, diaActual, pCanales, xCanales);
			}
			catch (ExcepcionChecker e) {
				cf = e.getCheckerFailure();
				cf.setUrl(call);
				cf.addMotivo("correctC1OneStudent: Diferencias en la lista de canales de "+diaActual);
				throw new ExcepcionChecker(cf);
			}



	        // las listas de canales para este día son iguales
		    // si este día es el de la consulta directa final, elegimos un canal al azar para la consulta directa final


	        if (diaActual.equals(dqDia)) {
	            posrandom = SintRandom.getRandomNumber(0, pCanales.size()-1);
	            Canal pCan = pCanales.get(posrandom);
	          	dqCanal = pCan.getNombreCanal();
	        }



	        // vamos con la tercera fase, las películas de un canal
	        // el bucle Y recorre todos los canales

	        Canal canalActual;

	        for (int y=0; y < pCanales.size(); y++) {

	        	canalActual = pCanales.get(y);

	    		// pedimos las películas de ese canal de ese día de sintprof

	    		qs = "?auto=si&"+CommonSINT.PFASE+"=13&"+PDIA+"="+diaActual+"&"+PCANAL+"="+canalActual.getNombreCanal()+"&p="+CommonSINT.PPWD;    // necesario URL-encoded??
	    		call = CommonTVChecker.servicioProf+qs;

	    		ArrayList<Programa> pPeliculas;
	    		try {
	    			pPeliculas = requestPeliculasCanalDia(call);
	    		}
	    		catch (ExcepcionChecker e) {
	    			cf = e.getCheckerFailure();
					cf.setCodigo("20_DIFS");
					cf.addMotivo("correctC1OneStudent: ExcepcionChecker solicitando la lista de películas a sintprof");
					throw new ExcepcionChecker(cf);
	    		}


	    		// pedimos las películas de ese canal de ese día del sintX

	    		qs = "?auto=si&"+CommonSINT.PFASE+"=13&"+PDIA+"="+diaActual+"&"+PCANAL+"="+canalActual.getNombreCanal()+"&p="+passwdAlu;    // necesario URL-encoded??
	    		call = servicioAluP+qs;

	    		ArrayList<Programa> xPeliculas;
	    		try {
	    			xPeliculas = requestPeliculasCanalDia(call);
	    		}
	    		catch (ExcepcionChecker e) {
	    			cf = e.getCheckerFailure();
					cf.setCodigo("20_DIFS");
					cf.addMotivo("correctC1OneStudent: ExcepcionChecker solicitando la lista de películas a "+usuario);
					throw new ExcepcionChecker(cf);
	    		}


	    		// comparamos las listas de sintprof y sintX

	    		try {
	    			Query1.comparaPeliculas(usuario, diaActual, canalActual.getNombreCanal(), pPeliculas, xPeliculas);
				}
				catch (ExcepcionChecker e) {
					cf = e.getCheckerFailure();
					cf.setUrl(call);
					cf.addMotivo("correctC1OneStudent: Diferencias en la lista de películas");
					throw new ExcepcionChecker(cf);
				}


	        } // for y

	    } // for x


		// finalmente la consulta directa

		try {
			Query1.checkDirectQueryC1(CommonTVChecker.servicioProf, usuario, servicioAluP, dqDia, dqCanal, passwdAlu);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("correctC1OneStudent: Resultado erróneo en la consulta directa");
			throw new ExcepcionChecker(cf);
		}

		// todas las consultas coincidieron

	    return;
	}









	// comprueba que las consultas directas son iguales

	private static void checkDirectQueryC1(String servicioProf, String usuario, String servicioAluP, String dia, String canal, String passwdAlu)
		throws ExcepcionChecker
	{
		CheckerFailure cf;
		ArrayList<Programa> pPelis, xPelis;

		// primero comprobamos que responde con el error apropiado si falta algún parámetro

  		try {
  			CommonTVChecker.checkLackParam(servicioAluP, passwdAlu, "13", PDIA, dia, PCANAL, canal);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("checkDirectQueryC1: No responde correctamente si falta algún parámetro obligatorio");
			throw new ExcepcionChecker(cf);
		}


 		// ahora comprobamos que los resultados son correctos

		String qs = "?auto=si&"+CommonSINT.PFASE+"=13&"+PDIA+"="+dia+"&"+PCANAL+"="+canal+"&p="+CommonSINT.PPWD;     // necesario URL-encoded, en otras consultas puede que si
	    String call = CommonTVChecker.servicioProf+qs;

   		try {
   			pPelis = Query1.requestPeliculasCanalDia(call);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("checkDirectQueryC1: ExcepcionChecker al pedir la consulta directa a sintprof");
			throw new ExcepcionChecker(cf);
		}

		qs = "?auto=si&"+CommonSINT.PFASE+"=13&"+PDIA+"="+dia+"&"+PCANAL+"="+canal+"&p="+passwdAlu;     // no es necesario URL-encoded
	    call = servicioAluP+qs;

  		try {
  			xPelis = Query1.requestPeliculasCanalDia(call);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("checkDirectQueryC1: ExcepcionChecker al pedir la consulta directa a "+usuario);
			throw new ExcepcionChecker(cf);
		}


		// comparamos las listas de peliculas resultado de sintprof y sintX

		try {
			Query1.comparaPeliculas(usuario, dia, canal, pPelis, xPelis);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setUrl(call);
			cf.addMotivo("checkDirectQueryC1: Diferencias en la lista de películas resultado");
			throw new ExcepcionChecker(cf);
		}


		// todo coincidió

		return;
	}












	// COMPROBACIÓN DEL SERVICIO DE TODOS LOS ESTUDIANTES

	// pantalla para comprobar todos los estudiantes, se pide el número de cuentas a comprobar (corregir su práctica)

	public static void doGetC1CorrectAllForm (HttpServletRequest request, PrintWriter out)
	{
		int esProfesor = 1;

		out.println("<html>");
		CommonTVChecker.printHead(out);
		CommonTVChecker.printBodyHeader(out);

		out.println("<h2>Consulta 1</h2>");
		out.println("<h3>Corrección de todos los servicios</h3>");

		out.println("<form>");
		out.println("<input type='hidden' name='screenP' value='131'>");

		out.println("Introduzca el número de cuentas SINT a corregir: ");
		out.println("<input id='inputNumCuentas' type='text' name='numCuentasP' size='3' pattern='[1-9]([0-9]{1,2})?' required>");

		if (esProfesor == 1)
			out.println("<p><input type='hidden' name='p' value='si'>");

		out.println("<input class='enviar' type='submit' value='Enviar' >");
		out.println("</form>");

		out.println("<form>");
		if (esProfesor == 1)
			out.println("<p><input type='hidden' name='p' value='si'>");
		out.println("<p><input class='home' type='submit' value='Inicio'>");
		out.println("</form>");

		CommonSINT.printFoot(out, CommonTVChecker.CREATED);

		out.println("</body></html>");
	}




	// pantalla para corregir a todos los estudiantes
	// presenta en pantalla diversas listas según el resultado de cada alumno
	// se crea un fichero con el resultado de cada corrección (webapps/CORRECCIONES/sintX/fecha-corrección)
	// se devuelven enlaces a esos ficheros

	public static void doGetC1CorrectAllReport(HttpServletRequest request, PrintWriter out)
						throws IOException, ServletException
	{
		int esProfesor = 1;

		out.println("<html>");
		CommonTVChecker.printHead(out);
		CommonTVChecker.printBodyHeader(out);

		out.println("<h2>Consulta 1</h2>");

		// si no se recibe el parámetro con el número de cuentas no se hace nada

		String numCuentasP = request.getParameter("numCuentasP");
		if (numCuentasP == null) {
			out.println("<h4>Error: no se ha recibido el número de cuentas</h4>");
			CommonSINT.printEndPageChecker(out,  "13", esProfesor, CommonTVChecker.CREATED);
			return;
		}

		int numCuentas=0;

		try {
			numCuentas = Integer.parseInt(numCuentasP);
		}
		catch (NumberFormatException e) {
			out.println("<h4>Error: el número de cuentas recibido no es un número válido</h4>");
			CommonSINT.printEndPageChecker(out,  "13", esProfesor, CommonTVChecker.CREATED);
			return;
		}

		if (numCuentas < 1) {
			out.println("<h4>Error: el número de cuentas recibido es menor que uno</h4>");
			CommonSINT.printEndPageChecker(out,  "13", esProfesor, CommonTVChecker.CREATED);
			return;
		}





		// todos los parámetros están bien


		out.println("<h3>Corrección de todos los servicios ("+numCuentas+")</h3>");


		// listas para almacenar en qué caso está cada alumno

		ArrayList<Integer> usersOK = new ArrayList<Integer>();	   // corrección OK

		ArrayList<Integer> usersE1NoContext= new ArrayList<Integer>();       // contexto no existe

		ArrayList<Integer> usersE2FileNotFound= new ArrayList<Integer>();    // servlet no declarado
		ArrayList<Integer> usersE3Encoding = new ArrayList<Integer>();       // respuesta mala codificación
		ArrayList<Integer> usersE4IOException= new ArrayList<Integer>();    // falta clase servlet o este produce una excepción
		ArrayList<Integer> usersE5Bf = new ArrayList<Integer>();            // respuesta mal formada
		ArrayList<Integer> usersE6Invalid = new ArrayList<Integer>();       // respuesta inválida
		ArrayList<Integer> usersE7Error = new ArrayList<Integer>();         // error desconocido
		ArrayList<Integer> usersE8OkNoPasswd = new ArrayList<Integer>();    // responde bien sin necesidad de passwd
		ArrayList<Integer> usersE9BadPasswd = new ArrayList<Integer>();    // la passwd es incorrecta
		ArrayList<Integer> usersE10BadAnswer = new ArrayList<Integer>();    // la respuesta no es la esperada
		ArrayList<Integer> usersE11NoPasswd = new ArrayList<Integer>();    // el usuario no tiene passwd
		ArrayList<Integer> usersE12Files = new ArrayList<Integer>();    // el usuario no tiene passwd

		ArrayList<Integer> usersE20Diff = new ArrayList<Integer>();	   // las peticiones del alumno tienen diferencias respecto a las del profesor

		String servicioAlu;

		// lista para almacenar el nombre del fichero de cada cuenta

		ArrayList<String> usersCompareResultFile = new ArrayList<String>();

		// variables para crear y escribir los ficheros
		File  folder, fileUser;
		BufferedWriter bw;
		Date fecha;

		// si no existe, se crea el directorio de las CORRECCIONES

		String correccionesPath = CommonTVChecker.servletContextSintProf.getRealPath("/")+"CORRECCIONES";

		folder = new File(correccionesPath);
		if (!folder.exists())
			folder.mkdir();

		// vamos a por las cuentas, de una en una

		String sintUser;

		bucle:
		for (int x=1; x <= numCuentas; x++) {

			sintUser="sint"+x;

			// si no existe, se crea el directorio del alumno

			folder = new File(correccionesPath+"/"+sintUser);
			if (!folder.exists())
				folder.mkdir();

			// se crea el fichero donde se almacenará esta corrección

			fecha = new Date();
			String nombreFicheroCorreccion = correccionesPath+"/"+sintUser+"/"+fecha.toString();
			fileUser = new File(nombreFicheroCorreccion);
			usersCompareResultFile.add(nombreFicheroCorreccion);
			bw = new BufferedWriter(new FileWriter(fileUser));


			// Comienza la comprobación del alumno


			// leemos la passwd del alumno

		        String passwdAlu;

			try {
				passwdAlu = CommonTVChecker.getAluPasswd(sintUser);
			}
			catch (ExcepcionSINT ex) {
				if (ex.getMessage().equals("NOCONTEXT")) {
					bw.write("No hay contexto");
					usersE1NoContext.add(x);
				}
				else {
					bw.write("No hay passwd");
					usersE11NoPasswd.add(x);
				}
				bw.close();
				continue bucle;
			}


			servicioAlu = "http://"+CommonTVChecker.server_port+"/"+sintUser+CommonTVChecker.SERVICE_NAME;

			try {
				Query1.correctC1OneStudent(request, sintUser, Integer.toString(x), servicioAlu, passwdAlu);
				bw.write("OK");
				bw.close();
				usersOK.add(x);
			}
			catch (ExcepcionChecker e) {
				CheckerFailure cf = e.getCheckerFailure();

			    switch (cf.getCodigo()) {

				case "01_NOCONTEXT":   // el contexto no está declarado o no existe su directorio
					bw.write(cf.toString());
					bw.close();
					usersE1NoContext.add(x);
					continue bucle;
				case "02_FILENOTFOUND":   // el servlet no está declarado.
					bw.write(cf.toString());
					bw.close();
					usersE2FileNotFound.add(x);
					continue bucle;
				case "03_ENCODING":   // la secuencia de bytes recibida UTF-8 está malformada
					bw.write(cf.toString());
					bw.close();
					usersE3Encoding.add(x);
					continue bucle;
				case "04_IOEXCEPTION":    // la clase del servlet no está o produjo una excepción
					bw.write(cf.toString());
					bw.close();
					usersE4IOException.add(x);
					continue bucle;
				case "05_BF":   // la respuesta no es well-formed
					bw.write(cf.toString());
					bw.close();
					usersE5Bf.add(x);
					continue bucle;
				case "06_INVALID":   // la respuesta es inválida
					bw.write(cf.toString());
					bw.close();
					usersE6Invalid.add(x);
					continue bucle;
				case "07_ERRORUNKNOWN":   // error desconocido
					bw.write(cf.toString());
					bw.close();
					usersE7Error.add(x);
					continue bucle;
				case "08_OKNOPASSWD":   // responde bien incluso sin passwd
					bw.write(cf.toString());
					bw.close();
					usersE8OkNoPasswd.add(x);
					continue bucle;
				case "09_BADPASSWD":   // la passwd es incorrecta
					bw.write(cf.toString());
					bw.close();
					usersE9BadPasswd.add(x);
					continue bucle;
				case "10_BADANSWER":   // la respuesta es inesperada
					bw.write(cf.toString());
					bw.close();
					usersE10BadAnswer.add(x);
					continue bucle;
			    case "12_FILES":
					bw.write(cf.toString());
					bw.close();
					usersE12Files.add(x);
					continue bucle;
				case "20_DIFS":
					bw.write(cf.toString());
					bw.close();
					usersE20Diff.add(x);
					continue bucle;
				default:      // error desconocido
					bw.write("Respuesta desconocida de la corrección:\n"+cf.toString());
					bw.close();
					usersE7Error.add(x);
					continue bucle;
			   } // switch
			}  // catch
		} // for

		// Breve resumen de los resultados por pantalla, con enlaces a los ficheros

		int numAlu;
		String fileAlu;

		if (usersOK.size() >0) {
			out.print("<h4 style='color: green'>Servicios OK ("+usersOK.size()+"): ");
			for (int x=0; x < usersOK.size(); x++) {
				numAlu = usersOK.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE12Files.size() >0) {
			out.print("<h4 style='color: red'>Servicios con errores en los ficheros ("+usersE12Files.size()+"): ");
			for (int x=0; x < usersE12Files.size(); x++) {
				numAlu = usersE12Files.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE20Diff.size() >0) {
			out.print("<h4 style='color: red'>Servicios con diferencias respecto a los resultados esperados ("+usersE20Diff.size()+"): ");
			for (int x=0; x < usersE20Diff.size(); x++) {
				numAlu = usersE20Diff.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE10BadAnswer.size() >0) {
			out.print("<h4 style='color: red'>Servicios que responden de forma inesperada ("+usersE10BadAnswer.size()+"): ");
			for (int x=0; x < usersE10BadAnswer.size(); x++) {
				numAlu = usersE10BadAnswer.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE9BadPasswd.size() >0) {
			out.print("<h4 style='color: red'>Servicios que no reconocen la passwd ("+usersE9BadPasswd.size()+"): ");
			for (int x=0; x < usersE9BadPasswd.size(); x++) {
				numAlu = usersE9BadPasswd.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE8OkNoPasswd.size() >0) {
			out.print("<h4 style='color: red'>Servicios que responden bien sin necesidad de passwd ("+usersE8OkNoPasswd.size()+"): ");
			for (int x=0; x < usersE8OkNoPasswd.size(); x++) {
				numAlu = usersE8OkNoPasswd.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE6Invalid.size() >0) {
			out.print("<h4 style='color: red'>Servicios con respuesta inválida a la solicitud de estado ("+usersE6Invalid.size()+"): ");
			for (int x=0; x < usersE6Invalid.size(); x++) {
				numAlu = usersE6Invalid.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE5Bf.size() >0) {
			out.print("<h4 style='color: red'>Servicios con respuesta mal formada a la solicitud de estado ("+usersE5Bf.size()+"): ");
			for (int x=0; x < usersE5Bf.size(); x++) {
				numAlu = usersE5Bf.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE4IOException.size() >0) {
			out.print("<h4 style='color: red'>Servicios donde falta la clase del servlet o éste produjo una excepción ("+usersE4IOException.size()+"): ");
			for (int x=0; x < usersE4IOException.size(); x++) {
				numAlu = usersE4IOException.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE3Encoding.size() >0) {
			out.print("<h4 style='color: red'>Servicios que responden con una codificación incorrecta a la solicitud de estado ("+usersE3Encoding.size()+"): ");
			for (int x=0; x < usersE3Encoding.size(); x++) {
				numAlu = usersE3Encoding.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE2FileNotFound.size() >0) {
			out.print("<h4 style='color: red'>Servicios sin el servlet declarado ("+usersE2FileNotFound.size()+"): ");
			for (int x=0; x < usersE2FileNotFound.size(); x++) {
				numAlu = usersE2FileNotFound.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE11NoPasswd.size() >0) {
			out.print("<h4 style='color: red'>Servicios que no tienen passwd ("+usersE11NoPasswd.size()+"): ");
			for (int x=0; x < usersE11NoPasswd.size(); x++) {
				numAlu = usersE11NoPasswd.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE1NoContext.size() >0) {
			out.print("<h4 style='color: red'>Servicios sin contexto ("+usersE1NoContext.size()+"): ");
			for (int x=0; x < usersE1NoContext.size(); x++) {
				numAlu = usersE1NoContext.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		if (usersE7Error.size() >0) {
			out.print("<h4 style='color: red'>Servicios con algún error desconocido ("+usersE7Error.size()+"): ");
			for (int x=0; x < usersE7Error.size(); x++) {
				numAlu = usersE7Error.get(x);
				fileAlu = usersCompareResultFile.get(numAlu-1);
				out.print("<a href='?screenP=4&file="+fileAlu+"'>"+numAlu+"</a> ");
			}
			out.print("</h4>");
		}

		CommonSINT.printEndPageChecker(out,  "13", esProfesor, CommonTVChecker.CREATED);
	}


    // para corregir todos los servicios uno a uno
	public static void doGetC1CorrectAllForm2 (HttpServletRequest request, PrintWriter out)
	{
		int esProfesor = 1;

		out.println("<html>");
		CommonTVChecker.printHead(out);
		CommonTVChecker.printBodyHeader(out);

		out.println("<h2>Consulta 1</h2>");
		out.println("<h3>Corrección de todos los servicios uno a uno</h3>");

		out.println("<form>");
		out.println("<input type='hidden' name='screenP' value='141'>");

		out.println("Introduzca el número de cuentas SINT a corregir: ");
		out.println("<input id='inputNumCuentas' type='text' name='numCuentasP' size='3' pattern='[1-9]([0-9]{1,2})?' required>");

		if (esProfesor == 1)
			out.println("<p><input type='hidden' name='p' value='si'>");

		out.println("<input class='enviar' type='submit' value='Enviar' >");
		out.println("</form>");

		out.println("<form>");
		if (esProfesor == 1)
			out.println("<p><input type='hidden' name='p' value='si'>");
		out.println("<p><input class='home' type='submit' value='Inicio'>");
		out.println("</form>");

		CommonSINT.printFoot(out, CommonTVChecker.CREATED);

		out.println("</body></html>");
	}


	public static void doGetC1CorrectAllReport2(HttpServletRequest request, HttpServletResponse response)
						throws IOException, ServletException
	{
	    PrintWriter out;
		int esProfesor = 1;

		// si no se recibe el parámetro con el número de cuentas no se hace nada

		String numCuentasP = request.getParameter("numCuentasP");

		if (numCuentasP == null) {
		   out = response.getWriter();
		   out.println("<html>");
		   CommonTVChecker.printHead(out);
		   CommonTVChecker.printBodyHeader(out);

		   out.println("<h4>Error: no se ha recibido el número de cuentas</h4>");
		   CommonSINT.printEndPageChecker(out,  "14", esProfesor, CommonTVChecker.CREATED);
		   return;
		}

		int numCuentas=0;

		try {
		   numCuentas = Integer.parseInt(numCuentasP);
		}
		catch (NumberFormatException e) {
		   out = response.getWriter();
		   out.println("<html>");
		   CommonTVChecker.printHead(out);
		   CommonTVChecker.printBodyHeader(out);

		   out.println("<h4>Error: el número de cuentas recibido no es un número válido</h4>");
		   CommonSINT.printEndPageChecker(out,  "14", esProfesor, CommonTVChecker.CREATED);
		   return;
		}


		if (numCuentas < 1) {
		   out = response.getWriter();
		   out.println("<html>");
		   CommonTVChecker.printHead(out);
		   CommonTVChecker.printBodyHeader(out);

		   out.println("<h4>Error: el número de cuentas recibido es menor que uno</h4>");
		   CommonSINT.printEndPageChecker(out,  "14", esProfesor, CommonTVChecker.CREATED);
		   return;
		}


		BeanResultados miBean = new BeanResultados();
		miBean.setCssFile(CommonTVChecker.CSS_FILE);
		miBean.setTitle(CommonTVChecker.MSG_TITLE);
		miBean.setLang(MsgsTVML.LANGUAGE);
		miBean.setCurso(MsgsTVML.CURSO);
		miBean.setNumCuentas(numCuentas);
		miBean.setEsProfesor(esProfesor);
		miBean.setCreated(CommonTVChecker.CREATED);

		request.setAttribute("db", miBean);

		try {
		    // ServletContext sc = getServletContext();

		    RequestDispatcher dispatcher = CommonTVChecker.servletContextSintProf.getRequestDispatcher("/InformeResultadosCorreccionC1.jsp");
		    dispatcher.forward(request, response);
		}
		catch (Exception s) {
		   out = response.getWriter();
		   out.println("<html>");
		   CommonTVChecker.printHead(out);
		   CommonTVChecker.printBodyHeader(out);

		   out.println("<h4>Error: Exception"+s.toString()+"</h4");
		   CommonSINT.printEndPageChecker(out,  "14", esProfesor, CommonTVChecker.CREATED);
		   return;
		}


	}



	public static void doGetC1CorrectAllReport2Run(HttpServletRequest request, HttpServletResponse response)
						throws IOException, ServletException
	{

	    response.setContentType("text/event-stream");
	    PrintWriter out = response.getWriter();

	    out.write("retry: -1\n");

	    String numCuentasP = request.getParameter("numCuentasP");
	    if (numCuentasP == null) {
		out.write("data: error no hay numCuentasP\n\n");
		out.close();
		return;
	    }

	    int numCuentas=0;

	    try {
		 numCuentas = Integer.parseInt(numCuentasP);
	    }
	    catch (NumberFormatException e) {
		out.write("data: error numCuentasP no es entero\n\n");
		out.close();
		return;
	    }

	   if (numCuentas < 1) {
		out.write("data: error numCuentasP es menor que uno\n\n");
		out.close();
		return;
	   }


	    String servicioAlu, sintUser, passwdAlu;

	    bucle:
	    for (int x=1; x <= numCuentas; x++) {
		out.flush();
		sintUser="sint"+x;

		try {
		    passwdAlu = CommonTVChecker.getAluPasswd(sintUser);
		}
		catch (ExcepcionSINT ex) {
		    if (ex.getMessage().equals("NOCONTEXT")) {
			out.write("data: "+x+",NOCONTEXT\n\n");   // usersE1NoContext.add(x);
		    }
		    else {
		        out.write("data: "+x+",NOPASSWD\n\n"); //  usersE11NoPasswd.add(x);
		    }
		    continue bucle;
		}


		servicioAlu = "http://"+CommonTVChecker.server_port+"/"+sintUser+CommonTVChecker.SERVICE_NAME;

		try {
		    Query1.correctC1OneStudent(request, sintUser, Integer.toString(x), servicioAlu, passwdAlu);
		    out.write("data: "+x+",OK\n\n"); //   usersOK.add(x);
		}
		catch (ExcepcionChecker e) {
		    CheckerFailure cf = e.getCheckerFailure();

	            switch (cf.getCodigo()) {

			case "01_NOCONTEXT":   // el contexto no está declarado o no existe su directorio
			    out.write("data: "+x+",NOCONTEXT\n\n");  //  usersE1NoContext.add(x);
			    continue bucle;
			case "02_FILENOTFOUND":   // el servlet no está declarado.
			    out.write("data: "+x+",FILENOTFOUND\n\n");   //  usersE2FileNotFound.add(x);
			    continue bucle;
			case "03_ENCODING":   // la secuencia de bytes recibida UTF-8 está malformada
			    out.write("data: "+x+",ENCODING\n\n");    // usersE3Encoding.add(x);
			    continue bucle;
			case "04_IOEXCEPTION":    // la clase del servlet no está o produjo una excepción
			    out.write("data: "+x+",IOEXCEPTION\n\n");     // usersE4IOException.add(x);
			    continue bucle;
			case "05_BF":   // la respuesta no es well-formed
			    out.write("data: "+x+",BF\n\n");       // usersE5Bf.add(x);
			    continue bucle;
			case "06_INVALID":   // la respuesta es inválida
			    out.write("data: "+x+",INVALID\n\n");     // usersE6Invalid.add(x);
			    continue bucle;
			case "07_ERRORUNKNOWN":   // error desconocido
			    out.write("data: "+x+",ERRORUNKNOWN\n\n");    //  usersE7Error.add(x);
			    continue bucle;
			case "08_OKNOPASSWD":   // responde bien incluso sin passwd
			    out.write("data: "+x+",OKNOPASSWD\n\n");     //   usersE8OkNoPasswd.add(x);
			    continue bucle;
			case "09_BADPASSWD":   // la passwd es incorrecta
			    out.write("data: "+x+",BADPASSWD\n\n");    //   usersE9BadPasswd.add(x);
			    continue bucle;
			case "10_BADANSWER":   // la respuesta es inesperada
			    out.write("data: "+x+",BADANSWER\n\n");     //  usersE10BadAnswer.add(x);
			    continue bucle;
			case "12_FILES":
			    out.write("data: "+x+",FILES\n\n");    // usersE12Files.add(x);
			    continue bucle;
			case "20_DIFS":
			    out.write("data: "+x+",DIFS\n\n");   // usersE20Diff.add(x);
			    continue bucle;
			default:      // error desconocido
			    out.write("data: "+x+",??\n\n");   // usersE7Error.add(x);
			    continue bucle;
		    } // switch
		 }  // catch
	      } // for

	      out.write("data: -1,FIN\n\n");
	      out.close();

	}











	// Métodos auxiliares para la correción de un alumno de la consulta 1


	// pide y devuelve la lista de días de un usuario
	// levanta ExcepcionChecker si algo va mal

	private static ArrayList<String> requestDias (String call)
									throws ExcepcionChecker
	{
		CheckerFailure cf;
		Document doc;
		ArrayList<String> listaDias = new ArrayList<String>();

		CommonTVChecker.errorHandler.clear();

		try {
			doc = CommonTVChecker.db.parse(call);
		}
		catch (SAXException ex) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo("requestDias: SAXException al solicitar y parsear la lista de días");
			throw new ExcepcionChecker(cf);
		}
		catch (Exception ex) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo("requestDias: Exception al solicitar y parsear la lista de días");
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
			cf.addMotivo("requestDias: La lista de días es inválida, tiene errors");
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
			cf.addMotivo("requestDias: La lista de días es inválida, tiene fatal errors");
			throw new ExcepcionChecker(cf);
		}

		if (doc == null) {
			cf = new CheckerFailure(call, "", "requestDias: Se recibe 'null' al solicitar y parsear la lista de días");
			throw new ExcepcionChecker(cf);
		}


		NodeList nlDias = doc.getElementsByTagName("dias");

		if (nlDias.getLength() != 1) {
		    	cf = new CheckerFailure(call, "", "requestDias: No se recibe '&lt;dias>' al solicitar y parsear la lista de días");
			throw new ExcepcionChecker(cf);
		}

		nlDias = doc.getElementsByTagName("dia");

		// procesamos todos los días

		for (int x=0; x < nlDias.getLength(); x++) {
			Element elemDia = (Element)nlDias.item(x);
			String dia = elemDia.getTextContent().trim();

			listaDias.add(dia);
		}

		return listaDias;
	}


	// para comparar el resultado de la F11: listas de días
	// no devuelve nada si son iguales
	// levanta ExcepcionChecker si hay diferencias

	private static void comparaDias (String usuario, ArrayList<String> pDias, ArrayList<String> xDias)
			throws ExcepcionChecker
	{
		CheckerFailure cf;

		if (pDias.size() != xDias.size()) {
			cf = new CheckerFailure("", "20_DIFS", "comparaDias: Debería devolver "+pDias.size()+" días, pero devuelve "+xDias.size());
			throw new ExcepcionChecker(cf);
		}


		for (int x=0; x < pDias.size(); x++)
			if (!xDias.get(x).equals(pDias.get(x))) {
				cf = new CheckerFailure("", "20_DIFS", "comparaDias: El día número "+x+" debería ser '"+pDias.get(x)+"', pero es '"+xDias.get(x)+"'");
				throw new ExcepcionChecker(cf);
			}

		return;
	}





	// pide y devuelve la lista de canales de un día
	// levanta ExcepcionChecker si algo va mal

	private static ArrayList<Canal> requestCanalesDia (String call)
					throws ExcepcionChecker
	{
		CheckerFailure cf;
		Document doc;
		ArrayList<Canal> listaCanales = new ArrayList<Canal>();

		CommonTVChecker.errorHandler.clear();

		try {
			doc = CommonTVChecker.db.parse(call);
		}
		catch (SAXException ex) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo("requestCanalesDia: SAXException al solicitar y parsear la lista de canales");
			throw new ExcepcionChecker(cf);
		}
		catch (Exception ex) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo("requestCanalesDia: Exception al solicitar y parsear la lista de canales");
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
			cf.addMotivo("requestCanalesDia: La lista de canales es inválida, tiene errors");
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
			cf.addMotivo("requestCanalesDia: La lista de canales es inválida, tiene fatal errors");
			throw new ExcepcionChecker(cf);
		}


		if (doc == null) {
			cf = new CheckerFailure(call, "", "requestCanalesDia: Se recibe 'null' al solicitar y parsear la lista de canales");
			throw new ExcepcionChecker(cf);
		}


		NodeList nlCanales = doc.getElementsByTagName("canales");

		if (nlCanales.getLength() != 1) {
		    	cf = new CheckerFailure(call, "", "requestCanalesDia: No se recibe '&lt;canales>' al solicitar y parsear la lista de canales");
			throw new ExcepcionChecker(cf);
		}

		nlCanales = doc.getElementsByTagName("canal");

		// procesamos todos los canales

		for (int x=0; x < nlCanales.getLength(); x++) {
			Element elemCanal = (Element)nlCanales.item(x);
			String nombreCanal = elemCanal.getTextContent().trim();

			String idioma = elemCanal.getAttribute("idioma");
			String grupo = elemCanal.getAttribute("grupo");

			listaCanales.add(new Canal("", idioma, nombreCanal, grupo));  // no ponemos el id del canal
		}

		return listaCanales;
	}


	// para comparar el resultado de la F12: listas de canales
	// no devuelve nada si son iguales
	// levanta ExcepcionChecker si hay diferencias

	private static void comparaCanales (String usuario, String diaActual, ArrayList<Canal> pCanales, ArrayList<Canal> xCanales)
			throws ExcepcionChecker
	{
		CheckerFailure cf;
		String pNombre, xNombre, pGrupo, xGrupo, pIdioma, xIdioma;

		if (pCanales.size() != xCanales.size()) {
			cf = new CheckerFailure("", "20_DIFS", "comparaCanales: "+usuario+"+"+diaActual+": debería devolver "+pCanales.size()+" canales, pero devuelve "+xCanales.size());
			throw new ExcepcionChecker(cf);
		}

		for (int y=0; y < pCanales.size(); y++) {

			pNombre = pCanales.get(y).getNombreCanal();
			xNombre = xCanales.get(y).getNombreCanal();

			if (!xNombre.equals(pNombre)) {
				cf = new CheckerFailure("", "20_DIFS", "comparaCanales: "+usuario+"+"+diaActual+": el nombre del canal número "+y+" debería ser '<pre>"+pNombre+"</pre>', pero es '<pre>"+xNombre+"</pre>'");
				throw new ExcepcionChecker(cf);
			}

			pGrupo = pCanales.get(y).getGrupo();
			xGrupo = xCanales.get(y).getGrupo();

			if (!xGrupo.equals(pGrupo)) {
				cf = new CheckerFailure("", "20_DIFS", "comparaCanales: "+usuario+"+"+diaActual+": el grupo del canal número "+y+" debería ser '<pre>"+pGrupo+"</pre>', pero es '<pre>"+xGrupo+"</pre>'");
				throw new ExcepcionChecker(cf);
			}

			pIdioma = pCanales.get(y).getIdioma();
			xIdioma = xCanales.get(y).getIdioma();

		   	if (!xIdioma.equals(pIdioma)) {
				cf = new CheckerFailure("", "20_DIFS", "comparaCanales: "+usuario+"+"+diaActual+": el disco número "+y+" debería ser de '"+pIdioma+"', pero es de '"+xIdioma+"'");
				throw new ExcepcionChecker(cf);
		   	}
		}

		return;
	}





	// pide y devuelve la lista de películas de un canal de un día
	// levanta ExcepcionChecker si algo va mal

	private static ArrayList<Programa> requestPeliculasCanalDia (String call)
									throws ExcepcionChecker
	{
		CheckerFailure cf;
		Document doc;
		ArrayList<Programa> listaPeliculas = new ArrayList<Programa>();

		CommonTVChecker.errorHandler.clear();

		try {
			doc = CommonTVChecker.db.parse(call);
		}
		catch (SAXException e) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo("requestPeliculasCanalDia: SAXException al solicitar y parsear la lista de peliculas");
			throw new ExcepcionChecker(cf);
		}
		catch (Exception e) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo("requestPeliculasCanalDia: Exception al solicitar y parsear la lista de peliculas");
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
			cf.addMotivo("requestPeliculasCanalDia: La lista de peliculas es inválida, tiene errors");
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
			cf.addMotivo("requestPeliculasCanalDia: La lista de peliculas es inválida, tiene fatal errors");
			throw new ExcepcionChecker(cf);
		}


		if (doc == null) {
			cf = new CheckerFailure(call, "", "requestPeliculasCanalDia: Se recibe 'null' al solicitar y parsear la lista de peliculas");
			throw new ExcepcionChecker(cf);
		}

		NodeList nlPeliculas = doc.getElementsByTagName("peliculas");

		if (nlPeliculas.getLength() != 1) {
		    	cf = new CheckerFailure(call, "", "requestPeliculasCanalDia: No se recibe '&lt;peliculas>' al solicitar y parsear la lista de peliculas");
			throw new ExcepcionChecker(cf);
		}

		nlPeliculas = doc.getElementsByTagName("pelicula");

		// procesamos todas las peliculas

		for (int x=0; x < nlPeliculas.getLength(); x++) {
			Element elemPelicula = (Element)nlPeliculas.item(x);

			String titulo = elemPelicula.getTextContent().trim();

			String edad = elemPelicula.getAttribute("edad");
			String hora = elemPelicula.getAttribute("hora");
			String resumen = elemPelicula.getAttribute("resumen");

			listaPeliculas.add(new Programa(edad, "", resumen, titulo, "", hora, ""));   // no ponemos langs, ni categoria, ni día de emisión
		}

		return listaPeliculas;
	}



	// para comparar el resultado de la F13: listas de peliculas
	// no devuelve nada si son iguales
	// levanta ExcepcionChecker si hay diferencias

	private static void comparaPeliculas (String usuario, String diaActual, String canalActual, ArrayList<Programa> pPeliculas, ArrayList<Programa> xPeliculas)
				throws ExcepcionChecker
	{
		CheckerFailure cf;

	    if (pPeliculas.size() != xPeliculas.size()) {
			cf = new CheckerFailure("", "20_DIFS", "comparaPeliculas: "+usuario+"+"+diaActual+"+"+canalActual+": debería devolver '"+pPeliculas.size()+"' películas, pero devuelve '"+xPeliculas.size()+"'");
			throw new ExcepcionChecker(cf);
	    }

	    for (int z=0; z < pPeliculas.size(); z++) {
	    	    if (!xPeliculas.get(z).getNombrePrograma().equals(pPeliculas.get(z).getNombrePrograma())) {
	    			cf = new CheckerFailure("", "20_DIFS", "comparaPeliculas: "+usuario+"+"+diaActual+"+"+canalActual+": el título de la película número "+z+" debería ser '<pre>"+pPeliculas.get(z).getNombrePrograma()+"</pre>', pero es '<pre>"+xPeliculas.get(z).getNombrePrograma()+"</pre>'");
	    			throw new ExcepcionChecker(cf);
	    	    }

	    	    if (!xPeliculas.get(z).getEdadminima().equals(pPeliculas.get(z).getEdadminima())) {
	    			cf = new CheckerFailure("", "20_DIFS", "comparaPeliculas: "+usuario+"+"+diaActual+"+"+canalActual+": la edad mínima de la película número "+z+" debería ser '<pre>"+pPeliculas.get(z).getEdadminima()+"</pre>', pero es '<pre>"+xPeliculas.get(z).getEdadminima()+"</pre>'");
	    			throw new ExcepcionChecker(cf);
	    	    }

	    	    if (!xPeliculas.get(z).getHoraInicio().equals(pPeliculas.get(z).getHoraInicio())) {
	    			cf = new CheckerFailure("", "20_DIFS", "comparaPeliculas: "+usuario+"+"+diaActual+"+"+canalActual+": la hora de inicio de la película número "+z+" debería ser '<pre>"+pPeliculas.get(z).getHoraInicio()+"</pre>', pero es '<pre>"+xPeliculas.get(z).getHoraInicio()+"</pre>'");
	    			throw new ExcepcionChecker(cf);
	    	    }

	    	    if (!xPeliculas.get(z).getResumen().equals(pPeliculas.get(z).getResumen())) {
	    			cf = new CheckerFailure("", "20_DIFS", "comparaPeliculas: "+usuario+"+"+diaActual+"+"+canalActual+": el resumen de la película número "+z+" debería ser '<pre>"+pPeliculas.get(z).getResumen()+"</pre>', pero es '<pre>"+xPeliculas.get(z).getResumen()+"</pre>'");
	    			throw new ExcepcionChecker(cf);
	    	    }
	    }

	    return;
	}



}
