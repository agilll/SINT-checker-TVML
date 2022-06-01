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


package docencia.sint.EAML;

import java.io.PrintWriter;
import java.util.ArrayList;

import docencia.sint.Common.CommonSINT;
import docencia.sint.Common.Msgs;


public class P2EAC2FE  {

    // XML
    public static void printF21XML (PrintWriter out, ArrayList<String> degrees)
    {
      out.println("<?xml version='1.0' encoding='utf-8'?>");
      out.println("<degrees>");

      for (int x=0; x < degrees.size(); x++)
          out.println("<degree>"+degrees.get(x).trim()+"</degree>");

      out.println("</degrees>");
    }


    public static void printF22XML (PrintWriter out, ArrayList<Student> students)
    {
			Student student;

      out.println("<?xml version='1.0' encoding='utf-8'?>");
      out.println("<students>");

      for (int x=0; x < students.size(); x++) {
        student = students.get(x);
        out.println("<student id='"+student.getId()+"' address='"+student.getDireccion()+"' >"+student.getNombre()+"</student>");
      }

      out.println("</students>");
    }


    public static void printF23XML (PrintWriter out, ArrayList<Subject> subjects)
    {
      Subject subject;

      out.println("<?xml version='1.0' encoding='utf-8'?>");
      out.println("<subjects>");

      for (int x=0; x < subjects.size(); x++) {
        subject = subjects.get(x);
        out.println("<subject idSub='"+subject.getIdSub()+"'  grade='"+subject.getGrade()+"'>"+subject.getNombre()+"</subject>");
      }

      out.println("</subjects>");
    }





    // HTML y AJAX
    public static void printF21HTML (PrintWriter out, String fe, ArrayList<String> degrees, String language)
    {
      if (fe.equals("html")) {
          out.println("<html>");
          CommonEAML.printHead(language, out);
          out.println("<body>");
					// 001 = "Servicio de consulta de expedientes académicos"
          out.println("<h2>"+MsgsEAML.getMsg("001", language)+"</h2>");
      }

			// 202 = "Consulta 2: Fase 1"
      out.println("<h3>"+MsgsEAML.getMsg("202", language)+"</h3>");
			// 208 = "Selecciona una titulación:"
      out.println("<h3>"+MsgsEAML.getMsg("208", language)+"</h3>");

      if (fe.equals("html")) {
          out.println("<ol>");

          for (int x=0; x < degrees.size(); x++)
            out.println("<li> <a href='?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=22&pdegree="+degrees.get(x)+"'>"+degrees.get(x)+"</a>");

          out.println("</ol>");
          out.println("<button class='home' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=01\"'>"+Msgs.getMsg(Msgs.CB01, language)+"</button>");  //CB01=Inicio

          CommonSINT.printFoot(out, MsgsEAML.CURSO);
          out.println("</body></html>");
      }
      else { //ajax
        out.println("<ol>");

        for (int x=0; x < degrees.size(); x++) {
             String cad = "<li><u onclick='sendRequest(\"22\", \"&pdegree="+degrees.get(x)+"\");'>"+degrees.get(x)+"</u>";
             out.println(cad);
        }

        out.println("</ol>");
      }
    }




