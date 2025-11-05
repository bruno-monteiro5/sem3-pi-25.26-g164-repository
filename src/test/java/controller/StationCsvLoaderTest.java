package controller;

import domain.Station;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationCsvLoaderTest {

    private static String L2(String a, String b) { return a + "\n" + b; }
    private static String L3(String a, String b, String c) { return a + "\n" + b + "\n" + c; }
    private static String L4(String a, String b, String c, String d) { return a + "\n" + b + "\n" + c + "\n" + d; }
    private static String L5(String a, String b, String c, String d, String e) { return a + "\n" + b + "\n" + c + "\n" + d + "\n" + e; }
    private static String L6(String a, String b, String c, String d, String e, String f) { return a + "\n" + b + "\n" + c + "\n" + d + "\n" + e + "\n" + f; }
    private static String L8(String a, String b, String c, String d, String e, String f, String g, String h) {
        return a + "\n" + b + "\n" + c + "\n" + d + "\n" + e + "\n" + f + "\n" + g + "\n" + h;
    }

    @Test
    void emptyInput_returnsEmptyList() throws Exception {
        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(new StringReader(""));
        assertTrue(list.isEmpty());
    }

    @Test
    void parsesTwoValidRows_andExtractsLisbonGroup() throws Exception {
        String header = "country,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport";
        String r1 = "PT,\"('Europe/Lisbon',)\",Lisbon Oriente,38.767,-9.099,1,1,0";
        String r2 = "ES,\"('Europe/Madrid',)\",Madrid,40.416,-3.703,1,1,0";
        String csv = L3(header, r1, r2);

        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(new StringReader(csv));

        assertEquals(2, list.size());
        Station s0 = list.get(0);
        assertEquals("PT", s0.getCountry());
        assertEquals("Europe/Lisbon", s0.getTimeZoneGroup());
        assertEquals("Lisbon Oriente", s0.getName());
    }

    @Test
    void ignoresInvalidLine_missingLatitude_and_coversGetOutOfBounds() throws Exception {
        String header = "country,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport";
        String valid = "PT,\"('Europe/Lisbon',)\",Lisbon Oriente,38.767,-9.099,1,1,0";
        String shortRow = "XX,BAD,NoLat";
        String csv = L3(header, valid, shortRow);

        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(new StringReader(csv));

        assertEquals(1, list.size());
        assertEquals("Lisbon Oriente", list.get(0).getName());
    }

    @Test
    void quotedCommas_andEscapedQuotes_inStationName() throws Exception {
        String header = "country,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport";
        String row = "FR,\"('Europe/Paris',)\",\"Paris, Gare \"\"Nord\"\"\",48.88,2.35,1,1,0";
        String csv = L2(header, row);

        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(new StringReader(csv));

        assertEquals(1, list.size());
        Station s = list.get(0);
        assertEquals("FR", s.getCountry());
        assertEquals("Europe/Paris", s.getTimeZoneGroup());
        assertEquals("Paris, Gare \"Nord\"", s.getName());
        assertEquals(48.88, s.getLocation().getLatitude(), 1e-9);
        assertEquals(2.35, s.getLocation().getLongitude(), 1e-9);
        assertTrue(s.isCity());
        assertTrue(s.isMainStation());
        assertFalse(s.isAirport());
    }

    @Test
    void timeZoneGroup_normalizesTupleParenAndBare() throws Exception {
        String header = "country,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport";
        String t1 = "DE,\"('Europe/Berlin')\",Berlin Hbf,52.525,13.369,1,1,0";
        String t2 = "IT,'Europe/Rome',Roma Termini,41.902,12.496,1,1,0";
        String t3 = "PT,WET/GMT,Guarda,40.538,-7.267,0,0,0";
        String csv = L4(header, t1, t2, t3);

        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(new StringReader(csv));

        assertEquals("Europe/Berlin", list.get(0).getTimeZoneGroup());
        assertEquals("Europe/Rome",   list.get(1).getTimeZoneGroup());
        assertEquals("WET/GMT",       list.get(2).getTimeZoneGroup());
    }

    @Test
    void booleans_accept1_true_yes_and_emptyAsFalse() throws Exception {
        String header = "country,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport";
        String a = "PT,\"('Europe/Lisbon',)\",Oriente,38.767,-9.099,1,true,yes";
        String b = "PT,\"('Europe/Lisbon',)\",Santa Apolonia,38.713,-9.122,,,";
        String csv = L3(header, a, b);

        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(new StringReader(csv));

        assertEquals(2, list.size());
        assertTrue(list.get(0).isCity());
        assertTrue(list.get(0).isMainStation());
        assertTrue(list.get(0).isAirport());

        assertFalse(list.get(1).isCity());
        assertFalse(list.get(1).isMainStation());
        assertFalse(list.get(1).isAirport());
    }

    @Test
    void emptyAndTrailingEmptyLines_doNotBreakParsing() throws Exception {
        String header = "country,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport";
        String r1 = "PT,\"('Europe/Lisbon',)\",Cascais,38.697,-9.421,1,0,0";
        String r2 = "";
        String r3 = "PT,\"('Europe/Lisbon',)\",Estoril,38.705,-9.397,1,0,0";
        String r4 = "";
        String csv = L5(header, r1, r2, r3, r4);

        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(new StringReader(csv));

        assertEquals(2, list.size());
        assertEquals("Cascais", list.get(0).getName());
        assertEquals("Estoril", list.get(1).getName());
    }

    @Test
    void headerWithBOM_isAccepted() throws Exception {
        String header = "\uFEFFcountry,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport";
        String row = "PT,\"('Europe/Lisbon',)\",Lisboa Rossio,38.714,-9.140,1,1,0";
        String csv = L2(header, row);

        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(new StringReader(csv));

        assertEquals(1, list.size());
        assertEquals("Lisboa Rossio", list.get(0).getName());
    }

    @Test
    void missingRequiredHeader_throws() {
        String badHeader = "country,time_zone_group,latitude,longitude,is_city,is_main_station,is_airport";
        String row = "PT,\"('Europe/Lisbon',)\",38.7,-9.1,1,1,0";
        String csv = L2(badHeader, row);

        StationCsvLoader loader = new StationCsvLoader();
        assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader(csv)));
    }

    @Test
    void invalidNumericRow_isIgnored_leavingOnlyValid() throws Exception {
        String header = "country,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport";
        String ok  = "PT,\"('Europe/Lisbon',)\",Valid,38.7,-9.1,1,1,0";
        String bad = "PT,\"('Europe/Lisbon',)\",Bad,38.7,abc,1,1,0";
        String csv = L3(header, ok, bad);

        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(new StringReader(csv));

        assertEquals(1, list.size());
        assertEquals("Valid", list.get(0).getName());
    }

    @Test
    void loadFromPath_overloadCovered_withoutNio() throws Exception {
        File tmp = File.createTempFile("stations-", ".csv");
        try {
            FileWriter fw = new FileWriter(tmp);
            fw.write(L2(
                    "country,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                    "ES,\"('Europe/Madrid',)\",Atocha,40.406,-3.690,1,1,0"
            ));
            fw.flush();
            fw.close();

            StationCsvLoader loader = new StationCsvLoader();
            List<Station> list = loader.load(tmp.getAbsolutePath());

            assertEquals(1, list.size());
            assertEquals("Atocha", list.get(0).getName());
            assertEquals("Europe/Madrid", list.get(0).getTimeZoneGroup());
        } finally {
            if (tmp.exists()) { tmp.delete(); }
        }
    }
}
