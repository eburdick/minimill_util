package com.android.eburdick.minimill_util;
/*
Minimill Util App

Version 1: This app is designed to simplify moving the x and y axis of a milling machine to the desired
positions, mainly for drilling.  Because many imported mini mills use a .0625 inch pitch on
their table lead screws, setting a position that is not a multiple of 1/16 inch.  In addition,
dealing with metric distances and turning the dials counter clockwise adds more chances for error.
Minimill Util take the desired offset in inches or millimeters, along with the direction of travel
(CW or CCW) and does some simple arithmetic to calculate the number of turns of the setting dials
and the final dial reading to achieve that offset.

Implementation: This is mostly user interface with one main routine for calculating and displaying
results.  This is fed by callbacks from the user interface elements, which are called whenever an
input value changes.

Version 2: After using version 1 for a few real operations in the shop, it became clear that this
app, though very handy, would be more useful if it could deal with multiple positions.  In the real
world, when a hole pattern is drilled, you pick a starting point, then move from there to the first
hole, well supported by this app, but then move from there to the next hole, and so on.  With this
app, you need to manually deal with the relative positions of the two holes.  The next version of
the app needs to provide some help there, either by allowing the user to enter all of the positions
desired, or by at least making it easy to keep the most recent value and use it as a starting point
for the next one.  Example: I want to make a motor mount hole pattern in the form of 2.5 cm square
with a center hole.  This is five holes.  The table motion might look like this:
    - find x and y edges and set dials to 0,0
    - move to center hole using the app twice to calculate it. Drill the hole.
    - move to lower left corner, using the app twice to calculate it, but also adding or
    subtracting the x and y values for the center.  The app should support this directly by
    keeping the current position and providing the necessary information to move to the new one.
    - move to the lower right corner, using the app only once, because only x is changing.  Still,
    we have to deal with manually calculating the relative offset.
    - move to upper right maually calculating the relative offset in y.
    - move to upper left, manually calculating the x offset.

The most direct way to simplify this for the user is to allow entry of all target coordinates into
the app and provide dial settings for each movement between positions. Starting with the V1
version, this means the offset, currently entered by the user, is calculated from difference
between successive positions.

User interface: The V1 interface is incrementally enhanced on top of a list of coordinate pairs.
There is a screen for each coordinate, X1, Y1, X2, Y2, etc, in that order. Each
screen is labeled Xn or Yn and there are buttons (maybe swipe) to move forward and backward through
the list. The user simply moves through the list entering the coordinates, and the app calculates
the offset values and dial operations to accomplish these relative movements. The screen on which
the user enters a new coordinate is where the new offset value and dial ops are displayed.

Screen items:
    - Row 1: Previous button | coordinate label (Xn or Yn) | Next button
    - Row 2: User entered coordinate value (X or Y) | inch/mm selector
    - Row 3  (offset = 0): "No Change" (Rows 4:7 blank)
             (offset != 0): Calculated offset in inch or mm
    - Row 4: (dial = 0): blank
             (dial != 0): "Turn clockwise to 0.000"
    - Row 5 (offset > 0): "Turn clockwise N turns"
            (offset < 0): "Turn counterclockwise N turns"
    - Row 6 (offset > 0): "Turn clockwise to VALUE"
    - Row 6 (offset < 0): "Turn clockwise one turn"
    - Row 7 (offset > 0): blank
    - Row 7 (offset < 0): Turn dial clockwise to (value)

User dial motion comments:
    Many hole patterns will require motion in both directions in one or both axes. Because of lead
    screw backlash, this can get complicated for the user if we try to finish settings in both
    directions, so for this app, final dial settings are always clockwise.  This means the
    program needs to compensate for backlash.  Roughly speaking, with the Little
    Machine Shop minimill this code is initially written for, the X backlash is about 10 mils --
    about 1/6 turn, and the Y backlash is about 20 or 30 mils, closer to half a turn. The full
    counterclockwise turns value needs to exceed the amount of backlash. We assume no machine has
    more than one turn of backlash in either axis and add a turn in negative moves to guarantee
    backlash is taken up.but not add a turn. To make turns counting easier, for the user, each
    positioning operation starts with advancing to zero with the appropriate offset added or
    subtracted from the subsequent motion. Examples:

    Table X is at 1.600" and next position is 1.9". Starting dial position is .0375. We need to move
    .3", which is 4 turns plus .050". The motions will be:
        - Move forward to zero.  This moves us .0625-.0375=.025 to 1.625, or 25/16"
        - Move forward .3-.025=.275. This is 4 turns clockwise to zero, then set clockwise to .025

    To reverse this motion: Table X is at 1.9" and next position is 1.6".  Starting dial position is
    .025. We need to move -.3" which is 4 turns counterclockwise (-.25), -.050". The motions will be
        - Move forward to zero. This moves us .0625-.025=.0375 to 1.9375 = 31/16
        - Move bacward .3 + .0375=.3375. This is 5 turns counterclockwise to zero, then further
        counterclockwise to .0375. We get the same result by doing 6 turns counterclockwise, then
        clockwise to .0375, which takes care of half a turn of backlash. Note this only works
        because our final value is larger that our expected backlash. If our final value was small,
        say .001", then we would have to go further counterclockwise to neutralize the backlash,
        which the user may or may not notice. Alternatively, doing 7 turns counterclockwise, then a
        full turn clockwise to take up any backlash, followed by clockwise motion to the final value
        is guaranteed to compensate for all backlash of less than a turn.

    Conclusion for motion conventions:

    The first example shows that motion in a positive direction is straightforward with no
    backlash issues. The second example illustrates the backlash issue. Further thinking this out,
    if we do a number of full counterclockwise turns to zero on the dial and then do our setting in
    a clockwise direction, then unless the final setting is larger than the amount of backlash, we
    will not succeed in taking up the backlash. The simplest solution to this is to add 2 full
    turns to the counterclockwise count, then advance a full turn clockwise to take up the backlash,
    tnen move clockwise to the final setting. Alternatively, we could add either 1 or 2 extra turns
    depending on whether the final setting is enough to take up the backlash. From a human factors
    point of view, this means the user will be doing one two different operations at the end of
    counterclockwise settings depending on final value, which will result in more mistakes, so in
    this app, we will add the two turns every time, which covers up to one turn of backlash.






 */
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemSelectedListener;
//import android.app.Activity;
import android.view.View;
import android.widget.Spinner;
//import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
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
        //                  Set up activity context
        //
        //The super.onCreate call restores any saved state to the activity and sets up
        //any required linkages to the OS user interface.  This is a call
        //to the parent class, which is in the operating system's user interface layer.
        //
        //The setContentView call sets up a connection to the activity's screen layout
        //resources defined in activity_main.xml (R.layout.activity_main) and "inflates"
        //it.
        //

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        //                  Set up denominator spinner callbacks
        //
        //Set up the denominator spinner. Though the spinner is completely set up by the
        //code generated from the layout XML files, we need to find its id in order to
        //create a callback to activate when an item is selected.  In this case, all the
        //callback does is call the do_calculate method, which in turn retrieves the spinner's
        //current value.
        //
        Spinner denom_sel_spinner = (Spinner) findViewById(R.id.denom_spinner);
        //
        // create a listener to activate when a spinner item is selected.
        //
        denom_sel_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> arg0, View v, int position, long id)
            {
                //We just call the do_calculate method, because it always retrieves the values
                //of all widgets.  The passed view is never used.
                do_calculate(v);
            }

            //This callback should never be called, because this spinner is always used
            //in dropdown mode, so something will always be selected
            public void onNothingSelected(AdapterView<?> arg0)
            {
                //debug code to indicate nothing selected
                //Log.v("denom", "nothing selected"
            }
        });

        //
        //                      Get offset and numerator widget ids
        //
        //Get the ids of the offset_text and numerator_text editText widgets.  These
        //are declared as final because they are constants from here on and any code
        // that tries to change them is a bug.
        //
        final EditText offset_text = (EditText) findViewById(R.id.offset_value);
        final EditText numerator_text = (EditText) findViewById(R.id.numerator_value);

        //
        //                      Clear offset and numerator on touch
        //
        //To avoid forcing the user to fiddle with the insertion point when entering values
        // via the EditText widgets, the fields are cleared when they are touched. A callback is
        //set up to be called when the region of the screen containing the corresponding
        //EditText widget is touched.  The callback clears the text, replacing it with the
        //widget's hint text as specified in the XML. Note that I originally used an onClick
        //listener for this, but because that is further down in the event hierarchy, it
        //often failed to be called because the high level touch event has been consumed.
        numerator_text.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                numerator_text.setText("");
                return false;
            }
        });

        offset_text.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                offset_text.setText("");
                return false;
            }
        });

        //
        //                  Set up text widget editor action callbacks
        //
        //Set an action listener for the offset_value widget.  This will get called when
        //the user is finished with the editor and touches the "done" button.  We need this
        //because we want our do_calculate method to be called when text entry is complete.
        //
        offset_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
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
        //
        //Set an action listener for the numerator_value widget.  This will get called when
        //the user is finished with the editor and touches the "done" button.
        //
        numerator_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //
                // The only action we want to do when the user is finished with entering
                // a number is to calculate the results, so we just call the do_calculate
                // method.
                //
                do_calculate(numerator_text);
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
    //                  Calculate and display results
    //
    //method for changes requiring a new calculation.  This is any time the
    //user changes the state of an input, including an input value, one of the
    //radio buttons, or the fraction spinner.  The widget listeners and their associated
    // callbacks call this method
    //
    public void do_calculate(View v)
    {
        //create references to our user interface objects. The class R is defined
        //in http://schemas.android.com/apk/res/android, which is referenced in
        //AndroidManifest.xml. R stands for "resources" and is extended by the
        //build process to contain information about our user interface resources.
        double offset_entered;
        double numerator;

        //
        //                  Machine specific constants
        //
        // This application exists because the table motion per turn of the machine is an
        // inconvenient value as set below.
        //
        final double offset_per_turn = .0625; // 1/16 inch
        //
        // To deal with the situation where we are turning an offset dial backward and we are
        // near zero, we need to prevent the dial from being shown as the maximum value instead
        // of zero.  To handle this, when the backward (counter clockwise) direction is being
        // calculated, we set a tolerance value. Dial values are shown to within .0001 inch, which
        // is a tenth of a division.  Anything less that that distance from the maximum value of
        // the dial is rounded to zero with an appropriate adjustment to turns count.
        final double dial_tolerance = .0001;
        //

        //
        //                  Get the id for each user interface widget
        //
        final TextView dialTurns = (TextView)findViewById(R.id.turns_value);
        final TextView dialValue = (TextView)findViewById(R.id.dial_value);
        final EditText offsetValue = (EditText)findViewById(R.id.offset_value);
        final EditText numeratorValue = (EditText)findViewById(R.id.numerator_value);
        final Spinner denom_spinner = (Spinner)findViewById(R.id.denom_spinner);

        //note we don't need all of the radio buttons because half of them always
        //have the complementary values of their group mates
        final RadioButton inchRadioButton = (RadioButton) findViewById(R.id.inchradiobutton);
        final RadioButton cwRadioButton = (RadioButton) findViewById(R.id.cwradiobutton);

        //
        //                  Get user entered offset value
        //
        //get the offset value value entered.  Check if offset string is blank or
        //a bare decimal point. If so, set to zero.
        String offset_string = String.valueOf(offsetValue.getText().toString());
        if (offset_string.length()==0)
        {
            offset_entered = 0.0;
            offsetValue.setText("0"); //display the zero
        }
        //check if the offset string is just a decimal point.  If so, set to zero.
        else if (offset_string.length()==1 && offset_string.charAt(0)=='.')
        {
            offset_entered = 0.0;
            offsetValue.setText("0"); //display the zero
        }
        else
        {
            //Offset string is not blank or a bare decimal point. Get the offset value from
            //the widget
            offset_entered = Double.valueOf(offset_string);
        }

        //
        //                  Get the user entered fraction
        //
        //The fraction comes from the numerator EditText widget and from the denominator
        //spinner, which is a menu of powers of two.  Create the fraction by dividing the
        //numerator by the denominator.
        //
        //get the denominator value from the spinner and remove all non-numeric characters
        // (the / and spaces before the number).  Then extract the value of the string.
        String denom_item_string = String.valueOf(denom_spinner.getSelectedItem().toString());
        denom_item_string = denom_item_string.replaceAll("[^0-9]", "");
        Double denominator = Double.valueOf(denom_item_string);

        //check if the numerator string is blank.  If so set the numerator to zero.  Otherwise
        //parse the value.  Note this string is constrained by the widget definition to be only
        //digits.
        String numerator_string = String.valueOf(numeratorValue.getText().toString());
        if (numerator_string.length()==0)
        {
            numerator = 0.0;
            numeratorValue.setText("0"); //display the zero
        }
        else
        {
            //numerator string is not blank and the user interface can only give us digits.
            //extract the value from the string.
            numerator = Double.valueOf(numerator_string);
        }
        double fraction_value = numerator/denominator;

        //
        //                  Calculate the user specified offset value
        //
        // This is just the entered offset plus any fraction so that the user may express
        // the value in SAE fractional terms, e.g. 2.125 = 2 1/8
        Double offset = offset_entered + fraction_value;

        //
        //                  Inch vs MM radio buttons
        //
        //If user specifies input is in millimeters, convert to inches.  If the inch radio
        //button is not checked, we assume the millimeter button is.
        if(!inchRadioButton.isChecked())
        {
            offset = offset/25.4;
        }

        //
        //                  Calculate and display dial turns
        //
        //The number of full dial turns to achieve the desired offset is the
        //integer part of the offset divided by the constant offset_per_turn,
        //which is the pitch of the X and Y lead screws
        //
        int turns = (int) Math.floor(offset / offset_per_turn);

        //
        //Now we calculate what the dial setting needs to be in order to finish
        //the desired offset.  For example, .1 inch will be 1 full turn (.0625) plus
        //.0375 on the dial. This number is the remainder, ie modulus.
        //
        double dial_value = offset % offset_per_turn;

        //
        //              Process CW vs CCW radio buttons
        //
        //If the dial is being turned counterclockwise, then the values are going
        //backward from offset_per_turn, so we just subtract the desired value from this
        //maximum. For the case where the offset is within the very small value of
        // dial_tolerance (1/10 of a machine division,) we do not do this subtraction, because 0
        // and offset_per_turn occupy the same physical location on the dial.
        //
        // For the same reason, for clockwise setting, if the dial value is within twice the
        // tolerance (1/10 of a machine division) of the maximum dial value we round it up,
        // setting the dial to zero and adding a turn.
        //
        // note that dial_value will never be greater than offset_per_turn because its
        // calculation was modulo offset_per_turn, so we never need to deal with that
        // side of the zero mark.
        //
        if(!cwRadioButton.isChecked())
        {
            if (dial_value >= dial_tolerance)
            {
                //reverse the direction of the dial
                dial_value = offset_per_turn - dial_value;
            }
            else
            {
                //dial value is so close to the end point that we round it to that point.
                dial_value = 0.0;
            }
        }
        else if(offset_per_turn - dial_value < dial_tolerance)
        {
            //dial value is so close to the end point that we round it to that point.  Since we
            //are going clockwise, we need to add a turn.
            dial_value = 0.0;
            turns += 1;
        }

        //
        //              Display results to four decimal places
        //
        //dialValue.setText(String.valueOf(dial_value));
        dialValue.setText(String.format("%.4f",dial_value));
        dialTurns.setText(String.valueOf(turns));

        //display the resulting offset value in a toast (pop-up text block appearing
        //near the bottom of the screen.
        Toast.makeText(this,String.format("%.4f",offset),Toast.LENGTH_LONG).show();
    }
    //}
}
