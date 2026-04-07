package dev.atilioaraujo.music.datasetapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtistControllerTest {

	private ArtistController controller;

	@BeforeEach
	void setUp() {
		controller = new ArtistController();
	}

	@Test
	void shouldLoadArtistPage() {
		assertEquals("artist", controller.artist());
	}
}

