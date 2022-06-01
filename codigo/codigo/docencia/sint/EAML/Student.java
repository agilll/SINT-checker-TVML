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


// objeto Student que almacena: nombre, id, direccion

public class Student implements Comparable<Student> {

	private String nombre, id, direccion;

	public Student (String n, String i, String d)  {
		nombre = n;
		id = i;
		direccion = d;
	}


	public String getNombre () {
		return nombre;
	}

	public String getId () {
		return id;
	}

	public String getDireccion () {
		return direccion;
	}



	// para ver si este Student ya está contenido en la lista que se le pasa

	public boolean isContainedInList (ArrayList<Student> listaStudents) {

		String elID;

		for (int x=0; x < listaStudents.size(); x++) {
				elID = listaStudents.get(x).getId();
				if (elID.equals(this.getId())) return true;
		}

		return false;
	}


	// orden principal: primero DNIs luego residentes (dentro de cada bloque por orden alfabético)

	public int compareTo (Student segundoStudent) {
      String esteNombre = this.getNombre();
      String segundoNombre = segundoStudent.getNombre();
			String esteId = this.getId();
      String segundoId = segundoStudent.getId();

			if (esteId.length() > segundoId.length()) return -1;
			if (esteId.length() < segundoId.length()) return 1;

      return esteNombre.compareTo(segundoNombre);
	}


	// orden alternativo: primero residentes luego DNis (dentro de cada bloque por orden alfabético)

	static final Comparator<Student> RESIDENTES =
			new Comparator<Student>() {
		public int compare(Student e1, Student e2) {
				String esteNombre = e1.getNombre();
				String segundoNombre = e2.getNombre();
				String esteId = e1.getId();
				String segundoId = e2.getId();

				if (esteId.length() > segundoId.length()) return 1;
				if (esteId.length() < segundoId.length()) return -1;

				return esteNombre.compareTo(segundoNombre);
		}
	};

}
