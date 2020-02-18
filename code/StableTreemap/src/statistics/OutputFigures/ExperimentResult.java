/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics.OutputFigures;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import statistics.Baseline.TreeMapReader;
import statistics.Stability.CornerTravelDistance;
import statistics.Stability.RelativeQuadrantStability;
import statistics.StatisticalTracker;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author MaxSondag
 */
public class ExperimentResult {

    public String algorithm;
    public String dataset;
    public String classificationString;

    protected List<TreeMap> outputTreeMaps = new ArrayList();
    protected List<TreeMap> baseLineTreemaps = new ArrayList();

    int fileAmount;

    double meanMeanAspectRatio;
    double meanMedianAspectRatio;
    double medianMeanAspectRatio;
    double medianMedianAspectRatio;

    StringBuilder baselineCTDStringBuilder = new StringBuilder();
    
    /**
     * Sondag stability score
     */
    double ssv;

    /**
     * Corner travel change score.
     */
    double ctd;

    double drift;
    /**
     * Sondag stability score with baseline measure
     */
    double baselineSSV;

    /**
     * Corner travel score with baseline measure
     */
    double baselineCT;

    public ExperimentResult(String algorithm, String dataset, double meanMeanAspectRatio, double meanMedianAspectRatio, double ssv, double ctd, double baselineSSV, double baselineCT) {
        this.algorithm = algorithm;
        this.dataset = dataset;
        this.meanMeanAspectRatio = meanMeanAspectRatio;
        this.meanMedianAspectRatio = meanMedianAspectRatio;
        this.ssv = ssv;
        this.ctd = ctd;
        this.baselineSSV = baselineSSV;
        this.baselineCT = baselineCT;
    }

    /**
     * Both files should be sorted on time
     *
     * @param normalFiles
     * @param baselineFiles
     */
    public ExperimentResult(List<File> normalFiles, List<File> baselineFiles) {
        TreeMapReader tmr = new TreeMapReader();

        for (File f : normalFiles) {
            TreeMap tm = tmr.readTreeMap(f);
            outputTreeMaps.add(tm);
        }
        for (File f : baselineFiles) {
            TreeMap tm = tmr.readTreeMap(f);
            baseLineTreemaps.add(tm);
        }
        calculateStatistics();

        //remove the lists as this is too much data when going through all files.
        //leaving them in for now for testing purposes only, otherwise it could be arguments.
        outputTreeMaps = null;
        baseLineTreemaps = null;
    }

    protected void calculateStatistics() {
        fileAmount = outputTreeMaps.size();

        calculateAspectRatios();
        calculateSSV();
        calculateCornerTravel();
        calculateBaselineSSV();
        calculateBaselineCornerTravel();
        String s = baselineCTDStringBuilder.toString();
        System.out.println("s = " + s);

    }

    protected void calculateAspectRatios() {
        StatisticalTracker st = new StatisticalTracker(null);
        List<Double> meanAspectRatios = new ArrayList();
        List<Double> medianAspectRatios = new ArrayList();
        for (TreeMap tm : outputTreeMaps) {
            meanAspectRatios.add(st.getMeanAspectRatioLeafs(tm));
            medianAspectRatios.add(st.getMedianAspectRatioLeafs(tm));
        }
        meanMeanAspectRatio = getMean(meanAspectRatios);
        meanMedianAspectRatio = getMean(medianAspectRatios);
        
        medianMeanAspectRatio = getMedian(meanAspectRatios);
        medianMedianAspectRatio = getMedian(medianAspectRatios);
    }

    protected void calculateSSV() {
        ssv = 0;
        for (int i = 0; i < (outputTreeMaps.size() - 1); i++) {
            TreeMap tm1 = outputTreeMaps.get(i);
            TreeMap tm2 = outputTreeMaps.get(i + 1);
            RelativeQuadrantStability rqs = new RelativeQuadrantStability();
            double stability = rqs.getInstability(tm1, tm2);
            ssv += stability;
        }
        ssv = ssv / (double) (outputTreeMaps.size() - 1);
        if (Double.isNaN(ssv)) {
            System.out.println("ssv = " + ssv);
        }
    }

