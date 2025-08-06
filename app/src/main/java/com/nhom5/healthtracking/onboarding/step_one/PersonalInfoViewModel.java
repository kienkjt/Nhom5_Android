package com.nhom5.healthtracking.onboarding.step_one;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PersonalInfoViewModel extends ViewModel {
    
    private MutableLiveData<String> fullName = new MutableLiveData<>();
    private MutableLiveData<String> gender = new MutableLiveData<>();
    private MutableLiveData<String> dateOfBirth = new MutableLiveData<>();
    private MutableLiveData<Boolean> isFormValid = new MutableLiveData<>();
    
    public PersonalInfoViewModel() {
        isFormValid.setValue(false);
    }
    
    public void savePersonalInfo(String fullName, String gender, String dateOfBirth) {
        this.fullName.setValue(fullName);
        this.gender.setValue(gender);
        this.dateOfBirth.setValue(dateOfBirth);
        validateForm();
    }
    
    public void setFullName(String fullName) {
        this.fullName.setValue(fullName);
        validateForm();
    }
    
    public void setGender(String gender) {
        this.gender.setValue(gender);
        validateForm();
    }
    
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth.setValue(dateOfBirth);
        validateForm();
    }
    
    private void validateForm() {
        String name = fullName.getValue();
        String genderValue = gender.getValue();
        String dob = dateOfBirth.getValue();
        
        boolean isValid = name != null && !name.trim().isEmpty() &&
                         genderValue != null && !genderValue.trim().isEmpty() &&
                         dob != null && !dob.trim().isEmpty();
        
        isFormValid.setValue(isValid);
    }
    
    // Getters
    public MutableLiveData<String> getFullName() {
        return fullName;
    }
    
    public MutableLiveData<String> getGender() {
        return gender;
    }
    
    public MutableLiveData<String> getDateOfBirth() {
        return dateOfBirth;
    }
    
    public MutableLiveData<Boolean> getIsFormValid() {
        return isFormValid;
    }
}