package com.finalexam.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.finalexam.MainActivity;
import com.finalexam.R;
import com.finalexam.custom.CoffeeItem;
import com.finalexam.custom.CoffeeItemAdaptor;
import com.finalexam.custom.CoffeeListItemView;
import com.github.islamkhsh.CardSliderViewPager;

import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity {
    CardSliderViewPager cardSliderViewPager;
    ArrayList<CoffeeItem> coffeeItems;

    Button cntMinBtn, cntPluBtn, addTotBtn;
    ImageButton mapBtn;
    TextView cntTxt, totPriceTxt, selectedDflTxt;
    LinearLayout selectedListView;
    HorizontalScrollView scrollListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_coffee);

        // add carousel items
        cardSliderViewPager = findViewById(R.id.itemSlider);
        addCoffeeItems();

        // init views
        cntMinBtn = findViewById(R.id.minusCntBtn);
        cntPluBtn = findViewById(R.id.addCntBtn);
        addTotBtn = findViewById(R.id.totalSumBtn);
        mapBtn = findViewById(R.id.mapBtn);

        cntTxt = findViewById(R.id.cntTxt);
        totPriceTxt = findViewById(R.id.totalPriceTxt);
        selectedDflTxt = findViewById(R.id.emptyMenutxt);

        selectedListView = findViewById(R.id.listView);
        scrollListView = findViewById(R.id.listItemScrollV);

        // register event handlers
        cntMinBtn.setOnClickListener(new CntBtnEventHandler());
        cntPluBtn.setOnClickListener(new CntBtnEventHandler());
        addTotBtn.setOnClickListener(new AddTotalBtnEventHandler());
        mapBtn.setOnClickListener(new MapBtnEventHandler());
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    // utils
    private void addCoffeeItems() {
        coffeeItems = new ArrayList<>();
        CoffeeItem americano = new CoffeeItem("Americano", R.drawable.americano);
        CoffeeItem latte = new CoffeeItem("Cafe Latte", R.drawable.latte);
        CoffeeItem cappuccino = new CoffeeItem("Cappuccino", R.drawable.cappuccino);

        coffeeItems.add(americano);
        coffeeItems.add(latte);
        coffeeItems.add(cappuccino);

        cardSliderViewPager.setAdapter(new CoffeeItemAdaptor(coffeeItems));
    }

    private void addSelectedCoffeeToList(String coffeeName, int price) {
        CoffeeListItemView newItem = new CoffeeListItemView(getApplicationContext());
        newItem.setCoffeeInfo(coffeeName, price);
        newItem.setOnClickListener(new SlcCoffeeItemEventHandler());

        selectedListView.addView(WrapWithLayout(newItem));  // to set margin in items
    }

    private LinearLayout WrapWithLayout(View item) {
        LinearLayout itemWrapper = new LinearLayout(getApplicationContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);  // width, height
        layoutParams.setMargins(0, 0, 12, 0);  // set margin
        itemWrapper.setLayoutParams(layoutParams);

        itemWrapper.addView(item);
        return itemWrapper;
    }

    private void updateTotalPrice(int price) {
        totPriceTxt.setText(price + "￦");
    }

    private boolean isSelectedListEmpty() {
        return selectedListView.getChildCount() == 0;
    }

    private void scrollToEnd() {
        scrollListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollListView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 150L);
    }

    // event handler classes
    class CntBtnEventHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int count = Integer.parseInt(cntTxt.getText().toString());
            if (view == cntMinBtn) {
                if (count < 1)  // not allow negative count
                    return;
                cntTxt.setText(--count + "");
            } else
                cntTxt.setText(++count + "");
        }
    }

    class AddTotalBtnEventHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            CoffeeItem item = coffeeItems.get(cardSliderViewPager.getCurrentItem());
            int totalPrice = Integer.parseInt(totPriceTxt.getText().toString().split("￦")[0]);
            int slcCoffeePri = item.getPrice();  // access to selected item to get price, name
            int count = Integer.parseInt(cntTxt.getText().toString());
            String coffeeName = item.getCoffeeName();

            if (isSelectedListEmpty()) selectedDflTxt.setText("");
            updateTotalPrice(totalPrice + (slcCoffeePri * count));
            cntTxt.setText("1");
            cardSliderViewPager.setCurrentItem(0);
            scrollToEnd();

            // add list items 'count' times
            for (int i = 0; i < count; i++)
                addSelectedCoffeeToList(coffeeName, slcCoffeePri);
        }
    }

    class SlcCoffeeItemEventHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int price = ((CoffeeListItemView) view).getPrice();
            int totalPrice = Integer.parseInt(totPriceTxt.getText().toString().split("￦")[0]);
            System.out.println(price);

            // alert if user really want to delete item
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(OrderActivity.this);
            alertBuilder.setTitle("메뉴 삭제");
            alertBuilder.setMessage("정말로 " + ((CoffeeListItemView) view).getItemName() + " 을(를) 삭제 하시겠습니까?");
            alertBuilder.setPositiveButton("삭제", (dialogInterface, i) -> {
                updateTotalPrice(totalPrice - price);
                selectedListView.removeView((LinearLayout) view.getParent()); // remove item in list
                scrollToEnd();  // delay for 1.5s and scroll to the end of the list
                if (isSelectedListEmpty()) selectedDflTxt.setText("메뉴를 선택해주세요!");
            });
            alertBuilder.setNegativeButton("취소", (dialogInterface, i) -> dialogInterface.cancel());
            alertBuilder.create();
            alertBuilder.show();
        }
    }

    class MapBtnEventHandler implements View.OnClickListener {  // go to third activity
        @Override
        public void onClick(View view) {
            Intent goMapIntent = new Intent(OrderActivity.this, MapActivity.class);
            startActivity(goMapIntent);  // extras에 주문정보 넘기기
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);  // slide animation
        }
    }
}
