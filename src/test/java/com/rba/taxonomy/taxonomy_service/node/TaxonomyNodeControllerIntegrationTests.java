package com.rba.taxonomy.taxonomy_service.node;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class TaxonomyNodeControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createsAndReadsHierarchyThroughClosurePaths() throws Exception {
		long rootId = createNode("Animals", null);
		long childId = createNode("Mammals", rootId);
		long grandchildId = createNode("Primates", childId);

		mockMvc.perform(get("/api/nodes/{id}", childId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Mammals")))
				.andExpect(jsonPath("$.parentId", is((int) rootId)));

		mockMvc.perform(get("/api/nodes/{id}/ancestors", grandchildId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].name", contains("Animals", "Mammals")))
				.andExpect(jsonPath("$[*].depth", contains(2, 1)));

		mockMvc.perform(get("/api/nodes/{id}/descendants", rootId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].name", contains("Mammals", "Primates")))
				.andExpect(jsonPath("$[*].depth", contains(1, 2)));
	}

	@Test
	void renamesANode() throws Exception {
		long nodeId = createNode("Original", null);

		mockMvc.perform(patch("/api/nodes/{id}", nodeId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"Renamed"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Renamed")));
	}

	private long createNode(String name, Long parentId) throws Exception {
		String parentPart = parentId == null ? "null" : parentId.toString();
		String response = mockMvc.perform(post("/api/nodes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"%s","parentId":%s}
						""".formatted(name, parentPart)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		JsonNode body = objectMapper.readTree(response);
		return body.get("id").asLong();
	}

}
