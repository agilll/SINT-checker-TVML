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

// etiquetas de mensajes para varias clases

package docencia.sint.TVML;

public class MsgsTVML {

	  public static final String LANGUAGE = "TVML";
	  public static final String CURSO = "2019-2020";

    static String M1 = "Servicio Cartelera TV";
    static String M2 = "Servicio de consulta de información sobre canales de TV";
		static String M3 = "Bienvenido a este servicio";
    static String M4 = "Ver los ficheros erróneos";
		static String M5 = "Ocultar los ficheros erróneos";
		static String M6 = "Selecciona una consulta:";
		static String M7 = "Consulta 1: Películas de un día en un canal";
		static String M8 = "Consulta 2: Canales con películas en un idioma en un día";

		static String M1e = "TV programming service";
		static String M2e = "Query service about TV channels programming";
		static String M3e = "Wellcome to this service";
		static String M4e = "Show error files";
		static String M5e = "Hide error files";
		static String M6e = "Select a query:";
		static String M7e = "Query 1: Movies in a channel at a day";
		static String M8e = "Query 2: Channels with movies in a language and day";

		static void Change2English () {
			M1 = M1e;
			M2 = M2e;
		  M3 = M3e;
		  M4 = M4e;
		  M5 = M5e;
		  M6 = M6e;
		  M7 = M7e;
		  M8 = M8e;
		}

}
