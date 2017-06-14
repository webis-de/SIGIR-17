package de.webis.datastructures;

/**
 * A datastructure storing EF1 values, a measure to evaluate a spell algorithm presented by Microsoft.
 */
public class EF1 {
    public Double expectedPrecision;
    public Double expectedRecall;

    public Double EF1;

    /**
     * Class constructor.
     * @param expectedPrecision EP
     * @param expectedRecall    ER
     * @param EF1               EF1
     */
    public EF1(Double expectedPrecision, Double expectedRecall, Double EF1){
        this.expectedPrecision = expectedPrecision;
        this.expectedRecall = expectedRecall;
        this.EF1 = EF1;
    }

    @Override
    public String toString(){
        return "EF1: "+this.EF1+"\n EP: "+this.expectedPrecision+"\n"+" ER: "+this.expectedRecall;
    }
}
