package tw.g35g.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import tw.g35g.widget.OnWheelChangedListener;
import tw.g35g.widget.OnWheelClickedListener;
import tw.g35g.widget.OnWheelScrollListener;
import tw.g35g.widget.WheelView;
import tw.g35g.widget.adapters.ArrayWheelAdapter;

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
        country.setDeleteButtonEnabled(true);
        country.setDeleteButtonFontSize(18);
        country.setDeleteButtonText("刪除");
        country.setDeleteButtonTextColor(Color.WHITE);
        country.setActionButtonEnabled(true);
        country.setActionButtonFontSize(22);
        country.setActionButtonTextColor(Color.WHITE);
        country.setActionButtonText(">");

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
                Log.i("TAG","onItemClicked:" + itemIndex);
            }

            @Override
            public void onItemSelected(WheelView wheel, int itemIndex) {
                Log.i("TAG","onItemSelected:" + itemIndex);
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
                Log.i("TAG","onActionClicked:" + itemIndex);
            }

            @Override
            public void onDeleteClicked(WheelView wheel, int itemIndex) {
                Log.i("TAG","onDeleteClicked:" + itemIndex);
            }

            @Override
            public void onItemUnderClicked(WheelView wheel, int itemIndex) {
                country.setCurrentItem(itemIndex - 1);
                Log.i("TAG", "onItemUnderClicked:" + itemIndex);
            }

            @Override
            public void onItemAboveClicked(WheelView wheel, int itemIndex) {
                country.setCurrentItem(itemIndex + 1);
                Log.i("TAG", "onItemAboveClicked:" + itemIndex);
            }
        });
    }

}
