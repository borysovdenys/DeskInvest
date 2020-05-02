package com.borysov.dev.models;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

/*    @ManyToMany(cascade=CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name="user_role",
            joinColumns={@JoinColumn(name="user_id", referencedColumnName="uuid")},
            inverseJoinColumns={@JoinColumn(name="role_id", referencedColumnName="uuid")})
    private List<Role> roles;*/

    @NotNull
    @Email
    private String email;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    private String picture;

    private String locale;

    private Boolean authorizedWithGoogle = false;

/*    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Item> items;*/

}
