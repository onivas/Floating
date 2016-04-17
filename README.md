# Floating
Move your views where you like on screen


#Import library
<pre>
import io.github.onivas.floating.Floating;
</pre>

#Steps

In activity onCreate:
<pre>
// inizialize
mFloating.init(this);

// Move button on screen by using screen percentage dialog
Button button = (Button) findViewById(R.id.button);
boolean isOriginalButtonCoords = mFloating.setPercentageCoords(button);
        if (!isOriginalButtonCoords) {
            mFloating.restoreLastCoords(button);
        }
</pre>

<pre>
// If you want to use gravity sensor
Button button = (Button) findViewById(R.id.button);
boolean isOriginalButtonCoords = mFloating.setSensorCoords(button);
        if (!isOriginalButtonCoords) {
            mFloating.restoreLastCoords(button);
        }
</pre>

You need it to unregister the gravity sensor
<pre>
@Override
    protected void onPause() {
        mFloating.unregisterSensor();
        super.onPause();
    }
</pre>
