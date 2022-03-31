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

import java.util.ArrayList;
import java.util.Comparator;

// objeto Programa que almacena edadminima, langs, resumen, NombrePrograma, Categoría, HoraInicio
// se usa también en el checker ??

public class Programa implements Comparable<Programa> {

	private String edadminima, langs, resumen, nombrePrograma, categoria, horaInicio, diaEmision;

	public Programa (String e, String l, String r, String n, String c, String h, String d)  {
		edadminima = e;
		langs = l;
		resumen = r;
		nombrePrograma = n;
		categoria = c;
		horaInicio = h;
		diaEmision = d;
	}


	public String getEdadminima () {
		return edadminima;
	}

	public String getLangs () {
		return langs;
	}

	public String getResumen () {
		return resumen;
	}

	public String getNombrePrograma () {
		return nombrePrograma;
	}

	public String getCategoría () {
		return categoria;
	}

	public String getHoraInicio () {
		return horaInicio;
	}

	public String getDiaEmision () {
		return diaEmision;
	}



	// para ver si este Programa ya está contenida en la lista que se le pasa

	public boolean isContainedInList (ArrayList<Programa> listaProgramas) {

		String nombre;

		for (int x=0; x < listaProgramas.size(); x++) {
			nombre = listaProgramas.get(x).getNombrePrograma();
			if (nombre.equals(this.getNombrePrograma())) return true;
		}

		return false;
	}


	// orden principal: por longitud titulo (si iguales, primero el que se emita antes)

	public int compareTo (Programa segundoPrograma) {
             String esteNombre = this.getNombrePrograma();
             String segundoNombre = segundoPrograma.getNombrePrograma();

             if (esteNombre.length() != segundoNombre.length())
            	 return  (esteNombre.length() - segundoNombre.length());
             else
                return  (this.getHoraInicio().compareTo(segundoPrograma.getHoraInicio()));
	}


	// orden alternativo: por día y dentro del día por el tamaño del resumen (creciente)

	static final Comparator<Programa> DIA_RESUMEN =
			new Comparator<Programa>() {
		public int compare(Programa c1, Programa c2) {
			if (c1.diaEmision.compareTo(c2.diaEmision) < 0 )  return -1;
			else
				if (c1.diaEmision.compareTo(c2.diaEmision) > 0 )  return 1;
				else
					if (c1.resumen.length() > c2.resumen.length())  return 1;
				    else
				    	if (c1.resumen.length() < c2.resumen.length())  return -1;
				    	else
				    		return (c1.resumen.compareTo(c2.resumen));
		}
	};

}
