package com.alexsamazingapps.ollie30gameandroid.Controller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.alexsamazingapps.ollie30gameandroid.Model.ProgressSpinner
import com.alexsamazingapps.ollie30gameandroid.Model.QuestionBank
import com.alexsamazingapps.ollie30gameandroid.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_game.*

//import kotlinx.android.synthetic.main.activity_instructions.*
//import android.R


class GameActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    val firebaseRef = FirebaseDatabase.getInstance()

    //MARK: - Global variables:
    val TAG = "TAG"
    val allQuestions = QuestionBank()
    val progressSpinner = ProgressSpinner()
    var questionNumber: Int = 0
    var score: Int = 0
    var answersPicked: MutableList<Int> = MutableList(20) { 0 }
    var finished: Boolean = false
    var connectedToFirebase: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        auth = FirebaseAuth.getInstance()

        //Temp for reset button:
        resetButton.visibility = View.INVISIBLE
        ///////////////////////////

        //TESTING:
        tempButton.visibility = View.INVISIBLE
        ///////////////////////////

        val gson = GsonBuilder().create()
        //val sharedPref = this.getPreferences(MODE_PRIVATE)

        //TESTING:
        val defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        println("2---------------------Finshed is: ${defaultSharedPref.getBoolean("Finished", true)}")
        println("2---------------------AnswersPicked Json is: ${defaultSharedPref.getString("AnswersPicked", "")}")
        ///////////////////////////


        questionNumber = defaultSharedPref.getInt("QuestionNumber", 0)
        println("Question number is $questionNumber")
        score = defaultSharedPref.getInt("Score", 0)
        println("Score is $score")
        finished = defaultSharedPref.getBoolean("Finished", false)
        println("Finished is $finished")

        val savedAnswersPickedJson = defaultSharedPref.getString("AnswersPicked", "")
        println("Saved answers JSON are: $savedAnswersPickedJson")

        val savedAnswersPicked = gson.fromJson(savedAnswersPickedJson, Array<Int>::class.java).toMutableList()
        println("Answers picked out of JSON are $savedAnswersPicked")

        answersPicked = savedAnswersPicked

        checkConnection()
        displayQuestion()
        updateUI()

        println("3---------------------Finshed is: ${defaultSharedPref.getBoolean("Finished", true)}")
        println("3---------------------AnswersPicked Json is: ${defaultSharedPref.getString("AnswersPicked", "")}")

    }

    fun checkConnection() {

        val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d(TAG, "connected")
                    connectedToFirebase = true
                    offlineMode.visibility = View.INVISIBLE
                    //TESTING:
                    val defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    println("DEFAULTSHAREDPREF: ${defaultSharedPref.all}")
                    /////////////////

                } else {
                    Log.d(TAG, "not connected")
                    connectedToFirebase = false
                    offlineMode.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Listener was cancelled")
            }
        })


    }


    fun saveData() {

        val gson = GsonBuilder().create()
        val jsonAnswersPicked = gson.toJson(answersPicked)

        val defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        with(defaultSharedPref.edit()) {
            putInt("QuestionNumber", questionNumber)
            commit()
        }
        with(defaultSharedPref.edit()) {
            putInt("Score", score)
            commit()
        }
        with(defaultSharedPref.edit()) {
            putBoolean("Finished", finished)
            commit()
        }
        with(defaultSharedPref.edit()) {
            putString("AnswersPicked", jsonAnswersPicked)
            commit()
        }

    }

    fun saveToFirebase(callback: (Boolean) -> Unit) {

        val gson = GsonBuilder().create()
        val defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())

        val uid = auth.currentUser?.uid

        val savedAnswersPickedJson = defaultSharedPref.getString("AnswersPicked", "")
        val savedAnswersPicked = gson.fromJson(savedAnswersPickedJson, Array<Int>::class.java).toMutableList()

        var firebaseUserDataMap = hashMapOf(
            "QuestionNumber" to defaultSharedPref.getInt("QuestionNumber", 0),
            "Score" to defaultSharedPref.getInt("Score", 0),
            "AnswersPicked" to savedAnswersPicked,
            "Finished" to defaultSharedPref.getBoolean("Finished", false)
        )

        var complete: Boolean = false

        firebaseRef.reference.child(uid!!).updateChildren(firebaseUserDataMap).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                println("Data successfully saved to Firebase")
                complete = true
            } else {
                println("Data not successfully saved to Firebase")
                complete = false
            }

            callback(complete)

        }

    }

    //MARK: - Quiz functionality

    fun instructionsButtonClicked(view: View) {

        val instructionsActivity = Intent(this, InstructionsActivity::class.java)
        startActivity(instructionsActivity)

    }

    fun answerButtonClicked(view: View) {

        checkConnection()

        val viewTagStr = view.tag.toString()
        val viewTagInt = viewTagStr.toInt()

        if (viewTagInt == allQuestions.list[questionNumber].correctAnswerNum + 1) {

            answersPicked[questionNumber] = viewTagInt
            println("Correct")
            score += 1
            println(score)
        } else if (viewTagInt == 5) {
            print("Question skipped")
        } else {
            answersPicked[questionNumber] = viewTagInt
            println("Wrong")
            println(score)
        }

        calculateNextQuestion()
        displayQuestion()
        updateUI()

    }


    fun displayQuestion() {

        saveData()
        this.saveToFirebase { success ->
            if (success) {
                println("Callback: Data was saved to Firebase")
            } else {
                println("Callback: Data was not saved to Firebase")
            }
        }


        if (finished == false && questionNumber < allQuestions.list.size) {
            questionLabel.text = allQuestions.list[questionNumber].questionText
            answerButton1.text = allQuestions.list[questionNumber].answerText[0]
            answerButton2.text = allQuestions.list[questionNumber].answerText[1]
            answerButton3.text = allQuestions.list[questionNumber].answerText[2]
            answerButton4.text = allQuestions.list[questionNumber].answerText[3]
        } else {

            println("Finished")
            println("Final score = $score")
            questionLabel.text = "Game over"
            questionNumberLabel.visibility = View.INVISIBLE
            answerButtonStack.visibility = View.INVISIBLE
            skipButton.visibility = View.INVISIBLE

            progressSpinner.show(indeterminateBarGame, getWindow())

            saveToFirebase { success ->

                if (success) {

                    questionLabel.text = "Thankyou for playing"
                    //Temp for reset button:
                    //resetButton.visibility = View.VISIBLE
                    ///////////////////////////

                    progressSpinner.hide(indeterminateBarGame, getWindow())

                } else {
                    println("Could not save final data.")
                    progressSpinner.hide(indeterminateBarGame, getWindow())

                }
            }

        }

    }


    fun updateUI() {

        val questionNumberForLabel = questionNumber + 1
        questionNumberLabel.text = "$questionNumberForLabel / 30"

    }


    fun calculateNextQuestion() {

        val totalQuestions = answersPicked.size

        for (i in 1..totalQuestions) {
            questionNumber += 1

            if (questionNumber >= totalQuestions) {
                questionNumber = 0
            }

            if (answersPicked[questionNumber] == 0) {
                return
            }
        }

        finished = true

    }

    //////////////////////////////////////////////////////
    //TESTING:
