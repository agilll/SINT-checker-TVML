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


public class P2EAC1FE  {

    // XML
    public static void printF11XML (PrintWriter out, ArrayList<String> degrees)
    {
      out.println("<?xml version='1.0' encoding='utf-8'?>");
      out.println("<degrees>");

      for (int x=0; x < degrees.size(); x++)
          out.println("<degree>"+degrees.get(x).trim()+"</degree>");

      out.println("</degrees>");
    }


    public static void printF12XML (PrintWriter out, ArrayList<Subject> subjects)
    {
      Subject subject;

      out.println("<?xml version='1.0' encoding='utf-8'?>");
      out.println("<subjects>");

      for (int x=0; x < subjects.size(); x++) {
        subject = subjects.get(x);
        out.println("<subject course='"+subject.getCurso()+"' type='"+subject.getTipo()+"' >"+subject.getNombre()+"</subject>");
      }

      out.println("</subjects>");
    }


    public static void printF13XML (PrintWriter out, ArrayList<Student> students)
    {
      Student student;

      out.println("<?xml version='1.0' encoding='utf-8'?>");
      out.println("<students>");

      for (int x=0; x < students.size(); x++) {
        student = students.get(x);
        out.println("<student id='"+student.getId()+"'  address='"+student.getDireccion()+"'>"+student.getNombre()+"</student>");
      }

      out.println("</students>");
    }





    // HTML y AJAX
    public static void printF11HTML (PrintWriter out, String fe, ArrayList<String> degrees, String language)
    {
      if (fe.equals("html")) {
          out.println("<html>");
          CommonEAML.printHead(language, out);
          out.println("<body>");
					// 001 = "Servicio de consulta de expedientes académicos"
          out.println("<h2>"+MsgsEAML.getMsg("001", language)+"</h2>");
      }

			// 102 = "Consulta 1: Fase 1"
      out.println("<h3>"+MsgsEAML.getMsg("102", language)+"</h3>");
			// 108 = "Selecciona una titulación:"
      out.println("<h3>"+MsgsEAML.getMsg("108", language)+"</h3>");

      if (fe.equals("html")) {
          out.println("<ol>");

          for (int x=0; x < degrees.size(); x++)
            out.println("<li> <a href='?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=12&pdegree="+degrees.get(x)+"'>"+degrees.get(x)+"</a>");

          out.println("</ol>");
          out.println("<button class='home' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=01\"'>"+Msgs.getMsg(Msgs.CB01, language)+"</button>");  //CB01=Inicio

          CommonSINT.printFoot(out, MsgsEAML.CURSO);
          out.println("</body></html>");
      }
      else { //ajax
        out.println("<ol>");

        for (int x=0; x < degrees.size(); x++) {
             String cad = "<li><u onclick='sendRequest(\"12\", \"&pdegree="+degrees.get(x)+"\");'>"+degrees.get(x)+"</u>";
             out.println(cad);
        }

        out.println("</ol>");
      }
    }




