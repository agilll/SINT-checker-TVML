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

// Implementación de la comprobación de la consulta 2 (alumnos de una asignatura de una titulación)

package docencia.sint.EAML.checker;

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
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import docencia.sint.Common.CheckerFailure;
import docencia.sint.Common.CommonSINT;
import docencia.sint.Common.ExcepcionChecker;
import docencia.sint.Common.SintRandom;
import docencia.sint.Common.BeanResultados;
import docencia.sint.Common.Msgs;
import docencia.sint.EAML.MsgsEAML;
import docencia.sint.EAML.Subject;
import docencia.sint.EAML.Student;


public class Query2 {

	// nombres de los parámetros de esta consulta

	static final String PDEGREE = "pdegree";
	static final String PSTUDENT = "pstudent";


	// COMPROBACIÓN DE LAS LLAMADAS A SINTPROF
	// es sólo para el profesor, no es necesario traducir

	public static void doGetC2CheckSintprofCalls(HttpServletRequest request, HttpServletResponse response, String lang)
						throws IOException, ServletException
	{
		PrintWriter out = response.getWriter();

		CheckerFailure cf;
		int esProfesor = 1;  // sólo el profesor debería llegar aquí, podemos poner esto a 1

		out.println("<html>");
		CommonEAChecker.printHead(out, lang);
		CommonEAChecker.printBodyHeader(out, lang);

		out.println("<h2>Consulta 2</h2>");
		out.println("<h3>Comprobar las llamadas al servicio de sintprof</h3>");

		// doOneCheckUpStatus: hace una petición de estado al servicio del profesor para ver si está operativo

		try {
			CommonEAChecker.doOneCheckUpStatus(request, "sintprof", CommonSINT.PPWD, lang);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			out.println("<h4 style='color: red'>doGetC2CheckSintprofCalls: error al preguntar por el estado de sintprof: <br>");
			out.println(cf.toHTMLString());
			out.println("</h4>");
			CommonSINT.printEndPageChecker(out,  "0", esProfesor, MsgsEAMLChecker.CREATED, lang);
			return;
		}

		out.println("<h4>Check Status OK</h4>");




		// empezamos por pedir los errores

		try {
			CommonEAChecker.requestErrores("sintprof", CommonEAChecker.servicioProf, CommonSINT.PPWD, lang);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			out.println("<h4 style='color: red'>doGetC2CheckSintprofCalls: ExcepcionChecker pidiendo los errores: <br>");
			out.println(cf.toHTMLString());
			out.println("</h4>");
			CommonSINT.printEndPageChecker(out,  "0", esProfesor, MsgsEAMLChecker.CREATED, lang);
			return;
    }
		catch (Exception ex) {
			out.println("<h4 style='color: red'>doGetC2CheckSintprofCalls: Exception pidiendo los errores: "+ex.toString()+"</h4>");
			CommonSINT.printEndPageChecker(out,  "0", esProfesor, MsgsEAMLChecker.CREATED, lang);
			return;
    }

		out.println("<h4>Errores OK</h4>");


		// y ahora todas y cada una de las consultas

		// pedimos la lista de degrees de sintprof

		String qs = "?auto=true&"+CommonSINT.PFASE+"=21&p="+CommonSINT.PPWD;
		String call = CommonEAChecker.servicioProf+qs;

		ArrayList<String> pDegrees;
		try {
			pDegrees = Query2.requestDegrees(call, lang);
			out.println("<h4>Degrees OK: "+pDegrees.size()+"</h4>");
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			out.println("<h4 style='color: red'>doGetC2CheckSintprofCalls: ExcepcionChecker pidiendo los degrees a sintprof: <br>");
			out.println(cf.toHTMLString());
			out.println("</h4>");
			CommonSINT.printEndPageChecker(out,  "0", esProfesor, MsgsEAMLChecker.CREATED, lang);
			return;
    }



		// vamos con la segunda fase, los students de cada degree
		// el bucle X recorre todos los students


		String degreeActual;

		for (int x=0; x < pDegrees.size(); x++) {
			String indent ="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

			degreeActual = pDegrees.get(x);

			// pedimos los students de ese degree de sintprof
			// los degrees pueden tener caracteres no ASCII, hay que aplicar un URLencode

			try {
			    qs = "?auto=true&"+CommonSINT.PFASE+"=22&"+PDEGREE+"="+URLEncoder.encode(degreeActual, "utf-8")+"&p="+CommonSINT.PPWD;
					call = CommonEAChecker.servicioProf+qs;
			}
		  catch (UnsupportedEncodingException ex) {
					out.println("<h4 style='color: red'>doGetC2CheckSintprofCalls: UnsupportedEncodingException pidiendo los subjects: UTF-8 no soportado</h4>");
					CommonSINT.printEndPageChecker(out,  "0", esProfesor, MsgsEAMLChecker.CREATED, lang);
					return;
		  }

			ArrayList<Student> pStudents;
			try {
				pStudents = requestStudentsDegree(call, lang);
				out.println("<h4>"+indent+degreeActual+": "+pStudents.size()+"  OK</h4>");
			}
			catch (ExcepcionChecker e) {
				cf = e.getCheckerFailure();
				out.println("<h4 style='color: red'>doGetC2CheckSintprofCalls: ExcepcionChecker pidiendo los students de "+degreeActual+" a sintporof: <br>");
				out.println(cf.toHTMLString());
				out.println("</h4>");
				CommonSINT.printEndPageChecker(out,  "0", esProfesor, MsgsEAMLChecker.CREATED, lang);
				return;
	        }




	        // vamos con la tercera fase, las notas (subjects) de un student en un degree
	        // el bucle Y recorre todos los students

	        Student studentActual;

	        for (int y=0; y < pStudents.size(); y++) {

		        	indent = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

		        	studentActual = pStudents.get(y);

		    		// pedimos los subjects de ese student de ese degree de sintprof
					  // los degrees y los students pueden tener caracteres no ASCII, hay que aplicar un URLencode

						try {
		    		    qs = "?auto=true&"+CommonSINT.PFASE+"=23&"+PDEGREE+"="+URLEncoder.encode(degreeActual, "utf-8")+"&"+PSTUDENT+"="+URLEncoder.encode(studentActual.getId(), "utf-8")+"&p="+CommonSINT.PPWD;
							  call = CommonEAChecker.servicioProf+qs;
						}
						catch (UnsupportedEncodingException ex) {
								out.println("<h4 style='color: red'>doGetC2CheckSintprofCalls: UnsupportedEncodingException pidiendo los subjects a sintprof: UTF-8 no soportado</h4>");
								CommonSINT.printEndPageChecker(out,  "0", esProfesor, MsgsEAMLChecker.CREATED, lang);
								return;
					  }

		    		ArrayList<Subject> pSubjects;
		    		try {
		    			pSubjects = requestSubjectsStudentDegree(call, lang);
		    			out.println("<h4>"+indent+degreeActual+"+"+studentActual.getNombre()+": "+pSubjects.size()+"  OK</h4>");
		    		}
		    		catch (ExcepcionChecker e) {
						cf = e.getCheckerFailure();
		    			out.println("<h4 style='color: red'>doGetC2CheckSintprofCalls: ExcepcionChecker pidiendo los subjects a sintprof: <br>");
						  out.println(cf.toHTMLString());
		    			out.println("</h4>");
		    			CommonSINT.printEndPageChecker(out,  "0", esProfesor, MsgsEAMLChecker.CREATED, lang);
		    			return;
		            }


	        } // for y

	    } // for x


		out.println("<h4>sintprof: Todo OK</h4>");

		CommonSINT.printEndPageChecker(out,  "0", esProfesor, MsgsEAMLChecker.CREATED, lang);
	}








