package com.ivan.reliant.Authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ivan.reliant.FirebaseService.AuthCredentialsCrud
import com.ivan.reliant.HomeActivity
import com.ivan.reliant.Model.Users
import com.ivan.reliant.R
import com.ivan.reliant.databinding.ActivityLogInBinding
import com.ivan.reliant.databinding.ActivitySigninBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SigninActivity : AppCompatActivity() {
    private lateinit var  binding:ActivitySigninBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseRef : DatabaseReference
    private val mainScope = MainScope()

    override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference

    }

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivitySigninBinding.inflate(layoutInflater)
            setContentView(binding.root)

            //hide the action bar
            supportActionBar!!.hide()

            binding.signInToLogInTv.setOnClickListener{
                val i = Intent(this@SigninActivity,LogInActivity::class.java)
                startActivity(i)
                finish()
            }

            binding.signInBtn.setOnClickListener {
                val username: String = binding.usernameEt.text.toString()
                val email: String = binding.emailEt.text.toString()
                val password: String = binding.passwordEt.text.toString()


                    if(isSafe(username,email,password)) {
                        Log.w("tag", "isSafe")
                        initiateSignInProcess(username,email,password)
                    }
                    else{
                        Toast.makeText(this@SigninActivity,"Incorrect credentials",Toast.LENGTH_SHORT)
                            .show()
                    }

            }
    }

    private fun initiateSignInProcess(username: String, email: String, password: String) {
        Log.w("tag", "initiateSignInProcess")

        mAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){
                if(it.isSuccessful){
                    val obj = getUserObject(username,email,password)
                    writeToFirebase(obj,mAuth.currentUser!!.uid)
                    val i = Intent(this@SigninActivity,HomeActivity::class.java)
                    i.putExtra("username",username)
                    Toast.makeText(this,"Success - sign",Toast.LENGTH_SHORT)
                        .show()
                    startActivity(i)
                    finishAffinity()
                }else{
                    Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun writeToFirebase(obj: Users, uid: String) {
        Log.w("tag", "writeToFirebase")
                AuthCredentialsCrud().writeToDb(obj, databaseRef,uid)
    }

    private fun getUserObject(username: String, email: String, password: String): Users {
       val obj = Users(username,email,password)
        return obj
    }

    private fun isSafe(username: String, email: String, password: String): Boolean {
        if(username.isBlank() or email.isBlank() or password.isBlank())return false
        return true
    }
}