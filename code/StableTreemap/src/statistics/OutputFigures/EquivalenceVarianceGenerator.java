/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics.OutputFigures;

import UserControl.Visualiser.IpeExporter;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import statistics.OutputFigures.RugGenerator.VizVariable;
import static statistics.OutputFigures.RugGenerator.VizVariable.AABASECTD;
import static statistics.OutputFigures.RugGenerator.VizVariable.AAMEANAR;
import treemap.dataStructure.Rectangle;
import utility.Pair;

/**
 *
 * @author msondag
 */
public class EquivalenceVarianceGenerator {

    private final List<ExperimentResult> allResults;
    private final File outputFile;
    private final int minDatasets;

    private OutputGrid outputGrid = new OutputGrid();

    public EquivalenceVarianceGenerator(List<ExperimentResult> allResults, String outputFileString, int minDatasets) throws IOException {
        this.allResults = allResults;
        this.outputFile = new File(outputFileString);
        this.minDatasets = minDatasets;
        generateVarianceGrid();
    }

    private void generateVarianceGrid() {
        //will store the ipe file
        String ipeOutputString = "";
        ipeOutputString += IpeExporter.getPreamble();

        ArrayList<VizVariable> visVariables = new ArrayList();
        visVariables.add(AAMEANAR);
        visVariables.add(AABASECTD);
//        visVariables.add(AASSV);

        List<String> classStrings = getLargeEnoughClasses(allResults);

        for (int i = 0; i < visVariables.size(); i++) {
            VizVariable var = visVariables.get(i);
            double globalVariance = getGlobalVariance(var);
            System.out.println("globalVariance = " + globalVariance + " for var " + var.name());
            for (int j = 0; j < classStrings.size(); j++) {
                String classString = classStrings.get(j);
                List<ExperimentResult> classResults = getResultsForClass(allResults, classString);
                double sumVariance = getSumOfAlgorithmVariances(classResults, var);
                if (sumVariance < 2) {
                    sumVariance = getSumOfAlgorithmVariances(classResults, var);
                }
                //Get the outputIpe string for the colored rectangle in the correct location for this class.
                ipeOutputString += drawResult(classString, var, sumVariance, globalVariance);
            }

        }

        ipeOutputString += outputGrid.drawGridOutline();

        ipeOutputString += IpeExporter.endIpe();

        writeFile(ipeOutputString);
    }

    /**
     * Returns a new list containing the names of the classes that have enough
     * datasets.
     *
     * @param get
     * @return
     */
    private List<String> getLargeEnoughClasses(List<ExperimentResult> allResults) {
        HashMap<String, Integer> classifications = new HashMap();
        Set<String> algorithms = new HashSet();
        for (ExperimentResult er : allResults) {
            String cString = er.classificationString;
            //add one or start at one
            classifications.put(cString, classifications.getOrDefault(cString, 0) + 1);
            algorithms.add(er.algorithm);
        }
        List<String> largeEnoughClasses = new ArrayList();
        for (String classification : classifications.keySet()) {
            //each dataset is in there #algorithms times.
            if (classifications.get(classification) > (minDatasets * algorithms.size())) {
                largeEnoughClasses.add(classification);
            }
        }
        return largeEnoughClasses;
    }

    /**
     * Gets the variance over all experimentResults.
     *
     * @param allClasses
     * @return
     */
    private double getGlobalVariance(VizVariable var) {
        List<String> datasets = getDatasets(allResults);
        List<ExperimentResult> trimmedSet = filterExperiments(allResults, datasets);
        return getSumOfAlgorithmVariances(trimmedSet, var);
    }

    /**
     * Returns a new list containing only the experimentresults that belong to
     * classString
     *
     * @param results     not modified
     * @param classString
     * @return
     */
    private List<ExperimentResult> getResultsForClass(List<ExperimentResult> results, String classString) {
        List<ExperimentResult> outputResults = new ArrayList();
        for (ExperimentResult er : results) {
            if (er.classificationString.equals(classString)) {
                outputResults.add(er);
            }
        }
        return outputResults;
    }

