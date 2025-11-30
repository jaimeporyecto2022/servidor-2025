package jjn;
import comunicaciondb.*;
import jakarta.persistence.*;

import java.io.*;
import java.net.*;

public class Main {
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
            };
        } catch (Exception e) {

        } finally {
            em.close();
        }
    }

}