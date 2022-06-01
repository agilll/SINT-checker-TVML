/****************************************************************
 *    SERVICIOS DE INTERNET
 *    EE TELECOMUNICACIÓN
 *    UNIVERSIDAD DE VIGO
 *
 *    Checker de la práctica EAML
 *
 *    Autor: Alberto Gil Solla
 *    Curso : 2020-2021
 ****************************************************************/

// etiquetas de mensajes en distintos idiomas para varias clases

package docencia.sint.EAML.checker;

import java.util.HashMap;
import docencia.sint.EAML.MsgsEAML;

public class MsgsEAMLChecker {

		public static final String CREATED = "2020";

		static HashMap<String, String[]> mapMsgs = new HashMap<String, String[]>();   // el hashmap de mensajes

		static {
			mapMsgs.put("0", new String[] {"Corrector de "+MsgsEAML.XML_LANGUAGE, MsgsEAML.XML_LANGUAGE+" checker"});
			mapMsgs.put("3", new String[] {"Comprobación de servicios sobre "+MsgsEAML.XML_LANGUAGE, "Checker of services about "+MsgsEAML.XML_LANGUAGE});
			mapMsgs.put("4", new String[] {"CURSO "+MsgsEAML.CURSO, "COURSE "+MsgsEAML.CURSO});

			mapMsgs.put("5", new String[] {"CONSULTA 1 (enero): Alumnos de una asignatura de una titulación ", "QUERY 1 (January): Students in a subject from a Degree"});
			mapMsgs.put("6", new String[] {"CONSULTA 2 (junio): Notas de un alumno en una titulación ", "QUERY 2 (June): Grades of a student from a Degree"});

			mapMsgs.put("21", new String[] {"Error solicitando la lista de ", "Error requesting list of "});
			mapMsgs.put("22", new String[] {"Diferencias en la lista de ", "Differences in list of "});
			mapMsgs.put("23", new String[] {"Creando la solicitud de la lista de ", "Creating request for list of "});
			mapMsgs.put("24", new String[] {"Resultado erróneo en la consulta directa", "Wrong result in the direct query"});

			mapMsgs.put("26", new String[] {"Al pedir la consulta directa ", "Requesting direct query "});
			mapMsgs.put("27", new String[] {"Al solicitar/parsear la lista de ", "Requesting/parsing the list of "});
			mapMsgs.put("28", new String[] {"Resultado inválido, '%s' al parsear la lista de ", "Invalid result, '%s' parsing the list of "});

			mapMsgs.put("30", new String[] {"El parser devuelve 'null' al parsear la lista de ", "Parser returns 'null' parsing the list of "});
			mapMsgs.put("31", new String[] {"No se recibe '&lt;degrees>' al solicitar y parsear la lista de degrees", "'&lt;degrees>' is not received when requesting the list of degrees"});
			mapMsgs.put("32", new String[] {"No se recibe '&lt;subjects>' al solicitar y parsear la lista de subjects", "'&lt;subjects>' is not received when requesting the list of subjects"});
			mapMsgs.put("33", new String[] {"No se recibe '&lt;students>' al solicitar y parsear la lista de students", "'&lt;students>' is not received when requesting the list of students",});

			mapMsgs.put("36", new String[] {"Diferencia en la lista de degrees: se recibe '%s' en la posición %d, pero se esperaba '%s'", "Difference in list of degrees: received '%s' in position %d, but it was expected '%s'"});

			mapMsgs.put("37", new String[] {"Se esperaba el subject '%s' en la posición %d y se recibió '%s'", "Subject '%s' was expected in position %d and it was received '%s'"});
			mapMsgs.put("38", new String[] {"Se esperaba el curso '%s' en la posición %d y se recibió '%s'", "Course '%s' was expected in position %d and it was received '%s'"});
			mapMsgs.put("39", new String[] {"Se esperaba el tipo '%s' en la posición %d y se recibió '%s'", "Type '%s' was expected in position %d and it was received '%s'"});

			mapMsgs.put("43", new String[] {"Se esperaba el idSub '%s' en la posición %d y se recibió '%s'", "idSub '%s' was expected in position %d and it was received '%s'"});
			mapMsgs.put("44", new String[] {"Se esperaba el grade '%s' en la posición %d y se recibió '%s'", "grade '%s' was expected in position %d and it was received '%s'"});

			mapMsgs.put("40", new String[] {"Se esperaba el student '%s' en la posición %d y se recibió '%s'", "Student '%s' was expected in position %d and it was received '%s'"});
			mapMsgs.put("41", new String[] {"Se esperaba el Id '%s' en la posición %d y se recibió '%s'", "Id '%s' was expected in position %d and it was received '%s'"});
			mapMsgs.put("42", new String[] {"Se esperaba la dirección '%s' en la posición %d y se recibió '%s'", "Address '%s' was expected in position %d and it was received '%s'"});

		}


		 // obtiene un mensaje (id) dependiendo del idioma (lang = "es" o "en")
 		static String getMsg(String id, String lang) {
 			 String value[] = mapMsgs.get(id);
			 if (value == null) return "ERROR MsgsEAMLChecker.getMsg key "+id;
 			 if (lang.equals("en"))  return value[1];
 			 else return value[0];
 		}


}
