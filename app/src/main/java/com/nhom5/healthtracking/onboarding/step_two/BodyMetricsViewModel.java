package com.nhom5.healthtracking.onboarding.step_two;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BodyMetricsViewModel extends ViewModel {
    
    private MutableLiveData<String> height = new MutableLiveData<>();
    private MutableLiveData<String> weight = new MutableLiveData<>();
    private MutableLiveData<Double> bmiValue = new MutableLiveData<>();
    private MutableLiveData<String> bmiCategory = new MutableLiveData<>();
    private MutableLiveData<Boolean> isFormValid = new MutableLiveData<>();
    
    public BodyMetricsViewModel() {
        isFormValid.setValue(false);
    }
    
    public void saveBodyMetrics(String height, String weight) {
        this.height.setValue(height);
        this.weight.setValue(weight);
        calculateBMI(height, weight);
        validateForm();
    }
    
    public void setHeight(String height) {
        this.height.setValue(height);
        String currentWeight = weight.getValue();
        if (currentWeight != null && !currentWeight.isEmpty()) {
            calculateBMI(height, currentWeight);
        }
        validateForm();
    }
    
    public void setWeight(String weight) {
        this.weight.setValue(weight);
        String currentHeight = height.getValue();
        if (currentHeight != null && !currentHeight.isEmpty()) {
            calculateBMI(currentHeight, weight);
        }
        validateForm();
    }
    
    private void calculateBMI(String heightStr, String weightStr) {
        try {
            if (heightStr != null && !heightStr.trim().isEmpty() && 
                weightStr != null && !weightStr.trim().isEmpty()) {
                
                double heightValue = Double.parseDouble(heightStr.trim());
                double weightValue = Double.parseDouble(weightStr.trim());
                
                if (heightValue > 0 && weightValue > 0) {
                    // Convert height from cm to meters
                    double heightInMeters = heightValue / 100.0;
                    double bmi = weightValue / (heightInMeters * heightInMeters);
                    
                    bmiValue.setValue(Math.round(bmi * 10.0) / 10.0); // Round to 1 decimal place
                    bmiCategory.setValue(getBMICategory(bmi));
                    return;
                }
            }
        } catch (NumberFormatException e) {
            // Invalid number format
        }
        
        // Clear BMI if calculation fails
        bmiValue.setValue(null);
        bmiCategory.setValue(null);
    }
    
    private String getBMICategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi < 25.0) {
            return "Normal weight";
        } else if (bmi < 30.0) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }
    
    private void validateForm() {
        String heightValue = height.getValue();
        String weightValue = weight.getValue();
        
        boolean isValid = heightValue != null && !heightValue.trim().isEmpty() &&
                         weightValue != null && !weightValue.trim().isEmpty();
        
        if (isValid) {
            try {
                double h = Double.parseDouble(heightValue.trim());
                double w = Double.parseDouble(weightValue.trim());
                isValid = h > 0 && w > 0 && h >= 50 && h <= 250 && w >= 20 && w <= 300;
            } catch (NumberFormatException e) {
                isValid = false;
            }
        }
        
        isFormValid.setValue(isValid);
    }
    
    public boolean isValidHeight(String heightStr) {
        try {
            if (heightStr == null || heightStr.trim().isEmpty()) {
                return false;
            }
            double height = Double.parseDouble(heightStr.trim());
            return height >= 50 && height <= 250;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public boolean isValidWeight(String weightStr) {
        try {
            if (weightStr == null || weightStr.trim().isEmpty()) {
                return false;
            }
            double weight = Double.parseDouble(weightStr.trim());
            return weight >= 20 && weight <= 300;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // Getters
    public MutableLiveData<String> getHeight() {
        return height;
    }
    
    public MutableLiveData<String> getWeight() {
        return weight;
    }
    
    public MutableLiveData<Double> getBmiValue() {
        return bmiValue;
    }
    
    public MutableLiveData<String> getBmiCategory() {
        return bmiCategory;
    }
    
    public MutableLiveData<Boolean> getIsFormValid() {
        return isFormValid;
    }
}