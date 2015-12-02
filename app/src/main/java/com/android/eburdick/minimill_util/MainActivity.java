package com.android.eburdick.minimill_util;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
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

        //Get the id of the offset_text editText widget
        //
        final EditText offset_text = (EditText) findViewById(R.id.offset_value);

        //Set an action listner for the offset_text widget.  This will get called when
        //the user is finished with the editor and touches the "done" button.
        //
        offset_text.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                //
                // The only action we want to do when the user is finished with entering
                // a number is to calculate the results, so we just call the do_calculate
                // method.
                //
                do_calculate(offset_text);
                //
                // Returning false tells the system that the calculate action we just did is
                // not to consume the event.  This means that the event can still trigger
                // removal of the soft keyboard after returning from this action method.
                //
                return false;
            }
        });
    }

    //
    //Action routine for changes requiring a new calculation.  This is any time the
    //user changes the state of an input, including the input value or one of the
    //radio buttons. This method is set to be the onclick method for all of the
    //buttons, and is called by the edit listener for the input offset value.
    //
    public void do_calculate(View v)
    {
        //create references to our user interface objects. The class R is defined
        //in http://schemas.android.com/apk/res/android, which is referenced in
        //AndroidManifest.xml. R stands for "resources" and is extended by the
        //build process to contain information about our user interface resources.
        double offset;
        TextView dialTurns = (TextView)findViewById(R.id.turns_value);
        TextView dialValue = (TextView)findViewById(R.id.dial_value);
        EditText offsetValue = (EditText)findViewById(R.id.offset_value);

        //note we don't need all of the radio buttons because half of them always
        //have the complementary values of their group mates
        RadioButton inchRadioButton = (RadioButton) findViewById(R.id.inchradiobutton);
        RadioButton cwRadioButton = (RadioButton) findViewById(R.id.cwradiobutton);

        //check if offset string is blank.  If so, set to zero. The editor insures that a non-blank value
        //is a valid number
        String offset_string = String.valueOf(offsetValue.getText().toString());
        if (offset_string.length()==0)
        {
            offset = 0.0;
        }
        //check if the offset string is just a decimal point.  If so, set to zero.
        else if (offset_string.length()==1 && offset_string.charAt(0)=='.')
        {
            offset = 0.0;
        }
        else
        {
            //Offset string is not blank. Get the offset value from edit text box
            offset = Double.valueOf(offsetValue.getText().toString());
        }
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
        //maximum. For the case where the offset is within .00005 of being a multiple of 1/16
        //inch, we do not do this subtraction, because 0 and .0625 occupy the same
        //physical location on the dial.
        //
        if(!cwRadioButton.isChecked() && dial_value >= 0.00005)
        {
            dial_value = offset_per_turn - dial_value;
        }

        //dialValue.setText(String.valueOf(dial_value));
        dialValue.setText(String.format("%.4f",dial_value));
    }
    //}
}
