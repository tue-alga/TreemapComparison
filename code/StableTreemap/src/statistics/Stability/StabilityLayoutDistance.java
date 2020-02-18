package statistics.Stability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author max
 */
public class StabilityLayoutDistance {

    public double getBaselineStability(TreeMap oldTm, TreeMap newTm, TreeMap baseTm) {

        List<TreeMap> oldRemaining = getOldRemainingLeafs(oldTm, newTm);

        //holds the stability score in the end. It is composed of the average stability
        //score of all items
        double stability = 0;

        HashMap<String, TreeMap> newMapping = new HashMap();
        for (TreeMap tmNew : newTm.getAllChildren()) {
            newMapping.put(tmNew.getLabel(), tmNew);
        }
        HashMap<String, TreeMap> baseLineMapping = new HashMap();
        for (TreeMap tmBase : baseTm.getAllLeafs()) {
            baseLineMapping.put(tmBase.getLabel(), tmBase);
        }

        for (TreeMap tmOld : oldRemaining) {
            TreeMap tmNew = newMapping.get(tmOld.getLabel());
            TreeMap tmBase = baseLineMapping.get(tmOld.getLabel());

            double distanceScoreNew = getDistanceScore(tmOld, tmNew);
            double distanceScoreBase = getDistanceScore(tmOld, tmBase);

            stability += Math.max(0, distanceScoreNew - distanceScoreBase);
        }
        //Normalize the stability score
        stability = stability / oldRemaining.size();

        return stability;
    }

    public double getStability(TreeMap oldTm, TreeMap newTm) {

        List<TreeMap> oldRemaining = getOldRemainingLeafs(oldTm, newTm);

        //holds the stability score in the end. It is composed of the average stability
        //score of all items
        double stability = 0;

        HashMap<String, TreeMap> newMapping = new HashMap();
        for (TreeMap tmNew : newTm.getAllLeafs()) {
            newMapping.put(tmNew.getLabel(), tmNew);
        }

        for (TreeMap tmOld : oldRemaining) {
            TreeMap tmNew = newMapping.get(tmOld.getLabel());
            double distanceScore = getDistanceScore(tmOld, tmNew);
            stability += distanceScore;
        }
        //Normalize the stability score
        stability = stability / oldRemaining.size();

        return stability;
    }

    private double getDistanceScore(TreeMap tm1, TreeMap tm2) {
        Rectangle rOld = tm1.getRectangle();
        Rectangle rNew = tm2.getRectangle();
        //Holds the sum of all individual stability scores
        double distanceScore = Math.sqrt(Math.pow(rOld.getX() - rNew.getX(), 2)
                                         + Math.pow(rOld.getY() - rNew.getY(), 2)
                                         + Math.pow(rOld.getWidth() - rNew.getWidth(), 2)
                                         + Math.pow(rOld.getHeight() - rNew.getHeight(), 2)
        );
        return distanceScore;
    }

    private List<TreeMap> getOldRemainingLeafs(TreeMap oldTm, TreeMap newTm) {
        List<TreeMap> remainingLeafs = new ArrayList();
        Set<String> labelsInNew = new HashSet();
        for (TreeMap tm : newTm.getAllLeafs()) {
            labelsInNew.add(tm.getLabel());
        }

        for (TreeMap tm : oldTm.getAllLeafs()) {
            if (labelsInNew.contains(tm.getLabel())) {
                //tm was present in both
                remainingLeafs.add(tm);
            }
        }
        return remainingLeafs;
    }
}