    public static void printF12HTML (PrintWriter out, String fe, String pdegree, ArrayList<Subject> subjects, String language)
    {
        Subject subject;

        if (fe.equals("html")) {
            out.println("<html>");
            CommonEAML.printHead(language, out);
            out.println("<body>");
						// 001 = "Servicio de consulta de expedientes académicos"
	          out.println("<h2>"+MsgsEAML.getMsg("001", language)+"</h2>");
        }

				// 103 = "Consulta 1: Fase 2 (Titulación=%s)"
        out.println("<h3>"+String.format(MsgsEAML.getMsg("103", language), pdegree)+"</h3>");

				// 110 ="No hay asignaturas en la titulación"
        if (subjects.size() == 0)
						out.println(MsgsEAML.getMsg("110", language)+pdegree);
				// 109 = "Selecciona una asignatura:"
				else
					out.println("<h3>"+MsgsEAML.getMsg("109", language)+"</h3>");

        if (fe.equals("html")) {
            out.println("<ol>");

            for (int x=0; x < subjects.size(); x++) {
              subject = subjects.get(x);
							// 111="Asignatura"    112="Curso"     113="Tipo"
              out.println("<li><a href='?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=13&pdegree="+pdegree+"&psubject="+subject.getNombre()+"'> <b>"+MsgsEAML.getMsg("111", language)+"</b> = '"+
                       subject.getNombre()+"'</a>  ---  <b>"+MsgsEAML.getMsg("112", language)+"</b> = '"+subject.getCurso()+"'  ---  <b>"+MsgsEAML.getMsg("113", language)+"</b> = '"+subject.getTipo()+"'");
            }

            out.println("</ol>");
            out.println("<button class='home' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=01\"'>"+Msgs.getMsg(Msgs.CB01, language)+"</button>");  //CB01=Inicio
						out.println("<button class='back' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=11\"'>"+Msgs.getMsg(Msgs.CB02, language)+"</button>");  //CB02=Atrás vuelve a la fase 11

            CommonSINT.printFoot(out, MsgsEAML.CURSO);
            out.println("</body></html>");
        }
        else {
              out.println("<ol>");

              for (int x=0; x < subjects.size(); x++) {
                subject = subjects.get(x);
								// 111="Asignatura"    112="Curso"     113="Tipo"
                String cad = "<li><u onclick='sendRequest(\"13\", \"&pdegree="+pdegree+"&psubject="+subject.getNombre()+"\");'> <b>"+MsgsEAML.getMsg("111", language)+"</b> ="+
								       subject.getNombre()+"</u> --- <b>"+MsgsEAML.getMsg("112", language)+"</b>="+subject.getCurso()+" --- <b>"+MsgsEAML.getMsg("113", language)+"</b>="+subject.getTipo();
                out.println(cad);
              }

              out.println("</ol>");
        }
    }



    public static void printF13HTML (PrintWriter out, String fe, String pdegree, String psubject, ArrayList<Student> students, String language)
    {
        Student student;

        if (fe.equals("html")) {
            out.println("<html>");
            CommonEAML.printHead(language, out);
            out.println("<body>");
						// 001 = "Servicio de consulta de expedientes académicos"
						out.println("<h2>"+MsgsEAML.getMsg("001", language)+"</h2>");
        }

				// 104 = "Consulta 1: Fase 3  (Titulación = %s, Asignatura = %s)"
        out.println("<h3>"+String.format(MsgsEAML.getMsg("104", language), pdegree, psubject)+"</h3>");
        out.println("<h3>"+Msgs.getMsg(Msgs.CP07, language)+"</h3>"); // CP07 = "Este es el resultado: "

					// 114 = "No hay alumnos en la asignatura %s de %s"
        if (students.size() == 0)
          out.println(String.format(MsgsEAML.getMsg("114", language), psubject, pdegree));

        if (fe.equals("html")) {
            out.println("<ol>");

            for (int x=0; x < students.size(); x++) {
              student = students.get(x);
							// 115 = "Nombre"  116="Dirección"
              out.println(" <li><b>"+MsgsEAML.getMsg("115", language)+"</b> = '"+student.getNombre()+"'  ---  <b>ID</b> = '"+student.getId()+
                          "'  ---  <b>"+MsgsEAML.getMsg("116", language)+"</b> = '"+student.getDireccion()+"'");
            }

            out.println("</ol>");
            out.println("<button class='home' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=01\"'>"+Msgs.getMsg(Msgs.CB01, language)+"</button>"); //CB01=Inicio
						out.println("<button class='back' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=12&pdegree="+pdegree+"\"'>"+Msgs.getMsg(Msgs.CB02, language)+"</button>");  //CB02=Atrás vuelve a la fase 12

            CommonSINT.printFoot(out, MsgsEAML.CURSO);
            out.println("</body></html>");
        }
        else {
            out.println("<ol>");

            for (int x=0; x < students.size(); x++) {
              student = students.get(x);
							// 115 = "Nombre"  116="Dirección"
              String cad = "<li><b>"+MsgsEAML.getMsg("115", language)+"</b>="+student.getNombre()+" --- <b>ID</b>="+student.getId()+
                           " --- <b>"+MsgsEAML.getMsg("116", language)+"</b>="+student.getDireccion()+"'";
              out.println(cad);
            }

            out.println("</ol>");
        }
    }

}
