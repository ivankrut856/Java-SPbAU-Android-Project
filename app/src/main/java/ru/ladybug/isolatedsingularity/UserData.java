package ru.ladybug.isolatedsingularity;

import ru.ladybug.isolatedsingularity.retrofitmodels.JUser;

public class UserData {
    private int id;
    private String name;
    private int money;

    public UserData(int id, String name, int money) {
        this.id = id;
        this.name = name;
        this.money = money;
    }

    public UserData(JUser user) {
        this.id = user.getId();
        this.name = user.getName();
        this.money = user.getMoney();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }
}
