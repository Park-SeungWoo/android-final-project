package com.finalexam.custom;

import java.io.Serializable;

public class CoffeeItem implements Serializable {
    String coffeeName;
    int coffeeImg;
    int price;

    public CoffeeItem(String name, int id) {
        this.coffeeName = name;
        this.coffeeImg = id;
        switch (name) {
            case "Americano":
                price += 2000;
                break;
            case "Cafe Latte":
                price += 3500;
                break;
            case "Cappuccino":
                price += 4000;
                break;
        }
    }

    public int getPrice() {
        return this.price;
    }

    public String getCoffeeName() {
        return this.coffeeName;
    }
}
