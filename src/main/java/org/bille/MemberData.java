package org.bille;

import java.util.Objects;

public class MemberData implements Comparable {

    private final String userName;
    private final String address;
    private final boolean isMax;
    private int cards;

    public MemberData(String userName, String address, boolean isMax, int cards) {

        this.userName = userName;
        this.address = address;
        this.isMax = isMax;
        this.cards = cards;
    }

    public String getUserName() {
        return userName;
    }

    public String getAddress() {
        return address;
    }

    public boolean isMax() {
        return isMax;
    }

    public boolean hasNoCards() {
        return !isMax && cards == 0;
    }

    public int getCards() {
        return cards;
    }

    public void setCards(int cards) {
        this.cards = cards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberData that = (MemberData) o;
        return Objects.equals(userName.toLowerCase(), that.userName.toLowerCase()) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName.toLowerCase(), address);
    }

    @Override
    public int compareTo(Object o) {
        MemberData o1 = (MemberData) o;
        return this.getUserName().compareToIgnoreCase(o1.getUserName());
    }

    @Override
    public String toString() {
        return "MemberData{" +
                "userName=" + userName +
                ", cards=" + cards +
                '}';
    }
}
