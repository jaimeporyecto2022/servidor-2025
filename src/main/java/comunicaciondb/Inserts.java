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
                                       String rol, String nombredep, String direccion) {
        System.out.println(nombre + mail + password + rol + nombredep + direccion);
        int idDepartamento;
        em.getTransaction().begin();
        try {
            // Validar que el mail no exista ya
            TypedQuery<Long> queryMail = em.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.mail = :mail", Long.class);
            queryMail.setParameter("mail", mail);
            if (queryMail.getSingleResult() > 0) {
                return;
            }
            TypedQuery<Integer> qDep = em.createQuery(
                    "SELECT d.id FROM Departamento d WHERE d.nombre = :nombre", Integer.class);
            qDep.setParameter("nombre", nombredep);
            idDepartamento = qDep.getSingleResult();

            Usuario nuevo = new Usuario();
            nuevo.setFechaAlta(java.sql.Date.valueOf(LocalDate.now()));
            nuevo.setNombre(nombre.trim());
            nuevo.setMail(mail.trim().toLowerCase());
            nuevo.setPassword(password); // En producción: BCrypt
            nuevo.setRol(rol != null ? rol.trim().toLowerCase() : "empleado");
            nuevo.setIdDepartamento(idDepartamento);
            nuevo.setDireccion(direccion);

            em.persist(nuevo);
            em.getTransaction().commit();


        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println(e);
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
                                                       String estadoStr) {
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
            reporte.setFechacreacion(java.sql.Date.valueOf(LocalDate.now()));

            em.persist(reporte);
            if (tarea.getEstado().equals("completado")) {
                System.out.println("nuevatarea en supuestamente tarea completada");
            }else {
                tarea.setEstado(reporte.getEstado());
            }


            // ACTUALIZAR ESTADO DE LA TAREA AL MISMO DEL REPORTE
            em.merge(tarea);

            em.getTransaction().commit();


        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();

        }
    }
}