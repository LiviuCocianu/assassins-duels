package io.github.idoomful.assassinsduels.arena;

public enum ArenaDuelType {
    VS1(1),
    VS2(2),
    VS3(3),
    VS4(4),
    VS5(5);

    private final int mult;

    ArenaDuelType(int multiplier) {
        mult = multiplier;
    }

    public int getMult() {
        return mult;
    }
}