	// COMPROBACIÓN DEL SERVICIO DE UN ÚNICO ESTUDIANTE

	// pantalla para ordenar comprobar un estudiante (se pide su número de login)
	// debería unificarse en uno sólo con el de la consulta 1, son casi iguales

	public static void doGetC2CorrectOneForm (HttpServletRequest request, HttpServletResponse response, String lang, int esProfesor)
						throws IOException
	{
		PrintWriter out = response.getWriter();

		out.println("<html>");
		CommonEAChecker.printHead(out, lang);

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

		out.println("inputServiceElem.value = 'http://"+CommonEAChecker.server_port+"/sint'+inputSint+'"+CommonEAChecker.SERVICE_NAME+"';");
		out.println("inputServiceElem.style.visibility='visible';");
		out.println("}");
		out.println("</script>");

		CommonEAChecker.printBodyHeader(out, lang);

		out.println("<h2>"+Msgs.getMsg(Msgs.CC03,lang)+" 2</h2>");   // CC03 = Consulta
		out.println("<h3>"+Msgs.getMsg(Msgs.CC04,lang)+"</h3>");   // CC04 = corrigiendo un servicio

		out.println("<form>");
		out.println("<input type='hidden' name='screenP' value='221'>");  // conduce a doGetC2CorrectOneReport

		out.println(Msgs.getMsg(Msgs.CC05,lang)); // CC05 = "Introduzca el número de la cuenta SINT a comprobar: "
		out.println("<input id='inputSint' type='text' name='alumnoP' size='3' onfocus='hideservice();' onblur='showservice();' onkeypress='stopEnter(event);' pattern='[1-9]([0-9]{1,2})?' required> <br>");

		out.println(Msgs.getMsg(Msgs.CC06,lang));  // CC06 = "URL del servicio del alumno: "
		out.println("<input style='visibility: hidden' id='serviceAluInput' type='text' name='servicioAluP' value='' size='40'><br>");

		if (esProfesor == 0) {
			// CC07 = "Passwd del servicio (10 letras o números): "
			out.println("<p>"+Msgs.getMsg(Msgs.CC07,lang)+" <input id='passwdAlu' type='text' name='passwdAlu'  pattern='[A-Za-z0-9]{10}?' required> <br><br>");
		}
		else {
			out.println("<p><input type='hidden' name='p' value='si'>");
		}

		out.println("<p><input class='enviar' id='sendButton' disabled='true' type='submit' value='"+Msgs.getMsg(Msgs.CB00,lang)+"'>");  //CB00=Enviar
		out.println("</form>");

		out.println("<form>");
		if (esProfesor == 1)
			out.println("<p><input type='hidden' name='p' value='si'>");
		out.println("<p><input class='home'  type='submit' value='"+Msgs.getMsg(Msgs.CB01,lang)+"'>");  //CB01=Inicio
		out.println("</form>");

		CommonSINT.printFoot(out, MsgsEAMLChecker.CREATED);

		out.println("</body></html>");
	}






	// pantalla para informar de la corrección de un sintX (se recibe en 'alumnoP' su número de login X)
	// también recibe en servicioAlu el URL del servicio del alumno

