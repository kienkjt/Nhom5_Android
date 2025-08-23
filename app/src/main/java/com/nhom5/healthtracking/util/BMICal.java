package com.nhom5.healthtracking.util;

public class BMICal {
    public static double calculateBMI(double weightKg, double heightMeters) {
        return weightKg / (heightMeters * heightMeters);
    }

    public static String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Gầy";
        else if (bmi < 24.9) return "Bình thường";
        else return "Thừa cân";
    }
}