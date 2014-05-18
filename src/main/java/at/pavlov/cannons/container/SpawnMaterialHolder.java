package at.pavlov.cannons.container;

/**
 * Created by Peter on 18.05.2014.
 */
public class SpawnMaterialHolder extends MaterialHolder {
    private int minAmount;
    private int maxAmount;

    public SpawnMaterialHolder(String str) {

        //split string at space
        // id:data min-max
        // 10:0 1-2

        super(str);
    }
}
