/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics.OutputFigures;

import utility.HSLColor;
import UserControl.Visualiser.IpeExporter;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import static statistics.OutputFigures.RugGenerator.VizVariable.*;
import treemap.dataStructure.Rectangle;
import utility.Pair;

/**
 *
 * @author msondag
 */
public class RugGenerator {

    int rugSize = 50;

    private final List<ExperimentResult> values;
    private final String header;
    private final File outputFile;
    private OutputGrid outputGrid = new OutputGrid();

    public final HashMap<String, String> algorithmShorthands;
    private Color outlierColor = new Color(106, 81, 163);

    public enum VizVariable {
        AAMEANAR,
        AAMEDIANAR,
        AASSV,
        AACTD,
        AABASESSV,
        AABASECTD
    }

    VizVariable vizVariable;

    public RugGenerator(List<ExperimentResult> values, VizVariable vizVariable, String header, String outputFileString) throws IOException {
        this.values = values;
        this.vizVariable = vizVariable;
        this.header = header;
        this.outputFile = new File(outputFileString);
        algorithmShorthands = new HashMap();
        algorithmShorthands.put("Approximation", "APP");
        algorithmShorthands.put("SizeBalancedPartition", "ZBP");
        algorithmShorthands.put("Squarified", "SQR");
        algorithmShorthands.put("Moore", "MOO");
        algorithmShorthands.put("Hilbert", "HIL");
        algorithmShorthands.put("Local4Moves1Repeats", "LM4");
        algorithmShorthands.put("SequenceBalancedPartition", "SBP");
        algorithmShorthands.put("Split", "SPL");
        algorithmShorthands.put("Local0Moves0Repeats", "LM0");
        algorithmShorthands.put("PivotByMiddle", "PBM");
        algorithmShorthands.put("PivotBySplut", "PBS");
        algorithmShorthands.put("PivotBySize", "PBZ");
        algorithmShorthands.put("Spiral", "SPI");
        algorithmShorthands.put("Strip", "STR");
        algorithmShorthands.put("SliceAndDice", "SND");
        algorithmShorthands.put("Git", "GIT");

        generateRug();
    }

    private void generateRug() throws IOException {
        //get a fixed amount of rugs
        List<String> datasets = getDatasets();
        //get the algorithms
        List<String> algorithms = getAlgorithms();

        if (algorithms.isEmpty() || datasets.isEmpty()) {
            System.out.println("datasets.size() = " + datasets.size());
            System.out.println("algorithms.size() = " + algorithms.size());
            return;
        }

        Color[][] matrix = new Color[datasets.size()][algorithms.size()];

        //for each column, set the colors according to the algorithm ranking
        for (int i = 0; i < datasets.size(); i++) {
            Color[] columnColor;
//            columnColor = getColorMappingEffect(algorithms, datasets.get(i));
//            columnColor = getColorMappingRanking(algorithms, datasets.get(i));
            columnColor = getColorMappingMedian(algorithms, datasets.get(i));
            matrix[i] = columnColor;
        }

        matrix = sortColumns(matrix);
        matrix = sortRows(matrix);//matrix itself is changed

        //Note: all rectangle coordinates are divided by 10 due to it being modified for large sized treemaps
        //output the colors
        String outputString = "";
        outputString += IpeExporter.getPreamble();

        Rectangle outputRectangle = outputGrid.getClassRectangle(header);
        double xoffset = outputRectangle.getX();
        double yoffset = outputRectangle.getY();

        double width = outputRectangle.getWidth() / datasets.size();
        double height = outputRectangle.getHeight() / algorithms.size();

        //draw each rug in a rectangle at the correct location to fit it into the outputrug
        for (int x = 0; x < datasets.size(); x++) {
            HashMap<String, Double> algorithmScoresMap = getAlgorithmScoresMap(datasets.get(x));
            if (algorithmScoresMap.size() != algorithms.size()) {
                continue;
            }
            for (int y = 0; y < algorithms.size(); y++) {
                Rectangle r = new Rectangle(((double) x) * width + xoffset, ((double) y) * height + yoffset, width, height);
                Color c = matrix[x][y];
                outputString += IpeExporter.getRectangle(r, "", c, false);
//                double score = (Math.round(10 * algorithmScoresMap.get(algorithms.get(y)))) / 10.0;
//                outputString += IpeExporter.getHugeString(x * size + size * 0.5, y * size + size * 0.5, "" + score, "center");
            }
        }

        outputString += IpeExporter.endIpe();

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        bw.write(outputString);
        bw.flush();
        bw.close();

//        printDistribution(matrix, algorithms);
    }

