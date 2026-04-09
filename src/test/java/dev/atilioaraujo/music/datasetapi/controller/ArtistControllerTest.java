package dev.atilioaraujo.music.datasetapi.controller;

import dev.atilioaraujo.music.datasetapi.dao.ArtistDao;
import dev.atilioaraujo.music.datasetapi.dto.ArtistView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ArtistControllerTest {

	@Mock
	private ArtistDao artistDao;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new ArtistController(artistDao))
				.setViewResolvers((viewName, locale) -> new View() {
					@Override
					public String getContentType() {
						return "text/html";
					}

					@Override
					public void render(Map<String, ?> model, jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
						// no-op: the test only needs controller/model wiring
					}
				})
				.build();
	}

	@Test
	void shouldLoadArtistPageWithDefaultPagination() throws Exception {
		List<ArtistView> artistViews = List.of(
				new ArtistView("Luna Velasquez", 42, "https://example.com/luna.png")
		);
		when(artistDao.findTopArtistViews(1, 8)).thenReturn(artistViews);
		when(artistDao.getTotalCount()).thenReturn(142);

		mockMvc.perform(get("/artist"))
				.andExpect(status().isOk())
				.andExpect(view().name("artist"))
				.andExpect(model().attribute("artistViews", artistViews))
				.andExpect(model().attribute("currentPage", 1))
				.andExpect(model().attribute("pageSize", 8))
				.andExpect(model().attribute("totalArtists", 142));

		verify(artistDao).findTopArtistViews(eq(1), eq(8));
		verify(artistDao).getTotalCount();
	}
}
