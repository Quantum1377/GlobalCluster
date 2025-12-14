package com.globalcluster.gateway;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Continent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class GeoIpService {

    private static final Logger logger = LoggerFactory.getLogger(GeoIpService.class);

    private DatabaseReader dbReader;

    @Value("classpath:GeoLite2-Country.mmdb")
    private Resource geolite2Database;

    @PostConstruct
    public void init() {
        File databaseFile = null;
        try (var inputStream = geolite2Database.getInputStream()) {
            databaseFile = File.createTempFile("GeoLite2-Country", ".mmdb");
            java.nio.file.Files.copy(inputStream, databaseFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            dbReader = new DatabaseReader.Builder(databaseFile).build();
            logger.info("GeoLite2-Country.mmdb loaded successfully from temporary file.");
        } catch (IOException e) {
            logger.error("Error loading GeoLite2-Country.mmdb. Please ensure the file is in 'src/main/resources/' and is a valid MaxMind database.", e);
        } finally {
            if (databaseFile != null) {
                databaseFile.deleteOnExit(); // Garante que o arquivo temporário será excluído na saída
            }
        }
    }

    public String getContinent(String ipAddress) {
        if (dbReader == null) {
            logger.warn("GeoIP database not loaded. Cannot resolve IP: {}", ipAddress);
            return "UNKNOWN";
        }
        try {
            InetAddress ip = InetAddress.getByName(ipAddress);
            CountryResponse response = dbReader.country(ip);
            Continent continent = response.getContinent();
            if (continent != null && continent.getName() != null) {
                return continent.getName().toUpperCase();
            }
        } catch (UnknownHostException e) {
            logger.error("Invalid IP address format: {}", ipAddress, e);
        } catch (GeoIp2Exception e) {
            logger.warn("GeoIP2 lookup failed for IP: {}. This might be a private IP or a lookup error.", ipAddress, e);
        } catch (IOException e) {
            logger.error("Error accessing GeoIP2 database for IP: {}", ipAddress, e);
        }
        return "UNKNOWN";
    }

    @PreDestroy
    public void destroy() {
        if (dbReader != null) {
            try {
                dbReader.close();
                logger.info("GeoIP database closed.");
            } catch (IOException e) {
                logger.error("Error closing GeoIP database.", e);
            }
        }
    }
}
