/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics.OutputFigures;

import UserControl.Visualiser.IpeExporter;
import treemap.dataStructure.Rectangle;

/**
 *
 * @author msondag
 */
public class OutputGrid {

    //Single column. Max width is 240. 24 is required from labels
//    public final double tableWidth = 240-24;
    //double column. Max width is 240. 24 is required from labels
    public final double tableWidth = 500 - 24;

    public final double horizontalSquareSize = tableWidth / 6 * 10; //*10 required as ipeexporter shrinks by default.

    public final double verticalSquareSize = horizontalSquareSize / 2;
    public final double spacing = 20;
    public final double horizontalGap = horizontalSquareSize + spacing;
    public final double verticalGap = verticalSquareSize + spacing;

    public OutputGrid() {

    }

    public String drawGridOutline() {
        //Add headers

        /*
        Format of table
                  | LWV|LWV|LWV|HWV|HWV|HWV|
                  | LWC|RWC|SWC|LWC|RWC|SWC|
        1L LID    |
        1L RID    |
        1L SID    |
        2/3L LID  |
        2/3L RID  |
        2/3L SID  |        
        4+L LID   |
        4+L RID   |
        4+L SID   |        
         */
        String outputString = "";
        double y = verticalGap * 9 / 10;
        outputString += IpeExporter.getString(horizontalGap * 1.5 / 10, y + 15, "LWV", "center");
        outputString += IpeExporter.getString(horizontalGap * 4.5 / 10, y + 15, "HWV", "center");
        for (double i = 0; i < 2; i++) {
            outputString += IpeExporter.getString(horizontalGap * (3 * i + 0.5) / 10, y + 5, "LWC", "center");
            outputString += IpeExporter.getString(horizontalGap * (3 * i + 1.5) / 10, y + 5, "RWC", "center");
            outputString += IpeExporter.getString(horizontalGap * (3 * i + 2.5) / 10, y + 5, "SWC", "center");
        }
        outputString += IpeExporter.getString(-15, verticalGap * 7.5 / 10, "\\rotatebox{90}{1L}", "right");
        outputString += IpeExporter.getString(-15, verticalGap * 4.5 / 10, "\\rotatebox{90}{2/3L}", "right");
        outputString += IpeExporter.getString(-15, verticalGap * 1.5 / 10, "\\rotatebox{90}{4+L}", "right");

        for (double i = 0; i < 3; i++) {
            outputString += IpeExporter.getString(-5, verticalGap * (3 * i + 0.5) / 10, "\\rotatebox{90}{SID}", "right");
            outputString += IpeExporter.getString(-5, verticalGap * (3 * i + 1.5) / 10, "\\rotatebox{90}{RID}", "right");
            outputString += IpeExporter.getString(-5, verticalGap * (3 * i + 2.5) / 10, "\\rotatebox{90}{LID}", "right");
        }

        //lines
        //y-axis
        double middleX = -13.5;
        outputString += IpeExporter.getLine(middleX, 0, middleX, verticalGap * 9 / 10, "fat");
        //thin lines
        for (double i = 0; i < 9; i++) {
            double lineY = verticalGap * i / 10;
            outputString += IpeExporter.getLine(middleX, lineY, middleX + 10, lineY, "heavier");
        }
        //thick lines
        for (double i = 0; i < 4; i++) {
            double lineY = verticalGap * i * 3 / 10;
            outputString += IpeExporter.getLine(middleX - 10, lineY, middleX + 10, lineY, "fat");
        }

        //x-axis
        double middleY = verticalGap * 9 / 10 + 10;
        outputString += IpeExporter.getLine(0, middleY, horizontalGap * 6 / 10, middleY, "fat");
        //thin lines
        for (double i = 0; i < 6; i++) {
            double lineX = horizontalGap * i / 10;
            outputString += IpeExporter.getLine(lineX, middleY, lineX, middleY - 10, "heavier");
        }
        //thick lines
        for (double i = 0; i < 3; i++) {
            double lineX = horizontalGap * 3 * i / 10;
            outputString += IpeExporter.getLine(lineX, middleY + 10, lineX, middleY - 10, "fat");
        }

        return outputString;
    }

    /**
     * Return the rectangle that is reserved for this equivalence class.
     *
     * @param classString
     * @return
     */
    public Rectangle getClassRectangle(String classString) {
        /*
        Format of table
                  | LWV|LWV|LWV|HWV|HWV|HWV|
                  | LWC|RWC|SWC|LWC|RWC|SWC|
        1L LID    |
        1L RID    |
        1L SID    |
        2/3L LID  |
        2/3L RID  |
        2/3L SID  |        
        4+L LID   |
        4+L RID   |
        4+L SID   |        
         */
        int xIndex = getXIndex(classString);
        int yIndex = getYIndex(classString);

        Rectangle r = new Rectangle(xIndex * horizontalGap, yIndex * verticalGap, horizontalSquareSize, verticalSquareSize);
        return r;

    }

    /**
     * Return the x index of the class string in the table
     *
     * @param classString
     * @return
     */
    private int getXIndex(String classString) {

        /*
        Format of table
                  | LWV|LWV|LWV|HWV|HWV|HWV|
                  | LWC|RWC|SWC|LWC|RWC|SWC|
        1L LID    |
        1L RID    |
        1L SID    |
        2/3L LID  |
        2/3L RID  |
        2/3L SID  |        
        4+L LID   |
        4+L RID   |
        4+L SID   |        
         */
        if (classString.contains("Low weight variance")) {
            if (classString.contains("Low weight change")) {
                return 0;
            }
            if (classString.contains("Regular weight change")) {
                return 1;
            }
            if (classString.contains("Spiky weight change")) {
                return 2;
            }
        }
        if (classString.contains("High weight variance")) {
            if (classString.contains("Low weight change")) {
                return 3;
            }
            if (classString.contains("Regular weight change")) {
                return 4;
            }
            if (classString.contains("Spiky weight change")) {
                return 5;
            }
        }
        return -1;
    }

    /**
     * Returns the y index of the classString in the table
     *
     * @param classString
     * @return
     */
    private int getYIndex(String classString) {
        /*
        Format of table
                  | LWV|LWV|LWV|HWV|HWV|HWV|
                  | LWC|RWC|SWC|LWC|RWC|SWC|
        1L LID    |
        1L RID    |
        1L SID    |
        2/3L LID  |
        2/3L RID  |
        2/3L SID  |        
        4+L LID   |
        4+L RID   |
        4+L SID   |        
         */

        //y needs to be inverted, as origin is at left bottom.
        if (classString.contains("Single level")) {
            if (classString.contains("Low insertions and deletions")) {
                return 8;
            }
            if (classString.contains("Regular insertions and deletions")) {
                return 7;
            }
            if (classString.contains("Spiky insertions and deletions")) {
                return 6;
            }
        }
        if (classString.contains("2 or 3 levels")) {
            if (classString.contains("Low insertions and deletions")) {
                return 5;
            }
            if (classString.contains("Regular insertions and deletions")) {
                return 4;
            }
            if (classString.contains("Spiky insertions and deletions")) {
                return 3;
            }
        }
        if (classString.contains("4+ levels")) {
            if (classString.contains("Low insertions and deletions")) {
                return 2;
            }
            if (classString.contains("Regular insertions and deletions")) {
                return 1;
            }
            if (classString.contains("Spiky insertions and deletions")) {
                return 0;
            }
        }
        return -1;
    }

}
