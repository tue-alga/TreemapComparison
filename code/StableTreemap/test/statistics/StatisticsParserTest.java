/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author MaxSondag
 */
public class StatisticsParserTest {

    public StatisticsParserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of isBaselineFile method, of class StatisticsParser.
     */
    @Test
    public void testIsBaselineFile() {
        System.out.println("isBaselineFile");
        File f = new File("Hilbert/baselineTestDataset/t1.rect");
        StatisticsParser instance = new StatisticsParser();
        boolean result = instance.isBaselineFile(f);
        assertEquals(true, result);

        f = new File("Hilbert/TestDataset/t1.rect");
        result = instance.isBaselineFile(f);
        assertEquals(false, result);

        f = new File("Hilbert\\baselineTestDataset\\t1.rect");
        result = instance.isBaselineFile(f);
        assertEquals(true, result);

        f = new File("Hilbert\\TestDataset\\t1.rect");
        result = instance.isBaselineFile(f);
        assertEquals(false, result);

    }

    /**
     * Test of getAlgorithm method, of class StatisticsParser.
     */
    @Test
    public void testGetAlgorithm() {
        File f = new File("Test/Hilbert/baselineTestDataset/t1.rect");
        StatisticsParser instance = new StatisticsParser();
        String result = instance.getAlgorithm(f);
        assertEquals("Hilbert", result);

        f = new File("Test/Hilbert/TestDataset/t1.rect");
        result = instance.getAlgorithm(f);
        assertEquals("Hilbert", result);

        f = new File("Test\\Hilbert\\baselineTestDataset\\t1.rect");
        result = instance.getAlgorithm(f);
        assertEquals("Hilbert", result);

        f = new File("Test/Hilbert/TestDataset/t1.rect");
        result = instance.getAlgorithm(f);
        assertEquals("Hilbert", result);
    }

    /**
     * Test of getDataSet method, of class StatisticsParser.
     */
    @Test
    public void testGetDataSet() {
        File f = new File("Test/Hilbert/baselineTestDataset/t1.rect");
        StatisticsParser instance = new StatisticsParser();
        String result = instance.getDataSet(f);
        assertEquals("TestDataset", result);

        f = new File("Test/Hilbert/TestDataset/t1.rect");
        result = instance.getDataSet(f);
        assertEquals("TestDataset", result);

        f = new File("Test\\Hilbert\\baselineTestDataset\\t1.rect");
        result = instance.getDataSet(f);
        assertEquals("TestDataset", result);

        f = new File("Test\\Hilbert\\TestDataset\\t1.rect");
        result = instance.getDataSet(f);
        assertEquals("TestDataset", result);
    }

    /**
     * Test of getTmNumber method, of class StatisticsParser.
     */
    @Test
    public void testGetTmNumber() {
        System.out.println("getTmNumber");
        File f = new File("Test/Hilbert/baselineTestDataset/t1.rect");
        StatisticsParser instance = new StatisticsParser();
        int result = instance.getTmNumber(f);
        assertEquals(1, result);

        f = new File("Test/Hilbert/TestDataset/t1.rect");
        result = instance.getTmNumber(f);
        assertEquals(1, result);

        f = new File("Test\\Hilbert\\baselineTestDataset\\t1.rect");
        result = instance.getTmNumber(f);
        assertEquals(1, result);

        f = new File("Test\\Hilbert\\TestDataset\\t1.rect");
        result = instance.getTmNumber(f);
        assertEquals(1, result);
    }

}
