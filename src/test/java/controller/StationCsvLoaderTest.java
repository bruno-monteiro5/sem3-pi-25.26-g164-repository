package controller;

import domain.Station;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationCsvLoaderTest {

    @Test
    void load_fromReader_parsesBasicRows() throws Exception {
        String csv = String.join("\n",
                "country,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,\"('Europe/Lisbon',)\",Lisbon Oriente,38.767,-9.099,1,1,0",
                "ES,\"('Europe/Madrid',)\",Madrid,40.416,-3.703,1,1,0"
        );
        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(new StringReader(csv));

        assertEquals(2, list.size());
        Station s0 = list.get(0);
        assertEquals("PT", s0.getCountry());
        assertEquals("Europe/Lisbon", s0.getTimeZoneGroup());
        assertEquals("Lisbon Oriente", s0.getName());
    }

    @Test
    void load_ignoresInvalidLines() throws Exception {
        String csv = String.join("\n",
                "country,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,\"('Europe/Lisbon',)\",Lisbon Oriente,38.767,-9.099,1,1,0",
                "XX,BAD,NoLat,, -9.1,0,0,0" // latitude vazia → deve ser ignorada pelo try/catch do loader
        );
        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(new StringReader(csv));

        assertEquals(1, list.size());
        assertEquals("Lisbon Oriente", list.get(0).getName());
    }
}
