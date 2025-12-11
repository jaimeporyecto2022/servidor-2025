package comunicaciondb;
import entity.*;
import jakarta.persistence.*;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static comunicaciondb.Inserts.parsearFechaSqlSegura;

public class Updates {
    private static final String SEP = "@Tr&m";

    public static void actualizarTarea(EntityManager em, PrintWriter out,
                                       int idTarea,int idCreador, int idAsignado,
                                       String informacion, String fechaInicio, String fechaFin,String estado,String titulo) {  // puede ser null si no se cambia
        em.getTransaction().begin();
        try {
            Tarea tarea = em.find(Tarea.class, idTarea);
            if (tarea == null) {
                out.println("UPDATE_TAREA_ERROR" + SEP + "Tarea no existe");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }
            Usuario user;
            tarea.setInformacion(informacion);
            tarea.setTitulo(titulo);
            //tarea.setCreador(idCreador);//no tiene sentido modificar el creador
            tarea.setIdUsuarioAsignado(idAsignado);
            tarea.setEstado(estado);
            tarea.setFechaInicio(parsearFechaSqlSegura(fechaInicio));
            tarea.setFechaFin(parsearFechaSqlSegura(fechaFin));

            em.merge(tarea);
            em.getTransaction().commit();


        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            //out.println("UPDATE_TAREA_ERROR" + SEP + "Error: " + e.getMessage());
        }
    }


    public static void actualizarEmpleado(EntityManager em, PrintWriter out,
                                          int idUsuario,
                                          String nuevoNombre,
                                          String nuevoEmail,
                                          String nuevaPassword,
                                          String nuevoRol,
                                          Integer nuevoDepartamento) {
        em.getTransaction().begin();
        try {
            Usuario usuario = em.find(Usuario.class, idUsuario);
            if (usuario == null) {
                out.println("UPDATE_EMPLEADO_ERROR" + SEP + "Empleado no existe");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            boolean cambiado = false;

            // NOMBRE
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                usuario.setNombre(nuevoNombre.trim());
                cambiado = true;
            }

            // EMAIL
            if (nuevoEmail != null && !nuevoEmail.trim().isEmpty()) {
                String email = nuevoEmail.trim().toLowerCase();
                TypedQuery<Long> query = em.createQuery(
                        "SELECT COUNT(u) FROM Usuario u WHERE u.mail = :mail AND u.id <> :id", Long.class);
                query.setParameter("mail", email);
                query.setParameter("id", idUsuario);

                if (query.getSingleResult() > 0) {
                    out.println("UPDATE_EMPLEADO_ERROR" + SEP + "Email ya está en uso");
                    em.getTransaction().rollback();
                    out.println("FIN_COMANDO");
                    return;
                }
                usuario.setMail(email);
                cambiado = true;
            }

            // CONTRASEÑA
            if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
                usuario.setPassword(nuevaPassword.trim()); // En producción: BCrypt
                cambiado = true;
            }

            // ROL
            if (nuevoRol != null && !nuevoRol.isEmpty()) {
                String rol = nuevoRol.trim().toLowerCase();
                if (List.of("empleado", "jefe", "ceo").contains(rol)) {
                    usuario.setRol(rol);
                    cambiado = true;
                } else {
                    out.println("UPDATE_EMPLEADO_ERROR" + SEP + "Rol inválido: empleado, jefe o ceo");
                    em.getTransaction().rollback();
                    out.println("FIN_COMANDO");
                    return;
                }
            }

            // DEPARTAMENTO
            if (nuevoDepartamento != null) {
                Departamento depto = em.find(Departamento.class, nuevoDepartamento);
                if (depto == null) {
                    out.println("UPDATE_EMPLEADO_ERROR" + SEP + "Departamento no existe");
                    em.getTransaction().rollback();
                    out.println("FIN_COMANDO");
                    return;
                }
                usuario.setIdDepartamento(nuevoDepartamento);
                cambiado = true;
            }

            if (!cambiado) {
                out.println("UPDATE_EMPLEADO_ERROR" + SEP + "No se envió ningún dato para actualizar");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            em.merge(usuario);
            em.getTransaction().commit();

            out.println("UPDATE_EMPLEADO_OK" + SEP +
                    usuario.getId() + SEP +
                    usuario.getNombre() + SEP +
                    usuario.getMail() + SEP +
                    usuario.getRol().toUpperCase());

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            out.println("UPDATE_EMPLEADO_ERROR" + SEP + "Error: " + e.getMessage());
        }
        out.println("FIN_COMANDO");
    }

