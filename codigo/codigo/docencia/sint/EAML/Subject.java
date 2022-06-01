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

import java.util.ArrayList;
import java.util.Comparator;

// objeto Subject, que almacena: nombre, curso, tipo

public class Subject implements Comparable<Subject> {

	private String nombre, curso, tipo, idSub, grade;


	public Subject (String n, String c, String t, String i, String g)  {
		nombre = n;
		curso = c;
		tipo = t;
		idSub = i;
		grade = g;
	}

	public String getNombre () {
		return nombre;
	}

	public String getCurso () {
		return curso;
	}

	public String getTipo () {
		return tipo;
	}

	public String getIdSub () {
		return idSub;
	}

	public String getGrade () {
		return grade;
	}



	// para ver si esta asignatura ya está contenida en la lista que se le pasa

	public boolean isContainedInList (ArrayList<Subject> listaSubjects) {

		String nombreS;

		for (int x=0; x < listaSubjects.size(); x++) {
			nombreS = listaSubjects.get(x).getNombre();
			if (nombreS.equals(this. getNombre())) return true;
		}

		return false;
	}


	// orden principal: orden alfabético

	public int compareTo(Subject segundoSubject) {
		if (this.getCurso().compareTo(segundoSubject.getCurso()) < 0)  return -1;
		else
			if (this.getCurso().compareTo(segundoSubject.getCurso()) > 0) return 1;
			else return (this.getNombre().compareTo(segundoSubject.getNombre()));
	}


	// orden alternativo: por nota, de mayor a menor (a igual nota por orden alfabético)

	static final Comparator<Subject> NOTAS =
			new Comparator<Subject>() {
		public int compare(Subject s1, Subject s2) {
				String esteNombre = s1.getNombre();
				String segundoNombre = s2.getNombre();
				String esteNota = s1.getGrade();
				String segundoNota = s2.getGrade();

				if (esteNota.compareTo(segundoNota) < 0)  return -1;
				else
					if (esteNota.compareTo(segundoNota) > 0) return 1;
					else return esteNombre.compareTo(segundoNombre);
		}
	};


}
