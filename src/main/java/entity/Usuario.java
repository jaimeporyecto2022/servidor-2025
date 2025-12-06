// Usuario.java
package entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String direccion;
    private String mail;
    private String password;
    private String rol;

    @Column(name = "fecha_alta")
    private java.sql.Date fechaAlta;

    @Column(name = "iddepartamento")
    private Integer idDepartamento;

    @Column(name = "idjefe")
    private Integer idJefe;
}