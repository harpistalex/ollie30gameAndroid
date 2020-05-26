package com.alexsamazingapps.ollie30gameandroid.Controller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.alexsamazingapps.ollie30gameandroid.Model.ProgressSpinner
import com.alexsamazingapps.ollie30gameandroid.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_login.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener


class LoginActivity : AppCompatActivity() {

    //1. Declare instance of FirebaseAuth:
    private lateinit var auth: FirebaseAuth
    //Other Global Variables:
    val firebaseRef = FirebaseDatabase.getInstance()
    val gson = GsonBuilder().create()
    var loginUIBool : Boolean = false //False means registerUI is showing. True means loginUI is showing.
    val progressSpinner = ProgressSpinner()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //2. Initialise FirebaseAuth instance:
        auth = FirebaseAuth.getInstance()

        //TESTING:
        val defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        println("DEFAULTSHAREDPREF: ${defaultSharedPref.all}")
        /////////////////

        //TEST BUTTONS:
        button2.visibility = View.INVISIBLE
        button3.visibility = View.INVISIBLE
        /////////////////

    }

    //3. Check to see if use is currently signed in:
    public override fun onStart() {
        super.onStart()

        //THIS CHECKS FIREBASE:
        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.getCurrentUser()
//        if (currentUser != null) {
//            val gameActivity = Intent(this, GameActivity::class.java)
//            startActivity(gameActivity)
//            finish()
//        } else {
//            println("Not logged in")
//        }

        //3.1 Check sharedPref for logged in boolean:
        val defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        val logInStatus = defaultSharedPref.getBoolean("LoggedIn", false)

        if (logInStatus == true) {
            print("logged in")
            val gameActivity = Intent(this, GameActivity::class.java)
            startActivity(gameActivity)
            finish()
        }
        else {
            print("not logged in")
        }

        val alreadyRegistered = defaultSharedPref.getBoolean("AlreadyRegistered", false)
        if (alreadyRegistered == true) {
            loginUI()
        } else {
            registerUI()
        }

    }

    //4.Create account:
    fun registerButtonClicked(view: View) {

        var username = usernameText.text.toString()
        var email = emailText.text.toString()
        var password = passwordText.text.toString()
        var retypePassword = retypePassword.text.toString()
        var code = codeText.text.toString()
        val TAG = "LOG:"

        if (username == "" || email == "" || password == "" || username == "") {
            Toast.makeText(baseContext, "Please enter all fields.",
                Toast.LENGTH_SHORT).show()
            return
        }

        if (password != retypePassword) {
            Toast.makeText(baseContext, "Passwords do not match.",
                Toast.LENGTH_SHORT).show()
            return
        }

        if (code == "3.14159") {

            progressSpinner.show(indeterminateBarGame, getWindow())

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")

                        // Create Firebase user data:
                        val uid = auth.currentUser?.uid
                        var firebaseUserDataMap = hashMapOf("email" to auth.currentUser?.email, "username" to username)
                        firebaseRef.reference.child(uid!!).setValue(firebaseUserDataMap)

                        val jsonAnswersPicked = gson.toJson(MutableList(30) { 0 })
                        val defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        with(defaultSharedPref.edit()) {
                            putBoolean("LoggedIn", true)
                            commit()
                        }
                        with(defaultSharedPref.edit()) {
                            putBoolean("Finished", false)
                            commit()
                        }
                        with(defaultSharedPref.edit()) {
                            putString("email", auth.currentUser?.email)
                            commit()
                        }
                        with(defaultSharedPref.edit()) {
                            putString("uid", uid)
                            commit()
                        }
                        with(defaultSharedPref.edit()) {
                            putInt("QuestionNumber", 0)
                            commit()
                        }
                        with(defaultSharedPref.edit()) {
                            putInt("Score", 0)
                            commit()
                        }
                        with(defaultSharedPref.edit()) {
                            putString("AnswersPicked", jsonAnswersPicked)
                            commit()
                        }
                        with(defaultSharedPref.edit()) {
                            putBoolean("AlreadyRegistered", true)
                            commit()
                        }
                        with(defaultSharedPref.edit()) {
                            putString("Username", username)
                            commit()
                        }

                        val gameActivity = Intent(this, GameActivity::class.java)
                        startActivity(gameActivity)
                        finish()
                        progressSpinner.hide(indeterminateBarGame, getWindow())
                    } else {
                        // If register in fails, display a message to the user.
                        progressSpinner.hide(indeterminateBarGame, getWindow())
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

        } else {
            Toast.makeText(baseContext, "Incorrect code", Toast.LENGTH_SHORT).show()
        }

    }

    fun loginButtonClicked(view: View) {

        var email = emailText.text.toString()
        var password = passwordText.text.toString()
        val TAG = "LOG:"

        if (email == "" || password == "") {
            Toast.makeText(baseContext, "Please enter both fields.",
                Toast.LENGTH_SHORT).show()
            return
        }

        progressSpinner.show(indeterminateBarGame, getWindow())

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    // Saving first data to sharedPref:
                    val uid = auth.currentUser?.uid
                    val defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    with (defaultSharedPref.edit()) {
                        putBoolean("LoggedIn", true)
                        commit()
                    }
                    with (defaultSharedPref.edit()) {
                        putString("email", auth.currentUser?.email)
                        commit()
                    }
                    with (defaultSharedPref.edit()) {
                        putString("uid", uid)
                        commit()
                    }

                    //Data from Firebase to SharedPref:
                    val dataListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Get Post object and use the values to update the UI
                            val FBDataMap = dataSnapshot.value as Map<String, Any>
                            println("Firebase data = ${FBDataMap.values}")

                            val finishedFB = FBDataMap["Finished"]
                            when {
                                finishedFB is Boolean ->
                                    with(defaultSharedPref.edit()) {
                                        putBoolean("Finished", finishedFB)
                                        commit()
                                    }
                            }
                            var questionNumberFBInt = 0
                            val questionNumberFB = FBDataMap["QuestionNumber"]
                            when {
                                questionNumberFB is Long ->
                                    questionNumberFBInt = questionNumberFB.toInt()
                            }
                            with(defaultSharedPref.edit()) {
                                putInt("QuestionNumber", questionNumberFBInt)
                                commit()
                            }

                            var scoreFBInt = 0
                            val scoreFB = FBDataMap["Score"]
                            when {
                                scoreFB is Long ->
                                    scoreFBInt = scoreFB.toInt()
                            }
                            with(defaultSharedPref.edit()) {
                                putInt("Score", scoreFBInt)
                                commit()
                            }

                            var answersPickedFBJson = ""
                            val answersPickedFB = FBDataMap["AnswersPicked"]
                            when {
                                answersPickedFB is MutableList<*> ->
                                    answersPickedFBJson = gson.toJson(answersPickedFB)
                            }
                            with(defaultSharedPref.edit()) {
                                putString("AnswersPicked", answersPickedFBJson)
                                commit()
                            }

                            //SEGUE TO GAME:
                            val gameActivity = Intent(this@LoginActivity, GameActivity::class.java)
                            startActivity(gameActivity)
                            progressSpinner.hide(indeterminateBarGame, getWindow())

                            println("1---------------------Finshed is: ${defaultSharedPref.getBoolean("Finished", true)}")
                            println("1---------------------AnswersPicked Json is: ${defaultSharedPref.getString("AnswersPicked", "")}")

                        }

                        //ERROR HANDLING FOR GETTING DATA:
                        override fun onCancelled(databaseError: DatabaseError) {
                            progressSpinner.hide(indeterminateBarGame, getWindow())
                            // Getting Post failed, log a message
                            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                        }
                    }
                    firebaseRef.reference.child(uid!!).addListenerForSingleValueEvent(dataListener)


                } else {
                    // If sign in fails, display a message to the user.
                    progressSpinner.hide(indeterminateBarGame, getWindow())
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }

            }

    }

    fun loginOrRegisterClicked(view: View) {
        if (loginUIBool == false) {
            loginUI()
        } else {
            registerUI()
        }
    }


    fun resetPassword(view: View) {

        var email = emailText.text.toString()
        val TAG = "LOG:"

        if (email == "") {
            Toast.makeText(baseContext, "Please enter your email address.",
                Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                    Toast.makeText(baseContext, "Email sent.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun loginUI() {
        usernameText.visibility = View.INVISIBLE
        codeText.visibility = View.INVISIBLE
        registerButton.visibility = View.INVISIBLE
        retypePassword.visibility = View.INVISIBLE
        loginButton.visibility = View.VISIBLE
        resetPasswordButton.visibility = View.VISIBLE
        loginOrRegisterButton.text = "Need to register? Create account."
        loginUIBool = true

    }


    fun registerUI() {
        usernameText.visibility = View.VISIBLE
        codeText.visibility = View.VISIBLE
        registerButton.visibility = View.VISIBLE
        retypePassword.visibility = View.VISIBLE
        loginButton.visibility = View.INVISIBLE
        resetPasswordButton.visibility = View.INVISIBLE
        loginOrRegisterButton.text = "Already have an account? Log in."
        loginUIBool = false

    }

/////////////////////////////////////////////////////////////////////////////////
    //TESTING:
//    fun tempButtonClicked(view: View) {
//
//        //LOGIN:
//        var email = "android@android.com"
//        var password = "123456"
//        val TAG = "LOG:"
//
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithEmail:success")
//                    // Saving first data to sharedPref:
//                    val uid = auth.currentUser?.uid
//                    val sharedPref = this.getPreferences(MODE_PRIVATE)
//                    with (sharedPref.edit()) {
//                        putBoolean("LoggedIn", true)
//                        commit()
//                    }
//                    with (sharedPref.edit()) {
//                        putString("email", auth.currentUser?.email)
//                        commit()
//                    }
//                    with (sharedPref.edit()) {
//                        putString("uid", uid)
//                        commit()
//                    }
//
//                    //Data from Firebase to SharedPref:
//                    println("------------------I AM LOGGED IN------------------")
//
//                    val dataListener = object : ValueEventListener {
//                        override fun onDataChange(dataSnapshot: DataSnapshot) {
//                            // Get Post object and use the values to update the UI
//                            val FBDataMap = dataSnapshot.value as Map<String, Any>
//                            println("Firebase data = ${FBDataMap.values}")
//
//                            val finishedFB = FBDataMap["Finished"]
//                            when {
//                                finishedFB is Boolean ->
//                                    with(sharedPref.edit()) {
//                                        putBoolean("LoggedIn", finishedFB)
//                                        commit()
//                                    }
//                            }
//                            var questionNumberFBInt = 0
//                            val questionNumberFB = FBDataMap["QuestionNumber"]
//                            when {
//                                questionNumberFB is Long ->
//                                    questionNumberFBInt = questionNumberFB.toInt()
//                            }
//                            with(sharedPref.edit()) {
//                                putInt("QuestionNumber", questionNumberFBInt)
//                                commit()
//                            }
//
//                            var scoreFBInt = 0
//                            val scoreFB = FBDataMap["Score"]
//                            when {
//                                scoreFB is Long ->
//                                    scoreFBInt = scoreFB.toInt()
//                            }
//                            with(sharedPref.edit()) {
//                                putInt("Score", scoreFBInt)
//                                commit()
//                            }
//
//                            var answersPickedFBJson = ""
//                            val answersPickedFB = FBDataMap["AnswersPicked"]
//                            when {
//                                answersPickedFB is MutableList<*> ->
//                                    answersPickedFBJson = gson.toJson(answersPickedFB)
//                            }
//                            with(sharedPref.edit()) {
//                                putString("AnswersPicked", answersPickedFBJson)
//                                commit()
//                            }
//
//
//
//                        }
//
//                        //ERROR HANDLING FOR GETTING DATA:
//                        override fun onCancelled(databaseError: DatabaseError) {
//                            // Getting Post failed, log a message
//                            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//                            // ...
//                        }
//                    }
//                    firebaseRef.reference.child(uid!!).addListenerForSingleValueEvent(dataListener)
//
//                    println("Finshed is: ${sharedPref.getBoolean("Finished", true)}")
//                    println("AnswersPicked Json is: ${sharedPref.getString("AnswersPicked", "")}")
//
//
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "signInWithEmail:failure", task.exception)
//                    Toast.makeText(baseContext, "Login failed, please try again.",
//                        Toast.LENGTH_SHORT).show()
//                }
//
//
//            }
//
//
//    }
//
//    fun tempLogoutClicked (view: View) {
//
//        FirebaseAuth.getInstance().signOut()
//        println("------------------I AM LOGGED OUT------------------")
//        val sharedPref = this.getPreferences(MODE_PRIVATE)
//        with (sharedPref.edit()) {
//            putBoolean("LoggedIn", false)
//            commit()
//        }
//
//    }

}
