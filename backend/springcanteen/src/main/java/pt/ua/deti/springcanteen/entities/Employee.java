package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @Email
    @NotNull
    private String email;

    // TODO password security
    @NotNull
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull
    private EmployeeRole role;

}
