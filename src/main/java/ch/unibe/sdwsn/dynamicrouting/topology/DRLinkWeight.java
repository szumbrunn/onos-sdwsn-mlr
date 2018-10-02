package ch.unibe.sdwsn.dynamicrouting.topology;

import org.onlab.graph.Weight;

public class DRLinkWeight implements Weight {

    private double cost;

    public DRLinkWeight() {
        this.cost = 1.0; // set the initial weight to 1
    }

    /**
     * Create a new linkweight by a skalar weight
     * @param cost
     */
    public DRLinkWeight(double cost) {
        this.cost = cost;
    }

    /**
     *
     * @param weight
     * @return the sum of this weight and the merged weight
     */
    @Override
    public Weight merge(Weight weight) {
        Weight tmpWheight = new DRLinkWeight(
                ((DRLinkWeight)weight).getCost() + this.cost);
        return tmpWheight;
    }

    /**
     *
     * @param weight
     * @return the difference of this weight and the subtracted weight
     */
    @Override
    public Weight subtract(Weight weight) {
        Weight tmpWheight = new DRLinkWeight(
                this.cost - ((DRLinkWeight)weight).getCost());
        return tmpWheight;
    }

    /**
     * @return true -> all established links are viable
     */
    @Override
    public boolean isViable() {
        return true;
    }

    /**
     *
     * @return false when cost is smaller than 0
     */
    @Override
    public boolean isNegative() {
        return cost < 0;
    }

    /**
     *
     * @param o the weight to compare to
     * @return -1 when this is smaller than o
     *         0 when this is eqal to o
     *         1 when this is greater than o
     */
    @Override
    public int compareTo(Weight o) {
        return (new Double(cost)).compareTo(
                new Double(((DRLinkWeight)o).getCost()));
    }

    /**
     *
     * @return cost as scalar
     */
    public double getCost() {
        return this.cost;
    }

    /**
     *
     * @param cost as scalar
     */
    public void setCost(double cost) {
        this.cost = cost;
    }
}
