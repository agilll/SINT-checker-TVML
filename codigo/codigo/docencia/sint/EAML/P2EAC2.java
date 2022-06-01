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

// consulta 2: notas de un alumno de una titulación

package docencia.sint.EAML;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import docencia.sint.Common.CommonSINT;
import docencia.sint.Common.Msgs;


   // MÉTODOS PARA LA SEGUNDA CONSULTA

public class P2EAC2 {

    // F21: método que imprime o devuelve la lista de titulaciones

    public static void doGetF21Degrees (HttpServletRequest request, HttpServletResponse response,
		                                    String language, String auto, String fe) throws IOException {

	    	ArrayList<String> degreesList = DataModel.getC2Degrees();   // se pide la lista de titulaciones

				if (degreesList.size() == 0) {
					// 105 = "No hay titulaciones"
	    		CommonSINT.doBadRequest(MsgsEAML.getMsg("105", language), request, response);
	    		return;
	    	}

        if (auto.equals("true")) {
            response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            P2EAC2FE.printF21XML(out, degreesList);
        }
        else {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            P2EAC2FE.printF21HTML(out, fe, degreesList, language);
	    	}
    }





	  // F22: método que imprime o devuelve la lista de alumnos de una titulación

	  public static void doGetF22Students (HttpServletRequest request, HttpServletResponse response,
																					String language, String auto, String fe) throws IOException
	  {
	    		ArrayList<Student> studentsList;

		    	String pdegree = request.getParameter("pdegree");
		    	if (pdegree == null) {
						// CP08 = "No param"
		    		CommonSINT.doBadRequest(Msgs.getMsg(Msgs.CP08, language)+"pdegree", request, response);
		    		return;
		    	}

		    	studentsList = DataModel.getC2Students(pdegree);   // pedimos la lista de alumnos de una titulación
					if (studentsList == null) {
							// 206 = "La titulación %s no existe"
			    		CommonSINT.doBadRequest(String.format(MsgsEAML.getMsg("206", language), pdegree), request, response);
			    		return;
			    }

	        if (auto.equals("true")) {
	            response.setContentType("text/xml");
	            PrintWriter out = response.getWriter();
	            P2EAC2FE.printF22XML(out, studentsList);
	        }
	        else {
	            response.setContentType("text/html");
	            PrintWriter out = response.getWriter();
	            P2EAC2FE.printF22HTML(out, fe, pdegree, studentsList, language);
	        }
	    }




    // F23: método que imprime o devuelve la lista de asignaturas/notas de un alumno de una titulación

    public static void doGetF23Subjects (HttpServletRequest request, HttpServletResponse response,
		                                     String language, String auto, String fe) throws IOException {

	    ArrayList<Subject> subjectsList;

	    String pdegree = request.getParameter("pdegree");
	    if (pdegree == null) {
				// CP08 = "No param"
	    	CommonSINT.doBadRequest(Msgs.getMsg(Msgs.CP08, language)+"pdegree", request, response);
	    	return;
	    }

			String pstudent = request.getParameter("pstudent");
			if (pstudent == null) {
				// CP08 = "No param"
				CommonSINT.doBadRequest(Msgs.getMsg(Msgs.CP08, language)+"pstudent", request, response);
				return;
			}

	    subjectsList = DataModel.getC2Subjects(pdegree, pstudent);  // se pide la lista de asignaturas/notas del alumno seleccionado de la titulación seleccionada
	    if (subjectsList == null) {
					// 207 = "La titulación %s o el alumno %s no existe"
	    		CommonSINT.doBadRequest(String.format(MsgsEAML.getMsg("207", language), pdegree, pstudent), request, response);
	    		return;
	    }

      if (auto.equals("true")) {
          response.setContentType("text/xml");
          PrintWriter out = response.getWriter();
          P2EAC2FE.printF23XML(out, subjectsList);
      }
      else {
          response.setContentType("text/html");
          PrintWriter out = response.getWriter();
          P2EAC2FE.printF23HTML(out, fe, pdegree, pstudent, subjectsList, language);
      }

    }

}
