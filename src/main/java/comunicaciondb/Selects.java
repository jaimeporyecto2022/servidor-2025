package comunicaciondb;

import entity.*;
import jakarta.persistence.*;
import java.io.PrintWriter;
import java.util.List;

public class Selects {

    private static final String SEP = "@Tr&m";
    // ==================== USUARIOS ====================
    public static void listarTodosLosUsuarios(EntityManager em, PrintWriter out) {
        try {
            String jpql = "SELECT u FROM Usuario u ORDER BY u.idDepartamento, u.nombre";
            TypedQuery<Usuario> query = em.createQuery(jpql, Usuario.class);
            List<Usuario> usuarios = query.getResultList();

            if (usuarios.isEmpty()) {
                out.println("TODOS_USUARIOS" + SEP + "0" + SEP + "No hay usuarios registrados");
            } else {
                out.println("TODOS_USUARIOS" + SEP + usuarios.size());

                for (Usuario u : usuarios) {
                    Integer idDepto = u.getIdDepartamento() != null ? u.getIdDepartamento() : 0;
                    String nombreDepto = "Sin departamento";
                    if (u.getIdDepartamento() != null) {
                        Departamento d = em.find(Departamento.class, u.getIdDepartamento());
                        nombreDepto = d != null ? d.getNombre() : "ID " + u.getIdDepartamento();
                    }

                    out.println(
                            u.getId() + SEP +
                                    u.getNombre() + SEP +
                                    u.getMail() + SEP +
                                    (u.getRol() != null ? u.getRol().toUpperCase() : "EMPLEADO") + SEP +
                                    idDepto + SEP +
                                    nombreDepto
                    );
                }
            }

        } catch (Exception e) {
            out.println("TODOS_USUARIOS_ERROR" + SEP + "Error: " + e.getMessage());
        }
        out.println("FIN_COMANDO");
    }

    // ==================== USUARIOS POR DEPARTAMENTO ====================
    public static void listarUsuariosPorDepartamento(EntityManager em, PrintWriter out, int idDepartamento) {
        try {
            Departamento dept = em.find(Departamento.class, idDepartamento);
            if (dept == null) {
                out.println("DEP_ERROR" + SEP + "Departamento no existe");
                out.println("FIN_COMANDO");
                return;
            }

            String jpql = "SELECT u FROM Usuario u WHERE u.idDepartamento = :idDept ORDER BY u.nombre";
            TypedQuery<Usuario> query = em.createQuery(jpql, Usuario.class);
            query.setParameter("idDept", idDepartamento);

            List<Usuario> usuarios = query.getResultList();

            if (usuarios.isEmpty()) {
                out.println("DEP_OK" + SEP + "0" + SEP + "Sin usuarios en " + dept.getNombre());
            } else {
                out.println("DEP_OK" + SEP + usuarios.size() + SEP + idDepartamento); // ← ID del depto en cabecera

                for (Usuario u : usuarios) {
                    out.println(u.getId() + SEP +
                            u.getNombre() + SEP +
                            u.getMail() + SEP +
                            (u.getRol() != null ? u.getRol().toUpperCase() : "EMPLEADO") + SEP +
                            idDepartamento); // ← ID del departamento (redundante pero útil)
                }
            }

        } catch (Exception e) {
            out.println("DEP_ERROR" + SEP + "Error al consultar usuarios");
        }
        out.println("FIN_COMANDO");
    }

    // ==================== LOGIN ====================
    public static void login(EntityManager em, PrintWriter out, String identificador, String password) {
        try {
            String jpql = "SELECT u FROM Usuario u WHERE (u.nombre = :identificador OR u.mail = :identificador) AND u.password = :password";
            TypedQuery<Usuario> query = em.createQuery(jpql, Usuario.class);
            query.setParameter("identificador", identificador);
            query.setParameter("password", password);

            Usuario usuario = query.getSingleResult();

            Integer idDepto = usuario.getIdDepartamento() != null ? usuario.getIdDepartamento() : 0;

            out.println("LOGIN_OK" + SEP +
                    usuario.getId() + SEP +
                    usuario.getNombre() + SEP +
                    usuario.getMail() + SEP +
                    (usuario.getRol() != null ? usuario.getRol().toUpperCase() : "EMPLEADO") + SEP +
                    idDepto); // ← NUEVO: ID del departamento

        } catch (NoResultException e) {
            out.println("LOGIN_ERROR" + SEP + "Credenciales incorrectas");
        } catch (Exception e) {
            out.println("LOGIN_ERROR" + SEP + "Error interno del servidor");
        }
        out.println("FIN_COMANDO");
    }

