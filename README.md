# Proyecto Socios

Proyecto de gestión de socios de club de baloncesto

La aplicación está implementada con Jakarta Servlet 6.0 y está pensada para
desplegarse en Apache Tomcat 10.1. El servlet gestiona el inicio, el listado, el
alta y el borrado de socios; la interfaz se genera desde el servlet y no utiliza JSP.

## Prerrequisitos

1. Importar la base de datos desde `src/main/java/baloncesto.sql`.
2. Desplegar el WAR generado con `mvn package` en Tomcat 10.1.
3. Configurar `DB_URL`, `DB_USER` y `DB_PASSWORD` en el entorno de Tomcat si las
   credenciales no son `jdbc:mysql://127.0.0.1:3306/baloncesto`, `user` y `user`.

La aplicación queda disponible en la raíz del contexto desplegado.

## Version v1.0.0

Esta version esta en Scriplet, es decir, .jsp 
para la facilidad de una programación más sencilla

## Version v2.0.0

Esta última versión hasta la fecha, implementa el servlet, para darle un toque más completo
