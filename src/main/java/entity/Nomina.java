package entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "nomina")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Nomina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @Column(nullable = false)
    private Date fecha;

    @Column(nullable = false, length = 255)
    private String concepto;

    @Column(nullable = false, length = 20)
    private String tipo; // salario, hora_extra, plus, deduccion

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    // Relación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", insertable = false, updatable = false)
    private Usuario usuario;
}