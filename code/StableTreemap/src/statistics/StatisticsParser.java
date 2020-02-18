/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import statistics.OutputFigures.ExperimentResult;
import statistics.OutputFigures.RugGenerator;
import statistics.OutputFigures.EquivalenceVarianceGenerator;
import dataSetClassifier.Classification;
import dataSetClassifier.DataSetClassifier;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import statistics.OutputFigures.RugGenerator.VizVariable;
import treemap.DataFaciliation.DataFacilitator;
import treemap.DataFaciliation.DataFileManagerFast;
import utility.Pair;

/**
 * Takes a folder which contains output files and their basefiles as exported by
 * treemaps.sh.
 *
 * @author msondag
 */
public class StatisticsParser {

    public static void main(String[] args) throws IOException {
        //root of the folder that contains all the test.
        //structure: algorithm//dataset//output

        File f = new File("test.csv");
        System.out.println("f.get = " + f.getAbsolutePath());
        if (true) {
            return;
        }
        
//        //used when statistics are not yet gathered
        File rootFolder = new File("../Data/OutputTreemaps"); 
        File dataSetFolder = new File("../Data/Datasets");
        StatisticsParser sp = new StatisticsParser();
        sp.parseStatisticsForFolder(rootFolder, dataSetFolder, false);


//        //Used when we wan to partially recalculate the values.
//        sp.parseStatisticsForFolder(rootFolder, dataSetFolder, true);

        //used when all values are known.
//        String statisticsString = "../Data/Statistics.csv";
//        String dataSetString = "../Data/DataSetClassifications.csv";
//        StatisticsParser sp = new StatisticsParser();
//        sp.parseStatisticsForFolder(statisticsString, dataSetString);
    }

    public void parseStatisticsForFolder(String treemapStatisticsFileString, String datasetFolder) throws IOException {
        File treemapStatisticsFile = new File(treemapStatisticsFileString);
        File dataSetFolder = new File(datasetFolder);

        List<ExperimentResult> experimentResults = parseTreemapStatistics(treemapStatisticsFile);

        generateRugs(experimentResults, dataSetFolder);
        generateTable(experimentResults, dataSetFolder);
    }

    private void generateTable(List<ExperimentResult> experimentResults, File datasetFolder) throws IOException {
        //Next statement is required as it automatically assigned classiffications to ExperimentResults.
        splitOnClassification(experimentResults, datasetFolder);
//        printClassificationsAmounts(experimentResults, new File("../Data/AllDatasets"));

        int minDataset = 50;
        EquivalenceVarianceGenerator evg = new EquivalenceVarianceGenerator(experimentResults, "VarianceTable.ipe", minDataset);
    }

