package com.ivan.reliant.FirebaseService

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ivan.reliant.Model.Users

class AuthCredentialsCrud() {

     fun writeToDb(user: Users,reference: DatabaseReference,userId:String){
        Log.w("tag", "writeToDb")
           reference.child("USERS")
               .child(userId)
               .setValue(user)
    }
}