    public static void actualizarNomina(EntityManager em, PrintWriter out,
                                        int idNomina,
                                        BigDecimal nuevoImporte,
                                        String nuevoConcepto,
                                        String nuevoTipo) {
        em.getTransaction().begin();
        try {
            Nomina nomina = em.find(Nomina.class, idNomina);
            if (nomina == null) {
                out.println("UPDATE_NOMINA_ERROR" + SEP + "Nómina no existe");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            boolean cambiado = false;

            // IMPORTE
            if (nuevoImporte != null && nuevoImporte.compareTo(BigDecimal.ZERO) > 0) {
                nomina.setImporte(nuevoImporte);
                cambiado = true;
            }

            // CONCEPTO
            if (nuevoConcepto != null && !nuevoConcepto.trim().isEmpty()) {
                nomina.setConcepto(nuevoConcepto.trim());
                cambiado = true;
            }

            // TIPO
            if (nuevoTipo != null && !nuevoTipo.isEmpty()) {
                String tipo = nuevoTipo.trim().toLowerCase();
                if (List.of("salario", "hora_extra", "plus", "deduccion").contains(tipo)) {
                    nomina.setTipo(tipo);
                    cambiado = true;
                } else {
                    out.println("UPDATE_NOMINA_ERROR" + SEP + "Tipo inválido: salario, hora_extra, plus o deduccion");
                    em.getTransaction().rollback();
                    out.println("FIN_COMANDO");
                    return;
                }
            }

            if (!cambiado) {
                out.println("UPDATE_NOMINA_ERROR" + SEP + "No se modificó ningún campo");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            em.merge(nomina);
            em.getTransaction().commit();

            Usuario usuario = em.find(Usuario.class, nomina.getIdUsuario());


        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            out.println("UPDATE_NOMINA_ERROR" + SEP + "Error: " + e.getMessage());
        }
    }

    public static void actualizarReporteConSync(EntityManager em, PrintWriter out,
                                                int idReporte,
                                                String nuevaInformacion,
                                                String nuevoEstado,
                                                String fechaInicioStr,
                                                String fechaFinStr) {
        em.getTransaction().begin();
        try {
            Reporte reporte = em.find(Reporte.class, idReporte);
            if (reporte == null) {
                out.println("UPDATE_REPORTE_ERROR" + SEP + "Reporte no existe");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            boolean cambiado = false;

            // INFORMACIÓN
            if (nuevaInformacion != null && !nuevaInformacion.trim().isEmpty()) {
                reporte.setInformacion(nuevaInformacion.trim());
                cambiado = true;
            }

            // ESTADO → ¡SINCRONIZACIÓN AUTOMÁTICA CON LA TAREA!
            if (nuevoEstado != null && !nuevoEstado.isEmpty()) {
                String estado = nuevoEstado.trim();
                if (List.of("pendiente", "No puedo hacerlo", "imposible", "completado").contains(estado)) {
                    reporte.setEstado(estado);

                    // SINCRONIZAR TAREA
                    Tarea tarea = em.find(Tarea.class, reporte.getIdTarea());
                    if (tarea != null) {
                        tarea.setEstado(estado);
                        em.merge(tarea); // Actualizamos la tarea también
                    }

                    cambiado = true;
                } else {
                    out.println("UPDATE_REPORTE_ERROR" + SEP + "Estado inválido");
                    em.getTransaction().rollback();
                    out.println("FIN_COMANDO");
                    return;
                }
            }

            // FECHAS (seguras)
            java.sql.Date fIni = parsearFechaSqlSegura(fechaInicioStr);
            java.sql.Date fFin = parsearFechaSqlSegura(fechaFinStr);
            if (fIni != null || fFin != null) {
                reporte.setFechaInicio(fIni);
                reporte.setFechaFin(fFin);
                cambiado = true;
            }

            if (!cambiado) {
                out.println("UPDATE_REPORTE_ERROR" + SEP + "No se modificó nada");
                em.getTransaction().rollback();
                out.println("FIN_COMANDO");
                return;
            }

            em.merge(reporte);
            em.getTransaction().commit();



        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            out.println("UPDATE_REPORTE_ERROR" + SEP + "Error: " + e.getMessage());
        }
    }

}
