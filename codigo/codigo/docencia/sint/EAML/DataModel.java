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

// consulta 1
// alumnos de una asignatura en una titulación

package docencia.sint.EAML;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import docencia.sint.Common.CommonSINT;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;


// MÉTODOS PARA CALCULAR LOS RESULTADOS

public class DataModel {

		// MÉTODOS PARA LA CONSULTA 1

    // método auxiliar que calcula la lista de titulaciones

    public static ArrayList<String> getC1Degrees () {
	    	if (CommonEAML.real == 0)
	    		return new ArrayList<String>(Arrays.asList("Empresas", "Fisica", "Minas", "Teleco"));
	    	else {
	    		ArrayList<String> listaDegrees = new ArrayList<String>();

	    		// convertimos las claves del hashmap en una lista

	    		Set<String> setDegrees = CommonEAML.mapDocs.keySet();
	    		listaDegrees.addAll(setDegrees);

	    		Collections.sort(listaDegrees);  // se ordenan alfabéticamente
	    		return listaDegrees;
	    	}
    }



    // método auxiliar que calcula la lista de asignaturas de una titulación

    public static ArrayList<Subject> getC1Subjects (String pdegree) {
	    if (CommonEAML.real == 0)
	    	return new ArrayList<Subject>(Arrays.asList(new Subject("A1","1", "core", "1", "5"), new Subject("A2","2", "specialty", "2", "7"), new Subject ("A3","c3", "optional", "3", "9")));
	    else {
	    		Document doc = CommonEAML.mapDocs.get(pdegree);
	    		if (doc == null) return null;  // no existe esa titulación

	    		ArrayList<Subject> listaSubjects = new ArrayList<Subject>();

	    		NodeList nlSubjects = doc.getElementsByTagName("Subject");  // pedimos el NodeList con todas las asignaturas de esa titulación

	    		Element elemSubject, elemCourse;
	    		String nombre, curso, tipo;

	    		// vamos a recopilar la información de todas las asignaturas

	    		for (int y=0; y < nlSubjects.getLength(); y++) {
	    			elemSubject = (Element)nlSubjects.item(y);  // estudiamos una asignatura

	    			nombre = CommonSINT.getTextContentOfChild(elemSubject, "Name");  // obtenemos el nombre de la asignatura

	    			tipo = elemSubject.getAttribute("type");   // leemos el tipo de la asignatura

            elemCourse = (Element)elemSubject.getParentNode();
	    			curso = elemCourse.getAttribute("number");   // leemos el curso de la asignatura


	    			listaSubjects.add(new Subject(nombre, curso, tipo, "", ""));  // creamos y añadimos la asignatura
	    		}

	    		Collections.sort(listaSubjects);  // ordenamos por cursos y dentro de cada uno, alfabéticamente

	    		return listaSubjects;
	    }
    }


    // método auxiliar que calcula la lista de alumnos de una asignatura de una titulación

    public static ArrayList<Student> getC1Students (String pdegree, String psubject) {
	    	if (CommonEAML.real == 0)
	    		return new ArrayList<Student>(Arrays.asList(new Student("Pepe","12345678A", "Camelias, 1"),
	    				new Student("Ana","87654321B", "Romil, 2"), new Student("Juan","J2233445", "Venezuela, 3")));
	    	else {

	    		Document doc = CommonEAML.mapDocs.get(pdegree);
	    		if (doc == null) return null;  // no existe esa titulación

	    		ArrayList<Student> listaStudents = new ArrayList<Student>();  // lista de alumnos a devolver

	        String xpathTarget =   "/Degree/Course/Subject[Name='"+psubject+"']/Student";    // los alumnos buscados
	        NodeList nlStudents=null;

	    		try {  // obtenemos los alumnos
	    			nlStudents = (NodeList)CommonEAML.xpath.evaluate(xpathTarget, doc, XPathConstants.NODESET);
	    		}
	    		catch (XPathExpressionException e) {CommonEAML.logEAML(e.toString()); return null;}

	    		if (nlStudents.getLength() == 0)
              		return listaStudents;

	    		Element elemStudent;
	    		String nombre, id, direccion;

	    		for (int x=0; x < nlStudents.getLength(); x++) {
	    			elemStudent = (Element) nlStudents.item(x);

	    			nombre = CommonSINT.getTextContentOfChild(elemStudent, "Name");
	    			id = CommonSINT.getTextContentOfChild(elemStudent, "Dni");
            if (id == null)
              	id = CommonSINT.getTextContentOfChild(elemStudent, "Resident");

            direccion = CommonSINT.getTextContent(elemStudent);

	    			listaStudents.add(new Student(nombre, id, direccion));
	    		}

    		Collections.sort(listaStudents);  // ordenamos la lista por su orden natural
    		return listaStudents;
    	}
    }






		// MÉTODOS PARA LA CONSULTA 2

		// método auxiliar que calcula la lista de titulaciones

