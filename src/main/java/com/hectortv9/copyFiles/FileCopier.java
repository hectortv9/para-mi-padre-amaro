package com.hectortv9.copyFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Properties;
import java.nio.file.Paths;

public class FileCopier {
    
    private static Properties privateProps = new Properties();
    static {
        try {
            privateProps.load(Thread
                .currentThread()
                .getContextClassLoader()
                .getResourceAsStream("private.properties/private.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(privateProps.toString());
    }

 
    //TODO: Is it ran on server or client side (Properties named to be ran on server)? Any folder structure @ source/destination?
    //TODO: Attempt to move file while opened and being edited. Should properties file being auto-reloaded?
    //TODO: Move files? Loop will keep iterating over files that might be already in destination folder
    //TODO: Add logger
    public static void main(String[] args) throws IOException {
        int seconds = Integer.parseInt(privateProps.getProperty("polling.time.seconds")) * 1000;
        while(true) {
            Path sourceDir = Paths.get(privateProps.getProperty("local.destination.path"));
            Path targetDir = Paths.get(privateProps.getProperty("remote.source.path.1"));
            System.out.println(sourceDir);
            System.out.println(targetDir);
            Iterator<Path> paths = Files
                .list(sourceDir)
                .filter(path -> ! Files.isDirectory(path))
                .iterator();
            while(paths.hasNext()) {
                Path sourcePath = paths.next();
                Path targetPath = targetDir.resolve(sourcePath.getFileName());
                System.out.println(sourcePath);
                System.out.println(targetPath);
                if(Files.exists(targetPath)) {
                    System.out.println("File already exists:" + targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.COPY_ATTRIBUTES); //StandardCopyOption.ATOMIC_MOVE
                }
            }
            try {
                Thread.sleep(seconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
