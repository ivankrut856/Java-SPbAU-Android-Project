package ru.ladybug.isolatedsingularity;

import ru.ladybug.isolatedsingularity.net.retrofitmodels.JUser;

/** Class represents user data relevant to the client side */
public class UserData {
    // стоит сделать финальными
    private String name;
    private int money;

    /** Field-wise constructor */
    public UserData(String name, int money) {
        this.name = name;
        this.money = money;
    }

    /** Constructor from api response type
     * @param user the user data in api response format
     */
    public UserData(JUser user) {
        this.name = user.getName();
        this.money = user.getMoney();
    }

    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }
}
