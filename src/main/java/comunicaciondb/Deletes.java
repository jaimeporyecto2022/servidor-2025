package comunicaciondb;

import entity.*;
import jakarta.persistence.*;
import java.io.PrintWriter;

public class Deletes {

    private static final String SEP = "@Tr&m";

    // ==================== ELIMINAR USUARIO ====================
    public static void eliminarUsuario(EntityManager em, PrintWriter out, int idUsuario) {
        em.getTransaction().begin();
        try {
            Usuario usuario = em.find(Usuario.class, idUsuario);
            if (usuario == null) {
                out.println("DELETE_USUARIO_ERROR" + SEP + "Usuario no existe");
            } else {
                em.remove(usuario); // CASCADE elimina tareas y reportes asociados
                em.getTransaction().commit();
                out.println("DELETE_USUARIO_OK" + SEP + idUsuario + SEP + usuario.getNombre() + " eliminado del imperio");
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            out.println("DELETE_USUARIO_ERROR" + SEP + "Error: " + e.getMessage());
        }
        out.println("FIN_COMANDO");
    }

    // ==================== ELIMINAR NÓMINA ====================
    public static void eliminarNomina(EntityManager em, PrintWriter out, int idNomina) {
        em.getTransaction().begin();
        try {
            Nomina nomina = em.find(Nomina.class, idNomina);
            if (nomina == null) {
                out.println("DELETE_NOMINA_ERROR" + SEP + "Nómina no existe");
            } else {
                em.remove(nomina);
                em.getTransaction().commit();
                out.println("DELETE_NOMINA_OK" + SEP + idNomina + SEP + nomina.getImporte() + " " + nomina.getTipo());
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            out.println("DELETE_NOMINA_ERROR" + SEP + "Error: " + e.getMessage());
        }
        out.println("FIN_COMANDO");
    }

    // ==================== ELIMINAR TAREA (y sus reportes) ====================
    public static void eliminarTarea(EntityManager em, PrintWriter out, int idTarea) {
        em.getTransaction().begin();
        try {
            Tarea tarea = em.find(Tarea.class, idTarea);
            if (tarea == null) {
                out.println("DELETE_TAREA_ERROR" + SEP + "Tarea no existe");
            } else {
                em.remove(tarea); // CASCADE elimina todos los reportes asociados
                em.getTransaction().commit();
                out.println("DELETE_TAREA_OK" + SEP + idTarea + SEP + "Tarea eliminada del sistema");
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            out.println("DELETE_TAREA_ERROR" + SEP + "Error: " + e.getMessage());
        }
        out.println("FIN_COMANDO");
    }

    // ==================== ELIMINAR REPORTE ====================
    public static void eliminarReporte(EntityManager em, PrintWriter out, int idReporte) {
        em.getTransaction().begin();
        try {
            Reporte reporte = em.find(Reporte.class, idReporte);
            if (reporte == null) {
                out.println("DELETE_REPORTE_ERROR" + SEP + "Reporte no existe");
            } else {
                em.remove(reporte);
                em.getTransaction().commit();
                out.println("DELETE_REPORTE_OK" + SEP + idReporte + SEP + "Reporte eliminado");
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            out.println("DELETE_REPORTE_ERROR" + SEP + "Error: " + e.getMessage());
        }
        out.println("FIN_COMANDO");
    }
}