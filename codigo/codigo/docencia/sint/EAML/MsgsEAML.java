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

// etiquetas de mensajes para varias clases

package docencia.sint.EAML;

import java.util.HashMap;

public class MsgsEAML {

		public static final String XML_LANGUAGE = "EAML";
		public static final String CURSO = "2020-2021";

		static HashMap<String, String[]> mapMsgs = new HashMap<String, String[]>();   // el hashmap de mensajes

		static {
			mapMsgs.put("001", new String[] {"Servicio de consulta de expendientes académicos", "Query service about academic records"});

			mapMsgs.put("101", new String[] {"Consulta 1: alumnos de una asignatura de una titulación", "Query 1: students enrolled in a subject of a degree"});
			mapMsgs.put("102", new String[] {"Consulta 1: Fase 1", "Query 1: Phase 1"});
			mapMsgs.put("103", new String[] {"Consulta 1: Fase 2 (Titulación = %s)", "Query 1: Phase 2 (Degree = %s)"});
			mapMsgs.put("104", new String[] {"Consulta 1: Fase 3  (Titulación = %s, Asignatura = %s)", "Query 1: Phase 3 (Degree = %s, Subject = %s)"});
			mapMsgs.put("105", new String[] {"No hay titulaciones", "No available degrees"});
			mapMsgs.put("106", new String[] {"La titulación %s no existe", "Degree %s not found"});
			mapMsgs.put("107", new String[] {"La titulación %s o la asignatura %s no existe", "Degree %s or subject %s not found"});
			mapMsgs.put("108", new String[] {"Selecciona una titulación:", "Select a degree:"});
			mapMsgs.put("109", new String[] {"Selecciona una asignatura:", "Select a subject:"});
			mapMsgs.put("110", new String[] {"No hay asignaturas en esta titulación", "No subjects in this degree"});
			mapMsgs.put("111", new String[] {"Asignatura", "Subject"});
			mapMsgs.put("112", new String[] {"Curso", "Course"});
			mapMsgs.put("113", new String[] {"Tipo", "Type"});
			mapMsgs.put("114", new String[] {"No hay alumnos en la asignatura %s de %s", "No students in subject %s of degree %s"});
			mapMsgs.put("115", new String[] {"Nombre", "Name"});
			mapMsgs.put("116", new String[] {"Dirección", "Address"});

			mapMsgs.put("201", new String[] {"Consulta 2: notas de un alumno en una titulación", "Query 2: grades of a student in a degree"});
			mapMsgs.put("202", new String[] {"Consulta 2: Fase 1", "Query 2: Phase 1"});
			mapMsgs.put("203", new String[] {"Consulta 2: Fase 2 (Titulación = %s)", "Query 2: Phase 2 (Degree = %s)"});
			mapMsgs.put("204", new String[] {"Consulta 2: Fase 3  (Titulación = %s, Alumno = %s)", "Query 2: Phase 3 (Degree = %s, Student = %s)"});
			mapMsgs.put("205", new String[] {"No hay titulaciones", "No available degrees"});
			mapMsgs.put("206", new String[] {"La titulación %s no existe", "Degree %s not found"});
			mapMsgs.put("207", new String[] {"La titulación %s o el alumno %s no existe", "Degree %s or student %s not found"});
			mapMsgs.put("208", new String[] {"Selecciona una titulación:", "Select a degree:"});
			mapMsgs.put("209", new String[] {"Selecciona un alumno:", "Select a student:"});
			mapMsgs.put("210", new String[] {"No hay alumnos en esta titulación", "No students in this degree"});
			mapMsgs.put("211", new String[] {"Nombre", "Name"});
			mapMsgs.put("212", new String[] {"ID", "ID"});
			mapMsgs.put("213", new String[] {"Dirección", "Address"});
			mapMsgs.put("214", new String[] {"No hay asignaturas para el alumno %s de %s", "No subjects for student %s of %s"});
			mapMsgs.put("215", new String[] {"Nombre", "Name"});
			mapMsgs.put("216", new String[] {"ID Asignatura", "Subject ID"});
			mapMsgs.put("217", new String[] {"Nota", "Grade"});
		}


		// obtiene un mensaje (id) dependiendo del idioma (lang = "es" o "en")
		static String getMsg(String id, String lang) {
			 String value[] = mapMsgs.get(id);
			 if (value == null) return "ERROR MsgsEAML.getMsg key "+id;
			 if (lang.equals("en"))  return value[1];
			 else return value[0];
		}

}
