package entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

@Entity
@Table(name = "reporte")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Reporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha_inicio", nullable = false)
    private Date fechaInicio;

    @Column(name = "fecha_fin")
    private Date fechaFin;

    @Column(columnDefinition = "TEXT")
    private String informacion;

    @Column(length = 20)
    private String estado = "en_curso"; // en_curso, finalizada, irrealizable, transferir

    @Column(name = "id_usuario_reporte", nullable = false)
    private Integer idUsuarioReporte;

    @Column(name = "id_tarea", nullable = false)
    private Integer idTarea;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_reporte", insertable = false, updatable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarea", insertable = false, updatable = false)
    private Tarea tarea;
}