    /**
     *
     * @param algorithmScores (algorithm, vizvariableScore)
     * @return
     */
    private HashMap<String, Double> getGrayValueByMedian(HashMap<String, Double> algorithmScores) {
        double median = getMedian(algorithmScores.values());
        double minimum = getMinimum(algorithmScores.values());

        HashMap<String, Double> grayScores = new HashMap();
        for (String algo : algorithmScores.keySet()) {
            double vizScore = algorithmScores.get(algo);
            double grayScore = getGrayValueByMedian(median, minimum, vizScore);
            grayScores.put(algo, grayScore);
        }
        return grayScores;
    }

    /**
     * Score is the score of an algorithm on a specific vizvariable
     *
     * @param medianScore
     * @param minimumScore
     * @param score
     * @return
     */
    private int getGrayValueByMedian(double medianScore, double minimumScore, double score) {
        double maximum = 2 * (medianScore - minimumScore) + minimumScore;

        double percentage = (score - minimumScore) / (maximum - minimumScore);

        if (percentage > 1) {
            return 0;
        }
        //range between 5 and 250. Best is low values and should be white
        int grayValue = (int) (250 - percentage * 245);
        return grayValue;
    }

    /**
     * Draws the rectangle for this vizvariable with the correct color in the
     * correct position.
     *
     * @param classString
     * @param var
     * @param variance
     * @param globalVariance
     * @return
     */
    private String drawResult(String classString, VizVariable var, double variance, double globalVariance) {
        //get the location for this class.
        Rectangle classRectangle = outputGrid.getClassRectangle(classString);
        //split the classRectangle in two and get the right side
        Rectangle varianceRectangle = getVizVariableRectangle(var, classRectangle);

        Color c = getColorFromVariance(variance, globalVariance);
        //Draw the rectangle in the right spot with the right color and return it.
        String label = "" + Math.round(variance);

        System.out.println("classString = " + classString);
        System.out.println("variance = " + variance);


        return IpeExporter.getRectangle(varianceRectangle, label, c, false);
    }

    /**
     * Returns the rectangle that is reserved for the vizvariable in
     * classRectangle
     *
     * @param var
     * @param classRectangle
     * @return
     */
    private Rectangle getVizVariableRectangle(VizVariable var, Rectangle classRectangle) {
        double y = classRectangle.getY();
        double height = classRectangle.getHeight();
        double width = classRectangle.getWidth() / 2;
        double x = -100;//stub
        if (var == AAMEANAR) {
            x = classRectangle.getX();
        }
        if (var == AABASECTD) {
            x = classRectangle.getX() + width;
        }
        return new Rectangle(x, y, width, height);
    }

    /**
     * Returns the variance of the entire class.
     *
     * @param classResults
     * @return
     */
    private double getSumOfAlgorithmVariances(List<ExperimentResult> classResults, VizVariable var) {
        //datasets are filtered in the same way as on the rug
        List<String> datasets = getDatasets(classResults);
        List<String> algorithms = getAlgorithms(classResults);

        //get the gray scale score for one algorithm over all datasets..
        HashMap<String, List<Double>> grayScaleScores = new HashMap();//algo, scores
        for (String dataset : datasets) {
            //get the scores on one dataset
            HashMap<String, Double> algorithmScores = new HashMap();
            for (String algorithm : algorithms) {
                double algorithmScore = getAlgorithmScore(dataset, algorithm, classResults, var);
                algorithmScores.put(algorithm, algorithmScore);
            }
            //convert to grayscale scores for a single dataset
            HashMap<String, Double> grayScaleScoresDataset = getGrayValueByMedian(algorithmScores);
            for (String algorithm : grayScaleScoresDataset.keySet()) {
                //get the array or start a new one
                List algoList = grayScaleScores.getOrDefault(algorithm, new ArrayList());
                //add the grayscore
                double grayScore = grayScaleScoresDataset.get(algorithm);
                algoList.add(grayScore);
                //put it back in the 
                grayScaleScores.put(algorithm, algoList);
            }

        }
        //calculate the variance per algorithm
        List<Double> algorithmVariances = new ArrayList();
        for (String algorithm : grayScaleScores.keySet()) {
            List<Double> scores = grayScaleScores.get(algorithm);
            double variance = getVariance(scores);
            algorithmVariances.add(variance);
        }
        //return sum of variance
        return getSum(algorithmVariances);
    }

