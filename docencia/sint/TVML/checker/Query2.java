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

// Implementación de la comprobación de la consulta 2 (canales con películas en un idioma en un día que hay películas infantiles en ese idioma)

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


public class Query2 {


	// nombres de los parámetros de esta consulta

	static final String PLANG = "plang";
	static final String PDIA = "pdia";


	// COMPROBACIÓN DE LAS LLAMADAS A SINTPROF

	public static void doGetC2CheckSintprofCalls(HttpServletRequest request, PrintWriter out)
						throws IOException, ServletException
	{
		CheckerFailure cf;
		int esProfesor = 1;  // sólo el profesor debería llegar aquí, podemos poner esto a 1

		out.println("<html>");
		CommonTVChecker.printHead(out);
		CommonTVChecker.printBodyHeader(out);

		out.println("<h2>Consulta 2</h2>");
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

		// pedimos la lista de idiomas de sintprof

		String qs = "?auto=si&"+CommonSINT.PFASE+"=21&p="+CommonSINT.PPWD;
		String call = CommonTVChecker.servicioProf+qs;

		ArrayList<String> pLangs;
		try {
			pLangs = Query2.requestLangs(call);
			out.println("<h4>Idiomas OK: "+pLangs.size()+"</h4>");
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			out.println("<h4 style='color: red'>ExcepcionSINT: sintprof (Langs): <br>");
			out.println(cf.toHTMLString());
			out.println("</h4>");
			CommonSINT.printEndPageChecker(out,  "0", esProfesor, CommonTVChecker.CREATED);
			return;
        }



		// vamos con la segunda fase, los programas infantiles de cada idioma
		// el bucle X recorre todos los langs


		String langActual;

		for (int x=0; x < pLangs.size(); x++) {
			String indent ="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

			langActual = pLangs.get(x);

			// pedimos los programas infantiles de ese lang de sintprof

			qs = "?auto=si&"+CommonSINT.PFASE+"=22&"+PLANG+"="+langActual+"&p="+CommonSINT.PPWD;
			call = CommonTVChecker.servicioProf+qs;

			ArrayList<Programa> pProgramas;
			try {
				pProgramas = requestInfantilesLang(call);
				out.println("<h4>"+indent+langActual+": "+pProgramas.size()+"  OK</h4>");
			}
			catch (ExcepcionChecker e) {
				cf = e.getCheckerFailure();
				out.println("<h4 style='color: red'>sintprof (Programas): <br>");
				out.println(cf.toHTMLString());
				out.println("</h4>");
				CommonSINT.printEndPageChecker(out,  "0", esProfesor, CommonTVChecker.CREATED);
				return;
	        }




	        // vamos con la tercera fase, los canales que emiten peliculas en un idioma en un día
	        // el bucle Y recorre todos los programas

	        Programa programaActual;

	        for (int y=0; y < pProgramas.size(); y++) {

		        	indent = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

		        	programaActual = pProgramas.get(y);

		    		// pedimos los canales que emiten películas en un idioma en un día de sintprof

		    		qs = "?auto=si&"+CommonSINT.PFASE+"=23&"+PLANG+"="+langActual+"&"+PDIA+"="+programaActual.getDiaEmision()+"&p="+CommonSINT.PPWD;    // necesario URL-encoded??
		    		call = CommonTVChecker.servicioProf+qs;

		    		ArrayList<Canal> pCanales;
		    		try {
		    			pCanales = requestCanalesLangDia(call);
		    			out.println("<h4>"+indent+langActual+"+"+programaActual.getDiaEmision()+": "+pCanales.size()+"  OK</h4>");
		    		}
		    		catch (ExcepcionChecker e) {
						cf = e.getCheckerFailure();
		    			out.println("<h4 style='color: red'>ExcepcionSINT: sintprof (Canales): <br>");
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
	// debería unificarse en uno sólo con el de la consulta 1, son casi iguales

	public static void doGetC2CorrectOneForm (HttpServletRequest request, PrintWriter out, int esProfesor)
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

		out.println("<h2>Consulta 2</h2>");                               // consulta 2
		out.println("<h3>Corrección de un único servicio</h3>");

		out.println("<form>");
		out.println("<input type='hidden' name='screenP' value='221'>");  // conduce a doGetC2CorrectOneReport

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

	public static void doGetC2CorrectOneReport(HttpServletRequest request, PrintWriter out, int esProfesor)
						throws IOException, ServletException
	{
		out.println("<html>");
		CommonTVChecker.printHead(out);
		CommonTVChecker.printBodyHeader(out);

		out.println("<h2>Consulta 2</h2>");
		out.println("<h3>Corrección de un único servicio</h3>");

		// leemos los datos del estudiante

		String alumnoP = request.getParameter("alumnoP");
		String servicioAluP = request.getParameter("servicioAluP");

		if ((alumnoP == null) || (servicioAluP == null)) {
			out.println("<h4 style='color: red'>Falta uno de los parámetros</h4>");  // si falta algún parámetro no se hace nada
			CommonSINT.printEndPageChecker(out,  "22", esProfesor, CommonTVChecker.CREATED);
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
			CommonSINT.printEndPageChecker(out,  "22", esProfesor, CommonTVChecker.CREATED);
			return;
		}

		if (esProfesor == 0) {  // lo está probando un alumno, es obligatorio que haya introducido su passwd
			passwdRcvd = request.getParameter("passwdAlu");   // leemos la passwd del alumno del parámetro

			if (passwdRcvd == null) {
				out.println("<h4 style='color: red'>No se ha recibido la passwd de "+usuario+"</h4>");
				CommonSINT.printEndPageChecker(out,  "22", esProfesor, CommonTVChecker.CREATED);
				return;
			}

			if (!passwdAlu.equals(passwdRcvd)) {
				out.println("<h4 style='color: red'>La passwd proporcionada no coincide con la almacenada en el sistema para "+usuario+"</h4>");
				CommonSINT.printEndPageChecker(out,  "22", esProfesor, CommonTVChecker.CREATED);
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
			CommonSINT.printEndPageChecker(out,  "22", esProfesor, CommonTVChecker.CREATED);
			return;
		}


		try {
			Query2.correctC2OneStudent(request, usuario, alumnoP, servicioAluP, passwdAlu);
		}
		catch (ExcepcionChecker e) {
			CheckerFailure cf = e.getCheckerFailure();
			out.println("<h4 style='color: red'>Resultado incorrecto para la práctica de "+usuario+" <br>");
			out.println(cf.toHTMLString());
			out.println("</h4>");
			CommonSINT.printEndPageChecker(out,  "22", esProfesor, CommonTVChecker.CREATED);
			return;
		}

		out.println("<h3>Resultado: OK</h3>");



		// terminamos con botones Atrás e Inicio

		CommonSINT.printEndPageChecker(out,  "22", esProfesor, CommonTVChecker.CREATED);
	}




	// método que corrige la consulta 2 de un estudiante

    private static void correctC2OneStudent (HttpServletRequest request,String usuario, String aluNum, String servicioAluP, String passwdAlu)
    			throws ExcepcionChecker
	{
    	CheckerFailure cf;

		// para la consulta directa final, vamos a escoger lang y dia al azar y a guardarlas en esas variables

		SintRandom.init(); // inicializamos el generador de números aleatorios
		int posrandom;

		String dqLang="";
		String dqDia="";

		// empezamos por comprobar los ficheros

		try {
			CommonTVChecker.doOneCheckUpFiles(aluNum, "2");
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
			cf.addMotivo("correctC2OneStudent: Diferencias en la lista de errores");
			throw new ExcepcionChecker(cf);
		}




		// y ahora todas y cada una de las consultas

		// pedimos la lista de langs de sintprof

		String qs = "?auto=si&"+CommonSINT.PFASE+"=21&p="+CommonSINT.PPWD;
		String call = CommonTVChecker.servicioProf+qs;

		ArrayList<String> pLangs;
		try {
			pLangs = Query2.requestLangs(call);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("correctC2OneStudent: ExcepcionChecker solicitando la lista de idiomas a sintprof");
			throw new ExcepcionChecker(cf);
		}


		// pedimos la lista de idiomas del sintX

		qs = "?auto=si&"+CommonSINT.PFASE+"=21&p="+passwdAlu;
		call = servicioAluP+qs;

		ArrayList<String> xLangs;
		try {
			xLangs = Query2.requestLangs(call);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("correctC2OneStudent: ExcepcionChecker solicitando la lista de idiomas a "+usuario);
			throw new ExcepcionChecker(cf);
		}


		// comparamos las listas de sintprof y sintX

		try {
		     Query2.comparaLangs(usuario, pLangs, xLangs);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setUrl(call);
			cf.addMotivo("correctC2OneStudent: Diferencias en la lista de idiomas");
			throw new ExcepcionChecker(cf);
		}



		// las listas de idiomas son iguales
		// elegimos un idioma al azar para la consulta directa final

		posrandom = SintRandom.getRandomNumber(0, pLangs.size()-1);
		dqLang = pLangs.get(posrandom);




		// vamos con la segunda fase, los progamas infantiles de cada idioma
		// el bucle X recorre todos los idiomas


		String langActual;

		for (int x=0; x < pLangs.size(); x++) {

			langActual = pLangs.get(x);

			// pedimos los programas infantiles en ese idioma de sintprof

			qs = "?auto=si&"+CommonSINT.PFASE+"=22&"+PLANG+"="+langActual+"&p="+CommonSINT.PPWD;
			call = CommonTVChecker.servicioProf+qs;

			ArrayList<Programa> pProgramas;
			try {
				pProgramas = Query2.requestInfantilesLang(call);
			}
			catch (ExcepcionChecker e) {
				cf = e.getCheckerFailure();
				cf.setCodigo("20_DIFS");
				cf.addMotivo("correctC2OneStudent: ExcepcionChecker solicitando la lista de programas infantiles en "+langActual+" a sintprof");
				throw new ExcepcionChecker(cf);
			}


			// pedimos los programas infantiles en ese idioma del sintX

			qs = "?auto=si&"+CommonSINT.PFASE+"=22&"+PLANG+"="+langActual+"&p="+passwdAlu;
			call = servicioAluP+qs;

			ArrayList<Programa> xProgramas;
			try {
				xProgramas = Query2.requestInfantilesLang(call);
			}
			catch (ExcepcionChecker e) {
				cf = e.getCheckerFailure();
				cf.setCodigo("20_DIFS");
				cf.addMotivo("correctC2OneStudent: Excepción solicitando la lista de programas infantiles en "+langActual+" a "+usuario);
				throw new ExcepcionChecker(cf);
			}


			// comparamos las listas de sintprof y sintX

			try {
				Query2.comparaInfantiles(usuario, langActual, pProgramas, xProgramas);
			}
			catch (ExcepcionChecker e) {
				cf = e.getCheckerFailure();
				cf.setUrl(call);
				cf.addMotivo("correctC2OneStudent: Diferencias en la lista de programas infantiles de "+langActual);
				throw new ExcepcionChecker(cf);
			}



	        // las listas de programas infantiles en ese idioma son iguales
		    // si este idioma es el de la consulta directa final, elegimos un día al azar para la consulta directa final


	        if (langActual.equals(dqLang)) {
	            posrandom = SintRandom.getRandomNumber(0, pProgramas.size()-1);
	            Programa pProg = pProgramas.get(posrandom);
	          	dqDia = pProg.getDiaEmision();
	        }



	        // vamos con la tercera fase, los canales que emiten películas en un idioma en un día
	        // el bucle Y recorre todos los programas

	        Programa programaActual;

	        for (int y=0; y < pProgramas.size(); y++) {

	        	programaActual = pProgramas.get(y);

	    		// pedimos los canales que emiten películas en ese idioma en ese día de sintprof

	    		qs = "?auto=si&"+CommonSINT.PFASE+"=23&"+PLANG+"="+langActual+"&"+PDIA+"="+programaActual.getDiaEmision()+"&p="+CommonSINT.PPWD;    // necesario URL-encoded??
	    		call = CommonTVChecker.servicioProf+qs;

	    		ArrayList<Canal> pCanales;
	    		try {
	    			pCanales = Query2.requestCanalesLangDia(call);
	    		}
	    		catch (ExcepcionChecker e) {
	    			cf = e.getCheckerFailure();
					cf.setCodigo("20_DIFS");
					cf.addMotivo("correctC2OneStudent: ExcepcionChecker solicitando la lista de canales a sintprof");
					throw new ExcepcionChecker(cf);
	    		}


	    		// pedimos los canales que emiten películas en ese idioma en ese día  del sintX

	    		qs = "?auto=si&"+CommonSINT.PFASE+"=23&"+PLANG+"="+langActual+"&"+PDIA+"="+programaActual.getDiaEmision()+"&p="+passwdAlu;    // necesario URL-encoded??
	    		call = servicioAluP+qs;

	    		ArrayList<Canal> xCanales;
	    		try {
	    			xCanales = Query2.requestCanalesLangDia(call);
	    		}
	    		catch (ExcepcionChecker e) {
	    			cf = e.getCheckerFailure();
					cf.setCodigo("20_DIFS");
					cf.addMotivo("correctC2OneStudent: ExcepcionChecker solicitando la lista de canales a "+usuario);
					throw new ExcepcionChecker(cf);
	    		}


	    		// comparamos las listas de sintprof y sintX

	    		try {
	    			Query2.comparaCanales(usuario, langActual, programaActual.getDiaEmision(), pCanales, xCanales);
				}
				catch (ExcepcionChecker e) {
					cf = e.getCheckerFailure();
					cf.setUrl(call);
					cf.addMotivo("correctC2OneStudent: Diferencias en la lista de canale");
					throw new ExcepcionChecker(cf);
				}


	        } // for y

	    } // for x


		// finalmente la consulta directa

		try {
			Query2.checkDirectQueryC2(CommonTVChecker.servicioProf, usuario, servicioAluP, dqLang, dqDia, passwdAlu);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("correctC2OneStudent: Resultado erróneo en la consulta directa");
			throw new ExcepcionChecker(cf);
		}

		// todas las consultas coincidieron

	    return;
	}









	// comprueba que las consultas directas son iguales

	private static void checkDirectQueryC2(String servicioProf, String usuario, String servicioAluP, String lang, String dia, String passwdAlu)
		throws ExcepcionChecker
	{
		CheckerFailure cf;
		ArrayList<Canal> pCanales, xCanales;

		// primero comprobamos que responde con el error apropiado si falta algún parámetro

  		try {
  			CommonTVChecker.checkLackParam(servicioAluP, passwdAlu, "23", PLANG, lang, PDIA, dia);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("checkDirectQueryC2: No responde correctamente si falta algún parámetro obligatorio");
			throw new ExcepcionChecker(cf);
		}


 		// ahora comprobamos que los resultados son correctos

		String qs = "?auto=si&"+CommonSINT.PFASE+"=23&"+PLANG+"="+lang+"&"+PDIA+"="+dia+"&p="+CommonSINT.PPWD;     // necesario URL-encoded, en otras consultas puede que si
	    String call = CommonTVChecker.servicioProf+qs;

   		try {
   			pCanales = Query2.requestCanalesLangDia(call);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("checkDirectQueryC2: ExcepcionChecker al pedir la consulta directa a sintprof");
			throw new ExcepcionChecker(cf);
		}

		qs = "?auto=si&"+CommonSINT.PFASE+"=23&"+PLANG+"="+lang+"&"+PDIA+"="+dia+"&p="+passwdAlu;     // no es necesario URL-encoded
	    call = servicioAluP+qs;

  		try {
  			xCanales = Query2.requestCanalesLangDia(call);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo("checkDirectQueryC2: ExcepcionChecker al pedir la consulta directa a "+usuario);
			throw new ExcepcionChecker(cf);
		}


		// comparamos las listas de peliculas resultado de sintprof y sintX

		try {
			Query2.comparaCanales(usuario, lang, dia, pCanales, xCanales);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setUrl(call);
			cf.addMotivo("checkDirectQueryC2: Diferencias en la lista de canales resultado");
			throw new ExcepcionChecker(cf);
		}


		// todo coincidió

                System.out.println("checkDirectQueryC2: "+ usuario +" OK");

		return;
	}












	// COMPROBACIÓN DEL SERVICIO DE TODOS LOS ESTUDIANTES

	// pantalla para comprobar todos los estudiantes, se pide el número de cuentas a comprobar (corregir su práctica)

	public static void doGetC2CorrectAllForm (HttpServletRequest request, PrintWriter out)
	{
		int esProfesor = 1;

		out.println("<html>");
		CommonTVChecker.printHead(out);
		CommonTVChecker.printBodyHeader(out);

		out.println("<h2>Consulta 2</h2>");
		out.println("<h3>Corrección de todos los servicios</h3>");

		out.println("<form>");
		out.println("<input type='hidden' name='screenP' value='231'>");

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

	public static void doGetC2CorrectAllReport(HttpServletRequest request, PrintWriter out)
						throws IOException, ServletException
	{
		int esProfesor = 1;

		out.println("<html>");
		CommonTVChecker.printHead(out);
		CommonTVChecker.printBodyHeader(out);

		out.println("<h2>Consulta 2</h2>");

		// si no se recibe el parámetro con el número de cuentas no se hace nada

		String numCuentasP = request.getParameter("numCuentasP");
		if (numCuentasP == null) {
			out.println("<h4>Error: no se ha recibido el número de cuentas</h4>");
			CommonSINT.printEndPageChecker(out,  "23", esProfesor, CommonTVChecker.CREATED);
			return;
		}

		int numCuentas=0;

		try {
			numCuentas = Integer.parseInt(numCuentasP);
		}
		catch (NumberFormatException e) {
			out.println("<h4>Error: el número de cuentas recibido no es un número válido</h4>");
			CommonSINT.printEndPageChecker(out,  "23", esProfesor, CommonTVChecker.CREATED);
			return;
		}

		if (numCuentas < 1) {
			out.println("<h4>Error: el número de cuentas recibido es menor que uno</h4>");
			CommonSINT.printEndPageChecker(out,  "23", esProfesor, CommonTVChecker.CREATED);
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
				Query2.correctC2OneStudent(request, sintUser, Integer.toString(x), servicioAlu, passwdAlu);
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

		CommonSINT.printEndPageChecker(out,  "23", esProfesor, CommonTVChecker.CREATED);
	}


    // para corregir todos los servicios uno a uno
	public static void doGetC2CorrectAllForm2 (HttpServletRequest request, PrintWriter out)
	{
		int esProfesor = 1;

		out.println("<html>");
		CommonTVChecker.printHead(out);
		CommonTVChecker.printBodyHeader(out);

		out.println("<h2>Consulta 2</h2>");
		out.println("<h3>Corrección de todos los servicios uno a uno</h3>");

		out.println("<form>");
		out.println("<input type='hidden' name='screenP' value='241'>");

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


	public static void doGetC2CorrectAllReport2(HttpServletRequest request, HttpServletResponse response)
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
		   CommonSINT.printEndPageChecker(out,  "24", esProfesor, CommonTVChecker.CREATED);
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
		   CommonSINT.printEndPageChecker(out,  "24", esProfesor, CommonTVChecker.CREATED);
		   return;
		}


		if (numCuentas < 1) {
		   out = response.getWriter();
		   out.println("<html>");
		   CommonTVChecker.printHead(out);
		   CommonTVChecker.printBodyHeader(out);

		   out.println("<h4>Error: el número de cuentas recibido es menor que uno</h4>");
		   CommonSINT.printEndPageChecker(out,  "24", esProfesor, CommonTVChecker.CREATED);
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

		    RequestDispatcher dispatcher = CommonTVChecker.servletContextSintProf.getRequestDispatcher("/InformeResultadosCorreccionC2.jsp");
		    dispatcher.forward(request, response);
		}
		catch (Exception s) {
		   out = response.getWriter();
		   out.println("<html>");
		   CommonTVChecker.printHead(out);
		   CommonTVChecker.printBodyHeader(out);

		   out.println("<h4>Error: Exception"+s.toString()+"</h4");
		   CommonSINT.printEndPageChecker(out,  "24", esProfesor, CommonTVChecker.CREATED);
		   return;
		}


	}



	public static void doGetC2CorrectAllReport2Run(HttpServletRequest request, HttpServletResponse response)
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
		    Query2.correctC2OneStudent(request, sintUser, Integer.toString(x), servicioAlu, passwdAlu);
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











	// Métodos auxiliares para la correción de un alumno de la consulta 2


	// pide y devuelve la lista de días de un usuario (para la F21)
	// levanta ExcepcionChecker si algo va mal

	private static ArrayList<String> requestLangs (String call)
									throws ExcepcionChecker
	{
		CheckerFailure cf;
		Document doc;
		ArrayList<String> listaLangs = new ArrayList<String>();

		CommonTVChecker.errorHandler.clear();

		try {
			doc = CommonTVChecker.db.parse(call);
		}
		catch (SAXException ex) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo("requestLangs: SAXException al solicitar y parsear la lista de idiomas");
			throw new ExcepcionChecker(cf);
		}
		catch (Exception ex) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo("requestLangs: Exception al solicitar y parsear la lista de idiomas");
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
			cf.addMotivo("requestLangs: La lista de idiomas es inválida, tiene errors");
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
			cf.addMotivo("requestLangs: La lista de idiomas es inválida, tiene fatal errors");
			throw new ExcepcionChecker(cf);
		}

		if (doc == null) {
			cf = new CheckerFailure(call, "", "requestLangs: Se recibe 'null' al solicitar y parsear la lista de idiomas");
			throw new ExcepcionChecker(cf);
		}

		NodeList nlLangs = doc.getElementsByTagName("langs");

		if (nlLangs.getLength() != 1) {
		    	cf = new CheckerFailure(call, "", "requestLangs: No se recibe '&lt;langs>' al solicitar y parsear la lista de idiomas");
			throw new ExcepcionChecker(cf);
		}

		nlLangs = doc.getElementsByTagName("lang");

		// procesamos todos los idiomas

		for (int x=0; x < nlLangs.getLength(); x++) {
			Element elemLang = (Element)nlLangs.item(x);
			String lang = elemLang.getTextContent().trim();

			listaLangs.add(lang);
		}

		return listaLangs;
	}


	// para comparar el resultado de la F21: listas de idiomas
	// no devuelve nada si son iguales
	// levanta ExcepcionChecker si hay diferencias

	private static void comparaLangs (String usuario, ArrayList<String> pLangs, ArrayList<String> xLangs)
			throws ExcepcionChecker
	{
		CheckerFailure cf;

		if (pLangs.size() != xLangs.size()) {
			cf = new CheckerFailure("", "20_DIFS", "comparaLangs: Debería devolver "+pLangs.size()+" idiomas, pero devuelve "+xLangs.size());
			throw new ExcepcionChecker(cf);
		}


		for (int x=0; x < pLangs.size(); x++)
			if (!xLangs.get(x).equals(pLangs.get(x))) {
				cf = new CheckerFailure("", "20_DIFS", "comparaLangs: El idioma número "+x+" debería ser '"+pLangs.get(x)+"', pero es '"+xLangs.get(x)+"'");
				throw new ExcepcionChecker(cf);
			}

                System.out.println("comparaLangs: "+ usuario +" OK");

		return;
	}










	// pide y devuelve la lista de programas infantiles en un idioma (F22)
	// levanta ExcepcionChecker si algo va mal

	private static ArrayList<Programa> requestInfantilesLang (String call)
									throws ExcepcionChecker
	{
		CheckerFailure cf;
		Document doc;
		ArrayList<Programa> listaInfantiles = new ArrayList<Programa>();

		CommonTVChecker.errorHandler.clear();

		try {
			doc = CommonTVChecker.db.parse(call);
		}
		catch (com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException ex) {
			CommonTVChecker.logTVChecker(ex.toString());
			cf = new CheckerFailure(call, "03_ENCODING", ex.toString());
			cf.addMotivo("requestInfantilesLang: (MalformedByteSequenceException) La codificación de caracteres recibida es incorrecta (no UTF-8)");
			throw new ExcepcionChecker(cf);
		}
		catch (SAXException e) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo("requestInfantilesLang: SAXException al solicitar y parsear la lista de programas infantiles");
			throw new ExcepcionChecker(cf);
		}
		catch (Exception e) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo("requestInfantilesLang: Exception al solicitar y parsear la lista de programas infantiles");
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
			cf.addMotivo("requestInfantilesLang: La lista de programas infantiles es inválida, tiene errors");
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
			cf.addMotivo("requestInfantilesLang: La lista de programas infantiles es inválida, tiene fatal errors");
			throw new ExcepcionChecker(cf);
		}


		if (doc == null) {
			cf = new CheckerFailure(call, "", "requestInfantilesLang: Se recibe 'null' al solicitar y parsear la lista de programas infantiles");
			throw new ExcepcionChecker(cf);
		}

		NodeList nlInfantiles = doc.getElementsByTagName("infantiles");

		if (nlInfantiles.getLength() != 1) {
		    	cf = new CheckerFailure(call, "", "requestInfantilesLang: No se recibe '&lt;infantiles>' al solicitar y parsear la lista de programas infantiles");
			throw new ExcepcionChecker(cf);
		}

		nlInfantiles = doc.getElementsByTagName("infantil");

		// procesamos todos los programas infantiles

		for (int x=0; x < nlInfantiles.getLength(); x++) {
			Element elemInfantil = (Element)nlInfantiles.item(x);

			String resumen = elemInfantil.getAttribute("resumen");
			String dia = elemInfantil.getAttribute("dia");
			String nombre = elemInfantil.getTextContent().trim();

			listaInfantiles.add(new Programa("", "", resumen, nombre, "", "", dia));   // no ponemos lo que no nos hace falta
		}

		return listaInfantiles;
	}



	// para comparar el resultado de la F22: listas de programas infantiles
	// no devuelve nada si son iguales
	// levanta ExcepcionChecker si hay diferencias

	private static void comparaInfantiles (String usuario, String langActual, ArrayList<Programa> pInfantiles, ArrayList<Programa> xInfantiles)
				throws ExcepcionChecker
	{
		CheckerFailure cf;

	    if (pInfantiles.size() != xInfantiles.size()) {
			cf = new CheckerFailure("", "20_DIFS", "comparaInfantiles: "+usuario+"+"+langActual+": debería devolver '"+pInfantiles.size()+"' programas infantiles, pero devuelve '"+xInfantiles.size()+"'");
			throw new ExcepcionChecker(cf);
	    }

	    for (int z=0; z < pInfantiles.size(); z++) {
	    	    if (!xInfantiles.get(z).getNombrePrograma().equals(pInfantiles.get(z).getNombrePrograma())) {
	    			cf = new CheckerFailure("", "20_DIFS", "comparaInfantiles: "+usuario+"+"+langActual+": el nombre del programa infantil número "+z+" debería ser '<pre>"+pInfantiles.get(z).getNombrePrograma()+"</pre>', pero es '<pre>"+xInfantiles.get(z).getNombrePrograma()+"</pre>'");
	    			throw new ExcepcionChecker(cf);
	    	    }

	    	    if (!xInfantiles.get(z).getHoraInicio().equals(pInfantiles.get(z).getHoraInicio())) {
	    			cf = new CheckerFailure("", "20_DIFS", "comparaInfantiles: "+usuario+"+"+langActual+": la fecha de emision del programa infantil número "+z+" debería ser '<pre>"+pInfantiles.get(z).getDiaEmision()+"</pre>', pero es '<pre>"+xInfantiles.get(z).getDiaEmision()+"</pre>'");
	    			throw new ExcepcionChecker(cf);
	    	    }

	    	    if (!xInfantiles.get(z).getResumen().equals(pInfantiles.get(z).getResumen())) {
	    			cf = new CheckerFailure("", "20_DIFS", "comparaInfantiles: "+usuario+"+"+langActual+": el resumen del programa infantil número "+z+" debería ser '<pre>"+pInfantiles.get(z).getResumen()+"</pre>', pero es '<pre>"+xInfantiles.get(z).getResumen()+"</pre>'");
	    			throw new ExcepcionChecker(cf);
	    	    }
	    }

            System.out.println("comparaInfantiles: "+ usuario +" OK");

	    return;
	}








	// pide y devuelve la lista de canales que emiten películas en un idioma en un día (F23)
	// levanta ExcepcionChecker si algo va mal

	private static ArrayList<Canal> requestCanalesLangDia (String call)
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
			cf.addMotivo("requestCanalesLangDia: SAXException al solicitar y parsear la lista de canales");
			throw new ExcepcionChecker(cf);
		}
		catch (Exception ex) {
			CommonTVChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo("requestCanalesLangDia: Exception al solicitar y parsear la lista de canales");
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
			cf.addMotivo("requestCanalesLangDia: La lista de canales es inválida, tiene errors");
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
			cf.addMotivo("requestCanalesLangDia: La lista de canales es inválida, tiene fatal errors");
			throw new ExcepcionChecker(cf);
		}


		if (doc == null) {
			cf = new CheckerFailure(call, "", "requestCanalesLangDia: Se recibe 'null' al solicitar y parsear la lista de canales");
			throw new ExcepcionChecker(cf);
		}


		NodeList nlCanales = doc.getElementsByTagName("canales");

		if (nlCanales.getLength() != 1) {
		    	cf = new CheckerFailure(call, "", "requestCanalesLangDia: No se recibe '&lt;canales>' al solicitar y parsear la lista de canales");
			throw new ExcepcionChecker(cf);
		}

		nlCanales = doc.getElementsByTagName("canal");

		// procesamos todos los canales

		for (int x=0; x < nlCanales.getLength(); x++) {
			Element elemCanal = (Element)nlCanales.item(x);
			String nombreCanal = elemCanal.getTextContent().trim();

			String id = elemCanal.getAttribute("id");
			String grupo = elemCanal.getAttribute("grupo");

			listaCanales.add(new Canal(id, "", nombreCanal, grupo));  // no ponemos lo que no se necesita
		}

		return listaCanales;
	}