    protected void calculateCornerTravel() {
        ctd = 0;
        for (int i = 0; i < (outputTreeMaps.size() - 1); i++) {
            TreeMap tm1 = outputTreeMaps.get(i);
            TreeMap tm2 = outputTreeMaps.get(i + 1);
            CornerTravelDistance ctd = new CornerTravelDistance();
            double stability = ctd.getInstability(tm1, tm2);
            this.ctd += stability;
        }
        ctd = ctd / (double) (outputTreeMaps.size() - 1);
    }

    protected void calculateBaselineSSV() {
        baselineSSV = 0;
        for (int i = 0; i < (outputTreeMaps.size() - 1); i++) {
            //compare every treemap to it's baseline
            TreeMap oldTm = outputTreeMaps.get(i);
            TreeMap newTm = outputTreeMaps.get(i + 1);
            TreeMap baseTm = baseLineTreemaps.get(i);
            RelativeQuadrantStability rqs = new RelativeQuadrantStability();
            double instability = rqs.getBaselineInstability(oldTm, newTm, baseTm);
            baselineSSV += instability;
        }
        baselineSSV = baselineSSV / (double) (outputTreeMaps.size() - 1);
    }

    protected void calculateBaselineCornerTravel() {
        baselineCT = 0;
        for (int i = 0; i < (outputTreeMaps.size() - 1); i++) {
            //compare every treemap to it's baseline
            TreeMap oldTm = outputTreeMaps.get(i);
            TreeMap newTm = outputTreeMaps.get(i + 1);
            TreeMap baseTm = baseLineTreemaps.get(i);
            CornerTravelDistance ctd = new CornerTravelDistance();
            double stability = ctd.getBaselineInstability(oldTm, newTm, baseTm,baselineCTDStringBuilder);
            baselineCT += stability;
        }
        baselineCT = baselineCT / (double) (outputTreeMaps.size() - 1);
    }

    //return the mean from a list of doubles
    protected double getMean(List<Double> list) {
        double sum = 0;
        for (Double d : list) {
            sum += d;
        }
        return sum / (double) list.size();
    }

    //return the median from a list of doubles.
    protected double getMedian(List<Double> list) {
        List<Double> copyList = new ArrayList(list);
        copyList.sort((Double d1, Double d2) -> (Double.compare(d1, d2)));
        return copyList.get(copyList.size() / 2);
    }

    public String getResults() {
        String returnString = "";
        returnString += algorithm + ";";
        returnString += dataset + ";";
        returnString += meanMeanAspectRatio + ";";
        returnString += meanMedianAspectRatio + ";";
        returnString += medianMeanAspectRatio + ";";
        returnString += medianMedianAspectRatio + ";";
        returnString += ssv + ";";
        returnString += ctd + ";";
        returnString += baselineCT + ";";
        returnString += baselineSSV + ";";
        return returnString;
    }

    public boolean hasNaN() {
        if (Double.isNaN(meanMeanAspectRatio)) {
            return true;
        }
        if (Double.isNaN(meanMedianAspectRatio)) {
            return true;
        }
        if (Double.isNaN(medianMeanAspectRatio)) {
            return true;
        }
        if (Double.isNaN(medianMedianAspectRatio)) {
            return true;
        }
        if (Double.isNaN(ssv)) {
            return true;
        }
        if (Double.isNaN(ctd)) {
            return true;
        }
        if (Double.isNaN(baselineSSV)) {
            return true;
        }
        if (Double.isNaN(baselineCT)) {
            return true;
        }
        return false;
    }

    public void setClassificationString(String classificationString) {
        this.classificationString = classificationString;
    }

    public String getClassificationString() {
        return classificationString;
    }
}
