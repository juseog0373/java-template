package com.nexacode.template.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

// 동적 where builder util
public class QueryConditionBuilder<T> {
    private final List<BiFunction<Root<T>, CriteriaBuilder, Predicate>> conditions = new ArrayList<>();

    private QueryConditionBuilder() {
    }

    public static <T> QueryConditionBuilder<T> of() {
        return new QueryConditionBuilder<>();
    }

    public QueryConditionBuilder<T> add(BiFunction<Root<T>, CriteriaBuilder, Predicate> condition) {
        if (condition != null) {
            this.conditions.add(condition);
        }
        return this;
    }

    public Specification<T> build() {
        return (root, query, cb) -> {
            List<Predicate> predicates = conditions.stream()
                    .map(condition -> condition.apply(root, cb))
                    .filter(predicate -> predicate != null)
                    .toList();

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
