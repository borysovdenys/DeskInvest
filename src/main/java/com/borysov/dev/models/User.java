package com.borysov.dev.models;

import com.borysov.dev.models.base.IndexEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Entity
@Table(name = "users")
@EqualsAndHashCode(exclude = "roles")
@ToString(exclude = "roles")
public class User extends IndexEntity {

    @ManyToMany(cascade=CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name="user_role",
            joinColumns={@JoinColumn(name="user_id", referencedColumnName="uuid")},
            inverseJoinColumns={@JoinColumn(name="role_id", referencedColumnName="uuid")})
    private List<Role> roles;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Item> items;

}
