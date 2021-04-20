package com.hectortv9.copyFiles;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.nio.file.Paths;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FileCopier {

    private static final String PRIVATE_PROPS_LOCATION = "private.properties/private.properties";
    private static final Pattern REMOTE_PATH_PATTERN = Pattern.compile(
            "^\\Qremote.path.\\E\\d+$", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Logger LOGGER = LogManager.getLogger(FileCopier.class);
    private static final Properties PRIVATE_PROPERTIES = new Properties();

    //TODO: Review logging strategies
    //TODO: Verify logging, resource loading (property) works after application is packed
    public static void main(String[] args) {
        try {
            loadProperties();
            startPolling(args);
        } catch (Throwable e) {
            LOGGER.error("Top logging level. Exception reached main() method.", e);
        }
    }

    public static void startPolling(String[] args) {
        int seconds = Integer.parseInt(PRIVATE_PROPERTIES.getProperty("polling.time.seconds")) * 1000;
        Path localDir = Paths.get(PRIVATE_PROPERTIES.getProperty("local.path"));
        LOGGER.info("Local directory: {}", localDir);
        ArrayList<Path> remoteDirs = getRemoteDirs();
        while (true) {
            syncFiles(localDir, remoteDirs);
            try {
                Thread.sleep(seconds);
            } catch (InterruptedException e) {
                LOGGER.error("Error while waiting for next remote-to-local file sync.", e);
            }
        }
    }

    public static void loadProperties() {
        LOGGER.info("Properties File Location: {}", PRIVATE_PROPS_LOCATION);
        try {
            PRIVATE_PROPERTIES
                    .load(Thread.currentThread().getContextClassLoader().getResourceAsStream(PRIVATE_PROPS_LOCATION));
        } catch (Throwable e) {
            LOGGER.error("Error while attempting to load the Properties file.", e);
            System.exit(1);
        }
        LOGGER.info("Properties File Content:{}{}", System.lineSeparator(),
                PRIVATE_PROPERTIES
                    .entrySet()
                    .stream()
                    .map(p -> String.format("%s=%s", p.getKey(), p.getValue()))
                    .collect(Collectors.joining(System.lineSeparator())));
    }

    public static ArrayList<Path> getRemoteDirs() {
        ArrayList<Path> remoteDirs = new ArrayList<Path>();
        for (Enumeration<Object> properties = PRIVATE_PROPERTIES.keys(); properties.hasMoreElements();) {
            String property = properties.nextElement().toString();
            Matcher resourceTypeMatcher = REMOTE_PATH_PATTERN.matcher(property);
            if ( resourceTypeMatcher.matches() ) {
                Path remotePath = Paths.get(PRIVATE_PROPERTIES.getProperty(property));
                LOGGER.info("Remote directory: {}", remotePath);
                remoteDirs.add(remotePath);
            } else {
                LOGGER.trace("Property key NOT identified as Remote Directory: {}", property);
            }
        }
        return remoteDirs;
    }

    public static void syncFiles(Path localDir, ArrayList<Path> remoteDirs) {
        for(Path remoteDir : remoteDirs) {
            Iterator<Path> remoteFiles = null;
            try {
                remoteFiles = Files
                        .list(remoteDir)
                        .filter(path -> !Files.isDirectory(path))
                        .iterator();
                if(! remoteFiles.hasNext()) {
                    LOGGER.info("No files available for sync in Remote Directory: {}", remoteDir);
                }
            } catch (FileSystemException e) {
                LOGGER.error("Error while accessing remote directory.", e);
                continue;
            } catch (IOException e) {
                LOGGER.error("Error while listing files in remote directory.", e);
                continue;
            }
            while (remoteFiles.hasNext()) {
                Path remoteFile = remoteFiles.next();
                Path localFile = localDir.resolve(remoteFile.getFileName());
                LOGGER.info("Remote file found: {}", remoteFile);
                if (Files.exists(localFile)) {
                    LOGGER.warn("Remote file already exists locally: {}", localFile);
                } else {
                    LOGGER.info("Moving remote file to local storage: {}", localFile);
                    try {
                        Files.move(remoteFile, localFile, StandardCopyOption.ATOMIC_MOVE);
                    } catch (IOException e) {
                        LOGGER.error("Error while moving file from remote location to local storage.", e);
                    }
                }
            }
        }

    }
}
