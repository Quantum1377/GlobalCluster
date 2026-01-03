package com.globalcluster.gateway;

import com.maxmind.geoip2.DatabaseReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Configuration
public class GeoIpConfig {

    private static final Logger logger = LoggerFactory.getLogger(GeoIpConfig.class);

    @Bean
    public DatabaseReader databaseReader(@Value("classpath:GeoLite2-Country.mmdb") Resource geolite2Database) throws IOException {
        File databaseFile = File.createTempFile("GeoLite2-Country", ".mmdb");
        databaseFile.deleteOnExit();

        try (InputStream inputStream = geolite2Database.getInputStream()) {
            Files.copy(inputStream, databaseFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("GeoLite2-Country.mmdb loaded successfully from temporary file: {}", databaseFile.getAbsolutePath());
            return new DatabaseReader.Builder(databaseFile).build();
        } catch (IOException e) {
            logger.error("Error loading GeoLite2-Country.mmdb. Please ensure the file is in 'src/main/resources/' and is a valid MaxMind database.", e);
            throw e;
        }
    }
}
