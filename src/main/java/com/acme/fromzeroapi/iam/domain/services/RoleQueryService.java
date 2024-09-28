package com.acme.fromzeroapi.iam.domain.services;

import com.acme.fromzeroapi.iam.domain.model.entities.Role;
import com.acme.fromzeroapi.iam.domain.model.queries.GetAllRolesQuery;
import com.acme.fromzeroapi.iam.domain.model.queries.GetRoleByNameQuery;

import java.util.List;
import java.util.Optional;

public interface RoleQueryService {
    List<Role> handle(GetAllRolesQuery query);
    Optional<Role> handle(GetRoleByNameQuery query);
}