    private Color[][] sortColumns(Color[][] matrix) {
        Color[][] outputMatrix = new Color[matrix.length][matrix[0].length];

        int highestColumn = getHighestColumn(matrix);
        outputMatrix[0] = matrix[highestColumn];

        Set<Integer> columnsLeft = new HashSet();
        for (int i = 0; i < outputMatrix.length; i++) {
            if (i == highestColumn) {
                continue;
            }
            columnsLeft.add(i);
        }

        for (int i = 1; i < outputMatrix.length; i++) {

            double bestDistance = Double.MAX_VALUE;
            int bestColumn = -1;
            for (Integer x : columnsLeft) {
                double distance = columnDistance(outputMatrix[i - 1], matrix[x]);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestColumn = x;
                }
            }
            outputMatrix[i] = matrix[bestColumn];
            columnsLeft.remove(bestColumn);
        }
        return outputMatrix;
    }

    private Color[][] sortRows(Color[][] matrix) {
        Color[][] outputMatrix = new Color[matrix.length][matrix[0].length];
        ArrayList<Pair<Integer, Double>> sumScores = new ArrayList();
        for (int j = 0; j < matrix[0].length; j++) {
            double sumScore = 0;
            for (int i = 0; i < matrix.length; i++) {
                sumScore += getColorScore(matrix[i][j]);
            }
            sumScores.add(new Pair(j, sumScore));
        }
        sumScores.sort((Pair<Integer, Double> p1, Pair<Integer, Double> p2) -> Double.compare(p1.y, p2.y));
        for (int j = 0; j < matrix[0].length; j++) {
            int inputRow = sumScores.get(j).x;
            for (int i = 0; i < matrix.length; i++) {
                outputMatrix[i][j] = matrix[i][inputRow];
            }
        }

        return outputMatrix;
    }

    private double getColorScore(Color c) {
        //The higher the value, the darker it is
        if (c == outlierColor) {
            // Nodes have high luminance on low values, but we invert everything so high values are on top
            return 100;
        }
        float[] hslc1 = HSLColor.fromRGB(c);
        double l1 = hslc1[2];
        //invert as we are measuring luminance, and we want it inverted
        return 100 - Math.abs(l1);
    }

    private int getHighestColumn(Color[][] matrix) {
        double maxSum = 0;
        int maxIndex = -1;
        for (int i = 0; i < matrix.length; i++) {
            double sum = 0;
            Color[] column = matrix[i];
            for (int j = 0; j < column.length; j++) {
                sum += getColorScore(column[j]);
            }
            if (sum > maxSum) {
                maxSum = sum;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private double columnDistance(Color[] c1, Color[] c2) {
        double distance = 0;
        for (int i = 0; i < c1.length; i++) {
            if (c1[i] == outlierColor) {
                //maximum distance
                distance += 1;
                continue;
            }
            float[] hslc1 = HSLColor.fromRGB(c1[i]);
            double l1 = hslc1[2];
            float[] hslc2 = HSLColor.fromRGB(c2[i]);
            double l2 = hslc2[2];
            distance += Math.abs(l1 - l2);
        }
        return distance;
    }

    /**
     * Gets the algorithms and sorts based on the average(total) score (when
     * colored)
     *
     * @param datasets
     * @return
     */
    private List<String> getAlgorithms() {
        List<String> algorithms = new ArrayList();
        for (ExperimentResult er : values) {
            if (!algorithms.contains(er.algorithm)) {
                algorithms.add(er.algorithm);
            }
        }
        return algorithms;
    }

    private List<String> getDatasets() {
        List<String> datasets = new ArrayList();
        for (ExperimentResult er : values) {
            if (!datasets.contains(er.dataset)) {
                datasets.add(er.dataset);
            }
        }

        //sort and then shuffle to ensure consistency.
        Collections.sort(datasets);
        Collections.shuffle(datasets, new Random(42));
        if (datasets.size() < 50) {
            System.out.println("datasets.size() = " + datasets.size());
            return new ArrayList();
        }
        if (datasets.size() > 50) {
            datasets = datasets.subList(0, 50);
        }

        return datasets;
    }

    private HashMap<String, Double> getAlgorithmScoresMap(String dataset) {
        HashMap<String, Double> algorithmScores = new HashMap();
        for (ExperimentResult er : values) {
            if (!er.dataset.equals(dataset)) {
                continue;
            }
            //got the right dataset, get the score
            String algorithm = er.algorithm;
            double score = getVizVariableScore(er);
            algorithmScores.put(algorithm, score);
        }
        return algorithmScores;
    }

    /**
     * Returns a list sorted in the same way as algorithms
     *
     * @param algorithms
     * @param dataset
     * @return
     */
    private List<Pair<String, Double>> getAlgorithmScores(List<String> algorithms, String dataset) {
        HashMap<String, Double> algorithmScoresMap = getAlgorithmScoresMap(dataset);
        List<Pair<String, Double>> sortedScores = new ArrayList();
        //sort based on algorithm
        for (int i = 0; i < algorithms.size(); i++) {
            String algorithm = algorithms.get(i);
            if (!algorithmScoresMap.containsKey(algorithm)) {
                continue;
            }
            double score = algorithmScoresMap.get(algorithm);
            sortedScores.add(new Pair(algorithm, score));
        }

        return sortedScores;
    }

    private Color[] getColorMappingMedian(List<String> algorithms, String dataset) {
        HashMap<String, Double> algorithmScoresMap = getAlgorithmScoresMap(dataset);

        Collection<Double> scores = algorithmScoresMap.values();
        double median = getMedian(scores);
        double minimum = getMinimum(scores);

        Color[] outputColors = new Color[algorithms.size()];
        for (int i = 0; i < algorithms.size(); i++) {
            String algorithm = algorithms.get(i);
            if (algorithm == null || !algorithmScoresMap.containsKey(algorithm)) {
                System.out.println("");
            }
            double score = algorithmScoresMap.get(algorithm);
            Color c = getColorByMedian(median, minimum, score);
            outputColors[i] = c;
        }

        return outputColors;
    }

    private double getVizVariableScore(ExperimentResult er) {
        double score;

        switch (vizVariable) {
            case AAMEANAR:
                //1-x as we want the worst score to be at the top.
                score = 1 - er.meanMeanAspectRatio;
                break;
            case AAMEDIANAR:
                //1-x as we want the worst score to be at the top.
                score = 1 - er.meanMedianAspectRatio;
                break;
            case AACTD:
                score = er.ctd;
                break;
            case AASSV:
                score = er.ssv;
                break;
            case AABASECTD:
                score = er.baselineCT;
                break;
            case AABASESSV:
                score = er.baselineSSV;
                break;
            default:
                throw new IllegalStateException("Vizvariable is not set to one of it's enum variables");
        }
        return score;
    }

    private int getRanking(List<Pair<String, Double>> algorithmScores, String algorithm) {

        double score = 0;
        for (int i = 0; i < algorithmScores.size(); i++) {
            if (algorithmScores.get(i).x.equals(algorithm)) {
                score = algorithmScores.get(i).y;
            }
        }
        int countBelow = 0;
        for (int i = 0; i < algorithmScores.size(); i++) {
            double aScore = algorithmScores.get(i).y;
            if (aScore < score) {
                countBelow++;
            }
        }

        return countBelow;
    }

    private Color getColorByRank(int ranking, int size) {
        int minColor = 250;
        int maxColor = 5;

        double interval = (maxColor - minColor) / (double) size;
        int grayValue;
        if (ranking == -1) {
            grayValue = 255;
        } else {
            grayValue = minColor + (int) Math.round(interval * ranking);
        }
        return new Color(grayValue, grayValue, grayValue);
    }

    private Color getColorByScore(double minScore, double maxScore, double score) {
        double minColor = 250;
        double maxColor = 0;

        double interval = (maxColor - minColor);
        double grayValue;

        double percentage = (score - minScore) / (maxScore - minScore);

        grayValue = percentage * interval + minColor;
        return new Color((int) grayValue, (int) grayValue, (int) grayValue);
    }

    private Color getColorByMedian(double median, double minimum, double score) {
        double maximum = 2 * (median - minimum) + minimum;

        double percentage = (score - minimum) / (maximum - minimum);

        if (percentage > 1) {
            return outlierColor;
        }
        if (Double.isNaN(percentage)) {
            percentage = 0;
        }

        float h = 209f;//360 degree angles
        float s = 56f;
        float l = 85f - (float) percentage * 55f;
        return HSLColor.toRGB(h, s, l);
    }

    private double getAverageScore(String algorithm, List<String> datasets) {
        double totalScore = 0;
        for (String dataset : datasets) {
            if (!getAlgorithmScoresMap(dataset).containsKey(algorithm)) {
                continue;
            }
            double score = getAlgorithmScoresMap(dataset).get(algorithm);
            totalScore += score;
        }
        return totalScore / datasets.size();
    }

    private double getMedianScore(String algorithm, List<String> datasets) {
        List<Double> scores = new ArrayList();
        for (String dataset : datasets) {
            if (!getAlgorithmScoresMap(dataset).containsKey(algorithm)) {
                continue;
            }
            double score = getAlgorithmScoresMap(dataset).get(algorithm);
            scores.add(score);
        }
        return getMedian(scores);
    }

    private double getMedian(Collection<Double> scores) {
        ArrayList<Double> sortedScores = new ArrayList(scores);
        Collections.sort(sortedScores);
        int middle = (int) Math.floor(scores.size() / 2);
        return sortedScores.get(middle);
    }

    private double getMinimum(Collection<Double> scores) {
        ArrayList<Double> sortedScores = new ArrayList(scores);
        Collections.sort(sortedScores);
        return sortedScores.get(0);
    }

    private double getMean(Collection<Double> scores) {
        double total = 0;
        for (Double d : scores) {
            total += d;
        }
        return total / (double) scores.size();
    }

    private double getVariance(Collection<Double> scores) {
        double mean = getMean(scores);
        double variance = 0;
        for (Double d : scores) {
            variance += (d - mean) * (d - mean);
        }

        return variance / (double) (scores.size());
    }
}
