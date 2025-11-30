package entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departamento")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Departamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Constructor personalizado (útil para crear departamentos rápido)
    public Departamento(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Constructor solo con nombre (descripción opcional)
    public Departamento(String nombre) {
        this.nombre = nombre;
        this.descripcion = null;
    }
}