package es.dwes.socios.dao;

import es.dwes.socios.model.Socio;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SocioDAO {
    private static final String DEFAULT_URL = "jdbc:mysql://127.0.0.1:3306/baloncesto";
    private static final String DEFAULT_USER = "user";
    private static final String DEFAULT_PASSWORD = "user";

    public List<Socio> listar() throws SQLException {
        String sql = "SELECT * FROM socio ORDER BY socioID";
        List<Socio> socios = new ArrayList<>();

        try (Connection conexion = getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultados = consulta.executeQuery()) {
            while (resultados.next()) {
                socios.add(new Socio(
                    resultados.getInt("socioID"),
                    resultados.getString("nombre"),
                    resultados.getInt("estatura"),
                    resultados.getInt("edad"),
                    resultados.getString("localidad")
                ));
            }
        }
        return socios;
    }

    public void crear(Socio socio) throws SQLException {
        String sql = "INSERT INTO socio (socioID, nombre, estatura, edad, localidad) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = getConnection();
             PreparedStatement insercion = conexion.prepareStatement(sql)) {
            insercion.setInt(1, socio.socioId());
            insercion.setString(2, socio.nombre());
            insercion.setInt(3, socio.estatura());
            insercion.setInt(4, socio.edad());
            insercion.setString(5, socio.localidad());
            insercion.executeUpdate();
        }
    }

    public void borrar(int socioId) throws SQLException {
        try (Connection conexion = getConnection();
             PreparedStatement borrado = conexion.prepareStatement("DELETE FROM socio WHERE socioID = ?")) {
            borrado.setInt(1, socioId);
            borrado.executeUpdate();
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("No se encontró MySQL Connector/J en el classpath de la aplicación.", exception);
        }
        return DriverManager.getConnection(
            setting("DB_URL", DEFAULT_URL),
            setting("DB_USER", DEFAULT_USER),
            setting("DB_PASSWORD", DEFAULT_PASSWORD)
        );
    }

    private String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
