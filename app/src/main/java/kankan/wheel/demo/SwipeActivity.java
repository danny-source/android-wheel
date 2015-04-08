package kankan.wheel.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelClickedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/**
 * Created by danny on 15/4/8.
 */
public class SwipeActivity extends Activity {
    // Scrolling flag
    private boolean scrolling = false;
    private String countries[] =
            new String[] {"0.Taiwan", "1.台灣", "2.USA", "3.Canada", "4.Ukraine", "5.France", "6.Japan", "7.Korea", "8.Africa"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.swipe_layout);

        final WheelView country = (WheelView) findViewById(R.id.swipe);
        //country.setVisibleItems(3);
        country.setButtonForActionEnabled(true);
        country.setButtonForActionFontSize(18);
        country.setButtonForActionText("刪除");
        country.setButtonForActionTextColor(Color.WHITE);

        ArrayWheelAdapter<String> adapter =
                new ArrayWheelAdapter<String>(this, countries);
        adapter.setTextSize(34);

        country.setViewAdapter(adapter);

        country.addChangingListener(new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!scrolling) {

                }
            }
        });

        country.addScrollingListener(new OnWheelScrollListener() {
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }

            public void onScrollingFinished(WheelView wheel) {
                scrolling = false;

            }
        });

        country.setCurrentItem(1);
        country.addClickingListener(new OnWheelClickedListener() {
            @Override
            public void onItemClicked(WheelView wheel, int itemIndex) {

            }

            @Override
            public boolean onItemSwipRight(WheelView wheel, int itemIndex) {
                if (itemIndex == 2) {

                    return false;
                }
                return true;
            }

            @Override
            public boolean onItemSwipLeft(WheelView wheel, int itemIndex) {
                return false;
            }

            @Override
            public void onActionClicked(WheelView wheel, int itemIndex) {
                Log.i("TAG","ActionClicked:" + itemIndex);
            }
        });
    }

}
