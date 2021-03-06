/**
 * MIT License
 *
 * Copyright (c) 2018 Wei SHEN 

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ciaoshen.leetcode.helper;

// java.util
import java.util.Properties;
import java.util.Arrays;
// java.io
import java.io.FileNotFoundException;
// java.nio
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.nio.file.NotDirectoryException;
// uri
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
// junit
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
// log4j
import org.apache.log4j.PropertyConfigurator;
// slf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * JUnit test
 * @author Wei SHEN
 */
public class TesterRunner {

    private static final String SPLITER = "/";

    /** com.ciaoshen.leetcode.helper --> /com/ciaoshen/leetcode/helper */
    private static String pckNameToPath(String pck) {
        pck = pck.replaceAll("\\.",SPLITER);
        if (pck.substring(pck.length() - 1, pck.length()) != SPLITER) { // Class.getResource() method requires leading slash, such as "/com/ciaoshen/leetcode/helper"
            pck = SPLITER + pck;
        }
        return pck;
    }

    /** /com/ciaoshen/leetcode/helper --> com.ciaoshen.leetcode.helper  */
    private static String pckPathToName(String pck) {
        pck = pck.replaceAll(SPLITER, "\\.");
        return pck.substring(1, pck.length());
    }

    /** test all classes in target package */
    private static void testPackage(String pck) {
        String packagePath = pckNameToPath(pck); // in form of: "/com/ciaoshen/leetcode/helper"
        try {
            URL url = TesterRunner.class.getResource(packagePath);
            if (url == null) {
                throw new FileNotFoundException("The URL taken from \"" + pck + "\" is null!");
            }
            Path dir = Paths.get(url.toURI()); // throws URISyntaxException
            DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{class}"); // throws NotDirectoryException, IOException
            for (Path path : stream) {
                String packageName = pckPathToName(packagePath); // in form of: "com.ciaoshen.leetcode.helper"
                String classFile = path.getFileName().toString(); // in form of: "ProblemBuilder.class"
                String className = classFile.substring(0, classFile.length() - 6); // in form of : "ProblemBuilder"
                if (className.length() >= 4 && className.substring(className.length() - 4, className.length()).equals("Test")) { // JUnit test class name end with "Test", such as: "TemplateSeekerTest.class"
                    String fullName = packageName + "." + className;
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("CLASS --> 【{}】", fullName);
                    }
                    Class klass = Class.forName(fullName);
                    Result result = JUnitCore.runClasses(klass);
                    for (Failure failure : result.getFailures()) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("{}", failure);
                        }
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Class {} pass junit test? {} \n", klass, result.wasSuccessful());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String LOG4J = "log4j-dev.properties";
    // call from slf4j facade
    private static final Logger LOGGER = LoggerFactory.getLogger(TesterRunner.class);


    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("TestRunner: Must have 1 argument: package name.");
        }
        System.out.println(Arrays.toString(args));
        // use log4j as Logger implementation
        Properties log4jProps = PropertyScanner.load(LOG4J);
        PropertyConfigurator.configure(log4jProps);

        for (String pck : args) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(" PACKAGE >=========================【{}】=========================<\n", pck);
            }
            testPackage(pck);
        }
    }
}
