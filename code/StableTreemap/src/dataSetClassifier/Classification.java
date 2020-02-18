/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataSetClassifier;

import java.util.List;

public class Classification {

    public String dataset;
    public int maxNodes;
    public int numberOfTimeSteps;
    public String depthCategory;
    public String sizeVarianceCategory;
    public String dataChangeCategory;
    public String sizeChangeCategory;
    public Integer sharedNodeTimeStepCount;//how many nodes

    public Classification(String dataset, int maxNodes, int numberOfTimeSteps, String depthCategory, String sizeVarianceCategory, String dataChangeCategory, String sizeChangeCategory) {
        this.dataset = dataset;
        this.maxNodes = maxNodes;
        this.numberOfTimeSteps = numberOfTimeSteps;
        this.depthCategory = depthCategory;
        this.sizeVarianceCategory = sizeVarianceCategory;
        this.dataChangeCategory = dataChangeCategory;
        this.sizeChangeCategory = sizeChangeCategory;
        this.sharedNodeTimeStepCount = null;
    }

    public Classification(String dataset, int maxNodes, int numberOfTimeSteps, String depthCategory, String sizeVarianceCategory, String dataChangeCategory, String sizeChangeCategory, int sharedNodeTimeStepCount) {
        this.dataset = dataset;
        this.maxNodes = maxNodes;
        this.numberOfTimeSteps = numberOfTimeSteps;
        this.depthCategory = depthCategory;
        this.sizeVarianceCategory = sizeVarianceCategory;
        this.dataChangeCategory = dataChangeCategory;
        this.sizeChangeCategory = sizeChangeCategory;
        this.sharedNodeTimeStepCount = sharedNodeTimeStepCount;
    }

    public void print(StringBuilder sb) {
        sb.append(dataset);
        sb.append(";").append(maxNodes);
        sb.append(";").append(numberOfTimeSteps);
        sb.append(";").append(depthCategory);
        sb.append(";").append(sizeVarianceCategory);
        sb.append(";").append(dataChangeCategory);
        sb.append(";").append(sizeChangeCategory);
        sb.append(";").append(sharedNodeTimeStepCount);
        sb.append(";" + "\r\n");
    }

    public String getCategoryString() {
        return "" + depthCategory + ";" + sizeVarianceCategory + ";" + dataChangeCategory + ";" + sizeChangeCategory;
    }

    public String getCategoryIDString() {
        return "" + getCategoryIDString() + ";" + getSizeVarianceCategoryId() + ";" + getDataChangeCategoryId() + ";" + getSizeVarianceCategoryId();
    }

    public int getDepthCategoryId() {
        if (depthCategory.equals("Single level")) {
            return 1;
        }
        if (depthCategory.equals("2 or 3 levels")) {
            return 2;
        }
        if (depthCategory.equals("4+ levels")) {
            return 3;
        }
        throw new IllegalStateException("Depthcategory " + depthCategory + " is not valid");
    }

    public int getSizeVarianceCategoryId() {
        if (sizeVarianceCategory.equals("High weight variance")) {
            return 1;
        }
        if (sizeVarianceCategory.equals("Low weight variance")) {
            return 2;
        }
        if (sizeVarianceCategory.equals("sizeCoefficient is exactly 1")) {
            return 3;
        }
        throw new IllegalStateException("SizeCategory " + sizeVarianceCategory + " is not valid");
    }

    public int getSizeChangeCategoryId() {
        if (sizeChangeCategory.equals("Low weight change")) {
            return 1;
        }
        if (sizeChangeCategory.equals("Regular weight change")) {
            return 2;
        }
        if (sizeChangeCategory.equals("Spiky weight change")) {
            return 3;
        }
        throw new IllegalStateException("SizeCategory " + sizeChangeCategory + " is not valid");
    }

    public int getDataChangeCategoryId() {
        if (dataChangeCategory.equals("Low insertions and deletions")) {
            return 1;
        }
        if (dataChangeCategory.equals("Regular insertions and deletions")) {
            return 2;
        }
        if (dataChangeCategory.equals("Spiky insertions and deletions")) {
            return 3;
        }
        throw new IllegalStateException("dataChange " + dataChangeCategory + " is not valid");
    }

}
