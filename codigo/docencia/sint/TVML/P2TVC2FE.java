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


package docencia.sint.TVML;

import java.io.PrintWriter;

import java.util.ArrayList;

import docencia.sint.Common.CommonSINT;


public class P2TVC2FE  {

    // XML
    public static void printF21XML (PrintWriter out, ArrayList<String> langs)
    {
      out.println("<?xml version='1.0' encoding='utf-8'?>");
      out.println("<langs>");

      for (int x=0; x < langs.size(); x++)
        out.println("<lang>"+langs.get(x).trim()+"</lang>");

      out.println("</langs>");
    }


    public static void printF22XML (PrintWriter out, ArrayList<Programa> programas)
    {
      Programa programa;

      out.println("<?xml version='1.0' encoding='utf-8'?>");

      out.println("<infantiles>");

      for (int x=0; x < programas.size(); x++) {
        programa = programas.get(x);

        out.println("<infantil dia='"+programa.getDiaEmision()+"' resumen='"+programa.getResumen()+"' >"+programa.getNombrePrograma()+"</infantil>");
      }

      out.println("</infantiles>");
    }


    public static void printF23XML (PrintWriter out, ArrayList<Canal> canales)
    {
      Canal canal;

      out.println("<?xml version='1.0' encoding='utf-8'?>");
      out.println("<canales>");

      for (int x=0; x < canales.size(); x++) {
        canal = canales.get(x);
        out.println("<canal id='"+canal.getIdCanal()+"'  grupo='"+canal.getGrupo()+"'>" +canal.getNombreCanal()+"</canal>");
      }

      out.println("</canales>");
    }



    // HTML y AJAX
    public static void printF21HTML (PrintWriter out, String fe, ArrayList<String> langs)
    {
      if (fe.equals("html")) {
          out.println("<html>");
          CommonTVML.printHead(out);
          out.println("<body>");
          out.println("<h2>"+MsgsTVML.M2+"</h2>");
      }

      out.println("<h3>Consulta 2: Fase 1</h3>");
      out.println("<h3>Idiomas disponibles. Selecciona uno:</h3>");

      if (fe.equals("html")) {
          out.println("<ol>");

          for (int x=0; x < langs.size(); x++)
             out.println("<li> <a href='?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=22&plang="+langs.get(x)+"'>"+langs.get(x)+"</a>");

          out.println("</ol>");
          out.println("<button class='home' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=01\"'>Inicio</button>");  // Inicio

          CommonSINT.printFoot(out, MsgsTVML.CURSO);
          out.println("</body></html>");
      }
      else {
          out.println("<ol>");

          for (int x=0; x < langs.size(); x++) {
               String cad = "<li><u onclick='sendRequest(\"22\", \"&plang="+langs.get(x)+"\");'>"+langs.get(x)+"</u>";
               out.println(cad);
          }

          out.println("</ol>");
        }
    }


    public static void printF22HTML (PrintWriter out, String fe, String plang, ArrayList<Programa> programas)
    {
       Programa programa;

       if (fe.equals("html")) {
           out.println("<html>");
           CommonTVML.printHead(out);
           out.println("<body>");
           out.println("<h2>"+MsgsTVML.M2+"</h2>");
       }

       out.println("<h3>Consulta 2: Fase 2 (Idioma="+plang+")</h3>");
       out.println("<h3>Programas infantiles en el idioma seleccionado. Selecciona uno:</h3>");

       if (programas.size() == 0)
           out.println("No hay programas infantiles en el idioma  "+plang);

       if (fe.equals("html")) {
           out.println("<ol>");

           for (int x=0; x < programas.size(); x++) {
             programa = programas.get(x);

             out.println("<li><a href='?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=23&plang="+plang+"&pdia="+programa.getDiaEmision()+"'> <b>Programa</b> = '"+
                      programa.getNombrePrograma()+"'</a>  ---  <b>Día</b> = '"+programa.getDiaEmision()+"'  ---  <b>Resumen</b> = '"+programa.getResumen()+"'");
           }

           out.println("</ol>");
           out.println("<button class='home' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=01\"'>Inicio</button>");  // Inicio
           out.println("<button class='back' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=21\"'>Atrás</button>"); // Atrás vuelve a la fase 21

           CommonSINT.printFoot(out, MsgsTVML.CURSO);
           out.println("</body></html>");
        }
        else {
            out.println("<ol>");

            for (int x=0; x < programas.size(); x++) {
              programa = programas.get(x);
              String cad = "<li><u onclick='sendRequest(\"23\", \"&plang="+plang+"&pdia="+programa.getDiaEmision()+"\");'><b>Programa</b>="+programa.getNombrePrograma()+"</u> --- <b>Día</b>="+programa.getDiaEmision()+" --- <b>Resumen</b>="+programa.getResumen();
              out.println(cad);
            }

            out.println("</ol>");
        }
     }



     public static void printF23HTML (PrintWriter out, String fe, String plang, String pdia, ArrayList<Canal> canales)
     {
        Canal canal;

        if (fe.equals("html")) {
            out.println("<html>");
            CommonTVML.printHead(out);
            out.println("<body>");
            out.println("<h2>"+MsgsTVML.M2+"</h2>");
        }

        out.println("<h3>Consulta 2: Fase 3 (Idioma="+plang+", Día="+pdia+")</h3>");
        out.println("<h3>Canales que emiten películas en el idioma seleccionado el día del programa seleccionado:</h3>");

        if (canales.size() == 0)
          out.println("No hay canales para el idioma "+plang+", y el día "+pdia);

        if (fe.equals("html")) {
            out.println("<ol>");

            for (int x=0; x < canales.size(); x++) {
              canal = canales.get(x);
              out.println("<li><b>Nombre</b> = '"+canal.getNombreCanal()+"'  ---  <b>Identificador</b> = '"+canal.getIdCanal()+
                          "'  ---  <b>Grupo</b> = '"+canal.getGrupo()+"'");
            }

            out.println("</ol>");
            out.println("<button class='home' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=01\"'>Inicio</button>");  // Inicio
            out.println("<button class='back' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=22&plang="+plang+"\"'>Atrás</button>");  // Atrás vuelve a la fase 12

            CommonSINT.printFoot(out, MsgsTVML.CURSO);
            out.println("</body></html>");
        }
        else {
            out.println("<ol>");

            for (int x=0; x < canales.size(); x++) {
              canal = canales.get(x);
              String cad = "<li><b>Nombre</b>="+canal.getNombreCanal()+" --- <b>Identificador</b>="+canal.getIdCanal()+
                           " --- <b>Grupo</b>="+canal.getGrupo()+"'";
              out.println(cad);
            }

            out.println("</ol>");
        }
      }
}
