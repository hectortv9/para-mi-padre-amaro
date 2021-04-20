package com.hectortv9;

import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.hectortv9.copyFiles.FileCopier;


public class FileCopierTest {

    @BeforeClass
    public static void loadProperties() {
        FileCopier.loadProperties();
    }

    @Test
    public void testGetRemoteDirs() {
        ArrayList<Path> files = FileCopier.getRemoteDirs();
        Assert.assertTrue("No remote files found in Properties file", files.size() > 0);
    }


}
