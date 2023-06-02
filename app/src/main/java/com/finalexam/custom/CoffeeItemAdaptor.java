package com.finalexam.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finalexam.R;
import com.github.islamkhsh.CardSliderAdapter;

import java.util.ArrayList;

public class CoffeeItemAdaptor extends CardSliderAdapter<CoffeeItemAdaptor.CoffeeItemHolder> {

    private ArrayList<CoffeeItem> coffeeItems;

    public CoffeeItemAdaptor(ArrayList<CoffeeItem> coffeeItems) {
        this.coffeeItems = coffeeItems;
    }

    @Override
    public int getItemCount() {
        return coffeeItems.size();
    }

    @NonNull
    @Override
    public CoffeeItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.coffee_item, parent, false);
        return new CoffeeItemHolder(view);
    }

    @Override
    public void bindVH(@NonNull CoffeeItemHolder coffeeItemHolder, int idx) {  // binding items
        CoffeeItem coffeeItem = coffeeItems.get(idx);
        View v = coffeeItemHolder.itemView;

        TextView coffeeName = v.findViewById(R.id.coffeeNameTxt);
        ImageView coffeeImg = v.findViewById(R.id.coffeeItemImg);

        coffeeName.setText(coffeeItem.coffeeName);
        coffeeImg.setImageResource(coffeeItem.coffeeImg);
    }

    class CoffeeItemHolder extends RecyclerView.ViewHolder {
        public CoffeeItemHolder(View view) {
            super(view);
        }
    }
}