	// para comparar el resultado de la F23: listas de canales
	// no devuelve nada si son iguales
	// levanta ExcepcionChecker si hay diferencias

	private static void comparaCanales (String usuario, String langActual, String diaActual, ArrayList<Canal> pCanales, ArrayList<Canal> xCanales)
			throws ExcepcionChecker
{
	CheckerFailure cf;

    if (pCanales.size() != xCanales.size()) {
		cf = new CheckerFailure("", "20_DIFS", "comparaCanales: "+usuario+"+"+langActual+"+"+diaActual+": debería devolver '"+pCanales.size()+"' canales, pero devuelve '"+xCanales.size()+"'");
		throw new ExcepcionChecker(cf);
    }

    for (int z=0; z < pCanales.size(); z++) {
    	    if (!xCanales.get(z).getNombreCanal().equals(pCanales.get(z).getNombreCanal())) {
    			cf = new CheckerFailure("", "20_DIFS", "comparaCanales: "+usuario+"+"+langActual+"+"+diaActual+": el nombre del canal número "+z+" debería ser '<pre>"+pCanales.get(z).getNombreCanal()+"</pre>', pero es '<pre>"+xCanales.get(z).getNombreCanal()+"</pre>'");
    			throw new ExcepcionChecker(cf);
    	    }

    	    if (!xCanales.get(z).getIdCanal().equals(pCanales.get(z).getIdCanal())) {
    			cf = new CheckerFailure("", "20_DIFS", "comparaCanales: "+usuario+"+"+langActual+"+"+diaActual+": el ID del canal número "+z+" debería ser '<pre>"+pCanales.get(z).getIdCanal()+"</pre>', pero es '<pre>"+xCanales.get(z).getIdCanal()+"</pre>'");
    			throw new ExcepcionChecker(cf);
    	    }

    	    if (!xCanales.get(z).getGrupo().equals(pCanales.get(z).getGrupo())) {
    			cf = new CheckerFailure("", "20_DIFS", "comparaCanales: "+usuario+"+"+langActual+"+"+diaActual+": el grupo del canal número "+z+" debería ser '<pre>"+pCanales.get(z).getGrupo()+"</pre>', pero es '<pre>"+xCanales.get(z).getGrupo()+"</pre>'");
    			throw new ExcepcionChecker(cf);
    	    }
    }

    System.out.println("comparaCanales: "+ usuario +" OK");

    return;
}




}
