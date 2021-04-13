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

// objeto Canal, que almacena: idCanal, lang, NombreCanal, Grupo 
// se usa también en el checker??

public class Canal implements Comparable<Canal> {

	private String idCanal, idioma, nombreCanal, grupo;


	public Canal (String i, String l, String n, String g)  {
		idCanal = i;
		idioma = l;
		nombreCanal = n;
		grupo = g;
	}
	
	public String getIdCanal () {
		return idCanal;
	}

	public String getIdioma () {
		return idioma;
	}
	
	public String getNombreCanal () {
		return nombreCanal;
	}
	
	public String getGrupo () {
		return grupo;
	}

	
	// para ver si este canal ya está contenido en la lista que se le pasa
	
	public boolean isContainedInList (ArrayList<Canal> listaCanales) {

		String identCanal;

		for (int x=0; x < listaCanales.size(); x++) {
			identCanal = listaCanales.get(x). getIdCanal();
			if (identCanal.equals(this. getIdCanal())) return true;
		}

		return false;
	}


	// orden principal: orden alfabético 
	
	public int compareTo(Canal segundoCanal) {
		return (this.getNombreCanal().compareTo(segundoCanal.getNombreCanal()));
	}
	
	
	// orden alternativo: por id decreciente de Canal
	
		static final Comparator<Canal> IDCANAL = 
				new Comparator<Canal>() {
			public int compare(Canal c1, Canal c2) {
		        return  c2.idCanal.compareTo(c1.idCanal);
			}
		};
	
	
	
}



