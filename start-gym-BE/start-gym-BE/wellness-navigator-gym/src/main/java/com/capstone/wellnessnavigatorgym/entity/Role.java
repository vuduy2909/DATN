package com.capstone.wellnessnavigatorgym.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Setter
@Getter
@RequiredArgsConstructor
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;
    
    @Column(name = "role_name")
    private String roleName;
    @JsonBackReference
    @ManyToMany(mappedBy = "roles")
    private Set<Account> accounts = new LinkedHashSet<>();

    public Role(Integer roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }
}
