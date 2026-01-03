package com.globalcluster.node;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Continent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class GeoIpService {

    private static final Logger logger = LoggerFactory.getLogger(GeoIpService.class);

    private final DatabaseReader dbReader;

    public GeoIpService(DatabaseReader dbReader) {
        this.dbReader = dbReader;
    }

    public String getContinent(String ipAddress) {
        if (dbReader == null) {
            logger.warn("GeoIP database not loaded. Cannot resolve IP: {}", ipAddress);
            return null;
        }
        try {
            InetAddress ip = InetAddress.getByName(ipAddress);
            if (ip.isSiteLocalAddress() || ip.isLoopbackAddress()) {
                logger.debug("IP address is a local/private address: {}. Skipping GeoIP lookup.", ipAddress);
                return null;
            }
            CountryResponse response = dbReader.country(ip);
            Continent continent = response.getContinent();
            if (continent != null && continent.getName() != null) {
                return continent.getName();
            }
        } catch (UnknownHostException e) {
            logger.error("Invalid IP address format: {}", ipAddress, e);
        } catch (GeoIp2Exception e) {
            // This is expected for private/local IPs
            logger.trace("GeoIP2 lookup failed for IP: {}. This is expected for private IPs.", ipAddress);
        } catch (IOException e) {
            logger.error("Error accessing GeoIP2 database for IP: {}", ipAddress, e);
        }
        return null;
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
