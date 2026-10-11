package com.bukimind.user.specification;

import com.bukimind.user.entity.User;
import com.bukimind.user.form.UserFilterForm;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    // build dynamic user filters for admin search
    public static Specification<User> buildWhere(UserFilterForm form) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // search by username (LIKE)
            if(form.getUsernameSearch() != null && !form.getUsernameSearch().isEmpty()){
                String value = "%" + form.getUsernameSearch().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(root.get("username"), value));
            }

            // search by email (LIKE)
            if(form.getEmailSearch() != null && !form.getEmailSearch().isEmpty()){
                String value = "%" + form.getEmailSearch().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(root.get("email"), value));
            }

            // search by status
            if(form.getStatusSearch() != null && !form.getStatusSearch().isEmpty()){
                String value = "%" + form.getStatusSearch().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(root.get("status"), value));
            }

            // id range filter
            if (form.getMinId() != null){
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("id"), form.getMinId()));
            }

            if (form.getMaxId() != null){
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("id"), form.getMaxId()));
            }

            // combine all search with AND
            return criteriaBuilder.and(predicates.toArray((new jakarta.persistence.criteria.Predicate[0])));
        };
    }
}
