package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StationTest {

    @Test
    void constructor_normalizesNameCountryAndTz() {
        Station s = new Station("  Lisbon   Oriente  ", " pt ",
                new Location(38.767, -9.099), "('Europe/Lisbon',)", true, true, false);

        assertEquals("Lisbon Oriente", s.getName());
        assertEquals("PT", s.getCountry());
        assertEquals("Europe/Lisbon", s.getTimeZoneGroup());
        assertEquals(38.767, s.getLocation().getLatitude(), 1e-9);
        assertEquals(-9.099, s.getLocation().getLongitude(), 1e-9);
        assertTrue(s.isCity());
        assertTrue(s.isMainStation());
        assertFalse(s.isAirport());
    }

    @Test
    void location_mustBeUsedForLatLon() {
        Station s = new Station("X", "PT", new Location(10.5, 20.5), "Europe/Lisbon", false, false, false);
        assertEquals(10.5, s.getLocation().getLatitude(), 1e-9);
        assertEquals(20.5, s.getLocation().getLongitude(), 1e-9);
    }
}