    /**
     * Returns the grayscale score of the algorithm on the dataset.
     *
     * @param dataset
     * @param algorithm
     * @param classResults
     * @return
     */
    private double getAlgorithmScore(String dataset, String algorithm, List<ExperimentResult> classResults, VizVariable var) {
        for (ExperimentResult er : classResults) {
            if (er.dataset.equals(dataset) && er.algorithm.equals(algorithm)) {
                if (var == AAMEANAR) {
                    return er.meanMedianAspectRatio;
                }
                if (var == AABASECTD) {
                    return er.baselineSSV;
                }
            }
        }
        return -1;
    }

    private Color getColorFromVariance(double variance, double globalVariance) {
        double ratio = Math.max(variance / globalVariance, globalVariance / variance);

        if (variance > globalVariance) {
            //not descriptive
            if (ratio > 2) {
                return new Color(203, 24, 29);
            } else if (ratio > 1.5) {
                return new Color(251, 106, 74);
            } else {
                return new Color(252, 187, 161);
            }
        } else {
            //variance < globalVariance
            if (ratio > 2) {
                return new Color(33, 113, 181);
            } else if (ratio > 1.5) {
                return new Color(107, 174, 214);
            } else {
                return new Color(198, 219, 239);
            }
        }
    }

    /**
     * Returns a random sample(consistent with the rugs) of minDatasets
     * datasets.
     *
     * @param classResults
     * @return
     */
    private List<String> getDatasets(List<ExperimentResult> classResults) {
        List<String> datasets = new ArrayList();
        for (ExperimentResult er : classResults) {
            if (!datasets.contains(er.dataset)) {
                datasets.add(er.dataset);
            }
        }

        //sort and then shuffle +trim to ensure consistency.
        Collections.sort(datasets);
        Collections.shuffle(datasets, new Random(42));
        if (datasets.size() < minDatasets) {
            return new ArrayList();
        }
        if (datasets.size() > minDatasets) {
            datasets = datasets.subList(0, minDatasets);
        }

        return datasets;
    }

    /**
     * Gets the algorithms (unsorted)
     *
     * @param datasets
     * @return
     */
    private List<String> getAlgorithms(List<ExperimentResult> classResults) {
        List<String> algorithms = new ArrayList();
        for (ExperimentResult er : classResults) {
            if (!algorithms.contains(er.algorithm)) {
                algorithms.add(er.algorithm);
            }
        }
        return algorithms;
    }

    private double getMinimum(Collection<Double> values) {
        double min = Double.MAX_VALUE;
        for (Double d : values) {
            min = Math.min(min, d);
        }
        return min;
    }

    /**
     * Returns the variance.
     *
     * @param values
     * @return
     */
    private double getVariance(Collection<Double> values) {
        double mean = getMean(values);
        double variance = 0;
        for (Double d : values) {
            variance += (d - mean) * (d - mean);
        }
        return variance / values.size();

    }

    /**
     * Returns the mean.
     *
     * @param values
     * @return
     */
    private double getMean(Collection<Double> values) {
        double mean = 0;
        for (Double d : values) {
            mean += d;
        }
        return mean / values.size();
    }

    private void writeFile(String ipeOutputString) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(outputFile));
            bw.write(ipeOutputString);
            bw.flush();
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(EquivalenceVarianceGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(EquivalenceVarianceGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private List<ExperimentResult> filterExperiments(List<ExperimentResult> results, List<String> datasets) {
        List<ExperimentResult> outputList = new ArrayList();
        for (ExperimentResult er : results) {
            if (datasets.contains(er.dataset)) {
                outputList.add(er);
            }
        }
        return outputList;
    }

    private double getMedian(Collection<Double> values) {
        ArrayList<Double> copyValues = new ArrayList(values);
        Collections.sort(copyValues);
        int middle = (int) Math.floor(copyValues.size() / 2);
        return copyValues.get(middle);

    }

    private double getSum(List<Double> algorithmVariances) {
        double sum = 0;
        for (Double d : algorithmVariances) {
            sum += d;
        }
        return sum;

    }

}
