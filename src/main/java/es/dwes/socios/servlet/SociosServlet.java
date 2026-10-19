package es.dwes.socios.servlet;

import es.dwes.socios.dao.SocioDAO;
import es.dwes.socios.model.Socio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = {"/", "/socios", "/socios/nuevo", "/socios/eliminar"})
public class SociosServlet extends HttpServlet {
    private final SocioDAO socioDAO = new SocioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "", "/" -> mostrarInicio(request, response);
            case "/socios" -> mostrarSocios(request, response);
            case "/socios/nuevo" -> mostrarFormulario(request, response);
            case "/socios/eliminar" -> mostrarFormularioBorrado(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        switch (request.getServletPath()) {
            case "/socios" -> crearSocio(request, response);
            case "/socios/eliminar" -> borrarSocio(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void mostrarInicio(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = startPage(request, response, "Gestión de socios");
        out.println("<h1>Bienvenido a la aplicación de gestión de socios</h1>");
        out.println("<p>Seleccione una opción:</p><ul>");
        out.println("<li><a href=\"" + url(request, "/socios/nuevo") + "\">Añadir nuevo socio</a></li>");
        out.println("<li><a href=\"" + url(request, "/socios") + "\">Ver listado de socios</a></li>");
        out.println("<li><a href=\"" + url(request, "/socios/eliminar") + "\">Borrar socio</a></li>");
        out.println("</ul>");
        endPage(out);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = startPage(request, response, "Nuevo socio");
        out.println("<h1>Introduzca los datos del nuevo socio</h1>");
        out.println("<form method=\"post\" action=\"" + url(request, "/socios") + "\">");
        input(out, "numero", "Nº socio", "number");
        input(out, "nombre", "Nombre", "text");
        input(out, "estatura", "Estatura (cm)", "number");
        input(out, "edad", "Edad", "number");
        input(out, "localidad", "Localidad", "text");
        out.println("<button type=\"submit\">Aceptar</button></form>");
        out.println("<p><a href=\"" + url(request, "/") + "\">Volver al inicio</a></p>");
        endPage(out);
    }

    private void mostrarFormularioBorrado(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = startPage(request, response, "Borrar socio");
        out.println("<h1>Borrar socio</h1>");
        out.println("<form method=\"post\" action=\"" + url(request, "/socios/eliminar") + "\">");
        input(out, "codigo", "Nº socio", "number");
        out.println("<button type=\"submit\">Borrar</button></form>");
        out.println("<p><a href=\"" + url(request, "/") + "\">Volver al inicio</a></p>");
        endPage(out);
    }

    private void mostrarSocios(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Socio> socios;
        try {
            socios = socioDAO.listar();
        } catch (SQLException exception) {
            log("No se pudo consultar el listado de socios.", exception);
            mostrarError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "No se pudo cargar el listado de socios.");
            return;
        }

        PrintWriter out = startPage(request, response, "Listado de socios");
        out.println("<h1>Listado de socios</h1>");
        out.println("<nav><a href=\"" + url(request, "/") + "\">Volver al inicio</a> | ");
        out.println("<a href=\"" + url(request, "/socios/nuevo") + "\">Añadir socio</a> | ");
        out.println("<a href=\"" + url(request, "/socios/eliminar") + "\">Borrar socio</a></nav>");
        out.println("<table><thead><tr><th>Código</th><th>Nombre</th><th>Estatura</th>"
            + "<th>Edad</th><th>Localidad</th></tr></thead><tbody>");
        for (Socio socio : socios) {
            out.println("<tr><td>" + socio.socioId() + "</td><td>" + escape(socio.nombre())
                + "</td><td>" + socio.estatura() + "</td><td>" + socio.edad()
                + "</td><td>" + escape(socio.localidad()) + "</td></tr>");
        }
        if (socios.isEmpty()) {
            out.println("<tr><td colspan=\"5\">No hay socios registrados.</td></tr>");
        }
        out.println("</tbody></table>");
        endPage(out);
    }

    private void crearSocio(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Socio socio = new Socio(
                positiveInteger(request, "numero"),
                requiredText(request, "nombre", 40),
                positiveInteger(request, "estatura"),
                positiveInteger(request, "edad"),
                requiredText(request, "localidad", 30)
            );
            socioDAO.crear(socio);
            response.sendRedirect(url(request, "/socios"));
        } catch (IllegalArgumentException exception) {
            mostrarError(request, response, HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (SQLException exception) {
            log("No se pudo guardar el socio.", exception);
            mostrarError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "No se pudo guardar el socio. Compruebe que el número no esté ya registrado.");
        }
    }

    private void borrarSocio(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            socioDAO.borrar(positiveInteger(request, "codigo"));
            response.sendRedirect(url(request, "/socios"));
        } catch (IllegalArgumentException exception) {
            mostrarError(request, response, HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (SQLException exception) {
            log("No se pudo borrar el socio.", exception);
            mostrarError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "No se pudo borrar el socio.");
        }
    }

    private void mostrarError(HttpServletRequest request, HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        PrintWriter out = startPage(request, response, "Error");
        out.println("<h1>No se pudo completar la operación</h1><p>" + escape(message) + "</p>");
        out.println("<p><a href=\"" + url(request, "/socios") + "\">Volver al listado</a></p>");
        endPage(out);
    }

    private PrintWriter startPage(HttpServletRequest request, HttpServletResponse response, String title)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("<link rel=\"stylesheet\" href=\"" + url(request, "/estilos.css") + "\">");
        out.println("<title>" + escape(title) + "</title></head><body>");
        return out;
    }

    private void input(PrintWriter out, String name, String label, String type) {
        out.println("<label>" + escape(label) + " <input type=\"" + type + "\" name=\"" + name
            + "\" required></label><br>");
    }

    private void endPage(PrintWriter out) {
        out.println("</body></html>");
    }

    private String url(HttpServletRequest request, String path) {
        return request.getContextPath() + path;
    }

    private int positiveInteger(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        try {
            int number = Integer.parseInt(value);
            if (number > 0) {
                return number;
            }
        } catch (NumberFormatException ignored) {
            // Report malformed or missing numeric input as a user-facing validation error.
        }
        throw new IllegalArgumentException("El campo numérico «" + name + "» debe ser un entero positivo.");
    }

    private String requiredText(HttpServletRequest request, String name, int maxLength) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new IllegalArgumentException("El campo «" + name + "» es obligatorio y admite hasta "
                + maxLength + " caracteres.");
        }
        return value.trim();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