		public static ArrayList<String> getC2Degrees () {
				if (CommonEAML.real == 0)
					return new ArrayList<String>(Arrays.asList("Empresas", "Fisica", "Minas", "Teleco"));
				else {
					ArrayList<String> listaDegrees = new ArrayList<String>();

					// convertimos las claves del hashmap en una lista

					Set<String> setDegrees = CommonEAML.mapDocs.keySet();
					listaDegrees.addAll(setDegrees);

					// se ordenan por longitud y después alfabéticamente
					Collections.sort(listaDegrees, new Comparator<String>(){

    					@Override
    					public int compare(String s1, String s2) {
								    if (s1.length() < s2.length()) return -1;
										else
												if (s1.length() > s2.length()) return 1;
        								else
													return s1.compareTo(s2);
    					}
					});

					return listaDegrees;
				}
		}




			// método auxiliar que calcula la lista de alumnos de una titulación

			public static ArrayList<Student> getC2Students (String pdegree) {
					if (CommonEAML.real == 0)
						return new ArrayList<Student>(Arrays.asList(new Student("Pepe","12345678A", "Camelias, 1"),
								new Student("Ana","87654321B", "Romil, 2"), new Student("Juan","J2233445", "Venezuela, 3")));
					else {

						Document doc = CommonEAML.mapDocs.get(pdegree);
						if (doc == null) return null;  // no existe esa titulación

						ArrayList<Student> listaStudents = new ArrayList<Student>();  // lista de alumnos a devolver

						String xpathTarget =   "/Degree/Course/Subject/Student";    // los alumnos buscados
						NodeList nlStudents=null;

						try {  // obtenemos los alumnos
							nlStudents = (NodeList)CommonEAML.xpath.evaluate(xpathTarget, doc, XPathConstants.NODESET);
						}
						catch (XPathExpressionException e) {CommonEAML.logEAML(e.toString()); return null;}

						if (nlStudents.getLength() == 0)
										return listaStudents;

						Element elemStudent;
						String nombre, id, direccion;

						for (int x=0; x < nlStudents.getLength(); x++) {
							elemStudent = (Element) nlStudents.item(x);

							nombre = CommonSINT.getTextContentOfChild(elemStudent, "Name");
							id = CommonSINT.getTextContentOfChild(elemStudent, "Dni");
							if (id == null)
									id = CommonSINT.getTextContentOfChild(elemStudent, "Resident");

							direccion = CommonSINT.getTextContent(elemStudent);

							Student newStudent = new Student(nombre, id, direccion);

							if (!newStudent.isContainedInList(listaStudents))
									listaStudents.add(new Student(nombre, id, direccion));
						}

					Collections.sort(listaStudents, Student.RESIDENTES);  // ordenamos la lista por su orden secundario, primero residentes
					return listaStudents;
				}
			}




		// método auxiliar que calcula las notas de las asignaturas de un alumno de una titulación

		public static ArrayList<Subject> getC2Subjects (String pdegree, String pstudent) {
			if (CommonEAML.real == 0)
				return new ArrayList<Subject>(Arrays.asList(new Subject("A1","1", "core", "1", "5"), new Subject("A2","2", "specialty", "2", "7"), new Subject ("A3","c3", "optional", "3", "9")));
			else {
					Document doc = CommonEAML.mapDocs.get(pdegree);
					if (doc == null) return null;  // no existe esa titulación

					ArrayList<Subject> listaSubjects = new ArrayList<Subject>();

					String xpathTarget =   "/Degree/Course/Subject/Student[(Dni = '"+pstudent+"') or (Resident = '"+pstudent+"')]";    // el alumno buscado en sus múltiples asignaturas

					NodeList nlStudents=null;

					try {  // obtenemos los alumnos
						nlStudents = (NodeList)CommonEAML.xpath.evaluate(xpathTarget, doc, XPathConstants.NODESET);
					}
					catch (XPathExpressionException e) {CommonEAML.logEAML(e.toString()); return null;}

					if (nlStudents.getLength() == 0)
									return listaSubjects;

					Element elemStudent, elemSubject;
					String nombreSubject, idSub, grade;

					// vamos a recopilar la información de todas las asignaturas

					for (int y=0; y < nlStudents.getLength(); y++) {
						elemStudent = (Element)nlStudents.item(y);  // estudiamos un alumno

						elemSubject = (Element)elemStudent.getParentNode();
						idSub = elemSubject.getAttribute("idSub");   // leemos el ID de la asignatura
						nombreSubject = CommonSINT.getTextContentOfChild(elemSubject, "Name");  // obtenemos el nombre de la asignatura

						grade = CommonSINT.getTextContentOfChild(elemStudent, "Grade");  // obtenemos la nota de la asignatura

						listaSubjects.add(new Subject(nombreSubject, "", "", idSub, grade));  // creamos y añadimos la asignatura
					}

					Collections.sort(listaSubjects, Subject.NOTAS);  // ordenamos por notas, y dentro de cada uno, alfabéticamente

					return listaSubjects;
			}
		}

}
