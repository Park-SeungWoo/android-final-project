package com.finalexam.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.finalexam.R;

public class CoffeeListItemView extends ConstraintLayout {
    TextView coffeeName;
    int price;

    public CoffeeListItemView(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.selected_coffee_list, this, false);
        addView(v);

        coffeeName = findViewById(R.id.listCoffeeTxt);
    }

    public void setCoffeeInfo(String coffeeName, int price) {
        this.coffeeName.setText(coffeeName);
        this.price = price;
    }

    public int getPrice() {
        return this.price;
    }

    public String getItemName() {
        return this.coffeeName.getText().toString();
    }
}
