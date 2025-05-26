package onetoone.users;

/**
 * Enum representing different weight classes for competition
 */
public enum WeightClass {
    LEAF("Leaf", 0, 125),
    TWIG("Twig", 126, 150),
    STICK("Stick", 151, 175),
    BRANCH("Branch", 176, 200),
    BOUGH("Bough", 201, 225),
    TRUNK("Trunk", 226, 250),
    LOG("Log", 251, 275),
    TREE("Tree", 276, Integer.MAX_VALUE);

    private final String name;
    private final int minWeight;
    private final int maxWeight;

    WeightClass(String name, int minWeight, int maxWeight) {
        this.name = name;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
    }

    public String getName() {
        return name;
    }

    public int getMinWeight() {
        return minWeight;
    }

    public int getMaxWeight() {
        return maxWeight;
    }

    /**
     * Find the appropriate weight class for a given weight
     *
     * @param weight Weight in pounds
     * @return The corresponding WeightClass
     */
    public static WeightClass getWeightClassForWeight(int weight) {
        for (WeightClass weightClass : WeightClass.values()) {
            if (weight >= weightClass.getMinWeight() && weight <= weightClass.getMaxWeight()) {
                return weightClass;
            }
        }
        return TREE; // Default to the heaviest weight class if something goes wrong
    }

    @Override
    public String toString() {
        if (this == TREE) {
            return name + " (" + minWeight + "+)";
        }
        return name + " (" + minWeight + "-" + maxWeight + ")";
    }
}