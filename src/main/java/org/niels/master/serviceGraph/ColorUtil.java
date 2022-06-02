package org.niels.master.serviceGraph;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ColorUtil {
    private static Map<String, String> colorsForHandling = new HashMap<>();

    public static String getColorForHandling(String handling) {
        if (colorsForHandling.containsKey(handling)) {
            return colorsForHandling.get(handling);
        }

        var generatedColor = generateRandomColor();

        colorsForHandling.put(handling, generatedColor);
        return generatedColor;
    }

    public static List<String> mColors = new LinkedList<String>(Arrays.asList(new String[]{
            "#39add1",
            "#3079ab",
            "#c25975",
            "#838cc7",
            "#7d669e",
            "#53bbb4",
            "#51b46d",
            "#e0ab18",
            "#f092b0",
            "#b7c0c7",
            "#7CFC00",
            "#87CEEB",
            "#DA70D6",
            "#FFEBCD",
            "#D2B48C",
            "#F0FFFF",
            "#DDA0DD",
            "#7FFFD4"
    }));

    private static Random randomGenerator = new Random();

    private static String generateRandomColor() {
        var selectedIndex = randomGenerator.nextInt(mColors.size());

        return mColors.remove(selectedIndex);
    }
}