//    fun resetButtonClicked(view: View) {
//
//        questionNumber = 0
//        score = 0
//        answersPicked = MutableList(30) {0}
//        finished = false
//
//        questionNumberLabel.visibility = View.VISIBLE
//        answerButtonStack.visibility = View.VISIBLE
//        skipButton.visibility = View.VISIBLE
//
//        displayQuestion()
//        updateUI()
//        resetButton.visibility = View.INVISIBLE
//
//    }
//////////////////////////////////////////////////////

    fun logOutButtonClicked(view: View) {

        if (connectedToFirebase == true) {

            progressSpinner.show(indeterminateBarGame, getWindow())

            saveData()
            this.saveToFirebase { success ->
                if (success) {
                    println("Callback: Data was saved to Firebase")

                    FirebaseAuth.getInstance().signOut()
                    //Alex's special error handling:
                    if (auth.currentUser == null) {

                        Log.w(TAG, "Logout success")

                        //Reset sharedPref:
                        val gson = GsonBuilder().create()
                        val jsonAnswersPicked = gson.toJson(MutableList(30) { 0 })

                        val defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        with(defaultSharedPref.edit()) {
                            putBoolean("LoggedIn", false)
                            commit()
                        }
                        with(defaultSharedPref.edit()) {
                            putBoolean("Finished", false)
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

                        println(
                            "Answers picked on logout: $jsonAnswersPicked, login is ${defaultSharedPref.getBoolean(
                                "LoggedIn",
                                false
                            )}"
                        )

                        val loginActivity = Intent(this, LoginActivity::class.java)
                        startActivity(loginActivity)
                        finish()
                        progressSpinner.hide(indeterminateBarGame, getWindow())

                    } else {
                        progressSpinner.hide(indeterminateBarGame, getWindow())
                        Log.w(TAG, "Logout failure")
                        Toast.makeText(
                            baseContext, "Logout failed, please check connection and try again.",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                } else {
                    progressSpinner.hide(indeterminateBarGame, getWindow())
                    println("Callback: Data was not saved to Firebase")
                    Log.w(TAG, "Logout failure, data could not be saved.")
                    Toast.makeText(
                        baseContext, "Logout failed, please check your internet connection.",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

        } else {
            progressSpinner.hide(indeterminateBarGame, getWindow())
            Log.w(TAG, "Logout failure, please check your internet connection")
            Toast.makeText(
                baseContext, "Logout failed, please check your internet connection.",
                Toast.LENGTH_SHORT
            ).show()

        }

    }

}

    //////////////////////////////////////////////////////
    //TESTING:
//    fun tempButtonClicked(view: View) {
//
//        val uid = auth.currentUser?.uid
//
//        val dataListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val map = dataSnapshot.value as Map<String, Any>
//                println("Firebase data = ${map.values}") // Only returns email (probably because it's set below).
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                println("loadPost:onCancelled ${databaseError.toException()}") // This is the error handling.
//            }
//        }
//        firebaseRef.reference.child(uid!!)
//            .addListenerForSingleValueEvent(dataListener) //Need this here to make the function above work.
//
//    }
    //////////////////////////////////////////////////////