package ru.ladybug.isolatedsingularity;

import java.math.BigInteger;
import java.util.List;

/** Constitutes of the chain data relevant to the client side */
public class ChainData {
    private ChainView view;
    private List<Contributor> contributors;
    /*
     * BigInteger -- это мощно. Почти наверняка, за всё время существования приложения,
     *   пользователи не смогут переполнить int. И точно не смогут переполнить long
     */
    private BigInteger myContribution;

    /** Field-wise constructor */
    public ChainData(ChainView view, List<Contributor> contributors, BigInteger myContribution) {
        this.view = view;
        this.contributors = contributors;
        this.myContribution = myContribution;
    }

    public ChainView getView() {
        return view;
    }

    public List<Contributor> getContributors() {
        return contributors;
    }

    public BigInteger getMyContribution() {
        return myContribution;
    }

    /** Class represents chain contributor's data relevant to the client side such as visible name and amount of contribution */
    public static class Contributor {
        private BigInteger contribution;
        private String name;

        /** Field-wise constructor */
        public Contributor(BigInteger contribution, String name) {
            this.contribution = contribution;
            this.name = name;
        }

        public BigInteger getContribution() {
            return contribution;
        }

        public String getName() {
            return name;
        }
    }
}
