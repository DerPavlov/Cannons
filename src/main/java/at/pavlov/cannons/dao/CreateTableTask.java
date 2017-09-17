package at.pavlov.cannons.dao;

import at.pavlov.cannons.Cannons;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.Statement;

public class CreateTableTask extends BukkitRunnable {
    @Override
    public void run() {
        String sql1 = String.format("CREATE TABLE IF NOT EXISTS %s (" +
                        "id VARCHAR(40) PRIMARY KEY, " +
                        "name VARCHAR(20) NOT NULL," +
                        "owner VARCHAR(40) NOT NULL," +
                        "world VARCHAR(40) NOT NULL," +
                        "cannon_direction VARCHAR(20)," +
                        "loc_x INTEGER," +
                        "loc_y INTEGER," +
                        "loc_z INTEGER," +
                        "soot DOUBLE," +
                        "gunpowder INTEGER," +
                        "projectile_id VARCHAR(40)," +
                        "projectile_pushed INTEGER," +
                        "cannon_temperature DOUBLE," +
                        "cannon_temperature_timestamp BIGINT," +
                        "horizontal_angle DOUBLE," +
                        "vertical_angle DOUBLE," +
                        "design_id VARCHAR(20)," +
                        "fired_cannonballs BIGINT," +
                        "target_mob BOOLEAN," +
                        "target_player BOOLEAN," +
                        "target_cannon BOOLEAN," +
                        "target_other BOOLEAN," +
                        "paid BOOLEAN)"
                , Cannons.getPlugin().getCannonDatabase());
        String sql2 = String.format("CREATE TABLE IF NOT EXISTS %s (" +
                        "cannon_bean_id VARCHAR(40) NOT NULL," +
                        "player VARCHAR(40) NOT NULL," +
                        "CONSTRAINT UC_Whitelist UNIQUE (cannon_bean_id, player)," +
                        "FOREIGN KEY (cannon_bean_id) REFERENCES %s (id) " +
                        "ON UPDATE CASCADE " +
                        "ON DELETE CASCADE" +
                        ")"
                , Cannons.getPlugin().getWhitelistDatabase(), Cannons.getPlugin().getCannonDatabase());
        try (Statement statement = Cannons.getPlugin().getConnection().createStatement()) {
            statement.execute(sql1);
            statement.execute(sql2);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
