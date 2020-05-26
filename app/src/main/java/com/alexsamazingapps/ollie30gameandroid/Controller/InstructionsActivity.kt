package com.alexsamazingapps.ollie30gameandroid.Controller

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.alexsamazingapps.ollie30gameandroid.R
import kotlinx.android.synthetic.main.activity_instructions.*

class InstructionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)

    instructions.text = "Welcome to ollie_is_30 the game! Answers can be found all around you. If you haven’t yet found the answer, press skip - you can come back to it later. You only get one shot at each question! Once you’ve answered, you can’t go back and change it.\n\nUse Google and ask those around you. Please leave clues where you found them.\n\nYour answers are synched with an online database, the winner will be announced at the end of the day. If you lose your internet connection, answers are stored locally and uploaded when the connection is resumed. For further info or if you’re really stuck, speak with Alex."

    }
}
