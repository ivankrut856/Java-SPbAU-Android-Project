package ru.ladybug.isolatedsingularity;

import java.math.BigInteger;
import java.util.List;

public class ChainData {
    private ChainView view;
    private List<Contributor> contributors;
    private BigInteger myContribution;


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

    public static class Contributor {
        private BigInteger contribution;
        private String name;
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
