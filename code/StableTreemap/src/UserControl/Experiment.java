package UserControl;

import TreeMapGenerator.LocalMoves.LocalMoves;
import TreeMapGenerator.TreeMapGenerator;
import java.io.File;
import treemap.DataFaciliation.DataFacilitator;

/**
 *
 * @author Max Sondag
 */
public class Experiment {

    private TreeMapGenerator generator;
    private DataFacilitator df;
    private int maxTime;
    private SimulatorMaster simulator;
    private String datasetFolderOutput;

    /**
     * Simulates a treemap algorithm for timeSteps steps using df as the data
     * facialotor, generator as the algorithm
     *
     * @param df
     * @param generator
     * @param timeSteps
     */
    public Experiment(DataFacilitator df, TreeMapGenerator generator, int timeSteps, SimulatorMaster simulator, String datasetFolderOutput) {
        this.df = df;
        this.generator = generator;
        this.maxTime = timeSteps;
        this.simulator = simulator;
        this.datasetFolderOutput = datasetFolderOutput;

        simulator.setDataFacilitator(df);
        simulator.setTreeMapGenerator(generator);

        maxTime = timeSteps;
        if (df.hasMaxTime()) {
            maxTime = df.getMaxTime();
        }
    }

    public void runExperiment() {
        boolean skipped = false;
        for (int time = 0; time <= maxTime; time++) {

            File f = new File(datasetFolderOutput + "/t" + time + ".rect");
            if (f.exists()) {
                skipped = true;
                continue;
            }

            String dataGeneratorParamaters = df.getParamaterDescription();
            String algorithmName = generator.getClass().getSimpleName();
            String algorithmParamaters = generator.getParamaterDescription();

            if (skipped && df.getClass().equals(LocalMoves.class)) {
                continue;
                //local moves has to be redone from scratch, as I do not store the hierarchy.
            }
            String commandIdentifier = "experiment;" + df.getExperimentName() + ";" + dataGeneratorParamaters + ";" + algorithmName + ";" + algorithmParamaters + ";time=" + time;
            generateTreeMap(time, commandIdentifier);
        }

    }

    private void generateTreeMap(int time, String commandIdentifier) {
        simulator.getTreeMap(time, false, "experimentnoStability");
    }
}
