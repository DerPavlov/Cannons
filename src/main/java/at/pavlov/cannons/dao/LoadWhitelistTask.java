package at.pavlov.cannons.dao;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

public class LoadWhitelistTask extends BukkitRunnable {
    private UUID cannonId;

    public LoadWhitelistTask(UUID cannonId){
        this.cannonId = cannonId;
    }

    @Override
    public void run() {
        //add whitelist
        try (Statement statement = Cannons.getPlugin().getConnection().createStatement()) {
            ResultSet rs = statement.executeQuery(
                    String.format("SELECT * FROM %s WHERE cannon_bean_id='%s'", Cannons.getPlugin().getWhitelistDatabase(), cannonId)
            );

            Cannon cannon = CannonManager.getCannon(cannonId);
            if (cannon == null){
                return;
            }

            while (rs.next()) {
                if (rs.getString("player") !=null)
                    cannon.addWhitelistPlayer(UUID.fromString(rs.getString("player")));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
