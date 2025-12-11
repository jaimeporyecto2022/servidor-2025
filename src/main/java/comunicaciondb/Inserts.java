// src/main/java/comunicaciondb/Inserts.java
package comunicaciondb;

import entity.*;
import jakarta.persistence.*;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Inserts {

    private static final String SEP = "@Tr&m";

    public static void insertarUsuario(EntityManager em, PrintWriter out,
                                       String nombre, String mail, String password,
                                       String rol, Integer idDepartamento) {
        em.getTransaction().begin();
        try {
            // Validar que el mail no exista ya
            TypedQuery<Long> queryMail = em.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.mail = :mail", Long.class);
            queryMail.setParameter("mail", mail);
            if (queryMail.getSingleResult() > 0) {
                out.println("INSERT_USUARIO_ERROR" + SEP + "El email ya está registrado");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre.trim());
            nuevo.setMail(mail.trim().toLowerCase());
            nuevo.setPassword(password); // En producción: BCrypt
            nuevo.setRol(rol != null ? rol.trim().toLowerCase() : "empleado");
            nuevo.setIdDepartamento(idDepartamento);

            em.persist(nuevo);
            em.getTransaction().commit();


        } catch (Exception e) {
            em.getTransaction().rollback();
        }
    }

    public static void insertarNomina(EntityManager em, PrintWriter out,
                                      int idUsuario,
                                      BigDecimal importe,
                                      String concepto,
                                      String tipo) {
        em.getTransaction().begin();
        try {
            Usuario usuario = em.find(Usuario.class, idUsuario);
            if (usuario == null) {
                out.println("INSERT_NOMINA_ERROR" + SEP + "Usuario no existe");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            // Validar tipo permitido
            if (!tipo.matches("(?i)salario|hora_extra|plus|deduccion")) {
                out.println("INSERT_NOMINA_ERROR" + SEP + "Tipo inválido. Usa: salario, hora_extra, plus o deduccion");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            Nomina nomina = new Nomina();
            nomina.setImporte(importe);
            nomina.setFecha(java.sql.Date.valueOf(LocalDate.now())); // hoy
            nomina.setConcepto(concepto);
            nomina.setTipo(tipo.toLowerCase());
            nomina.setIdUsuario(idUsuario);

            em.persist(nomina);
            em.getTransaction().commit();



        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        }
    }

    public static void insertarTarea(EntityManager em, PrintWriter out,
                                     int idCreador, int idAsignado,
                                     String informacion, String fechaInicio, String fechaFin,String estado,String titulo) {
        em.getTransaction().begin();
        try {
            Usuario creador = em.find(Usuario.class, idCreador);
            Usuario asignado = em.find(Usuario.class, idAsignado);

            if (creador == null || asignado == null) {
                out.println("INSERT_TAREA_ERROR" + SEP + "Usuario creador o asignado no existe");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            if (informacion == null || informacion.trim().isEmpty()) {
                out.println("INSERT_TAREA_ERROR" + SEP + "La información es obligatoria");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            Tarea tarea = new Tarea();
            tarea.setIdUsuarioCreador(idCreador);
            tarea.setIdUsuarioAsignado(idAsignado);
            tarea.setInformacion(informacion.trim());
            tarea.setEstado(estado);
            tarea.setTitulo(titulo);

            // Fecha de creación: HOY (segura)
            tarea.setFechaCreacion(java.sql.Date.valueOf(LocalDate.now()));

            // FECHAS INICIO Y FIN: 100% SEGURAS CON TRY-CATCH
            tarea.setFechaInicio(parsearFechaSqlSegura(fechaInicio));
            tarea.setFechaFin(parsearFechaSqlSegura(fechaFin));

            tarea.setEstado("pendiente");

            em.persist(tarea);
            em.getTransaction().commit();


        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        }
    }

    public static java.sql.Date parsearFechaSqlSegura(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate ld = LocalDate.parse(fechaStr.trim());
            return java.sql.Date.valueOf(ld);
        } catch (Exception e) {
            return null; // Si la fecha está mal escrita → simplemente null
        }
    }
    public static void insertarReporteYActualizarTarea(EntityManager em, PrintWriter out,
                                                       int idUsuarioReporte,
                                                       int idTarea,
                                                       String informacion,
                                                       String estadoStr,
                                                       String fechaInicioStr,
                                                       String fechaFinStr) {
        em.getTransaction().begin();
        try {
            // Validar usuario y tarea
            Usuario usuario = em.find(Usuario.class, idUsuarioReporte);
            Tarea tarea = em.find(Tarea.class, idTarea);

            if (usuario == null) {
                out.println("REPORTE_ERROR" + SEP + "Usuario no existe");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }
            if (tarea == null) {
                out.println("REPORTE_ERROR" + SEP + "Tarea no existe");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            // Validar estado permitido
            String estado = estadoStr != null ? estadoStr.trim() : "pendiente";
            if (!List.of("pendiente", "No puedo hacerlo", "imposible", "completado").contains(estado)) {
                out.println("REPORTE_ERROR" + SEP + "Estado inválido");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            // Crear reporte
            Reporte reporte = new Reporte();
            reporte.setIdUsuarioReporte(idUsuarioReporte);
            reporte.setIdTarea(idTarea);
            reporte.setInformacion(informacion != null ? informacion.trim() : "");
            reporte.setEstado(estado);
            reporte.setFechaInicio(parsearFechaSqlSegura(fechaInicioStr));
            reporte.setFechaFin(parsearFechaSqlSegura(fechaFinStr));

            em.persist(reporte);

            // ACTUALIZAR ESTADO DE LA TAREA AL MISMO DEL REPORTE
            tarea.setEstado(estado);
            em.merge(tarea);

            em.getTransaction().commit();

            out.println("REPORTE_OK" + SEP +
                    reporte.getId() + SEP +
                    tarea.getId() + SEP +
                    "Tarea #" + tarea.getId() + " ahora: " + estado.toUpperCase());

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            out.println("REPORTE_ERROR" + SEP + "Error: " + e.getMessage());
        }
        out.println("FIN_COMANDO");
    }
}