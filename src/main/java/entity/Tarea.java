package entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarea")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Tarea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String informacion;

    @Column(name = "fecha_inicio")
    private Date fechaInicio;

    @Column(name = "fecha_fin")
    private Date fechaFin;

    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @Column(length = 20)
    private String estado = "pendiente";

    @Column(name = "id_usuario_creador", nullable = false)
    private Integer idUsuarioCreador;

    @Column(name = "id_usuario_asignado", nullable = false)
    private Integer idUsuarioAsignado;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creador", insertable = false, updatable = false)
    private Usuario creador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_asignado", insertable = false, updatable = false)
    private Usuario asignado;
}