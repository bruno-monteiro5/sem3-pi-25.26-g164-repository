package domain;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Minimal JUnit 4 test suite achieving 100% coverage of {@link TZKey}.
 * Tests:
 * <ul>
 *     <li>Constructor and {@link TZKey#getStation()}</li>
 *     <li>{@link TZKey#compareTo(TZKey)} with different time zone groups</li>
 *     <li>{@link TZKey#compareTo(TZKey)} with same group and different countries</li>
 *     <li>{@link TZKey#compareTo(TZKey)} with same group, same country, different names</li>
 *     <li>{@link TZKey#compareTo(TZKey)} with all fields equal</li>
 * </ul>
 */
public class TZKeyTest {

    /**
     * Helper factory to create a Station with given name, country and time zone group.
     */
    private Station station(String name, String country, String group) {
        return new Station(
                name,
                country,
                new Location(0.0, 0.0),
                group,
                false,
                false,
                false
        );
    }

    /**
     * Ensures constructor stores the station and getStation() returns it.
     */
    @Test
    public void testConstructorAndGetter() {
        Station s = station("Alpha", "PT", "Europe/Lisbon");
        TZKey key = new TZKey(s);

        assertNotNull("TZKey instance should not be null", key);
        assertSame("getStation() must return the same Station instance", s, key.getStation());
    }

    /**
     * Different time zone groups must be ordered lexicographically by group.
     */
    @Test
    public void testCompareDifferentGroups() {
        TZKey america = new TZKey(station("A", "US", "America/New_York"));
        TZKey europe  = new TZKey(station("B", "PT", "Europe/Lisbon"));

        assertTrue("America/New_York < Europe/Lisbon", america.compareTo(europe) < 0);
        assertTrue("Europe/Lisbon > America/New_York", europe.compareTo(america) > 0);
    }

    /**
     * Same group but different countries: comparison must use country code.
     */
    @Test
    public void testCompareSameGroupDifferentCountries() {
        TZKey de = new TZKey(station("Berlin", "DE", "Europe/Berlin"));
        TZKey fr = new TZKey(station("Paris",  "FR", "Europe/Berlin"));

        assertTrue("DE < FR in same group", de.compareTo(fr) < 0);
        assertTrue("FR > DE in same group", fr.compareTo(de) > 0);
    }

    /**
     * Same group and same country but different names: comparison must use name.
     */
    @Test
    public void testCompareSameGroupSameCountryDifferentNames() {
        TZKey alpha = new TZKey(station("Alpha", "PT", "Europe/Lisbon"));
        TZKey beta  = new TZKey(station("Beta",  "PT", "Europe/Lisbon"));

        assertTrue("Alpha < Beta in same group and country", alpha.compareTo(beta) < 0);
        assertTrue("Beta > Alpha in same group and country", beta.compareTo(alpha) > 0);
    }

    /**
     * All fields (group, country, name) equal → compareTo must return 0.
     */
    @Test
    public void testCompareFullyEqual() {
        Station s1 = station("Same", "PT", "Europe/Lisbon");
        Station s2 = station("Same", "PT", "Europe/Lisbon");

        TZKey key1 = new TZKey(s1);
        TZKey key2 = new TZKey(s2);

        assertEquals("Keys with same group, country and name must be equal",
                0, key1.compareTo(key2));
    }
}
