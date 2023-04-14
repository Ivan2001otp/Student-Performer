package com.ivan.reliant.Authentication

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.ivan.reliant.FirebaseService.AuthCredentialsCrud
import com.ivan.reliant.HomeActivity
import com.ivan.reliant.Model.Users
import com.ivan.reliant.R
import com.ivan.reliant.databinding.ActivityLogInBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.log2

class LogInActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLogInBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseRef : DatabaseReference
   // private val mainScope = MainScope()
    private lateinit var googleSignInClient:GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(R.string.default_web_client_id)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)


        binding.googlebtn.setOnClickListener {
            signInGoogle()
        }

        supportActionBar!!.hide()

        binding.signInBtn.setOnClickListener {
            val email:String = binding.emailEt.text.toString()
            val password :String = binding.passwordEt.text.toString()

            if(isConstrained(email,password)){
                initiateSignInProcess(mAuth,email,password)
            }else{
                Toast.makeText(this@LogInActivity,"Null credentials",Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.logInToSignInTv.setOnClickListener{
            val i = Intent(this@LogInActivity,SigninActivity::class.java)
            startActivity(i)
            finish()
        }


        binding.googlebtn.setOnClickListener {  }

    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->
        if(result.resultCode== Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if(task.isSuccessful){
            val account : GoogleSignInAccount?=task.result
            if(account!=null){
                updateUI(account)
            }
        }else{
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken,null)
        mAuth.signInWithCredential(credentials)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    val intent : Intent = Intent(this,HomeActivity::class.java)
                    intent.putExtra("email",account.displayName)
                    startActivity(intent)
                }else{

                }
            }

    }

    private fun initiateSignInProcess(mauth: FirebaseAuth, email: String, password: String) {
        mauth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this)
            { result ->
                if (result.isSuccessful) {
                    Toast.makeText(this,"success",Toast.LENGTH_SHORT)
                        .show()
                    val i = Intent(this@LogInActivity,HomeActivity::class.java)
                    val id:String = mauth.currentUser!!.uid
                    var u_name = ""

                    //read the username
                    databaseRef.addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                                u_name = snapshot.child(id).child("username").value.toString()
                            Log.w("tag","onDataChange: "+u_name)
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                    i.putExtra("username",u_name)
                    startActivity(i)
                    finishAffinity()
                }else if(result.isCanceled){
                    Toast.makeText(this,"canceled",Toast.LENGTH_SHORT)
                        .show()
                }else{
                    Toast.makeText(this,"weak internet connection",Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }



    private fun returnUser(username: String, email: String, password: String):Users {
        val u : Users = Users(username, email, password)
        return u
    }

    private fun isConstrained( email: String, password: String): Boolean {
        if( email.isBlank() or password.isBlank()){
            return false
        }
        return true
    }
}