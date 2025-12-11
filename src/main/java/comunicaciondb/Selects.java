package comunicaciondb;

import entity.*;
import jakarta.persistence.*;
import jjn.Main;

import java.io.PrintWriter;
import java.util.List;

public class Selects {

    private static final String SEP = "@Tr&m";
    // ==================== USUARIOS ====================
    public static void listarUsuariosSimple(EntityManager em, PrintWriter out) {
        try {
            String jpql = "SELECT u FROM Usuario u ORDER BY u.nombre";
            TypedQuery<Usuario> query = em.createQuery(jpql, Usuario.class);
            List<Usuario> usuarios = query.getResultList();

            for (Usuario u : usuarios) {

                // ---- ENVIAR AL CLIENTE ----
                out.println(
                                u.getId() + Main.SEP +
                                u.getNombre() + Main.JUMP
                );

                // ---- DEBUG SERVIDOR ----
                System.out.println("fromListElement -> " +
                        u.getId() + Main.SEP + u.getNombre() + "FIN");
            }


        } catch (Exception e) {
            out.println("USUARIOS_SIMPLE_ERROR" + Main.SEP + e.getMessage());
            e.printStackTrace();
        }
        out.println("FIN_COMANDO");
    }
    public static void listarTodosLosUsuarios(EntityManager em, PrintWriter out) {
        try {
            String jpql = "SELECT u FROM Usuario u";
            TypedQuery<Usuario> query = em.createQuery(jpql, Usuario.class);
            List<Usuario> usuarios = query.getResultList();
            Departamento d;
            int id;
            String nombre;
            for (Usuario u : usuarios) {
                if (u.getIdDepartamento() == null){
                    id=0;
                    nombre="sin departamento";

                }else{
                    d = em.find(Departamento.class, u.getIdDepartamento());
                    id=d.getId();
                    nombre=d.getNombre();
                }

                    out.println(
                                            u.getId() + SEP +
                                            u.getNombre() + SEP +
                                            u.getMail() + SEP +
                                            u.getRol() + SEP +
                                            id + SEP +
                                            nombre + SEP +
                                            u.getFechaAlta() + SEP +
                                            u.getDireccion() + (Main.JUMP)
                    );
                    System.out.println("respuesta listarTodos Los Usuarios ->"+usuarios.size());
                            //+u.getId() + SEP +u.getNombre() + SEP +u.getMail() + SEP +u.getRol() + SEP +d.getId() + SEP + d.getNombre() + "FIN_COMANDO");

            }
            out.println("FIN_COMANDO");//fin del comando
        } catch (Exception e) {
            System.out.println("TODOS_USUARIOS_ERROR" + SEP + "Error: " + e.getMessage());
        }

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
        System.out.println("=== INTENTO DE LOGIN ===");
        System.out.println("Identificador: [" + identificador + "]");

        try {
            String jpql = "SELECT u FROM Usuario u " +
                    "WHERE LOWER(u.nombre) = LOWER(:identificador) " +
                    "   OR LOWER(u.mail) = LOWER(:identificador)";

            TypedQuery<Usuario> query = em.createQuery(jpql, Usuario.class);
            query.setParameter("identificador", identificador.trim());

            Usuario usuario = query.getSingleResult();

            // COMPROBAR CONTRASEÑA
            if (!usuario.getPassword().equals(password)) {
                out.println("LOGIN_ERROR" + SEP + "Contraseña incorrecta");
                System.out.println("FALLO → contraseña incorrecta");
                out.println("FIN_COMANDO");
                return;
            }

            // OBTENER DEPARTAMENTO
            String nombreDepartamento = "Sin departamento";
            if (usuario.getIdDepartamento() != null && usuario.getIdDepartamento() != 0) {
                try {
                    Departamento depto = em.find(Departamento.class, usuario.getIdDepartamento());
                    if (depto != null) nombreDepartamento = depto.getNombre();
                } catch (Exception e) {
                    System.out.println("Error cargando departamento: " + e.getMessage());
                }
            }
            // ENVIAR RESPUESTA CORRECTA
            out.println("LOGIN_OK" + SEP +
                    usuario.getId() + SEP +
                    usuario.getNombre() + SEP +
                    usuario.getMail() + SEP +
                    usuario.getRol() + SEP +
                    usuario.getIdDepartamento() + SEP +
                    nombreDepartamento + SEP +
                    usuario.getFechaAlta() + SEP +
                    (usuario.getDireccion() != null ? usuario.getDireccion() : ""));

            System.out.println("LOGIN EXITOSO → " + usuario.getNombre() + " | " + usuario.getRol());

        } catch (NoResultException e) {
            out.println("LOGIN_ERROR" + SEP + "Usuario no encontrado");
            System.out.println("FALLO → usuario no existe");

        } catch (Exception e) {
            e.printStackTrace();
            out.println("LOGIN_ERROR" + SEP + "Error del servidor");
            System.out.println("ERROR CRÍTICO EN LOGIN");

        } finally {
            // SOLO UN FIN_COMANDO → SIEMPRE AL FINAL
            out.println("FIN_COMANDO");
        }
    }
    public static void listarNominasUsuario(EntityManager em, PrintWriter out, int idUsuario) {
        try {
            String jpql = "SELECT n FROM Nomina n WHERE n.idUsuario = :idUsuario ORDER BY n.fecha DESC";
            TypedQuery<Nomina> query = em.createQuery(jpql, Nomina.class);
            query.setParameter("idUsuario", idUsuario);

            List<Nomina> nominas = query.getResultList();

            for (Nomina n : nominas) {

                // ===== ENVIAR AL CLIENTE =====
                out.println(
                        n.getId() + Main.SEP +
                                n.getImporte() + Main.SEP +
                                n.getFecha() + Main.SEP +
                                n.getConcepto() + Main.SEP +
                                n.getTipo() + Main.SEP +
                                n.getIdUsuario() +
                                Main.JUMP
                );

                // ===== DEBUG SERVIDOR =====
                System.out.println("fromNomina -> " +
                        n.getId() + Main.SEP +
                        n.getImporte() + Main.SEP +
                        n.getFecha() + Main.SEP +
                        n.getConcepto() + Main.SEP +
                        n.getTipo() + Main.SEP +
                        n.getIdUsuario() + " FIN");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("ERROR");
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
            Usuario creador = em.find(Usuario.class, idUsuarioCreador);
            if (creador == null) {
                out.println("TAREAS_CREADAS_ERROR" + SEP + "Usuario no encontrado");
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
            for (Tarea t : tareas) {
                    // Obtener nombre del usuario asignado
                    Usuario asignado = em.find(Usuario.class, t.getIdUsuarioAsignado());
                    String nombreAsignado = asignado != null ? asignado.getNombre() : "Sin asignar";
                    String nombreCreador = creador.getNombre();

                    // Formato EXACTO que espera el cliente: 9 campos separados por SEP
                    out.println(
                                    t.getId() + SEP +                                     // 0 - ID
                                    (t.getTitulo() != null ? t.getTitulo() : "Sin título") + SEP +  // 1 - Título
                                    t.getInformacion() + SEP +                            // 2 - Descripción (texto completo)
                                    t.getFechaCreacion() + SEP +                          // 3 - Fecha creación (LocalDate → 2025-12-06)
                                    (t.getFechaInicio() != null ? t.getFechaInicio() : "null") + SEP +   // 4 - Fecha inicio
                                    (t.getFechaFin() != null ? t.getFechaFin() : "null") + SEP +         // 5 - Fecha fin
                                    (t.getEstado() != null ? t.getEstado() : "pendiente") + SEP +        // 6 - Estado
                                    nombreCreador + SEP +                                 // 7 - Nombre del creador
                                    nombreAsignado + SEP +
                                    t.getIdUsuarioAsignado() + Main.JUMP                                                // 8 - Nombre del asignado
                    );

            }

            out.println("FIN_COMANDO");

        } catch (Exception e) {
            e.printStackTrace();
            out.println("TAREAS_CREADAS_ERROR" + SEP + "Error: " + e.getMessage());
            out.println("FIN_COMANDO");
        }
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