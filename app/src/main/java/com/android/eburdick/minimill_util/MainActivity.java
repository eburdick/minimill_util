package com.android.eburdick.minimill_util;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.EditText;
import java.lang.Double;

/*
MainActivity is essentially the main program for the main screen of the app.  The Android
operating system calls its onCreate method to start the activity.  The onCreate method takes
a savedInstanceState Bundle as its only argument.  This bundle comes from the operating system
when it calls this method, and contains any state that may have been saved the last time the
activity was destroyed.
*/

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //
        //The super.onCreate call restores any saved state to the activity and sets up
        //any required linkages to the OS user interface.  This is a call
        //to the parent class, which is in the operating system's user interface layer.
        //
        //The setContentView call sets up a connection to the activity's screen layout
        //resources defined in activity_main.xml (R.layout.activity_main)
        //

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
/*
        // Get a reference to the layout's button.  There is only one in this app.
        Button button = (Button) findViewById(R.id.calc_button);
        //
        //Set the listener for button click.
        //The listener is the action routine for button clicks.  It is defined here in place
        //with no additional name.This is the OnClick method for the button object we just created.
        //
        button.setOnClickListener
        (
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        //create references to our user interface objects
                        TextView dialTurns = (TextView)findViewById(R.id.turns_value);
                        TextView dialValue = (TextView)findViewById(R.id.dial_value);
                        EditText offsetValue = (EditText)findViewById(R.id.offset_value);

                        //note we don't need all of the radio buttons because half of them always
                        //have the complementary values of their group mates
                        RadioButton inchRadioButton = (RadioButton) findViewById(R.id.inchradiobutton);
                        RadioButton cwRadioButton = (RadioButton) findViewById(R.id.cwradiobutton);

                        //Get the desired table offset value from edit text box
                        double offset = Double.valueOf(offsetValue.getText().toString());

                        //If user specifies input is in millimeters, convert to inches
                        if(!inchRadioButton.isChecked())
                        {
                            offset = offset/25.4;
                        }

                        //
                        //The number of full dial turns to achieve the desired offset is the
                        //integer part of the offset divided by 1/16, which is the pitch
                        //of the lead screws
                        //
                        double offset_per_turn = .0625; //minimill lead screw pitch = 1/16 inch
                        int turns = (int) Math.floor(offset / offset_per_turn);
                        dialTurns.setText(String.valueOf(turns));

                        //
                        //Now we calculate what the dial setting needs to be in order to finish
                        //the desired offset.  For example, .1 inch will be 1 full turn (.0625) plus
                        //.0375 on the dial. This number is the remainder, ie modulus.
                        //
                        double dial_value = offset % offset_per_turn;

                        //
                        //If the dial is being turned counterclockwise, then the values are going
                        //backward from .0625, so we just subtract the desired value from this
                        //maximum. For the case where the offset is exactly a multiple of 1/16
                        //inch, we do not do this subtraction, because 0 and .0625 occupy the same
                        //physical location on dial.
                        //
                        if(!cwRadioButton.isChecked() && dial_value > 0)
                        {
                            dial_value = offset_per_turn - dial_value;
                        }

                        //dialValue.setText(String.valueOf(dial_value));
                        dialValue.setText(String.format("%.4f",dial_value));
                    }
                }
        );
*/

    public void do_calculate(View v)
    {
        //create references to our user interface objects
        TextView dialTurns = (TextView)findViewById(R.id.turns_value);
        TextView dialValue = (TextView)findViewById(R.id.dial_value);
        EditText offsetValue = (EditText)findViewById(R.id.offset_value);

        //note we don't need all of the radio buttons because half of them always
        //have the complementary values of their group mates
        RadioButton inchRadioButton = (RadioButton) findViewById(R.id.inchradiobutton);
        RadioButton cwRadioButton = (RadioButton) findViewById(R.id.cwradiobutton);

        //Get the desired table offset value from edit text box
        double offset = Double.valueOf(offsetValue.getText().toString());

        //If user specifies input is in millimeters, convert to inches
        if(!inchRadioButton.isChecked())
        {
            offset = offset/25.4;
        }

        //
        //The number of full dial turns to achieve the desired offset is the
        //integer part of the offset divided by 1/16, which is the pitch
        //of the lead screws
        //
        double offset_per_turn = .0625; //minimill lead screw pitch = 1/16 inch
        int turns = (int) Math.floor(offset / offset_per_turn);
        dialTurns.setText(String.valueOf(turns));

        //
        //Now we calculate what the dial setting needs to be in order to finish
        //the desired offset.  For example, .1 inch will be 1 full turn (.0625) plus
        //.0375 on the dial. This number is the remainder, ie modulus.
        //
        double dial_value = offset % offset_per_turn;

        //
        //If the dial is being turned counterclockwise, then the values are going
        //backward from .0625, so we just subtract the desired value from this
        //maximum. For the case where the offset is exactly a multiple of 1/16
        //inch, we do not do this subtraction, because 0 and .0625 occupy the same
        //physical location on dial.
        //
        if(!cwRadioButton.isChecked() && dial_value > 0)
        {
            dial_value = offset_per_turn - dial_value;
        }

        //dialValue.setText(String.valueOf(dial_value));
        dialValue.setText(String.format("%.4f",dial_value));
    }
    //}
}
