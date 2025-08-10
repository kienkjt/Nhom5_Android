package com.nhom5.healthtracking.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseModule {
  public static FirebaseAuth auth () {
    return FirebaseAuth.getInstance();
  }
  public static FirebaseFirestore db () {
    return FirebaseFirestore.getInstance();
  }
}
