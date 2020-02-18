/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author MaxSondag
 */
public class CornerTravelDistance {

    public double getBaselineInstability(TreeMap oldTm, TreeMap newTm, TreeMap baseTm) {
        return getBaselineInstability(oldTm, newTm, baseTm, null);
    }

    public double getBaselineInstability(TreeMap oldTm, TreeMap newTm, TreeMap baseTm, StringBuilder sb) {

        List<TreeMap> oldRemaining = getOldRemainingLeafs(oldTm, newTm);

        if (oldRemaining.isEmpty()) {
            return 0;
        }

        //holds the stability score in the end.
        double stability = 0;

        //4*sqrt(w^2+h^2) of input rectangle
        double normalize = 4 * Math.sqrt(Math.pow(oldTm.getRectangle().getWidth(), 2) + Math.pow(oldTm.getRectangle().getHeight(), 2));

        HashMap<String, TreeMap> newMapping = new HashMap();
        for (TreeMap tmNew : newTm.getAllLeafs()) {
            newMapping.put(tmNew.getLabel(), tmNew);
        }

        HashMap<String, TreeMap> baseMapping = new HashMap();
        for (TreeMap tmBase : baseTm.getAllLeafs()) {
            baseMapping.put(tmBase.getLabel(), tmBase);
        }

        for (TreeMap tmOld : oldRemaining) {
            TreeMap tmNew = newMapping.get(tmOld.getLabel());
            TreeMap tmBase = baseMapping.get(tmOld.getLabel());
            //normalize score base on input rectangle.
            double distanceScoreNew = getDistanceScore(tmOld, tmNew) / normalize;
            double distanceScoreBase = getDistanceScore(tmOld, tmBase) / normalize;

//            stability += distanceScoreBase; //used to visualise the comparison between scores
            stability += Math.max(0, distanceScoreNew - distanceScoreBase);
        }
        //Normalize the stability score wbase on size
        stability = stability / oldRemaining.size();

        return stability;
    }

    public double getInstability(TreeMap oldTm, TreeMap newTm) {

        List<TreeMap> oldRemaining = getOldRemainingLeafs(oldTm, newTm);

        if (oldRemaining.isEmpty()) {
            return 0;
        }

        //holds the stability score in the end. It is composed of the average stability
        //score of all items
        double stability = 0;

        //4*sqrt(w^2+h^2) of input rectangle
        double normalize = 4 * Math.sqrt(Math.pow(oldTm.getRectangle().getWidth(), 2) + Math.pow(oldTm.getRectangle().getHeight(), 2));

        HashMap<String, TreeMap> newMapping = new HashMap();
        for (TreeMap tmNew : newTm.getAllLeafs()) {
            newMapping.put(tmNew.getLabel(), tmNew);
        }

        for (TreeMap tmOld : oldRemaining) {
            TreeMap tmNew = newMapping.get(tmOld.getLabel());
            if (tmNew == null) {
                System.out.println("");
            }
            //normalize score base on input rectangle.
            double distanceScore = getDistanceScore(tmOld, tmNew) / normalize;
            stability += distanceScore;
        }
        //Normalize the stability score wbase on size
        stability = stability / oldRemaining.size();

        return stability;
    }

    private double getDistanceScore(TreeMap tm1, TreeMap tm2) {
        Rectangle r1 = tm1.getRectangle();
        Rectangle r2 = tm2.getRectangle();
        //Holds the sum of all individual stability scores
        double distanceScore = Math.abs(r1.getX() - r2.getX())
                               + Math.abs(r1.getY() - r2.getY())
                               + Math.abs(r1.getX2() - r2.getX2())
                               + Math.abs(r1.getY2() - r2.getY2()); 
        distanceScore *=2;//need to determine how much each of the corners traveled, this is equivalent under l1 distance
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
