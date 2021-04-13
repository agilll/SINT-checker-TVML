/****************************************************************
 *    SERVICIOS DE INTERNET
 *    EE TELECOMUNICACIÓN
 *    UNIVERSIDAD DE VIGO
 *
 *    Prácticas de SINT  (paquete común a todas las prácticas)
 *
 *    Autor: Alberto Gil Solla
 ****************************************************************/


// filtro para comprobar la passwd

package docencia.sint.Common;

import javax.servlet.FilterConfig;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

 public class PasswdFilter implements Filter {

	    protected FilterConfig filterConfig;

	    // Se llama cuando se crea una instancia del filtro.

	    public void init(FilterConfig filterConfig) throws ServletException {
	       this.filterConfig = filterConfig;
	    }

	    public void destroy() {
	       this.filterConfig = null;
	    }

	    // Se llama por cada solicitud correlacionada a este filtro.

	    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws java.io.IOException, ServletException   {

	        // efectuar aquí la acción del filtro previa a la invocación

			String passwd = request.getParameter("p");

		    if (passwd == null) {
		    		CommonSINT.doBadRequest("no passwd", request, response);
				return;
			}

			if (!passwd.equals(CommonSINT.PPWD)) {
				CommonSINT.doBadRequest("bad passwd", request, response);
				return;
			}

	        chain.doFilter(request, response);

	       // efectuar aquí la acción posterior
	    }

}
