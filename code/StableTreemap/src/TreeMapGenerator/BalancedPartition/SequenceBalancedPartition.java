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
public class SequenceBalancedPartition extends BalancedPartition {

    @Override
    protected List<DataMap> getSortedDataMaps(List<DataMap> dataMaps) {
        //return as is. No sorting should be done.
        return dataMaps;
    }

    @Override
    public String getSimpleName() {
        return "SequenceBalancedPartition";
    }
}
