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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
        try {
            File database = geolite2Database.getFile();
            dbReader = new DatabaseReader.Builder(database).build();
            logger.info("GeoLite2-Country.mmdb loaded successfully.");
        } catch (IOException e) {
            logger.error("Error loading GeoLite2-Country.mmdb. Please ensure the file is in 'src/main/resources/' and is a valid MaxMind database.", e);
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
