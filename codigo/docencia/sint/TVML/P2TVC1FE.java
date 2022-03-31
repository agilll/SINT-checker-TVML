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
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import docencia.sint.Common.CommonSINT;


public class P2TVC1FE  {

    // XML
    public static void printF11XML (PrintWriter out, ArrayList<String> dias)
    {
      out.println("<?xml version='1.0' encoding='utf-8'?>");
      out.println("<dias>");

      for (int x=0; x < dias.size(); x++)
          out.println("<dia>"+dias.get(x).trim()+"</dia>");

      out.println("</dias>");
    }


    public static void printF12XML (PrintWriter out, ArrayList<Canal> canales)
    {
      Canal canal;

      out.println("<?xml version='1.0' encoding='utf-8'?>");
      out.println("<canales>");

      for (int x=0; x < canales.size(); x++) {
        canal = canales.get(x);
        out.println("<canal idioma='"+canal.getIdioma()+"' grupo='"+canal.getGrupo()+"' >"+canal.getNombreCanal()+"</canal>");
      }

      out.println("</canales>");
    }


    public static void printF13XML (PrintWriter out, ArrayList<Programa> programas)
    {
      Programa programa;

      out.println("<?xml version='1.0' encoding='utf-8'?>");
      out.println("<peliculas>");

      for (int x=0; x < programas.size(); x++) {
        programa = programas.get(x);
        out.println("<pelicula edad='"+programa.getEdadminima()+"'  hora='"+programa.getHoraInicio()+
                    "'    resumen='"+programa.getResumen()+"'>"+programa.getNombrePrograma()+"</pelicula>");
      }

      out.println("</peliculas>");
    }





    // HTML y AJAX
    public static void printF11HTML (PrintWriter out, String fe, ArrayList<String> dias)
    {
      if (fe.equals("html")) {
          out.println("<html>");
          CommonTVML.printHead(out);
          out.println("<body>");
          out.println("<h2>"+MsgsTVML.M2+"</h2>");
      }

      out.println("<h3>Consulta 1: Fase 1</h3>");
      out.println("<h3>Selecciona un día:</h3>");

      if (fe.equals("html")) {
          out.println("<ol>");

          for (int x=0; x < dias.size(); x++)
            out.println("<li> <a href='?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=12&pdia="+dias.get(x)+"'>"+dias.get(x)+"</a>");

          out.println("</ol>");
          out.println("<button class='home' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=01\"'>Inicio</button>");  // Inicio

          CommonSINT.printFoot(out, MsgsTVML.CURSO);
          out.println("</body></html>");
      }
      else { //ajax
        out.println("<ol>");

        for (int x=0; x < dias.size(); x++) {
             String cad = "<li><u onclick='sendRequest(\"12\", \"&pdia="+dias.get(x)+"\");'>"+dias.get(x)+"</u>";
             out.println(cad);
        }

        out.println("</ol>");
      }
    }




    public static void printF12HTML (PrintWriter out, String fe, String pdia, ArrayList<Canal> canales)
    {
        Canal canal;

        if (fe.equals("html")) {
            out.println("<html>");
            CommonTVML.printHead(out);
            out.println("<body>");
            out.println("<h2>"+MsgsTVML.M2+"</h2>");
        }

        out.println("<h3>Consulta 1: Fase 2 (Día="+pdia+")</h3>");
        out.println("<h3>Selecciona un canal:</h3>");

        if (canales.size() == 0)
            out.println("No hay canales en el día "+pdia);

        if (fe.equals("html")) {
            out.println("<ol>");

            for (int x=0; x < canales.size(); x++) {
              canal = canales.get(x);
              out.println("<li><a href='?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=13&pdia="+pdia+"&pcanal="+canal.getNombreCanal()+"'> <b>Canal</b> = '"+
                       canal.getNombreCanal()+"'</a>  ---  <b>Idioma</b> = '"+canal.getIdioma()+"'  ---  <b>Grupo</b> = '"+canal.getGrupo()+"'");
            }

            out.println("</ol>");
            out.println("<button class='home' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=01\"'>Inicio</button>");  // Inicio
            out.println("<button class='back' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=11\"'>Atrás</button>");  // Atrás vuelve a la fase 11

            CommonSINT.printFoot(out, MsgsTVML.CURSO);
            out.println("</body></html>");
        }
        else {
              out.println("<ol>");

              for (int x=0; x < canales.size(); x++) {
                canal = canales.get(x);
                String cad = "<li><u onclick='sendRequest(\"13\", \"&pdia="+pdia+"&pcanal="+canal.getNombreCanal()+"\");'><b>Canal</b>="+canal.getNombreCanal()+" </u> --- <b>Idioma</b>="+canal.getIdioma()+" --- <b>Grupo</b>="+canal.getGrupo();
                out.println(cad);
              }

              out.println("</ol>");
        }
    }

    public static void printF13HTML (PrintWriter out, String fe, String pdia, String pcanal, ArrayList<Programa> programas)
    {
        Programa programa;

        if (fe.equals("html")) {
            out.println("<html>");
            CommonTVML.printHead(out);
            out.println("<body>");
            out.println("<h2>"+MsgsTVML.M2+"</h2>");
        }

        out.println("<h3>Consulta 1: Fase 3  (Día="+pdia+", Canal="+pcanal+")</h3>");
        out.println("<h3>Este es el resultado:</h3>");

        if (programas.size() == 0)
          out.println("No hay películas en el canal "+pcanal+", el día "+pdia);

        if (fe.equals("html")) {
            out.println("<ol>");

            for (int x=0; x < programas.size(); x++) {
              programa = programas.get(x);
              out.println(" <li><b>Título</b> = '"+programa.getNombrePrograma()+"'  ---  <b>Edad Mínima</b> = '"+programa.getEdadminima()+
                          "'  ---  <b>Hora</b> = '"+programa.getHoraInicio()+"'  --- <b>Resumen</b> = '"+programa.getResumen()+"'");
            }

            out.println("</ol>");
            out.println("<button class='home' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=01\"'>Inicio</button>");  // Inicio
            out.println("<button class='back' onClick='window.location.href=\"?p="+CommonSINT.PPWD+"&"+CommonSINT.PFASE+"=12&pdia="+pdia+"\"'>Atrás</button>");  // Atrás vuelve a la fase 12

            CommonSINT.printFoot(out, MsgsTVML.CURSO);
            out.println("</body></html>");
        }
        else {
            out.println("<ol>");

            for (int x=0; x < programas.size(); x++) {
              programa = programas.get(x);
              String cad = "<li><b>Título</b>="+programa.getNombrePrograma()+" --- <b>Edad Mínima</b>="+programa.getEdadminima()+
                           " --- <b>Hora</b>="+programa.getHoraInicio()+" --- <b>Resumen</b> = '"+programa.getResumen()+"'";
              out.println(cad);
            }

            out.println("</ol>");
        }
    }




}