    // ==================== Tareas asignadas al usuario ====================
    public static void enviarTareasDeUsuario(EntityManager em, PrintWriter out, int idUsuarioAsignado) {
        try {
            Usuario asignado = em.find(Usuario.class, idUsuarioAsignado);
            if (asignado == null) {
                out.println("TAREAS_ERROR" + SEP + "Usuario con ID " + idUsuarioAsignado + " no existe");
                out.println("FIN_COMANDO");
                return;
            }

            String jpql = "SELECT t FROM Tarea t WHERE t.idUsuarioAsignado = :idAsignado ORDER BY t.fechaCreacion DESC";
            TypedQuery<Tarea> query = em.createQuery(jpql, Tarea.class);
            query.setParameter("idAsignado", idUsuarioAsignado);

            List<Tarea> tareas = query.getResultList();

            if (tareas.isEmpty()) {
                out.println("TAREAS_OK" + SEP + "0" + SEP + asignado.getNombre() + " no tiene tareas asignadas");
            } else {
                out.println("TAREAS_OK" + SEP + tareas.size());

                for (Tarea t : tareas) {
                    Usuario creador = em.find(Usuario.class, t.getIdUsuarioCreador());
                    String nombreCreador = creador != null ? creador.getNombre() : "Desconocido";
                    String nombreAsignado = asignado.getNombre();

                    String responsable = "from " + nombreCreador + " to " + nombreAsignado;

                    out.println(
                            t.getId() + SEP +
                                    t.getInformacion().replace(SEP, " ") + SEP +
                                    (t.getFechaInicio() != null ? t.getFechaInicio() : "Sin fecha") + SEP +
                                    (t.getFechaFin() != null ? t.getFechaFin() : "Sin fecha") + SEP +
                                    t.getEstado().toUpperCase() + SEP +
                                    responsable
                    );
                }
            }

        } catch (Exception e) {
            out.println("TAREAS_ERROR" + SEP + "Error interno: " + e.getMessage());
        }
        out.println("FIN_COMANDO");
    }

    // ==================== Tareas creadas por el usuario ====================
    public static void enviarTareasCreadasPorUsuario(EntityManager em, PrintWriter out, int idUsuarioCreador) {
        try {
            // Verificar que el creador existe
            Usuario creador = em.find(Usuario.class, idUsuarioCreador);
            if (creador == null) {
                out.println("TAREAS_CREADAS_ERROR" + SEP + "Usuario con ID " + idUsuarioCreador + " no existe");
                out.println("FIN_COMANDO");
                return;
            }

            String jpql = """
                SELECT t FROM Tarea t 
                WHERE t.idUsuarioCreador = :idCreador 
                ORDER BY t.fechaCreacion DESC
                """;

            TypedQuery<Tarea> query = em.createQuery(jpql, Tarea.class);
            query.setParameter("idCreador", idUsuarioCreador);

            List<Tarea> tareas = query.getResultList();

            if (tareas.isEmpty()) {
                out.println("TAREAS_CREADAS_OK" + SEP + "0" + SEP + creador.getNombre() + " no ha creado ninguna tarea");
            } else {
                out.println("TAREAS_CREADAS_OK" + SEP + tareas.size());

                for (Tarea t : tareas) {
                    // Usuario asignado
                    Usuario asignado = em.find(Usuario.class, t.getIdUsuarioAsignado());
                    String nombreAsignado = asignado != null ? asignado.getNombre() : "Desconocido";

                    String responsable = "from " + creador.getNombre() + " to " + nombreAsignado;

                    out.println(
                            t.getId() + SEP +
                                    t.getInformacion().replace(SEP, " ") + SEP +
                                    (t.getFechaInicio() != null ? t.getFechaInicio() : "Sin fecha") + SEP +
                                    (t.getFechaFin() != null ? t.getFechaFin() : "Sin fecha") + SEP +
                                    t.getEstado().toUpperCase() + SEP +
                                    responsable
                    );
                }
            }

        } catch (Exception e) {
            out.println("TAREAS_CREADAS_ERROR" + SEP + "Error interno: " + e.getMessage());
        }
        out.println("FIN_COMANDO");
    }

    // ==================== reportes creados por el usuario ====================
    public static void enviarReportesCreadosPorUsuario(EntityManager em, PrintWriter out, int idUsuarioCreador) {
        try {
            // Verificar que el usuario existe
            Usuario creador = em.find(Usuario.class, idUsuarioCreador);
            if (creador == null) {
                out.println("REPORTES_CREADOS_ERROR" + SEP + "Usuario no existe");
                out.println("FIN_COMANDO");
                return;
            }

            String jpql = """
                SELECT r FROM Reporte r 
                WHERE r.idUsuarioReporte = :idCreador 
                ORDER BY r.fechaInicio DESC
                """;

            TypedQuery<Reporte> query = em.createQuery(jpql, Reporte.class);
            query.setParameter("idCreador", idUsuarioCreador);

            List<Reporte> reportes = query.getResultList();

            if (reportes.isEmpty()) {
                out.println("REPORTES_CREADOS_OK" + SEP + "0" + SEP + creador.getNombre() + " no ha creado reportes");
            } else {
                out.println("REPORTES_CREADOS_OK" + SEP + reportes.size());

                for (Reporte r : reportes) {
                    // Nombre de la tarea asociada (si existe)
                    String nombreTarea = "Sin tarea asociada";
                    if (r.getIdTarea() != null) {
                        Tarea t = em.find(Tarea.class, r.getIdTarea());
                        if (t != null) {
                            nombreTarea = t.getInformacion().length() > 40
                                    ? t.getInformacion().substring(0, 37) + "..."
                                    : t.getInformacion();
                        }
                    }

                    out.println(
                            r.getId() + SEP +
                                    (r.getFechaInicio() != null ? r.getFechaInicio() : "Sin fecha") + SEP +
                                    (r.getFechaFin() != null ? r.getFechaFin() : "Sin fecha") + SEP +
                                    (r.getInformacion() != null ? r.getInformacion().replace(SEP, " ") : "Sin contenido") + SEP +
                                    r.getEstado().toUpperCase() + SEP +
                                    "por " + creador.getNombre() + SEP +
                                    nombreTarea
                    );
                }
            }

        } catch (Exception e) {
            out.println("REPORTES_CREADOS_ERROR" + SEP + "Error: " + e.getMessage());
        }
        out.println("FIN_COMANDO");
    }




}