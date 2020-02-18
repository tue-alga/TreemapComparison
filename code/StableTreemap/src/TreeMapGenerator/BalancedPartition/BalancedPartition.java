package TreeMapGenerator.BalancedPartition;

import TreeMapGenerator.TreeMapGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;
import utility.Pair;

/**
 *
 * @author max
 */
public abstract class BalancedPartition implements TreeMapGenerator {

    @Override
    public TreeMap generateTreeMap(DataMap dataMap, Rectangle inputR) {
        TreeMap returnTreeMap;
        if (!dataMap.hasChildren()) {
            //base case, we do not have to recurse anymore
            returnTreeMap = new TreeMap(inputR, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(), null);
            return returnTreeMap;
        }

        List<DataMap> children = new ArrayList<>();
        children.addAll(dataMap.getChildren());

        //generate the rectangle positions for each child
        Map<DataMap, Rectangle> mapping = generateLevel(children, inputR);
        //recursively go through the children to generate all treemaps
        List<TreeMap> treeChildren = new ArrayList();
        for (DataMap dm : mapping.keySet()) {
            TreeMap tm = generateTreeMap(dm, mapping.get(dm));
            treeChildren.add(tm);
        }

        returnTreeMap = new TreeMap(inputR, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(), treeChildren);
        return returnTreeMap;
    }

    /**
     * DataMap is sorted on size
     *
     * @param inputDataMaps
     * @param inputR
     * @return
     */
    private Map<DataMap, Rectangle> generateLevel(List<DataMap> inputDataMaps, Rectangle inputR) {

        //sorting method is different for different algorithms
        List<DataMap> dataMaps = getSortedDataMaps(inputDataMaps);

        List<DataMap> list1 = new ArrayList();
        List<DataMap> list2 = new ArrayList();

        list2.addAll(dataMaps);

        //need at least 1 element in both lists
        list1.add(list2.get(0));
        list2.remove(0);

        while (!checkDone(list1, list2)) {
            list1.add(list2.get(0));
            list2.remove(0);
        }

        //We now have the two lists of elements we are spliting in. 
        //We now distribute them over 2 subRectangle
        Pair<Rectangle, Rectangle> splitRectangles = getSplitRectangles(inputR, list1, list2);
        Rectangle r1 = splitRectangles.x;
        Rectangle r2 = splitRectangles.y;

        //recursively map the rectangles. If the size of the list equals 1
        //we are in the basecase and the mapping is known
        Map<DataMap, Rectangle> mapping = new HashMap();
        if (list1.size() == 1) {
            mapping.put(list1.get(0), r1);
        } else if (list1.size() > 1) {
            mapping.putAll(generateLevel(list1, r1));
        }

        if (list2.size() == 1) {
            mapping.put(list2.get(0), r2);
        } else if (list2.size() > 1) {
            mapping.putAll(generateLevel(list2, r2));
        }

        return mapping;
    }

    @Override
    public String getParamaterDescription() {
        return "";
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return this;
    }

    protected boolean checkDone(List<DataMap> list1, List<DataMap> list2) {
        //done if swapping one more element from list2 to list1 makes list1 bigger.
        if (list2.isEmpty()) {
            return true;
        }

        return DataMap.getTotalSize(list1) + 2 * list2.get(0).getTargetSize() > DataMap.getTotalSize(list2);
    }

    protected Pair<Rectangle, Rectangle> getSplitRectangles(Rectangle inputR, List<DataMap> list1, List<DataMap> list2) {
        //split based on the input rectangle.
        Rectangle r1, r2;
        double totalSize = DataMap.getTotalSize(list1) + DataMap.getTotalSize(list2);
        double lengthPercentageR1 = DataMap.getTotalSize(list1) / totalSize;
        double x1 = inputR.getX();
        double y1 = inputR.getY();
        double height = inputR.getHeight();
        double width = inputR.getWidth();

        if (inputR.getHeight() >= inputR.getWidth()) {
            r1 = new Rectangle(x1, y1, width, lengthPercentageR1 * height);
            r2 = new Rectangle(x1, y1 + lengthPercentageR1 * height, width, height - lengthPercentageR1 * height);
        } else {
            r1 = new Rectangle(x1, y1, lengthPercentageR1 * width, height);
            r2 = new Rectangle(x1 + lengthPercentageR1 * width, y1, width - lengthPercentageR1 * width, height);
        }
        return new Pair(r1, r2);
    }

    protected List<DataMap> getSortedDataMaps(List<DataMap> dataMaps) {
        //make a copy to keep the input list in the correct order.
        List<DataMap> outputMap = new ArrayList(dataMaps);
        Collections.sort(outputMap, (DataMap o1, DataMap o2) -> Double.compare(o2.getTargetSize(), o1.getTargetSize()));
        return outputMap;
    }

}
