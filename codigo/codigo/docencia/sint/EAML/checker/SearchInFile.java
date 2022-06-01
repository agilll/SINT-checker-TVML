/****************************************************************
 *    SERVICIOS DE INTERNET
 *    EE TELECOMUNICACIÓN
 *    UNIVERSIDAD DE VIGO
 *
 *    Checker de la práctica de EAML
 *
 *    Autor: Alberto Gil Solla
 *    Curso : 2020-2021
 ****************************************************************/

// en esta clase van variables y métodos usados para la búsqueda en ficheros

package docencia.sint.EAML.checker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class SearchInFile {

		public void SearchInFile () {
				return;
			}



		public static int isInFile(String cad, File fd) {
				String line, line2;
				int veces = 0, idx;
				boolean inComment = false;

				BufferedReader br = null;
				try {
						br = new BufferedReader(new FileReader(fd));
						line = br.readLine();

						while (line != null)  // procesamos cada línea hasta acabar
						{
							line = line.trim();

							if (inComment) {  // estamos dentro de un comentario
								idx = line.indexOf("*/");
								if (idx == -1) {  // si no contiene */ leemos otra línea
									line = br.readLine();
								}
								else {  // contiene */, se terminó el comentario
									inComment = false;
									line = line.substring(idx+2);  // continuamos procesando la línea tras el */
								}
								continue;
							}

							idx = line.indexOf("/*");
							if (idx != -1) {    // contiene un principio de comentario
								inComment = true;
								line2 = line.substring(0, idx);  // nos quedamos con la parte antes del principio del comentario
								if (line2.contains(cad)) veces++;  // y buscamos la cadena
								line = line.substring(idx+2);  // nos quedamos con la parte posterior al inicio del comentario y la procesamos
								continue;
							}

							// aquí llega si es una línea funcional

							if (line.startsWith("System.out.print")) continue;  // descartamos los prints
							idx = line.indexOf("//");   // si tiene parte de comentario,
							if (idx != -1) {
								line = line.substring(0, idx);  // nos quedamos con la parte anterior
							}

							if (line.contains(cad)) veces++;  // buscamos la cadena

							line = br.readLine();  
						}
				}
				catch (FileNotFoundException e) {
						System.out.println("Error: Fichero no encontrado");
						System.out.println(e.getMessage());
				}
				catch(Exception e) {
						System.out.println("Error de lectura del fichero");
						System.out.println(e.getMessage());
				}
				finally {
						try {
								if(br != null)
								br.close();
						}
						catch (Exception e) {
								System.out.println("Error al cerrar el fichero");
								System.out.println(e.getMessage());
						}
				}

				return veces;
		}

}