	public static void doGetC2CorrectOneReport(HttpServletRequest request, HttpServletResponse response, String lang, int esProfesor)
						throws IOException, ServletException
	{
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();

		PrintWriter out = response.getWriter();

		out.println("<html>");
		CommonEAChecker.printHead(out, lang);
		CommonEAChecker.printBodyHeader(out, lang);

		out.println("<h2>"+Msgs.getMsg(Msgs.CC03,lang)+" 2</h2>");  // CC03 = Consulta
		out.println("<h3>"+Msgs.getMsg(Msgs.CC04,lang)+"</h3>");  // CC04 = corrigiendo un servicio

		// leemos los datos del estudiante

		String alumnoP = request.getParameter("alumnoP");
		String servicioAluP = request.getParameter("servicioAluP");

		if ((alumnoP == null) || (servicioAluP == null)) {
			out.println("<h4 style='color: red'>"+currentMethod+": "+Msgs.getMsg(Msgs.CC40, lang)+"</h4>");  // CC40 = falta uno de los parámetros (alumnoP, servicioAluP)
			CommonSINT.printEndPageChecker(out,  "22", esProfesor, MsgsEAMLChecker.CREATED, lang);
			return;
		}

		String usuario="sint"+alumnoP;
		String passwdAlu, passwdRcvd;



		try {
			passwdAlu = CommonEAChecker.getAluPasswd(usuario);
		}
		catch (ExcepcionChecker ex) {
			String codigo = ex.getCheckerFailure().getCodigo();
			if (codigo.equals("NOCONTEXT"))
				out.println("<h4 style='color: red'>"+currentMethod+": ExcepcionChecker: "+Msgs.getMsg(Msgs.CC39,lang)+usuario+"</h4>"); // CC39 = Todavía no se ha creado el contexto del usuario, o este fue eliminado tras un error
			else
				out.println("<h4 style='color: red'>"+currentMethod+": ExcepcionChecker: "+Msgs.getMsg(Msgs.CC41, lang)+usuario+"</h4>"); // CC41 = "Imposible recuperar la passwd del contexto de "
			CommonSINT.printEndPageChecker(out,  "22", esProfesor, MsgsEAMLChecker.CREATED, lang);
			return;
		}

		if (esProfesor == 0) {  // lo está probando un alumno, es obligatorio que haya introducido su passwd
			passwdRcvd = request.getParameter("passwdAlu");   // leemos la passwd del alumno del parámetro

			if (passwdRcvd == null) {
				out.println("<h4 style='color: red'>"+currentMethod+": "+Msgs.getMsg(Msgs.CC42,lang)+usuario+"</h4>");  // CC42 = No se ha recibido la passwd de
				CommonSINT.printEndPageChecker(out,  "22", esProfesor, MsgsEAMLChecker.CREATED, lang);
				return;
			}

			if (!passwdAlu.equals(passwdRcvd)) {
				// CC10 = "La passwd proporcionada no coincide con la almacenada en el sistema para "
				out.println("<h4 style='color: red'>"+currentMethod+": "+Msgs.getMsg(Msgs.CC10,lang)+usuario+"</h4>");
				CommonSINT.printEndPageChecker(out,  "22", esProfesor, MsgsEAMLChecker.CREATED, lang);
				return;
			}

		}

		out.println("<h3>"+Msgs.getMsg(Msgs.CC08,lang)+usuario+" ("+servicioAluP+")</h3>");  // CC08 = comprobando el servicio del usuario X


		// doOneCheckUpStatus: hace una petición de estado al servicio del profesor para ver si está operativo

		try {
			CommonEAChecker.doOneCheckUpStatus(request, "sintprof", CommonSINT.PPWD, lang);
		}
		catch (ExcepcionChecker e) {
			CheckerFailure cf = e.getCheckerFailure();
			out.println("<h4 style='color: red'>"+currentMethod+": ExcepcionChecker: "+Msgs.getMsg(Msgs.CC43,lang)+"<br>"); // CC43 = Error al preguntar por el estado de la práctica del profesor
			out.println(cf.toHTMLString());
			out.println("</h4>");
			CommonSINT.printEndPageChecker(out,  "22", esProfesor, MsgsEAMLChecker.CREATED, lang);
			return;
		}


		try {
			Query2.correctC2OneStudent(request, usuario, alumnoP, servicioAluP, passwdAlu, lang);
		}
		catch (ExcepcionChecker e) {
			CheckerFailure cf = e.getCheckerFailure();
			out.println("<h4 style='color: red'>"+currentMethod+": "+Msgs.getMsg(Msgs.CC11,lang)+usuario+" <br>"); // CC11 = "Resultado incorrecto comprobando el servicio de "
			out.println(cf.toHTMLString());
			out.println("</h4>");
			CommonSINT.printEndPageChecker(out,  "22", esProfesor, MsgsEAMLChecker.CREATED, lang);
			return;
		}

		out.println("<h3>"+Msgs.getMsg(Msgs.CC09,lang)+"OK</h3>");  // CC09 = "Resultado: "



		// terminamos con botones Atrás e Inicio

		CommonSINT.printEndPageChecker(out,  "22", esProfesor, MsgsEAMLChecker.CREATED, lang);
	}




	// método que corrige la consulta 1 de un estudiante

    private static void correctC2OneStudent (HttpServletRequest request,String usuario, String aluNum, String servicioAluP, String passwdAlu, String lang)
    			throws ExcepcionChecker
	{
    	CheckerFailure cf;
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();

		// para la consulta directa final, vamos a escoger degree y student al azar y a guardarlos en esas variables

		SintRandom.init(); // inicializamos el generador de números aleatorios
		int posrandom;

		String dqDegree="";
		String dqStudent="";  // el ID

		// empezamos por comprobar los ficheros

		try {
			CommonEAChecker.doOneCheckUpFiles(aluNum, "2", lang);  // 2 es el número de la consulta
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+Msgs.getMsg(Msgs.CC12,lang));  // CC12 = "Error en los ficheros fuente"
			throw new ExcepcionChecker(cf);
		}


        // ahora comprobamos que el servicio está operativo

