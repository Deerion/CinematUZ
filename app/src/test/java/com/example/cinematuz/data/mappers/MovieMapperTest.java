package com.example.cinematuz.data.mappers;

import static org.junit.Assert.assertEquals;
import com.example.cinematuz.data.local.MovieEntity;
import com.example.cinematuz.data.models.MediaItem;
import org.junit.Test;

public class MovieMapperTest {

    @Test
    public void testMapEntityToModel() {
        MovieEntity entity = new MovieEntity(
                101,
                "Incepcja",
                "/poster.jpg",
                "Opis filmu",
                9.0,
                "movie",
                false,
                true
        );

        MediaItem item = new MediaItem();
        item.setId(entity.getId());
        item.setTitle(entity.getTitle());
        item.setVoteAverage(entity.getVoteAverage());

        assertEquals("ID powinno się zgadzać", 101, item.getId());
        assertEquals("Tytuł powinien się zgadzać", "Incepcja", item.getTitle());
        assertEquals("Ocena powinna się zgadzać", 9.0, item.getVoteAverage(), 0.001);
    }
}