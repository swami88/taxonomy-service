package com.rba.taxonomy.taxonomy_service.node;

import jakarta.validation.constraints.NotBlank;

public record CreateTaxonomyNodeRequest(@NotBlank String name, Long parentId) {
}
