
package domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationIndexTest {

    private Station st(String name, String country, double lat, double lon, String tz) {
        return new Station(name, country, new Location(lat, lon), tz, false, false, false);
    }


    @Test
    void addAll_andCounts_areExact() {
        StationIndex idx = new StationIndex();
        idx.addAll(Arrays.asList(
                st("Lisbon Oriente", "PT", 38.767, -9.099, "Europe/Lisbon"),
                st("Porto Campanha", "PT", 41.149, -8.610, "Europe/Lisbon"),
                st("Madrid Atocha", "ES", 40.416, -3.703, "Europe/Madrid")
        ));
        assertEquals(3, idx.sizeLatKeys());
        assertEquals(3, idx.sizeLonKeys());
        assertEquals(2, idx.sizeTzGroups());
    }

    @Test
    void addStation_reusesExistingBuckets() {
        StationIndex idx = new StationIndex();
        // mesmas chaves lat/lon → mesmo bucket
        idx.addStation(st("BBB", "PT", 40.0, -8.0, "Europe/Lisbon"));
        idx.addStation(st("AAA", "ES", 40.0, -8.0, "Europe/Madrid"));

        assertEquals(1, idx.sizeLatKeys());
        assertEquals(1, idx.sizeLonKeys());
        assertEquals(2, idx.sizeTzGroups());

        List<Station> latExact = idx.queryLatitudeWindow(40.0, 40.0);
        assertEquals(2, latExact.size());

        List<Station> lonExact = idx.queryLongitudeWindow(-8.0, -8.0);
        assertEquals(2, lonExact.size());
    }


    @Test
    void latitudeWindow_returnsSortedByLat_thenName_thenCountry() {
        StationIndex idx = new StationIndex();
        idx.addAll(Arrays.asList(
                st("BBB", "PT", 40.000, -8.0, "Europe/Lisbon"),
                st("AAA", "ES", 40.000, -7.0, "Europe/Madrid"),
                st("CCC", "FR", 41.000,  2.0, "Europe/Paris"),
                st("DDD", "PT", 38.000, -9.0, "Europe/Lisbon")
        ));
        List<Station> res = idx.queryLatitudeWindow(40.0, 41.0);
        assertEquals(3, res.size());
        // lat=40.0: ordena por name -> country
        assertEquals("AAA", res.get(0).getName());
        assertEquals("BBB", res.get(1).getName());
        // depois lat=41.0
        assertEquals(41.000, res.get(2).getLocation().getLatitude(), 1e-9);
    }

    @Test
    void longitudeWindow_returnsSortedByLon_thenName_thenCountry() {
        StationIndex idx = new StationIndex();
        idx.addAll(Arrays.asList(
                st("BBB", "PT", 39.0, -8.000, "Europe/Lisbon"),
                st("AAA", "ES", 38.0, -8.000, "Europe/Madrid"),
                st("CCC", "FR", 41.0,  2.000, "Europe/Paris"),
                st("DDD", "PT", 37.0, -9.000, "Europe/Lisbon")
        ));
        List<Station> res = idx.queryLongitudeWindow(-8.0, 2.0);
        assertEquals(3, res.size());
        // lon=-8.0: ordena por name -> country
        assertEquals("AAA", res.get(0).getName());
        assertEquals("BBB", res.get(1).getName());
        // depois lon=2.0
        assertEquals("CCC", res.get(2).getName());
    }

    @Test
    void latitudeWindow_empty_whenOutsideRange() {
        StationIndex idx = new StationIndex();
        idx.addAll(Arrays.asList(
                st("X", "PT", 10.0, 0.0, "TZ1"),
                st("Y", "PT", 20.0, 0.0, "TZ1")
        ));
        List<Station> res = idx.queryLatitudeWindow(30.0, 40.0);
        assertTrue(res.isEmpty());
    }

    @Test
    void longitudeWindow_empty_whenOutsideRange() {
        StationIndex idx = new StationIndex();
        idx.addAll(Arrays.asList(
                st("X", "PT", 0.0, -20.0, "TZ1"),
                st("Y", "PT", 0.0, -10.0, "TZ1")
        ));
        List<Station> res = idx.queryLongitudeWindow(-5.0, -1.0);
        assertTrue(res.isEmpty());
    }


    @Test
    void tzGroup_returnsCountryThenName_order_andUsesLatLonTies() {
        StationIndex idx = new StationIndex();
        idx.addAll(Arrays.asList(
                st("Zeta",  "PT", 38.7, -9.1, "Europe/Lisbon"),
                st("Alpha", "ES", 40.4, -3.7, "Europe/Lisbon"),
                st("Beta",  "ES", 41.4, -3.6, "Europe/Lisbon"),
                // empates por country e name, desempate por latitude:
                st("Tie",   "PT", 10.0,  1.0, "Europe/Lisbon"),
                st("Tie",   "PT", 11.0,  1.0, "Europe/Lisbon"),
                // empates por country, name, latitude; desempate por longitude:
                st("Tie2",  "PT", 12.0,  1.0, "Europe/Lisbon"),
                st("Tie2",  "PT", 12.0,  2.0, "Europe/Lisbon")
        ));
        List<Station> res = idx.queryByTzGroup("Europe/Lisbon");
        assertEquals(7, res.size());

        // country ASC: ES (Alpha, Beta), depois PT (Tie*, Zeta)
        assertEquals("ES", res.get(0).getCountry());
        assertEquals("Alpha", res.get(0).getName());
        assertEquals("ES", res.get(1).getCountry());
        assertEquals("PT", res.get(2).getCountry());

        int tieIdx1 = -1, tieIdx2 = -1;
        int tie2Idx1 = -1, tie2Idx2 = -1;
        for (int i = 0; i < res.size(); i++) {
            Station s = res.get(i);
            if ("Tie".equals(s.getName())) {
                if (tieIdx1 == -1) tieIdx1 = i; else tieIdx2 = i;
            }
            if ("Tie2".equals(s.getName())) {
                if (tie2Idx1 == -1) tie2Idx1 = i; else tie2Idx2 = i;
            }
        }
        assertTrue(tieIdx1 != -1 && tieIdx2 != -1);
        assertTrue(tie2Idx1 != -1 && tie2Idx2 != -1);

        // "Tie" com lat=10.0 vem antes de "Tie" com lat=11.0
        assertTrue(res.get(tieIdx1).getLocation().getLatitude() < res.get(tieIdx2).getLocation().getLatitude());
        // "Tie2" têm a mesma lat; desempata por lon: 1.0 antes de 2.0
        assertTrue(res.get(tie2Idx1).getLocation().getLongitude() < res.get(tie2Idx2).getLocation().getLongitude());
    }

    @Test
    void tzGroup_unknown_returnsEmpty_andListIsACopy() {
        StationIndex idx = new StationIndex();
        idx.addAll(Arrays.asList(st("Only", "PT", 0.0, 0.0, "Known")));
        List<Station> none = idx.queryByTzGroup("Unknown");
        assertNotNull(none);
        assertTrue(none.isEmpty());

        List<Station> copy = idx.queryByTzGroup("Known");
        int before = copy.size();
        copy.clear();
        List<Station> again = idx.queryByTzGroup("Known");
        assertEquals(before, again.size());
    }

    @Test
    void tzWindow_unionOfGroups() {
        StationIndex idx = new StationIndex();
        idx.addAll(Arrays.asList(
                st("A", "PT", 0.0, 0.0, "G1"),
                st("B", "PT", 1.0, 1.0, "G2"),
                st("C", "PT", 2.0, 2.0, "G3")
        ));
        List<Station> res = idx.queryByTzWindow(Arrays.asList("G1", "G3"));
        assertEquals(2, res.size());

        boolean hasA = false, hasC = false;
        for (int i = 0; i < res.size(); i++) {
            String n = res.get(i).getName();
            if ("A".equals(n)) hasA = true;
            if ("C".equals(n)) hasC = true;
        }
        assertTrue(hasA);
        assertTrue(hasC);
    }

    @Test
    void tzAndCountry_filtersWithinGroup_andKeepsNameOrder() {
        StationIndex idx = new StationIndex();
        idx.addAll(Arrays.asList(
                st("Zeta",  "PT", 38.7, -9.1, "G"),
                st("Alpha", "ES", 40.4, -3.7, "G"),
                st("Beta",  "ES", 41.4, -3.6, "G"),
                st("Abby",  "ES", 41.5, -3.5, "G")
        ));
        List<Station> es = idx.queryByTzAndCountry("G", "ES");
        assertEquals(3, es.size());
        // Ordem por nome dentro do país ES: Abby, Alpha, Beta
        assertEquals("Abby",  es.get(0).getName());
        assertEquals("Alpha", es.get(1).getName());
        assertEquals("Beta",  es.get(2).getName());
    }


    @Test
    void queriesOnEmptyIndex_returnEmptyAndNoExceptions() {
        StationIndex idx = new StationIndex();
        assertTrue(idx.queryLatitudeWindow(0, 1).isEmpty());
        assertTrue(idx.queryLongitudeWindow(0, 1).isEmpty());
        assertTrue(idx.queryByTzGroup("X").isEmpty());
        assertTrue(idx.queryByTzWindow(Collections.singletonList("X")).isEmpty());
        assertTrue(idx.queryByTzAndCountry("X", "PT").isEmpty());
        assertEquals(0, idx.sizeLatKeys());
        assertEquals(0, idx.sizeLonKeys());
        assertEquals(0, idx.sizeTzGroups());
    }
}
