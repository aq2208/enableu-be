package com.capstone.enableu.custom.resolver;

import com.capstone.enableu.common.resolver.BaseResolver;
import com.capstone.enableu.custom.enums.Role;
import com.capstone.enableu.custom.request.ModuleRequest;
import com.capstone.enableu.custom.response.ModuleResponse;
import com.capstone.enableu.custom.response.PaginationResponse;
import com.capstone.enableu.custom.service.ModuleService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ModuleResolver extends BaseResolver<ModuleService> {

    @MutationMapping(name = "createModule")
    @Secured(Role.TRAINER_CONSTANT)
    public ModuleResponse createModule(@Argument Integer categoryId, @Argument(name = "input") ModuleRequest moduleRequest) {
        return service.createModule(categoryId, moduleRequest);
    }

    @MutationMapping(name = "softDeleteModule")
    @Secured(Role.TRAINER_CONSTANT)
    public String softDeleteModule(@Argument Integer id) {
        return service.softDeleteModule(id);
    }

    @MutationMapping(name = "updateModule")
    @Secured(Role.TRAINER_CONSTANT)
    public ModuleResponse updateModule(@Argument Integer id, @Argument(name = "input") ModuleRequest moduleRequest) {
        return service.updateModule(id, moduleRequest);
    }

    @QueryMapping(name = "getModuleDetail")
    @Secured(Role.TRAINEE_CONSTANT)
    public ModuleResponse getModuleDetail(@Argument Integer id) {
        return service.getModuleDetail(id);
    }

    @QueryMapping(name = "getModuleListOfCategory")
    @Secured(Role.TRAINEE_CONSTANT)
    public PaginationResponse<ModuleResponse> getModuleListOfCategory(@Argument Integer categoryId, @Argument Integer pageNumber, @Argument Integer pageSize) {
        return PaginationResponse.of(service.getModuleListOfCategory(categoryId, pageNumber, pageSize));
    }

}
