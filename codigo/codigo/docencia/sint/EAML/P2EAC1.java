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

// consulta 1: alumnos de una asignatura en una titulación

package docencia.sint.EAML;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import docencia.sint.Common.CommonSINT;
import docencia.sint.Common.Msgs;


   // MÉTODOS PARA LA PRIMERA CONSULTA

public class P2EAC1 {

    // F11: método que imprime o devuelve la lista de titulaciones

    public static void doGetF11Degrees (HttpServletRequest request, HttpServletResponse response,
		                                    String language, String auto, String fe) throws IOException {

	    	ArrayList<String> degreesList = DataModel.getC1Degrees();   // se pide la lista de titulaciones

	    	if (degreesList.size() == 0) {
					// 105 = "No hay titulaciones"
	    		CommonSINT.doBadRequest(MsgsEAML.getMsg("105", language), request, response);
	    		return;
	    	}

        if (auto.equals("true")) {
            response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            P2EAC1FE.printF11XML(out, degreesList);
        }
        else {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            P2EAC1FE.printF11HTML(out, fe, degreesList, language);
	    	}
    }




    // F12: método que imprime o devuelve la lista de asignaturas de una titulación

    public static void doGetF12Subjects (HttpServletRequest request, HttpServletResponse response,
		                                     String language, String auto, String fe) throws IOException {

	    ArrayList<Subject> subjectsList;

	    String pdegree = request.getParameter("pdegree");
	    if (pdegree == null) {
					// CP08 = "No param:"
	    	CommonSINT.doBadRequest(Msgs.getMsg(Msgs.CP08, language)+"pdegree", request, response);
	    	return;
	    }

	    subjectsList = DataModel.getC1Subjects(pdegree);  // se pide la lista de asignaturas de la titulación seleccionada
	    if (subjectsList == null) {
					// 106 = "La titulación %s no existe"
	    		CommonSINT.doBadRequest(String.format(MsgsEAML.getMsg("106", language), pdegree), request, response);
	    		return;
	    }

      if (auto.equals("true")) {
          response.setContentType("text/xml");
          PrintWriter out = response.getWriter();
          P2EAC1FE.printF12XML(out, subjectsList);
      }
      else {
          response.setContentType("text/html");
          PrintWriter out = response.getWriter();
          P2EAC1FE.printF12HTML(out, fe, pdegree, subjectsList, language);
      }

    }





    // F13: método que imprime o devuelve la lista de alumnos de una asignatura de una titulación

    public static void doGetF13Students (HttpServletRequest request, HttpServletResponse response,
		                                     String language, String auto, String fe) throws IOException
    {
    		ArrayList<Student> studentsList;

	    	String pdegree = request.getParameter("pdegree");
	    	if (pdegree == null) {
					// CP08 = "No param:"
	    		CommonSINT.doBadRequest(Msgs.getMsg(Msgs.CP08, language)+"pdegree", request, response);
	    		return;
	    	}

	    	String psubject = request.getParameter("psubject");
	    	if (psubject == null) {
					// CP08 = "No param:"
	    		CommonSINT.doBadRequest(Msgs.getMsg(Msgs.CP08, language)+"psubject", request, response);
	    		return;
	    	}


	    	studentsList = DataModel.getC1Students(pdegree, psubject);   // pedimos la lista de alumnos de una materia de una titulación
	    	if (studentsList == null) {
					// 107 = "La titulación %s o la asignatura %s no existe: "
	    		CommonSINT.doBadRequest(String.format(MsgsEAML.getMsg("107", language), pdegree, psubject), request, response);
	    		return;
	    	}

        if (auto.equals("true")) {
            response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            P2EAC1FE.printF13XML(out, studentsList);
        }
        else {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            P2EAC1FE.printF13HTML(out, fe, pdegree, psubject, studentsList, language);
        }
    }

}
