package com.globalcluster.gateway;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Continent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GeoIpServiceTest {

    @Mock
    private DatabaseReader databaseReader;

    @Test
    void whenGetContinent_thenReturnContinentName() throws IOException, GeoIp2Exception {
        // Given
        String ip = "8.8.8.8";
        String expectedContinent = "North America";

        GeoIpService geoIpService = new GeoIpService(databaseReader);

        Continent continent = new Continent(
                Collections.singletonList("en"),
                "NA",
                6255149,
                Map.of("en", expectedContinent)
        );

        CountryResponse mockResponse = new CountryResponse(
                continent,
                null, // country
                null, // maxMind
                null, // registeredCountry
                null, // representedCountry
                null // traits
        );

        when(databaseReader.country(any(InetAddress.class))).thenReturn(mockResponse);

        // When
        String actualContinent = geoIpService.getContinent(ip);

        // Then
        assertEquals(expectedContinent, actualContinent);
    }

    @Test
    void whenGetContinentAndIpIsLocal_thenReturnNull() {
        // Given
        String ip = "127.0.0.1";
        GeoIpService geoIpService = new GeoIpService(databaseReader);

        // When
        String actualContinent = geoIpService.getContinent(ip);

        // Then
        assertNull(actualContinent);
    }
}
