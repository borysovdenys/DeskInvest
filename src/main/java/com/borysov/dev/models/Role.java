package com.borysov.dev.models;

import com.borysov.dev.models.base.IndexEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(exclude = "users")
@ToString(exclude = "users")
public class Role extends IndexEntity {

    @NotNull
    private String name;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    private List<User> users;
}
