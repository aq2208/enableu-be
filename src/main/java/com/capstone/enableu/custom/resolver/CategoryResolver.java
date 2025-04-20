package com.capstone.enableu.custom.resolver;

import com.capstone.enableu.common.resolver.BaseResolver;
import com.capstone.enableu.custom.dto.CategoryFilter;
import com.capstone.enableu.custom.enums.Role;
import com.capstone.enableu.custom.request.CategoryRequest;
import com.capstone.enableu.custom.request.OrderRequest;
import com.capstone.enableu.custom.request.SearchAllRequest;
import com.capstone.enableu.custom.response.CategoryResponse;
import com.capstone.enableu.custom.response.PaginationResponse;
import com.capstone.enableu.custom.response.SearchAllResponse;
import com.capstone.enableu.custom.response.VoiceSearchResponse;
import com.capstone.enableu.custom.service.CategoryService;
import com.capstone.enableu.custom.service.StorageService;
import com.capstone.enableu.custom.service.VoiceSearchService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@AllArgsConstructor
public class CategoryResolver extends BaseResolver<CategoryService> {

    private final VoiceSearchService voiceSearchService;

    @MutationMapping(name = "createCategory")
    @Secured(Role.TRAINER_CONSTANT)
    public CategoryResponse createCategory(@Argument(name = "input") CategoryRequest categoryRequest) {
        return service.createCategory(categoryRequest);
    }

    @MutationMapping(name = "softDeleteCategory")
    @Secured(Role.TRAINER_CONSTANT)
    public String softDeleteCategory(@Argument Integer id) {
        return service.softDeleteCategory(id);
    }

    @MutationMapping(name = "updateCategory")
    @Secured(Role.TRAINER_CONSTANT)
    public CategoryResponse updateCategory(@Argument Integer id, @Valid @Argument(name = "input") CategoryRequest categoryRequest) {
        return service.updateCategory(id, categoryRequest);
    }

    @QueryMapping(name = "getCategoryDetail")
    @Secured(Role.TRAINEE_CONSTANT)
    public CategoryResponse getCategoryDetail(@Argument Integer id) {
        return service.getCategoryDetail(id);
    }

    @QueryMapping(name = "getCategoryList")
    @Secured(Role.TRAINEE_CONSTANT)
    public PaginationResponse<CategoryResponse> getCategoryList(@Argument CategoryFilter filter, @Argument String keyword, @Argument Integer pageNumber, @Argument Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return PaginationResponse.of(service.getCategoryList(filter, keyword), pageable);
    }

    @QueryMapping(name = "getCategoryListByUserId")
    @Secured(Role.TRAINEE_CONSTANT)
    public PaginationResponse<CategoryResponse> getCategoryListByUserId(@Argument Integer userId, @Argument Integer pageNumber, @Argument Integer pageSize) {
        return PaginationResponse.of(service.getCategoryListByUserId((long)userId, pageNumber, pageSize));
    }

    @QueryMapping(name = "getListSuggestions")
    @Secured(Role.TRAINEE_CONSTANT)
    public PaginationResponse<CategoryResponse> getListSuggestions(@Argument Integer userId, @Argument Integer pageNumber, @Argument Integer pageSize) {
        return PaginationResponse.of(service.getListSuggestions((long)userId, pageNumber, pageSize));
    }

    @QueryMapping(name="searchAll")
    @Secured(Role.TRAINEE_CONSTANT)
    public PaginationResponse<SearchAllResponse> searchAll(@Argument(name = "input") SearchAllRequest searchAllRequest) {
        Pageable pageable = PageRequest.of(searchAllRequest.getPageNumber(), searchAllRequest.getPageSize());
        return PaginationResponse.of(service.searchAll(searchAllRequest.getSearch()), pageable);
    }

    @MutationMapping(name = "updateTaskProcess")
    @Secured(Role.TRAINEE_CONSTANT)
    public String updateTaskProcess(@Argument Integer taskId, @Argument Integer categoryId) {
        return service.updateTaskProcess(taskId, categoryId);
    }

    @QueryMapping(name = "getCategoryListByCreatedUser")
    @Secured(Role.TRAINER_CONSTANT)
    public PaginationResponse<CategoryResponse> getCategoryListByCreatedUser(@Argument Integer createdUserId, @Argument Integer pageNumber, @Argument Integer pageSize) {
        return PaginationResponse.of(service.getCategoriesByCreatedUser((long)createdUserId, pageNumber, pageSize));
    }

    @MutationMapping(name = "updateCategoryStatus")
    @Secured(Role.ADMIN_CONSTANT)
    public CategoryResponse updateCategoryStatus(@Argument Integer id, @Argument String status) {
        return service.changeCategoryStatus(Long.valueOf(id), status);
    }

    @MutationMapping(name="updateOrderInCategory")
    @Secured(Role.ADMIN_CONSTANT)
    public CategoryResponse updateOrderInCategory(@Argument Integer categoryId, @Argument List<OrderRequest> orderRequests) {
        return service.changeOrderId((long)categoryId, orderRequests);
    }

    @MutationMapping
//    @Secured(Role.TRAINEE_CONSTANT)
    public VoiceSearchResponse getTranscript(@Argument MultipartFile file) {
        return voiceSearchService.getTranscript(file);
    }

}
