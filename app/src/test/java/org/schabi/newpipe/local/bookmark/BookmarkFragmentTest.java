package org.schabi.newpipe.local.bookmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BookmarkFragmentTest {

    @Test
    public void freshNameIsUnchanged() {
        final Set<String> used = new HashSet<>();
        assertEquals("Favorites", BookmarkFragment.uniquePlaylistName("Favorites", used));
        assertTrue(used.contains("Favorites"));
    }

    @Test
    public void existingNameGetsCounter() {
        final Set<String> used = new HashSet<>(Collections.singleton("Favorites"));
        assertEquals("Favorites (2)", BookmarkFragment.uniquePlaylistName("Favorites", used));
        assertTrue(used.contains("Favorites (2)"));
    }

    @Test
    public void skipsTakenCounters() {
        final Set<String> used = new HashSet<>(Arrays.asList("A", "A (2)", "A (3)"));
        assertEquals("A (4)", BookmarkFragment.uniquePlaylistName("A", used));
    }

    @Test
    public void sameNameTwiceWithinOneImport() {
        final Set<String> used = new HashSet<>();
        assertEquals("Mix", BookmarkFragment.uniquePlaylistName("Mix", used));
        assertEquals("Mix (2)", BookmarkFragment.uniquePlaylistName("Mix", used));
        assertEquals("Mix (3)", BookmarkFragment.uniquePlaylistName("Mix", used));
    }
}
