/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TreeMapGenerator.BalancedPartition;

import java.util.List;
import treemap.dataStructure.DataMap;

/**
 *
 * @author msondag
 */
public class NumberBalancedPartition extends BalancedPartition {

    @Override
    protected boolean checkDone(List<DataMap> list1, List<DataMap> list2) {
        //stopping if list1 becomes bigger than list2 after splitting
        return list1.size() + 2 > list2.size();
    }

    @Override
    protected List<DataMap> getSortedDataMaps(List<DataMap> dataMaps) {
        //return as is. No sorting should be done.
        return dataMaps;
    }

    @Override
    public String getSimpleName() {
        return "NumberBalancedPartition";
    }

}