		try {
			CommonEAChecker.doOneCheckUpStatus(request, usuario, passwdAlu, lang);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+Msgs.getMsg(Msgs.CC13,lang));  // CC13 = "Error al comprobar si el servicio del estudiante está operativo"
			throw new ExcepcionChecker(cf);
		}





		// ahora comprobamos los errores

		try {
			CommonEAChecker.comparaErrores(usuario, servicioAluP, passwdAlu, lang);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+Msgs.getMsg(Msgs.CC14,lang));  // CC14 = "Diferencias en la lista de errores"
			throw new ExcepcionChecker(cf);
		}




		// y ahora todas y cada una de las consultas

		// pedimos la lista de degrees de sintprof

		String qs = "?auto=true&"+CommonSINT.PFASE+"=21&p="+CommonSINT.PPWD;
		String call = CommonEAChecker.servicioProf+qs;

		ArrayList<String> pDegrees;
		try {
			pDegrees = Query2.requestDegrees(call, lang);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("21",lang)+"degrees (sintprof)"); // error solicitando lista de
			throw new ExcepcionChecker(cf);
		}


		// pedimos la lista de degrees del sintX

		qs = "?auto=true&"+CommonSINT.PFASE+"=21&p="+passwdAlu;
		call = servicioAluP+qs;

		ArrayList<String> xDegrees;
		try {
			xDegrees = Query2.requestDegrees(call, lang);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("21",lang)+"degrees ("+usuario+")");  // error solicitando lista de
			throw new ExcepcionChecker(cf);
		}


		// comparamos las listas de sintprof y sintX

		try {
		     Query2.comparaDegrees(usuario, pDegrees, xDegrees, lang);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setUrl(call);
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("22",lang)+"degrees");  // diferencias en la lista de
			throw new ExcepcionChecker(cf);
		}



		// las listas de degrees son iguales
		// elegimos un degree al azar para la consulta directa final

		posrandom = SintRandom.getRandomNumber(0, pDegrees.size()-1);
		dqDegree = pDegrees.get(posrandom);




		// vamos con la segunda fase, los subjects de cada degree
		// el bucle X recorre todos los degrees


		String degreeActual;

		for (int x=0; x < pDegrees.size(); x++) {

			degreeActual = pDegrees.get(x);

			// pedimos los students de ese degree de sintprof
		  // los degrees pueden tener caracteres no ASCII, hay que aplicar un URLencode

			try {
					qs = "?auto=true&"+CommonSINT.PFASE+"=22&"+PDEGREE+"="+URLEncoder.encode(degreeActual, "utf-8")+"&p="+CommonSINT.PPWD;
					call = CommonEAChecker.servicioProf+qs;
			}
		  catch (UnsupportedEncodingException ex) {
				CommonEAChecker.logCall(call);

				cf = new CheckerFailure(call, "20_DIFS", ex.toString());
				cf.addMotivo(currentMethod+": UnsupportedEncodingException: "+MsgsEAMLChecker.getMsg("23",lang)+"students (sintprof)");  // error creando solicitud lista de
				throw new ExcepcionChecker(cf);
			}

			ArrayList<Student> pStudents;
			try {
				pStudents = requestStudentsDegree(call, lang);
			}
			catch (ExcepcionChecker e) {
				cf = e.getCheckerFailure();
				cf.setCodigo("20_DIFS");
				cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("21",lang)+"students - "+degreeActual+" (sintprof)"); // error solicitando lista de
				throw new ExcepcionChecker(cf);
			}


			// pedimos los students de ese degree del sintX
			// los degrees pueden tener caracteres no ASCII, hay que aplicar un URLencode

			try {
			   qs = "?auto=true&"+CommonSINT.PFASE+"=22&"+PDEGREE+"="+URLEncoder.encode(degreeActual, "utf-8")+"&p="+passwdAlu;
				 call = servicioAluP+qs;
			 }
			 catch (UnsupportedEncodingException ex) {
				 CommonEAChecker.logCall(call);

 				 cf = new CheckerFailure(call, "20_DIFS", ex.toString());
 				 cf.addMotivo(currentMethod+": UnsupportedEncodingException: "+MsgsEAMLChecker.getMsg("23",lang)+"students ("+usuario+")"); // error creando solicitud lista de
 				 throw new ExcepcionChecker(cf);
			 }

			ArrayList<Student> xStudents;
			try {
				xStudents = requestStudentsDegree(call, lang);
			}
			catch (ExcepcionChecker e) {
				cf = e.getCheckerFailure();
				cf.setCodigo("20_DIFS");
				cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("21",lang)+"students - "+degreeActual+" ("+usuario+")");  // error solicitando lista de
				throw new ExcepcionChecker(cf);
			}


			// comparamos las listas de sintprof y sintX

			try {
				Query2.comparaStudents(usuario, degreeActual, pStudents, xStudents, lang);
			}
			catch (ExcepcionChecker e) {
				cf = e.getCheckerFailure();
				cf.setUrl(call);
				cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("22",lang)+"students ("+degreeActual+")"); // diferencias en la lista de
				throw new ExcepcionChecker(cf);
			}



	        // las listas de students para este degree son iguales
		    // si este degree es el de la consulta directa final, elegimos un student al azar para la consulta directa final


	        if (degreeActual.equals(dqDegree)) {
	            posrandom = SintRandom.getRandomNumber(0, pStudents.size()-1);
	            Student pStu = pStudents.get(posrandom);
	          	dqStudent = pStu.getId();
	        }



	        // vamos con la tercera fase, los subjects de un student
	        // el bucle Y recorre todos los students

	        Student studentActual;

	        for (int y=0; y < pStudents.size(); y++) {

	        	studentActual = pStudents.get(y);

	    		// pedimos los subjects de ese student de ese degree de sintprof
				  // los degrees y los students pueden tener caracteres no ASCII, hay que aplicar un URLencode

					try {
	    		    qs = "?auto=true&"+CommonSINT.PFASE+"=23&"+PDEGREE+"="+URLEncoder.encode(degreeActual, "utf-8")+"&"+PSTUDENT+"="+URLEncoder.encode(studentActual.getId(), "utf-8")+"&p="+CommonSINT.PPWD;
	    		    call = CommonEAChecker.servicioProf+qs;
					}
					catch (UnsupportedEncodingException ex) {
						 CommonEAChecker.logCall(call);

	  				 cf = new CheckerFailure(call, "20_DIFS", ex.toString());
	  				 cf.addMotivo(currentMethod+": UnsupportedEncodingException: "+MsgsEAMLChecker.getMsg("23",lang)+"subjects (sintprof)"); // error creando solicitud lista de
	  				 throw new ExcepcionChecker(cf);
					}

	    		ArrayList<Subject> pSubjects;
	    		try {
	    			pSubjects = requestSubjectsStudentDegree(call, lang);
	    		}
	    		catch (ExcepcionChecker e) {
	    			cf = e.getCheckerFailure();
					cf.setCodigo("20_DIFS");
					cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("21",lang)+"subjects (sintprof)");  // error solicitando lista de
					throw new ExcepcionChecker(cf);
	    		}


	    		// pedimos los subjects de ese student de ese degree del sintX
					// los degrees y los students pueden tener caracteres no ASCII, hay que aplicar un URLencode

					try {
	    		   qs = "?auto=true&"+CommonSINT.PFASE+"=23&"+PDEGREE+"="+URLEncoder.encode(degreeActual, "utf-8")+"&"+PSTUDENT+"="+URLEncoder.encode(studentActual.getId(), "utf-8")+"&p="+passwdAlu;
	    		   call = servicioAluP+qs;
					}
 				  catch (UnsupportedEncodingException ex) {
						CommonEAChecker.logCall(call);

						cf = new CheckerFailure(call, "20_DIFS", ex.toString());
						cf.addMotivo(currentMethod+": UnsupportedEncodingException: "+MsgsEAMLChecker.getMsg("23",lang)+"subjects ("+usuario+")"); // error creando solicitud lista de
						throw new ExcepcionChecker(cf);
 					}

	    		ArrayList<Subject> xSubjects;
	    		try {
	    			xSubjects = requestSubjectsStudentDegree(call, lang);
	    		}
	    		catch (ExcepcionChecker e) {
	    			cf = e.getCheckerFailure();
					cf.setCodigo("20_DIFS");
					cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("21",lang)+"subjects ("+usuario+")"); // error solicitando lista de
					throw new ExcepcionChecker(cf);
	    		}


	    		// comparamos las listas de sintprof y sintX

	    		try {
	    			Query2.comparaSubjects(usuario, degreeActual, studentActual.getId(), pSubjects, xSubjects, lang);
				}
				catch (ExcepcionChecker e) {
					cf = e.getCheckerFailure();
					cf.setUrl(call);
					cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("22",lang)+"subjects ("+degreeActual+", "+studentActual.getId()+")"); // diferencias en la lista de
					throw new ExcepcionChecker(cf);
				}


	        } // for y

	    } // for x


		// finalmente la consulta directa

		try {
			Query2.checkDirectQueryC2(CommonEAChecker.servicioProf, usuario, servicioAluP, dqDegree, dqStudent, passwdAlu, lang);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("24",lang));  // resultado erróneo de la consuta directa
			throw new ExcepcionChecker(cf);
		}

		// todas las consultas coincidieron

	    return;
	}









	// comprueba que las consultas directas son iguales

	private static void checkDirectQueryC2(String servicioProf, String usuario, String servicioAluP, String degree, String student, String passwdAlu, String lang)
		throws ExcepcionChecker
	{
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();
		CheckerFailure cf;
		ArrayList<Subject> pSubjects, xSubjects;
		String qs="", call="";

		// primero comprobamos que responde con el error apropiado si falta algún parámetro

  		try {
  			CommonEAChecker.checkLackParam(servicioAluP, passwdAlu, "23", PDEGREE, degree, PSTUDENT, student, lang);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+Msgs.getMsg(Msgs.CC64,lang));  // CC64 = "No responde correctamente si falta algún parámetro obligatorio"
			throw new ExcepcionChecker(cf);
		}


 		// ahora comprobamos que los resultados son correctos

		// pedimos la consulta a sintprof
		// los degrees y los subjects pueden tener caracteres no ASCII, hay que aplicar un URLencode

		try {
		    qs = "?auto=true&"+CommonSINT.PFASE+"=23&"+PDEGREE+"="+URLEncoder.encode(degree, "utf-8")+"&"+PSTUDENT+"="+URLEncoder.encode(student, "utf-8")+"&p="+CommonSINT.PPWD;
				call = CommonEAChecker.servicioProf+qs;
		}
		catch (UnsupportedEncodingException ex) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "20_DIFS", ex.toString());
			cf.addMotivo(currentMethod+": UnsupportedEncodingException: "+MsgsEAMLChecker.getMsg("23",lang)+"subjects (sintprof)"); // error creando solicitud lista de
			throw new ExcepcionChecker(cf);
		}

   		try {
   			pSubjects = Query2.requestSubjectsStudentDegree(call, lang);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("26",lang)+" (sintprof)");  // error pidiendo consulta directa
			throw new ExcepcionChecker(cf);
		}

		// pedimos la consulta al sintX
		// los degrees y los subjects pueden tener caracteres no ASCII, hay que aplicar un URLencode

		try {
		    qs = "?auto=true&"+CommonSINT.PFASE+"=23&"+PDEGREE+"="+URLEncoder.encode(degree, "utf-8")+"&"+PSTUDENT+"="+URLEncoder.encode(student, "utf-8")+"&p="+passwdAlu;
				call = servicioAluP+qs;
		}
		catch (UnsupportedEncodingException ex) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "20_DIFS", ex.toString());
			cf.addMotivo(currentMethod+": UnsupportedEncodingException:"+MsgsEAMLChecker.getMsg("23",lang)+" subjects ("+usuario+")"); // // error creando solicitud lista de
			throw new ExcepcionChecker(cf);
		}

  		try {
  			xSubjects = Query2.requestSubjectsStudentDegree(call, lang);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setCodigo("20_DIFS");
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("26",lang)+" ("+usuario+")"); // error pidiendo consulta directa
			throw new ExcepcionChecker(cf);
		}


		// comparamos las listas de subjects resultado de sintprof y sintX

		try {
			Query2.comparaSubjects(usuario, degree, student, pSubjects, xSubjects, lang);
		}
		catch (ExcepcionChecker e) {
			cf = e.getCheckerFailure();
			cf.setUrl(call);
			cf.addMotivo(currentMethod+": ExcepcionChecker: "+MsgsEAMLChecker.getMsg("22",lang)+" subjects");
			throw new ExcepcionChecker(cf);
		}


		// todo coincidió

		return;
	}












	// COMPROBACIÓN DEL SERVICIO DE TODOS LOS ESTUDIANTES

	// pantalla para comprobar todos los estudiantes, se pide el número de cuentas a comprobar (corregir su práctica)

	public static void doGetC2CorrectAllForm (HttpServletRequest request, HttpServletResponse response, String lang) throws IOException
	{
		PrintWriter out = response.getWriter();
		int esProfesor = 1;

		out.println("<html>");
		CommonEAChecker.printHead(out, lang);
		CommonEAChecker.printBodyHeader(out, lang);

		out.println("<h2>Consulta 1</h2>");
		out.println("<h3>Corrección de todos los servicios</h3>");

		out.println("<form>");
		out.println("<input type='hidden' name='screenP' value='231'>");

		out.println("Introduzca el número de cuentas SINT a corregir: ");
		out.println("<input id='inputNumCuentas' type='text' name='numCuentasP' size='3' pattern='[1-9]([0-9]{1,2})?' required>");

		if (esProfesor == 1)
			out.println("<p><input type='hidden' name='p' value='si'>");

		out.println("<input class='enviar' type='submit' value='"+Msgs.getMsg(Msgs.CB00,lang)+"' >");  //CB00=Enviar
		out.println("</form>");

		out.println("<form>");
		if (esProfesor == 1)
			out.println("<p><input type='hidden' name='p' value='si'>");
		out.println("<p><input class='home' type='submit' value='"+Msgs.getMsg(Msgs.CB01,lang)+"'>");  //CB01=Inicio
		out.println("</form>");

		CommonSINT.printFoot(out, MsgsEAMLChecker.CREATED);

		out.println("</body></html>");
	}




	// pantalla para corregir a todos los estudiantes
	// presenta en pantalla diversas listas según el resultado de cada alumno
	// se crea un fichero con el resultado de cada corrección (webapps/CORRECCIONES/sintX/fecha-corrección)
	// se devuelven enlaces a esos ficheros

	public static void doGetC2CorrectAllReport(HttpServletRequest request, HttpServletResponse response, String lang)
						throws IOException, ServletException
	{
		PrintWriter out = response.getWriter();
		int esProfesor = 1;

		out.println("<html>");
		CommonEAChecker.printHead(out, lang);
		CommonEAChecker.printBodyHeader(out, lang);

		out.println("<h2>Consulta 2</h2>");

		// si no se recibe el parámetro con el número de cuentas no se hace nada

		String numCuentasP = request.getParameter("numCuentasP");
		if (numCuentasP == null) {
			out.println("<h4>Error: no se ha recibido el número de cuentas</h4>");
			CommonSINT.printEndPageChecker(out,  "23", esProfesor, MsgsEAMLChecker.CREATED, lang);
			return;
		}

		int numCuentas=0;

		try {
			numCuentas = Integer.parseInt(numCuentasP);
		}
		catch (NumberFormatException e) {
			out.println("<h4>Error: el número de cuentas recibido no es un número válido</h4>");
			CommonSINT.printEndPageChecker(out,  "23", esProfesor, MsgsEAMLChecker.CREATED, lang);
			return;
		}

		if (numCuentas < 1) {
			out.println("<h4>Error: el número de cuentas recibido es menor que uno</h4>");
			CommonSINT.printEndPageChecker(out,  "23", esProfesor, MsgsEAMLChecker.CREATED, lang);
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

		String correccionesPath = CommonEAChecker.servletContextSintProf.getRealPath("/")+"CORRECCIONES";

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
				passwdAlu = CommonEAChecker.getAluPasswd(sintUser);
			}
			catch (ExcepcionChecker ex) {
				String codigo = ex.getCheckerFailure().getCodigo();
				if (codigo.equals("NOCONTEXT")) {
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


			servicioAlu = "http://"+CommonEAChecker.server_port+"/"+sintUser+CommonEAChecker.SERVICE_NAME;

			try {
				Query2.correctC2OneStudent(request, sintUser, Integer.toString(x), servicioAlu, passwdAlu, lang);
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

		CommonSINT.printEndPageChecker(out,  "23", esProfesor, MsgsEAMLChecker.CREATED, lang);
	}




    // para corregir todos los servicios uno a uno
	public static void doGetC2CorrectAllForm2 (HttpServletRequest request, HttpServletResponse response, String lang) throws IOException
	{
		PrintWriter out = response.getWriter();
		int esProfesor = 1;

		out.println("<html>");
		CommonEAChecker.printHead(out, lang);
		CommonEAChecker.printBodyHeader(out, lang);

		out.println("<h2>Consulta 2</h2>");
		out.println("<h3>Corrección de todos los servicios uno a uno</h3>");

		out.println("<form>");
		out.println("<input type='hidden' name='screenP' value='241'>");

		out.println("Introduzca el número de cuentas SINT a corregir: ");
		out.println("<input id='inputNumCuentas' type='text' name='numCuentasP' size='3' pattern='[1-9]([0-9]{1,2})?' required>");

		if (esProfesor == 1)
			out.println("<p><input type='hidden' name='p' value='si'>");

		out.println("<input class='enviar' type='submit' value='"+Msgs.getMsg(Msgs.CB00,lang)+"' >");  //CB00=Enviar
		out.println("</form>");

		out.println("<form>");
		if (esProfesor == 1)
			out.println("<p><input type='hidden' name='p' value='si'>");
		out.println("<p><input class='home' type='submit' value='"+Msgs.getMsg(Msgs.CB01,lang)+"'>");  //CB01=Inicio
		out.println("</form>");

		CommonSINT.printFoot(out, MsgsEAMLChecker.CREATED);

		out.println("</body></html>");
	}




	public static void doGetC2CorrectAllReport2(HttpServletRequest request, HttpServletResponse response, String lang)
						throws IOException, ServletException
	{
	    PrintWriter out;
		int esProfesor = 1;

		// si no se recibe el parámetro con el número de cuentas no se hace nada

		String numCuentasP = request.getParameter("numCuentasP");

		if (numCuentasP == null) {
		   out = response.getWriter();
		   out.println("<html>");
		   CommonEAChecker.printHead(out, lang);
		   CommonEAChecker.printBodyHeader(out, lang);

		   out.println("<h4>Error: no se ha recibido el número de cuentas</h4>");
		   CommonSINT.printEndPageChecker(out,  "24", esProfesor, MsgsEAMLChecker.CREATED, lang);
		   return;
		}

		int numCuentas=0;

		try {
		   numCuentas = Integer.parseInt(numCuentasP);
		}
		catch (NumberFormatException e) {
		   out = response.getWriter();
		   out.println("<html>");
		   CommonEAChecker.printHead(out, lang);
		   CommonEAChecker.printBodyHeader(out, lang);

		   out.println("<h4>Error: el número de cuentas recibido no es un número válido</h4>");
		   CommonSINT.printEndPageChecker(out,  "24", esProfesor, MsgsEAMLChecker.CREATED, lang);
		   return;
		}


		if (numCuentas < 1) {
		   out = response.getWriter();
		   out.println("<html>");
		   CommonEAChecker.printHead(out, lang);
		   CommonEAChecker.printBodyHeader(out, lang);

		   out.println("<h4>Error: el número de cuentas recibido es menor que uno</h4>");
		   CommonSINT.printEndPageChecker(out,  "24", esProfesor, MsgsEAMLChecker.CREATED, lang);
		   return;
		}


		BeanResultados miBean = new BeanResultados();
		miBean.setCssFile(CommonEAChecker.CSS_FILE);
		miBean.setTitle(MsgsEAMLChecker.getMsg("0",lang));
		miBean.setLang(MsgsEAML.XML_LANGUAGE);
		miBean.setCurso(MsgsEAML.CURSO);
		miBean.setNumCuentas(numCuentas);
		miBean.setEsProfesor(esProfesor);
		miBean.setCreated(MsgsEAMLChecker.CREATED);

		request.setAttribute("db", miBean);

		try {
		    // ServletContext sc = getServletContext();

				// transfiere el control a una página JSP con SSE, para ser notificada de cada corrección terminada
		    RequestDispatcher dispatcher = CommonEAChecker.servletContextSintProf.getRequestDispatcher("/InformeResultadosCorreccionC2.jsp");
		    dispatcher.forward(request, response);
		}
		catch (Exception s) {
		   out = response.getWriter();
		   out.println("<html>");
		   CommonEAChecker.printHead(out, lang);
		   CommonEAChecker.printBodyHeader(out, lang);

		   out.println("<h4>Error: Exception"+s.toString()+"</h4");
		   CommonSINT.printEndPageChecker(out,  "24", esProfesor, MsgsEAMLChecker.CREATED, lang);
		   return;
		}


	}



	public static void doGetC2CorrectAllReport2Run(HttpServletRequest request, HttpServletResponse response, String lang)
						throws IOException, ServletException
	{

		  // notifica a la página JSP que se ha terminado cada corrección

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
		    passwdAlu = CommonEAChecker.getAluPasswd(sintUser);
		}
		catch (ExcepcionChecker ex) {
				String codigo = ex.getCheckerFailure().getCodigo();
		    if (codigo.equals("NOCONTEXT")) {
			out.write("data: "+x+",NOCONTEXT\n\n");   // usersE1NoContext.add(x);
		    }
		    else {
		        out.write("data: "+x+",NOPASSWD\n\n"); //  usersE11NoPasswd.add(x);
		    }
		    continue bucle;
		}


		servicioAlu = "http://"+CommonEAChecker.server_port+"/"+sintUser+CommonEAChecker.SERVICE_NAME;

		try {
		    Query2.correctC2OneStudent(request, sintUser, Integer.toString(x), servicioAlu, passwdAlu, lang);
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


	// pide y devuelve la lista de degrees de un usuario
	// levanta ExcepcionChecker si algo va mal

	private static ArrayList<String> requestDegrees (String call, String lang)
									throws ExcepcionChecker
	{
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();
		CheckerFailure cf;
		Document doc;
		ArrayList<String> listaDegrees = new ArrayList<String>();

		CommonEAChecker.errorHandler.clear();

		try {
			doc = CommonEAChecker.db.parse(call);
		}
		catch (SAXException ex) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo(currentMethod+": SAXException: "+MsgsEAMLChecker.getMsg("27",lang)+"degrees"); // solicitando/parseando la lista de
			throw new ExcepcionChecker(cf);
		}
		catch (Exception ex) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo(currentMethod+": Exception: "+MsgsEAMLChecker.getMsg("27",lang)+"degrees"); // solicitando/parseando la lista de
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
			cf.addMotivo(currentMethod+": "+String.format(MsgsEAMLChecker.getMsg("28",lang), "errors")+"degrees"); // resultado inválido, 'errors' en la lista de
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
			cf.addMotivo(currentMethod+": "+String.format(MsgsEAMLChecker.getMsg("28",lang), "fatal errors")+"degrees"); // resultado inválido, 'fatal errors' en la lista de
			throw new ExcepcionChecker(cf);
		}

		if (doc == null) {
			cf = new CheckerFailure(call, "", currentMethod+": "+MsgsEAMLChecker.getMsg("30",lang)+"degrees");   // el parser devuelve 'null' al parsear la lista de
			throw new ExcepcionChecker(cf);
		}


		NodeList nlDegrees = doc.getElementsByTagName("degrees");

		if (nlDegrees.getLength() != 1) {
		    	cf = new CheckerFailure(call, "", currentMethod+": "+MsgsEAMLChecker.getMsg("31",lang));  // No se recibe '&lt;degrees>' al solicitar y parsear la lista de degrees
			throw new ExcepcionChecker(cf);
		}

		nlDegrees = doc.getElementsByTagName("degree");

		// procesamos todos los degrees

		for (int x=0; x < nlDegrees.getLength(); x++) {
			Element elemDegree = (Element)nlDegrees.item(x);
			String degree = elemDegree.getTextContent().trim();

			listaDegrees.add(degree);
		}

		return listaDegrees;
	}


	// para comparar el resultado de la F21: listas de degrees
	// no devuelve nada si son iguales
	// levanta ExcepcionChecker si hay diferencias

	private static void comparaDegrees (String usuario, ArrayList<String> pDegrees, ArrayList<String> xDegrees, String lang)
			throws ExcepcionChecker
	{
		CheckerFailure cf;
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();

		if (pDegrees.size() != xDegrees.size()) {
			cf = new CheckerFailure("", "20_DIFS", currentMethod+": "+Msgs.getMsg(Msgs.CC56,lang)+pDegrees.size()+" degrees, "+Msgs.getMsg(Msgs.CC57,lang)+xDegrees.size()); // CC56...CC57 = debería devolver... pero ddevuelve
			throw new ExcepcionChecker(cf);
		}


		for (int x=0; x < pDegrees.size(); x++)
			if (!xDegrees.get(x).equals(pDegrees.get(x))) {
				cf = new CheckerFailure("", "20_DIFS", currentMethod+": "+String.format(MsgsEAMLChecker.getMsg("36",lang), xDegrees.get(x), x, pDegrees.get(x))); // "Diferencia en la lista de degrees: se recibe '%s' en la posición %d, pero se esperaba '%s'"
				throw new ExcepcionChecker(cf);
			}

		return;
	}





	// pide y devuelve la lista de students de un degree
	// levanta ExcepcionChecker si algo va mal

	private static ArrayList<Student> requestStudentsDegree (String call, String lang)
					throws ExcepcionChecker
	{
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();
		CheckerFailure cf;
		Document doc;
		ArrayList<Student> listaStudents = new ArrayList<Student>();

		CommonEAChecker.errorHandler.clear();

		try {
			doc = CommonEAChecker.db.parse(call);
		}
		catch (SAXException ex) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo(currentMethod+": SAXException: "+MsgsEAMLChecker.getMsg("27",lang)+"students"); // solicitando/parseando la lista de
			throw new ExcepcionChecker(cf);
		}
		catch (Exception ex) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", ex.toString());
			cf.addMotivo(currentMethod+": Exception: "+MsgsEAMLChecker.getMsg("27",lang)+"students"); // solicitando/parseando la lista de
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
			cf.addMotivo(currentMethod+": "+String.format(MsgsEAMLChecker.getMsg("28",lang), "errors")+"students"); // resultado inválido, errors en la lista de
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
			cf.addMotivo(currentMethod+": "+String.format(MsgsEAMLChecker.getMsg("28",lang), "fatalerrors")+"students"); // resultado inválido, fatal errors en la lista de
			throw new ExcepcionChecker(cf);
		}


		if (doc == null) {
			cf = new CheckerFailure(call, "", currentMethod+": "+MsgsEAMLChecker.getMsg("30",lang)+"students");   // el parser devuelve 'null' al parsear la lista de
			throw new ExcepcionChecker(cf);
		}


		NodeList nlStudents = doc.getElementsByTagName("students");

		if (nlStudents.getLength() != 1) {
		    	cf = new CheckerFailure(call, "", currentMethod+": "+MsgsEAMLChecker.getMsg("33",lang));  // No se recibe '&lt;students>' al solicitar y parsear la lista de students
			throw new ExcepcionChecker(cf);
		}

		nlStudents = doc.getElementsByTagName("student");

		// procesamos todos los students

		for (int x=0; x < nlStudents.getLength(); x++) {
			Element elemStudent = (Element)nlStudents.item(x);
			String nombreStudent= elemStudent.getTextContent().trim();

			String id = elemStudent.getAttribute("id");
			String address = elemStudent.getAttribute("address");

			listaStudents.add(new Student(nombreStudent, id, address));
		}

		return listaStudents;
	}


	// para comparar el resultado de la F22: listas de students
	// no devuelve nada si son iguales
	// levanta ExcepcionChecker si hay diferencias

	private static void comparaStudents (String usuario, String degreeActual, ArrayList<Student> pStudents, ArrayList<Student> xStudents, String lang)
				throws ExcepcionChecker
	{
		CheckerFailure cf;
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();

			if (pStudents.size() != xStudents.size()) {
			cf = new CheckerFailure("", "20_DIFS", currentMethod+": STUDENTS ("+usuario+"+"+degreeActual+"): "+Msgs.getMsg(Msgs.CC56,lang)+pStudents.size()+" students, "+Msgs.getMsg(Msgs.CC57,lang)+xStudents.size()); // CC56...CC57 = debería devolver...pero devuelve
			throw new ExcepcionChecker(cf);
			}

			String pNombre, xNombre, pId, xId, pAddress, xAddress;

			for (int z=0; z < pStudents.size(); z++) {
						pNombre = pStudents.get(z).getNombre();
						xNombre = xStudents.get(z).getNombre();

						if (!xNombre.equals(pNombre)) {
								cf = new CheckerFailure("", "20_DIFS", currentMethod+": STUDENTS ('"+usuario+"', '"+degreeActual+"'): "+String.format(MsgsEAMLChecker.getMsg("40",lang), pNombre, z, xNombre)); // "Se esperaba el student '%s' en la posición %d y se recibió '%s'"
								throw new ExcepcionChecker(cf);
						}

						pId = pStudents.get(z).getId();
						xId = xStudents.get(z).getId();

						if (!xId.equals(pId)) {
								cf = new CheckerFailure("", "20_DIFS", currentMethod+": STUDENTS ('"+usuario+"', '"+degreeActual+"'): "+String.format(MsgsEAMLChecker.getMsg("41",lang), pId, z, xId)); // "Se esperaba el ID '%s' en la posición %d y se recibió '%s'"
								 throw new ExcepcionChecker(cf);
						}

						pAddress = pStudents.get(z).getDireccion();
						xAddress = xStudents.get(z).getDireccion();

						if (!xAddress.equals(pAddress)) {
								 cf = new CheckerFailure("", "20_DIFS", currentMethod+": STUDENTS ('"+usuario+"', '"+degreeActual+"'): "+String.format(MsgsEAMLChecker.getMsg("42",lang), pAddress, z, xAddress)); // "Se esperaba la dirección '%s' en la posición %d y se recibió '%s'"
								 throw new ExcepcionChecker(cf);
						}
			}

			return;
	}




	// pide y devuelve la lista de subject (notas) de un student de un degree
	// levanta ExcepcionChecker si algo va mal

	private static ArrayList<Subject> requestSubjectsStudentDegree (String call, String lang)
									throws ExcepcionChecker
	{
		String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();
		CheckerFailure cf;
		Document doc;
		ArrayList<Subject> listaSubjects = new ArrayList<Subject>();

		CommonEAChecker.errorHandler.clear();

		try {
			doc = CommonEAChecker.db.parse(call);
		}
		catch (SAXException e) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo(currentMethod+": SAXException: "+MsgsEAMLChecker.getMsg("27",lang)+"subjects"); // solicitando/parseando la lista de
			throw new ExcepcionChecker(cf);
		}
		catch (Exception e) {
			CommonEAChecker.logCall(call);

			cf = new CheckerFailure(call, "", e.toString());
			cf.addMotivo(currentMethod+": Exception: "+MsgsEAMLChecker.getMsg("27",lang)+"subjects"); // solicitando/parseando la lista de
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
			cf.addMotivo(currentMethod+": "+String.format(MsgsEAMLChecker.getMsg("28",lang), "errors")+"subjects"); // resultado inválido, errors en la lista de
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
			cf.addMotivo(currentMethod+": "+String.format(MsgsEAMLChecker.getMsg("28",lang), "fatalerrors")+"subjects"); // resultado inválido, fatalerrors en la lista de
			throw new ExcepcionChecker(cf);
		}


		if (doc == null) {
			cf = new CheckerFailure(call, "", currentMethod+": "+MsgsEAMLChecker.getMsg("30",lang)+"subjects");   // el parser devuelve 'null' al parsear la lista de
			throw new ExcepcionChecker(cf);
		}

		NodeList nlSubjects = doc.getElementsByTagName("subjects");

		if (nlSubjects.getLength() != 1) {
		    	cf = new CheckerFailure(call, "", currentMethod+": "+MsgsEAMLChecker.getMsg("32",lang));  // No se recibe '&lt;subjects>' al solicitar y parsear la lista de subjects
			throw new ExcepcionChecker(cf);
		}

		nlSubjects = doc.getElementsByTagName("subject");

		// procesamos todas los subjects

		for (int x=0; x < nlSubjects.getLength(); x++) {
			Element elemSubject = (Element)nlSubjects.item(x);

			String nombre = elemSubject.getTextContent().trim();

			String idSub = elemSubject.getAttribute("idSub");
			String grade = elemSubject.getAttribute("grade");

			listaSubjects.add(new Subject(nombre, "", "", idSub, grade)); // curso y tipo no se reciben
		}

		return listaSubjects;
	}


		// para comparar el resultado de la F23: listas de subjects
		// no devuelve nada si son iguales
		// levanta ExcepcionChecker si hay diferencias

		private static void comparaSubjects (String usuario, String degreeActual, String studentActual, ArrayList<Subject> pSubjects, ArrayList<Subject> xSubjects, String lang)
				throws ExcepcionChecker
		{
			String currentMethod = Thread.currentThread().getStackTrace()[1].getMethodName();
			CheckerFailure cf;

			if (pSubjects.size() != xSubjects.size()) {
				cf = new CheckerFailure("", "20_DIFS", currentMethod+": SUBJECTS ("+usuario+"+"+degreeActual+"+"+studentActual+"): "+Msgs.getMsg(Msgs.CC56,lang)+pSubjects.size()+" subjects, "+Msgs.getMsg(Msgs.CC57,lang)+xSubjects.size());  // CC56...CC57 = debería devolver...pero devuelve
				throw new ExcepcionChecker(cf);
			}

			String pNombre, xNombre, pIdSub, xIdSub, pGrade, xGrade;

			for (int y=0; y < pSubjects.size(); y++) {

				pNombre = pSubjects.get(y).getNombre();
				xNombre = xSubjects.get(y).getNombre();

				if (!xNombre.equals(pNombre)) {
					cf = new CheckerFailure("", "20_DIFS", currentMethod+": SUBJECTS ('"+usuario+"', '" +degreeActual+"+"+studentActual+"'): "+String.format(MsgsEAMLChecker.getMsg("37",lang), pNombre, y, xNombre)); // "Se esperaba el subject '%s' en la posición %d y se recibió '%s'"
					throw new ExcepcionChecker(cf);
				}

				pIdSub = pSubjects.get(y).getCurso();
				xIdSub = xSubjects.get(y).getCurso();

				if (!xIdSub.equals(pIdSub)) {
					cf = new CheckerFailure("", "20_DIFS", currentMethod+": SUBJECTS ('"+usuario+"', '"+degreeActual+"+"+studentActual+"'): "+String.format(MsgsEAMLChecker.getMsg("43",lang), pIdSub, y, xIdSub)); // "Se esperaba el idSub '%s' en la posición %d y se recibió '%s'"
					throw new ExcepcionChecker(cf);
				}

				pGrade = pSubjects.get(y).getTipo();
				xGrade = xSubjects.get(y).getTipo();

			   	if (!xGrade.equals(pGrade)) {
					cf = new CheckerFailure("", "20_DIFS", currentMethod+": SUBJECTS ('"+usuario+"', '"+degreeActual+"+"+studentActual+"'): "+String.format(MsgsEAMLChecker.getMsg("44",lang), pGrade, y, xGrade)); // "Se esperaba el grade '%s' en la posición %d y se recibió '%s'"
					throw new ExcepcionChecker(cf);
			   	}
			}

			return;
		}






}