    public void parseStatisticsForFolder(File rootFolder, File datasetFolder, boolean filter) throws IOException {

        //get the matching output files and their baselines
        Set<File> leafFolders = getLeafFolders(rootFolder);
        System.out.println("leafFolders.size() = " + leafFolders.size());
        if (filter) {
            //filterFolders is only used to redo some files.
            leafFolders = filterFolders(leafFolders);
        }
        System.out.println("leafFolders.size() after filter = " + leafFolders.size());

        List<Pair<File, File>> normalBasePairList = getNormalBasePairs(leafFolders);
        System.out.println("normalBasePairList.size() = " + normalBasePairList.size());

        //calculate all statistics
        double total = normalBasePairList.size();
        System.out.println("total = " + total);
        double count = 0;
        long startTime = System.currentTimeMillis();
        try {
            File f = new File("./Data/Statistics.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));

            bw.append("algo;dataset;meanMeanAr;meanMedianAr;medianMeanAr;medianMedianAr;ssv;ctd;baselineCTD;baselineSSV\n");

            for (Pair<File, File> normalBasePair : normalBasePairList) {
                File normalFolder = normalBasePair.x;
                File baseFolder = normalBasePair.y;

                //dataset and algoritm are the same for both
                String identifier = getIdentifier(normalFolder);
                System.out.println("normalFolder.getAbsolutePath() = " + normalFolder.getAbsolutePath());
                System.out.println("identifier = " + identifier);
                List<File> normalFiles = getOutputFilesByNumber(normalFolder);
                List<File> baseFiles = getOutputFilesByNumber(baseFolder);

                if (normalFiles.isEmpty()) {
                    System.err.println("normalFiles.isEmpty() == true for " + identifier);
                    continue;
                }

                if (getAlgorithm(normalFolder).equals("Git")) {
                    System.err.println("Removing final normal treemap for GIT for consistency.");
                    //remove the last one for consistency

                    normalFiles.remove(normalFiles.size() - 1);
                }

                if (normalFiles.size() != (baseFiles.size() + 1)) {
                    System.err.println("The amount of baselines does not match up with the amount of normalfiles.");
                    System.err.println("normalFiles.size() = " + normalFiles.size());
                    System.err.println("baseFiles.size()+1" + (baseFiles.size() + 1));
                }

                ExperimentResult er = new ExperimentResult(normalFiles, baseFiles);
                er.algorithm = getAlgorithm(normalFolder);
                er.dataset = getDataSet(normalFolder);

                long currentTime = System.currentTimeMillis();
                long timeElapsed = currentTime - startTime;
                count++;

                double remaining = total - count;

                double timePerResult = ((double) timeElapsed) / count;

                bw.append(er.getResults());
                bw.append("\n");
                bw.flush();

                System.out.println("timeElapsed in seconds = " + timeElapsed / 1000);
                System.out.println("Estimated completion time in seconds = " + (remaining * timePerResult / 1000));
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(StatisticsParser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected boolean isBaselineFile(File folder) {
        return folder.getName().toLowerCase().startsWith("baseline");
    }

    protected String getAlgorithm(File f) {
        //returns the algorithm used
        return f.getParentFile().getName();
    }

    /**
     * Returns the dataset name. Removes baseline if needed.
     *
     * @param f
     * @return
     */
    protected String getDataSet(File f) {
        //returns the dataset used
        String name = f.getName();
        String trimmedName = name.replaceFirst("baseLine", "");
        trimmedName = trimmedName.replaceFirst("baseline", "");
        return trimmedName;
    }

    protected int getTmNumber(File f) {
        String name = f.getName();

        //find the value of the first integer.
        Matcher matcher = Pattern.compile("\\d+").matcher(name);
        matcher.find();
        int number = Integer.valueOf(matcher.group());

        return number;
    }

    private static Set<File> getSubFiles(File folder) {
        Set<File> returnFolder = new HashSet();
        if (!folder.isDirectory()) {
            returnFolder.add(folder);
            return returnFolder;
        }
        File[] folders = folder.listFiles();
        for (File f : folders) {
            returnFolder.addAll(getSubFiles(f));
        }
        return returnFolder;
    }

    private static Set<File> getLeafFolders(File folder) {
        Set<File> returnFolder = new HashSet();
        for (File f : folder.listFiles()) {

            if (f.isDirectory()) {
                Set<File> leafFolders = getLeafFolders(f);
                if (leafFolders.isEmpty()) {
                    //this was a leafFolder
                    returnFolder.add(f);
                } else {
                    returnFolder.addAll(leafFolders);
                }
            }
        }
        return returnFolder;
    }

    private List<Pair<File, File>> getNormalBasePairs(Set<File> leafFolders) {
        List<Pair<File, File>> pairList = new ArrayList();

        //dataset+algorithm to folder
        HashMap<String, File> fileMapping = new HashMap();
        //Normal file to baseline
        //get the normalfile ins
        for (File leafFolder : leafFolders) {
            boolean isBaseline = isBaselineFile(leafFolder);
            if (isBaseline) {
                continue;
            }

            String identifier = getIdentifier(leafFolder);
            fileMapping.put(identifier, leafFolder);

        }
        //get the baseline pairs
        for (File leafFolder : leafFolders) {
            boolean isBaseline = isBaselineFile(leafFolder);
            if (!isBaseline) {
                continue;
            }
            String identifier = getIdentifier(leafFolder);

            File originalFolder = fileMapping.get(identifier);
            Pair p = new Pair(originalFolder, leafFolder);
            pairList.add(p);
        }

        return pairList;
    }

    private List<File> getOutputFilesByNumber(File folder) {
        List<File> returnFolder = new ArrayList();
        File[] folders = folder.listFiles();
        for (File f : folders) {
            returnFolder.addAll(getSubFiles(f));
        }

        returnFolder.sort((File f1, File f2) -> Double.compare(getTmNumber(f1), getTmNumber(f2)));
        return returnFolder;
    }

    private String getIdentifier(File folder) {
        String dataSet = getDataSet(folder);
        if (dataSet.toLowerCase().startsWith("baseline")) {
            dataSet = dataSet.substring(8);
        }
        String algorithm = getAlgorithm(folder);
        String identifier = dataSet + "-" + algorithm;
        return identifier;
    }

    private void generateRugs(List<ExperimentResult> experimentResults, File datasetFolder) throws IOException {

        HashMap<String, List<ExperimentResult>> classificationSplits = splitOnClassification(experimentResults, datasetFolder);
//        HashMap<String, List<ExperimentResult>> classificationSplits = splitOnFacet(experimentResults, datasetFolder);
        System.out.println("Classifications split");

        for (VizVariable vizVariable : VizVariable.values()) {
            String outputFolderString = "../rugs/" + vizVariable.name();
            File f = new File(outputFolderString);
            f.mkdir();
        }

        for (String classificationName : classificationSplits.keySet()) {
            System.out.println("Generating rug for classification: " + classificationName);
            List<ExperimentResult> classificationResults = classificationSplits.get(classificationName);

            //loop over all metrics
            for (VizVariable vizVariable : VizVariable.values()) {
                System.out.println("Considering visual variable: " + vizVariable);
                try {
                    String header = "" + vizVariable.toString() + "-" + classificationName;
                    String outputFileString = "../rugs/" + vizVariable.name() + "/" + header + ".ipe";
                    RugGenerator rg = new RugGenerator(classificationResults, vizVariable, header, outputFileString);
                } catch (IOException ex) {
                    Logger.getLogger(StatisticsParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    private HashMap<String, Classification> getDatasetClassifications(File dataSetFile) throws IOException {

        if (dataSetFile.getName().endsWith(".csv")) {
            //already generated and specified output file, just use that
            return parseDatasetClassifications(dataSetFile);
        }

        DataSetClassifier dsc = new DataSetClassifier();
        List<Classification> classifiedDataSets = new ArrayList();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("classifications.csv")));
            System.out.println("classifying dataset");
            for (File dataFile : dataSetFile.listFiles()) {
                System.out.println("Classifying dataset " + dataFile.getAbsolutePath());
                DataFacilitator df = new DataFileManagerFast(dataFile, false);
                Classification classification = dsc.classifyDataSet(df);
                classifiedDataSets.add(classification);
                System.out.println("Classified dataset " + dataFile.getAbsolutePath());

                bw.append(dataFile.getAbsolutePath());
                bw.append(";");
                bw.append(classification.getCategoryString());
                bw.append("\n");
                bw.flush();
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(StatisticsParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("mapping datasets to classifications.");
        //maps a datasetname to a classification

        System.out.println("Done mapping");
        HashMap<String, Classification> dataSetClassification = new HashMap();

        for (Classification classification : classifiedDataSets) {
            dataSetClassification.put(classification.dataset, classification);
        }

        return dataSetClassification;
    }

    private HashMap<String, List<ExperimentResult>> splitOnClassification(List<ExperimentResult> experimentResults, File datasetFolder) throws IOException {
        HashMap<String, Classification> datasetClassification = getDatasetClassifications(datasetFolder);

        //remove datasets that are not fully generated for local moves
        removeIncompleteDatasetsAndExperimentResults(datasetClassification, experimentResults);

        List<String> categoryStrings = new ArrayList();

        for (Classification classification : datasetClassification.values()) {
            String categoryString = classification.getCategoryString();
            if (!categoryStrings.contains(categoryString)) {
                categoryStrings.add(categoryString);
            }
        }

        System.out.println("Mapping results to classification");
        HashMap<String, List<ExperimentResult>> classifications = new HashMap();
        //go through each facet of our data
        for (String categoryString : categoryStrings) {
            System.out.println("Mapping for classification " + categoryString);
            List<ExperimentResult> inClassification = new ArrayList();
            for (ExperimentResult er : experimentResults) {
                if (datasetClassification.get(er.dataset).getCategoryString().equals(categoryString)) {
                    //if this result is classified to the string
                    inClassification.add(er);
                    er.setClassificationString(categoryString);
                }
            }
            classifications.put(categoryString, inClassification);
        }
        //add one to the all classification
        List<ExperimentResult> allResults = new ArrayList();
        for (ExperimentResult er : experimentResults) {
            //if this result is classified to the string
            allResults.add(er);
        }
        classifications.put("CompleteResults", allResults);

        System.out.println("Done with classifications");
        return classifications;
    }

    private List<ExperimentResult> parseTreemapStatistics(File treemapStatisticsFile) throws IOException {
        List<ExperimentResult> results = new ArrayList();
        BufferedReader br = new BufferedReader(new FileReader(treemapStatisticsFile));
        String line;
        //skip header
        br.readLine();
        while ((line = br.readLine()) != null) {
            String[] split = line.split(";");
            String algo = split[0];
            String dataset = split[1];
            double meanMeanAR = Double.parseDouble(split[2]);
            double meanMedianAR = Double.parseDouble(split[3]);
            //lower values are betters. Everything calculates the instability
            double ssv = Double.parseDouble(split[4]);
            double ctd = Double.parseDouble(split[5]);
            double baseCtd = Double.parseDouble(split[6]);
            double baseSsv = Double.parseDouble(split[7]);

            ExperimentResult er = new ExperimentResult(algo, dataset, meanMeanAR, meanMedianAR, ssv, ctd, baseSsv, baseCtd);
            results.add(er);
        }
        return results;
    }

    private HashMap<String, Classification> parseDatasetClassifications(File dataSetFile) throws IOException {
        HashMap<String, Classification> results = new HashMap();
        BufferedReader br = new BufferedReader(new FileReader(dataSetFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split(";");
            String dataset = split[0];
            dataset = dataset.substring(dataset.lastIndexOf("\\") + 1);

            String depthCategory = split[1];
            String sizeVarianceCategory = split[2];
            String dataChangeCategory = split[3];
            String sizeChangeCategory = split[4];
//            int sharedNodeTimeStepCount = Integer.parseInt(split[5]);

            Classification c = new Classification(dataset, 0, 0, depthCategory, sizeVarianceCategory, dataChangeCategory, sizeChangeCategory);//, sharedNodeTimeStepCount);
            results.put(dataset, c);
        }
        return results;
    }

    private void removeIncompleteDatasetsAndExperimentResults(HashMap<String, Classification> datasetClassification, List<ExperimentResult> experimentResults) {
        HashMap<String, Integer> count = new HashMap();
        Set<String> toDelete = new HashSet();
        int countBefore = datasetClassification.keySet().size();

        int max = 0;
        for (ExperimentResult er : experimentResults) {
            //add one every time we get the dataset
            int newCount = count.getOrDefault(er.dataset, 0) + 1;
            count.put(er.dataset, newCount);
            max = Math.max(max, newCount);

            if (er.hasNaN()) {
                toDelete.add(er.dataset);
            }
        }
        System.out.println("Removed " + toDelete.size() + " extra datasets out of " + countBefore + " due to nAn");

        //max is equal to the amount of algorithms, as there is one dataset where all algorithms worked.
        for (String dataset : datasetClassification.keySet()) {
            if (!count.containsKey(dataset) || count.get(dataset) != max) {
                //either no data at all or no data for each algorithm.
                toDelete.add(dataset);

            }
        }
        for (String dataset : toDelete) {
            datasetClassification.remove(dataset);
        }
        System.out.println("Removed " + toDelete.size() + " extra datasets out of " + countBefore);

        Set<ExperimentResult> exToDelete = new HashSet();
        for (ExperimentResult er : experimentResults) {
            if (toDelete.contains(er.dataset)) {
                exToDelete.add(er);
            }
        }
        experimentResults.removeAll(exToDelete);
    }

    private Set<File> filterFolders(Set<File> leafFolders) {
        BufferedReader br = null;
        List<Pair<String, String>> todo = new ArrayList();
        try {
            System.err.println("Warning! Not doing all files");
            File f = new File("../rugs/filtered.csv");
            br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                String algo = line.split(";")[0];
                String dataset = line.split(";")[1];

                todo.add(new Pair(algo, dataset));
            }
        } catch (IOException ex) {
            Logger.getLogger(StatisticsParser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(StatisticsParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //alreadyDone contains the files we do not want to redo, only keep the others
        Set<File> newFolderList = new HashSet();
//        newFolderList.addAll(leafFolders); //used when we specify which we still want to do
        for (File f : leafFolders) {
            String algorithm = getAlgorithm(f);
            String dataSet = getDataSet(f);
            for (Pair<String, String> filter : todo) {
                if (filter.x.equals(algorithm) && filter.y.equals(dataSet)) {
                    //on the list to do
                    newFolderList.add(f);
                    
                    //Used when removingalready done it, so remove.
//                    newFolderList.remove(f);
                    break;
                }
            }
        }

        return newFolderList;
    }

    private void printClassificationsAmounts(List<ExperimentResult> experimentResults, File allDatasetsFolder) {
        System.out.println("printing classifications");

        HashSet<String> filteredDatasets = new HashSet();
        HashSet<String> unfilteredDatasets = new HashSet();

        try {
            HashMap<String, Classification> datasetClassifications = getDatasetClassifications(allDatasetsFolder);

            System.out.println("got classifications");
            Set<File> leafFolders = getSubFiles(allDatasetsFolder);
            for (File f : leafFolders) {
                String dataSet = getDataSet(f);
                unfilteredDatasets.add(dataSet);

            }
            System.out.println("got unfiltered datasets");
            for (ExperimentResult er : experimentResults) {
                filteredDatasets.add(er.dataset);
            }
            System.out.println("got filtereddatasets");
            System.out.println("filteredDatasets.size() = " + filteredDatasets.size());
            System.out.println("unfilteredDatasets.size() = " + unfilteredDatasets.size());
            //filtered datasets and unfiltered datasets now both contain the correct amount

            //count how many we have in each classification
            HashMap<String, Integer> filteredCountMap = new HashMap();
            HashMap<String, Integer> totalCountMap = new HashMap();
            for (String dataset : unfilteredDatasets) {
                Classification c = datasetClassifications.get(dataset);
                int newCount = totalCountMap.getOrDefault(c.getCategoryString(), 0) + 1;
                totalCountMap.put(c.getCategoryString(), newCount);
            }
            for (String dataset : filteredDatasets) {
                Classification c = datasetClassifications.get(dataset);
                int newCount = filteredCountMap.getOrDefault(c.getCategoryString(), 0) + 1;
                filteredCountMap.put(c.getCategoryString(), newCount);
            }

            for (String categoryString : totalCountMap.keySet()) {
                int totalCount = totalCountMap.get(categoryString);
                int filteredCount = filteredCountMap.getOrDefault(categoryString, 0);
                System.out.println(categoryString + " used to have " + totalCount + " and now has " + filteredCount);
            }

        } catch (Exception ex) {
            Logger.getLogger(StatisticsParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
