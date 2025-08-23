package com.nhom5.healthtracking.util;

public class BMICal {
    public static double calculateBMI(double weightKg, double heightMeters) {
        return weightKg / (heightMeters * heightMeters);
    }

    /**
     * Calculate BMI with height in centimeters
     * @param weightKg Weight in kilograms
     * @param heightCm Height in centimeters
     * @return BMI value
     */
    public static double calculateBMIFromCm(double weightKg, double heightCm) {
        double heightMeters = heightCm / 100.0;
        return calculateBMI(weightKg, heightMeters);
    }

    public static String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Gầy";
        else if (bmi < 24.9) return "Bình thường";
        else return "Thừa cân";
    }
}