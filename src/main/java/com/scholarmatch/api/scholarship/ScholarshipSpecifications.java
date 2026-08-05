package com.scholarmatch.api.scholarship;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class ScholarshipSpecifications {

  private ScholarshipSpecifications() {
  }

  public static Specification<Scholarship> isActive() {
    return (root, query, cb) -> cb.isTrue(root.get("isActive"));
  }

  public static Specification<Scholarship> matchesQuery(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    String pattern = "%" + text.toLowerCase() + "%";
    return (root, query, cb) -> cb.or(
        cb.like(cb.lower(root.get("title")), pattern),
        cb.like(cb.lower(root.get("providerName")), pattern),
        cb.like(cb.lower(root.get("fieldOfStudy")), pattern),
        cb.like(cb.lower(root.get("nationality")), pattern));
  }

  public static Specification<Scholarship> fieldOfStudyEquals(String fieldOfStudy) {
    if (fieldOfStudy == null || fieldOfStudy.isBlank()) {
      return null;
    }
    return (root, query, cb) -> cb.equal(cb.lower(root.get("fieldOfStudy")), fieldOfStudy.toLowerCase());
  }

  public static Specification<Scholarship> minGpaAtMost(BigDecimal gpa) {
    if (gpa == null) {
      return null;
    }
    return (root, query, cb) -> {
      Predicate isNull = cb.isNull(root.get("minGpa"));
      Predicate atMost = cb.lessThanOrEqualTo(root.get("minGpa"), gpa);
      return cb.or(isNull, atMost);
    };
  }

  public static Specification<Scholarship> financialNeedEquals(Boolean financialNeed) {
    if (financialNeed == null) {
      return null;
    }
    return (root, query, cb) -> cb.equal(root.get("financialNeed"), financialNeed);
  }

  @SafeVarargs
  public static Specification<Scholarship> combine(Specification<Scholarship>... specs) {
    Specification<Scholarship> result = Specification.unrestricted();
    for (Specification<Scholarship> spec : specs) {
      if (spec != null) {
        result = result.and(spec);
      }
    }
    return result;
  }
}
