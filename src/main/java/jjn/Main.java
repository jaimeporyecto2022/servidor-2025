package jjn;
import comunicaciondb.*;
import jakarta.persistence.*;

import java.io.*;
import java.net.*;
import java.math.BigDecimal;

public class Main {
    private static final String SEP = "@Tr&m";
    private static final int PUERTO = 5000;
    private static EntityManagerFactory emf;
    public static void main(String[] args) {//TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        System.out.println("entra");
        // Conectar a la base de datos
        emf = Persistence.createEntityManagerFactory("gestionPU");
        System.out.println("SERVIDOR INICIADO EN PUERTO " + PUERTO);
        System.out.println("¡0_0!ok");

        //bucle infinito que recibe clientes
        try (ServerSocket server = new ServerSocket(PUERTO)) {
            while (true) {
                Socket cliente = server.accept();
                System.out.println("Cliente conectado desde: " + cliente.getInetAddress());
                new Thread(() -> manejarCliente(cliente)).start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        } finally {
            if (emf != null) emf.close();
        }
    }
    private static void manejarCliente(Socket cliente) {
        try (PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()))) {

            String comando;
            while ((comando = in.readLine()) != null) {
                if (comando.equalsIgnoreCase("SALIR")) break;
                procesarComando(comando.trim(),out);
            }
        } catch (Exception e) {
            System.out.println("Error con cliente: " + e.getMessage());
        } finally {
            try { cliente.close(); } catch (IOException ignored) {}
        }
    }
    private static void procesarComando(String cmd, PrintWriter out) {
        EntityManager em = emf.createEntityManager();
        try {
            String[] partes = cmd.split("\\|", -1);

            switch (partes[0].toUpperCase()) {
                //======================= SELECTS===============================
                case "LOGIN" -> {
                    String user = partes[1];
                    String pass = partes[2];
                    Selects.login(em, out, user, pass);

                }

                case "USUARIOS_DEP" -> {
                    int idDept = Integer.parseInt(partes[1]);
                    Selects.listarUsuariosPorDepartamento(em, out, idDept);
                }

                case "TODOS_USUARIOS" -> {
                    Selects.listarTodosLosUsuarios(em, out);
                }

                case "MIS_TAREAS" -> {
                    int idUsuario = Integer.parseInt(partes[1]);
                    Selects.enviarTareasDeUsuario(em, out, idUsuario);
                }
                case "MIS_REPORTES" -> {
                    int idCreador = Integer.parseInt(partes[1]);
                    Selects.enviarReportesCreadosPorUsuario(em, out, idCreador);

                }
                case "MIS_TAREAS_CREADAS" -> {
                    int idCreador = Integer.parseInt(partes[1]);
                    Selects.enviarTareasCreadasPorUsuario(em, out, idCreador);

                }

                //================================ INSERTS=================================

                case "CREAR_USUARIO" -> {
                    String nombre = partes[1];
                    String mail = partes[2];
                    String pass = partes[3];
                    String rol = partes.length > 4 ? partes[4] : null;
                    Integer depto = partes.length > 5 ? Integer.parseInt(partes[5]) : null;
                    Inserts.insertarUsuario(em, out, nombre, mail, pass, rol, depto);

                }

                case "CREAR_NOMINA" -> {
                    Inserts.insertarNomina(
                            em,
                            out,
                            Integer.parseInt(partes[1]),
                            new BigDecimal(partes[2]),
                            partes[3],
                            partes[4]
                    );
                }

                case "CREAR_TAREA" -> {
                    int creador = Integer.parseInt(partes[1]);
                    int asignado = Integer.parseInt(partes[2]);
                    String info = partes[3];
                    String fIni = partes.length > 4 ? partes[4] : null;
                    String fFin = partes.length > 5 ? partes[5] : null;
                    Inserts.insertarTarea(em, out, creador, asignado, info, fIni, fFin);

                }

                case "CREAR_REPORTE" -> {
                    String informacion = partes.length > 3 ? partes[3] : "";
                    String estado = partes.length > 4 ? partes[4] : "pendiente";
                    String fIni = partes.length > 5 ? partes[5] : null;
                    String fFin = partes.length > 6 ? partes[6] : null;

                    Inserts.insertarReporteYActualizarTarea(em, out,
                            Integer.parseInt(partes[1]),
                            Integer.parseInt(partes[2]),
                            informacion, estado, fIni, fFin
                    );
                }
                //======================================= DELETE =======================================
                case "ELIMINAR_USUARIO" -> {
                    Deletes.eliminarUsuario(em, out, Integer.parseInt(partes[1]));
                }

                case "ELIMINAR_NOMINA" -> {
                    Deletes.eliminarNomina(em, out, Integer.parseInt(partes[1]));
                }

                case "ELIMINAR_TAREA" -> {
                    Deletes.eliminarTarea(em, out, Integer.parseInt(partes[1]));
                }

                case "ELIMINAR_REPORTE" -> {
                    Deletes.eliminarReporte(em, out, Integer.parseInt(partes[1]));
                }
                //================================== Update ===================

                case "ACTUALIZAR_EMPLEADO" -> {
                    String nombre = partes.length > 2 && !partes[2].isEmpty() ? partes[2] : null;
                    String email = partes.length > 3 && !partes[3].isEmpty() ? partes[3] : null;
                    String pass = partes.length > 4 && !partes[4].isEmpty() ? partes[4] : null;
                    String rol = partes.length > 5 && !partes[5].isEmpty() ? partes[5] : null;
                    Integer depto = partes.length > 6 && !partes[6].isEmpty() ? Integer.parseInt(partes[6]) : null;

                    Updates.actualizarEmpleado(em, out,
                            Integer.parseInt(partes[1]),
                            nombre, email, pass, rol, depto
                    );
                }
                case "ACTUALIZAR_TAREA" -> {
                    String info = partes.length > 2 && !partes[2].isEmpty() ? partes[2] : null;
                    String estado = partes.length > 3 && !partes[3].isEmpty() ? partes[3] : null;
                    String fIni = partes.length > 4 && !partes[4].isEmpty() ? partes[4] : null;
                    String fFin = partes.length > 5 && !partes[5].isEmpty() ? partes[5] : null;
                    Integer nuevoAsignado = partes.length > 6 && !partes[6].isEmpty() ? Integer.parseInt(partes[6]) : null;

                    Updates.actualizarTarea(em, out,
                            Integer.parseInt(partes[1]),
                            info, estado, fIni, fFin, nuevoAsignado
                    );
                }
                case "ACTUALIZAR_NOMINA" -> {
                    BigDecimal importe = partes.length > 2 && !partes[2].isEmpty() ? new BigDecimal(partes[2]) : null;
                    String concepto = partes.length > 3 && !partes[3].isEmpty() ? partes[3] : null;
                    String tipo = partes.length > 4 && !partes[4].isEmpty() ? partes[4] : null;

                    Updates.actualizarNomina(em, out,
                            Integer.parseInt(partes[1]),
                            importe, concepto, tipo
                    );
                }
                case "ACTUALIZAR_REPORTE" -> {
                    String info = partes.length > 2 && !partes[2].isEmpty() ? partes[2] : null;
                    String estado = partes.length > 3 && !partes[3].isEmpty() ? partes[3] : null;
                    String fIni = partes.length > 4 && !partes[4].isEmpty() ? partes[4] : null;
                    String fFin = partes.length > 5 && !partes[5].isEmpty() ? partes[5] : null;

                    Updates.actualizarReporteConSync(em, out,
                            Integer.parseInt(partes[1]),
                            info, estado, fIni, fFin
                    );
                }
            };
        } catch (Exception e) {

        } finally {
            em.close();
        }
    }

}