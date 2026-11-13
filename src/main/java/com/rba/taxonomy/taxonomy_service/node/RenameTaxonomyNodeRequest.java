package com.rba.taxonomy.taxonomy_service.node;

import jakarta.validation.constraints.NotBlank;

public record RenameTaxonomyNodeRequest(@NotBlank String name) {
}
