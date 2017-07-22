package at.pavlov.cannons.dao;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Statement;
import java.util.UUID;

public class DeleteCannonTask extends BukkitRunnable{
    private UUID cannonId = null;
    private UUID playerId = null;
    public DeleteCannonTask(){
        this.cannonId = null;
        this.playerId = null;
    }

    public DeleteCannonTask(UUID cannonId){
        this.cannonId = cannonId;
        this.playerId = null;
    }

    public DeleteCannonTask(UUID playerId, boolean player){
        this.cannonId = null;
        this.playerId = playerId;
    }


    @Override
    public void run() {
        try (Statement statement = Cannons.getPlugin().getConnection().createStatement()) {
            if (cannonId == null && playerId == null){
                statement.executeUpdate(String.format("DELETE FROM %s", Cannons.getPlugin().getCannonDatabase()));
            }
            else if (cannonId != null) {
                statement.executeUpdate(String.format("DELETE FROM %s WHERE id=%s", Cannons.getPlugin().getCannonDatabase(), cannonId));
            }
            else{
                statement.executeUpdate(String.format("DELETE FROM %s WHERE owner=%s", Cannons.getPlugin().getCannonDatabase(), playerId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
