package com.capstone.enableu.custom.response;

import com.capstone.enableu.custom.entity.CategoryEntity;
import com.capstone.enableu.custom.entity.TaskEntity;
import com.capstone.enableu.custom.enums.SearchType;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchAllResponse {
   private Long id;
   private String name;
   private String description;
   private SearchType typeResponse;
   private String thumbnail;
   private String status;
   private List<TaskSearchResponse> tasks;
   @Getter
   @Setter
   public static class TaskSearchResponse {
       private Long id;
       private String name;
       private String description;

       public static TaskSearchResponse fromTaskEntity(TaskEntity taskEntity) {
         TaskSearchResponse taskSearchResponse = new TaskSearchResponse();
         taskSearchResponse.setId(taskEntity.getId());
         taskSearchResponse.setName(taskEntity.getName());
         taskSearchResponse.setDescription(taskEntity.getDescription());
         return taskSearchResponse;
      }
   }

   public static SearchAllResponse fromCategoryEntity(CategoryEntity categoryEntity) {
      SearchAllResponse searchAllResponse = new SearchAllResponse();
      if (categoryEntity.isDeleted()) {
          return null;
      }
      searchAllResponse.setId(categoryEntity.getId());
      searchAllResponse.setName(categoryEntity.getName());
      searchAllResponse.setDescription(categoryEntity.getDescription());
      searchAllResponse.setTypeResponse(SearchType.CATEGORY);
      searchAllResponse.setThumbnail(categoryEntity.getThumbnail());
      searchAllResponse.setStatus(categoryEntity.getStatus());

      return searchAllResponse;
   }
    public static SearchAllResponse fromTaskEntity(TaskEntity taskEntity) {
        SearchAllResponse searchAllResponse = new SearchAllResponse();
        if (taskEntity.isDeleted()) {
            return null;
        }
        searchAllResponse.setId(taskEntity.getId());
        searchAllResponse.setName(taskEntity.getName());
        searchAllResponse.setDescription(taskEntity.getDescription());
        searchAllResponse.setTypeResponse(SearchType.TASK);
        searchAllResponse.setThumbnail(taskEntity.getThumbnail());
        searchAllResponse.setStatus(taskEntity.getStatus());

        return searchAllResponse;
    }
}