    public static void printF22HTML (PrintWriter out, String fe, String pdegree, ArrayList<Student> students, String language)
    {
        Student student;

        if (fe.equals("html")) {
            out.println("<html>");
            CommonEAML.printHead(language, out);
            out.println("<body>");
						// 001 = "Servicio de consulta de expedientes académicos"
	          out.println("<h2>"+MsgsEAML.getMsg("001", language)+"</h2>");
        }

				// 203 = "Consulta 2: Fase 2 (Titulación = %s)"
        out.println("<h3>"+String.format(MsgsEAML.getMsg("203", language), pdegree)+"</h3>");

				// 210 ="No hay alumnos en esta titulación"
        if (students.size() == 0)
            out.println(MsgsEAML.getMsg("210", language)+pdegree);
				else
						// 209 = "Selecciona un alumno:"
						out.println("<h3>"+MsgsEAML.getMsg("209", language)+"</h3>");

        if (fe.equals("html")) {
            out.println("<ol>");

            for (int x=0; x < students.size(); x++) {
              student = students.get(x);
							// 211="Nombre"   212="ID"   213="Direccion"
              out.println("<li><a href='?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=23&pdegree="+pdegree+"&pstudent="+student.getId()+"'> <b>"+MsgsEAML.getMsg("211", language)+"</b> = '"+
                       student.getNombre()+"'</a>  ---  <b>"+MsgsEAML.getMsg("212", language)+"</b> = '"+student.getId()+"'  ---  <b>"+MsgsEAML.getMsg("213", language)+"</b> = '"+student.getDireccion()+"'");
            }

            out.println("</ol>");
            out.println("<button class='home' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=01\"'>"+Msgs.getMsg(Msgs.CB01, language)+"</button>");  //CB01=Inicio
						out.println("<button class='back' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=21\"'>"+Msgs.getMsg(Msgs.CB02, language)+"</button>");  //CB02=Atrás vuelve a la fase 21

            CommonSINT.printFoot(out, MsgsEAML.CURSO);
            out.println("</body></html>");
        }
        else {
              out.println("<ol>");

              for (int x=0; x < students.size(); x++) {
                student = students.get(x);
                String cad = "<li><u onclick='sendRequest(\"23\", \"&pdegree="+pdegree+"&pstudent="+student.getId()+"\");'> <b>Student</b> ="+student.getNombre()+"</u> --- <b>ID</b>="+student.getId()+" --- <b>Address</b>="+student.getDireccion();
                out.println(cad);
              }

              out.println("</ol>");
        }
    }



    public static void printF23HTML (PrintWriter out, String fe, String pdegree, String pstudent, ArrayList<Subject> subjects, String language)
    {
        Subject subject;

        if (fe.equals("html")) {
            out.println("<html>");
            CommonEAML.printHead(language, out);
            out.println("<body>");
						// 001 = "Servicio de consulta de expedientes académicos"
	          out.println("<h2>"+MsgsEAML.getMsg("001", language)+"</h2>");
        }

				// 204 = "Consulta 2: Fase 3  (Titulación = %s, Alumno = %s)"
        out.println("<h3>"+String.format(MsgsEAML.getMsg("204", language), pdegree, pstudent)+"</h3>");
        out.println("<h3>"+Msgs.getMsg(Msgs.CP07, language)+"</h3>"); // CP07 = "Este es el resultado: "

				// 214 = "No hay asignaturas para el alumno %s de %s"
        if (subjects.size() == 0)
          out.println("<h3>"+String.format(MsgsEAML.getMsg("214", language), pstudent, pdegree)+"</h3>");

        if (fe.equals("html")) {
            out.println("<ol>");

            for (int x=0; x < subjects.size(); x++) {
              subject = subjects.get(x);
							// 215 = "Nombre"   216="ID"   217="Nota"
              out.println(" <li><b>"+MsgsEAML.getMsg("215", language)+"</b> = '"+subject.getNombre()+"'  ---  <b>"+MsgsEAML.getMsg("216", language)+"</b> = '"+subject.getIdSub()+
                          "'  ---  <b>"+MsgsEAML.getMsg("217", language)+"</b> = '"+subject.getGrade()+"'");
            }

            out.println("</ol>");
            out.println("<button class='home' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=01\"'>"+Msgs.getMsg(Msgs.CB01, language)+"</button>");  //CB01=Inicio
						out.println("<button class='back' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=22&pdegree="+pdegree+"\"'>"+Msgs.getMsg(Msgs.CB02, language)+"</button>");  //CB02=Atrás vuelve a la fase 22

            CommonSINT.printFoot(out, MsgsEAML.CURSO);
            out.println("</body></html>");
        }
        else {
            out.println("<ol>");

            for (int x=0; x < subjects.size(); x++) {
              subject = subjects.get(x);
							// 215 = "Nombre"   216="ID"   217="Nota"
              String cad = "<li><b>"+MsgsEAML.getMsg("215", language)+"</b>="+subject.getNombre()+" --- <b>"+MsgsEAML.getMsg("216", language)+"</b>="+subject.getIdSub()+
                           " --- <b>"+MsgsEAML.getMsg("217", language)+"</b>="+subject.getGrade()+"'";
              out.println(cad);
            }

            out.println("</ol>");
        }
    }




}
