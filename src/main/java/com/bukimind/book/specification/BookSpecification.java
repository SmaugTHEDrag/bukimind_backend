package com.bukimind.book.specification;

import com.bukimind.book.entity.Book;
import com.bukimind.book.form.BookFilterForm;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    // build dynamic query based on filter conditions
    public static Specification<Book> buildWhere(BookFilterForm form) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // global search: title OR author
            if(form.getSearch() != null && !form.getSearch().isEmpty()){
                String value = "%" + form.getSearch().toLowerCase() + "%";

                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), value);
                Predicate authorPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("author")), value);

                predicates.add(criteriaBuilder.or(titlePredicate, authorPredicate));
            }

            else {
                // separate title / author filters
                if(form.getTitleSearch() != null && !form.getTitleSearch().isEmpty()){
                    String value = "%" + form.getTitleSearch().toLowerCase() + "%";
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")), value));
                }

                if(form.getAuthorSearch() != null && !form.getAuthorSearch().isEmpty()){
                    String value = "%" + form.getAuthorSearch().toLowerCase() + "%";
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("author")), value));
                }
            }
            // category filter
            if(form.getCategorySearch() != null && !form.getCategorySearch().isEmpty()){
                String value = "%" + form.getCategorySearch().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(root.get("category"), value));
            }

            // id range filter
            if (form.getMinId() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("id"), form.getMinId()));
            }

            if (form.getMaxId() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("id"), form.getMaxId()));
            }

            // combine all conditions with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

