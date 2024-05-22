package at.pavlov.cannons.dao;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.projectile.ProjectileStorage;
import at.pavlov.cannons.scheduler.CreateCannon;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

public class LoadCannonTask extends BukkitRunnable{
    public LoadCannonTask(){

    }

    @Override
    public void run() {
        ArrayList<UUID> invalid = new ArrayList<>();
        int i = 0;

        try (Statement statement = Cannons.getPlugin().getConnection().createStatement()) {
            // create a query that returns CannonBean

            ResultSet rs = statement.executeQuery(
                    String.format("SELECT * FROM %s", Cannons.getPlugin().getCannonDatabase())
            );

            // found cannons - load them
            while (rs.next()) {
                UUID cannon_id = UUID.fromString(rs.getString("id"));
                //check if cannon design exists
                CannonDesign design = Cannons.getPlugin().getCannonDesign(rs.getString("design_id"));
                if (design == null) {
                    Cannons.getPlugin().logDebug("Design " + rs.getString("design_id") + " not found in plugin/designs");
                    invalid.add(cannon_id);
                    continue;
                    //deleteCannon(bean.getId());
                }

                //load values for the cannon
                UUID world = UUID.fromString(rs.getString("world"));
                //test if world is valid
                World w = Bukkit.getWorld(world);
                if (w == null) {
                    Cannons.getPlugin().logDebug("World of cannon " + cannon_id + " is not valid");
                    invalid.add(cannon_id);
                    continue;
                }

                String owner_str = rs.getString("owner");
                if (owner_str == null) {
                    Cannons.getPlugin().logDebug("Owner of cannon " + cannon_id + " is null");
                    invalid.add(cannon_id);
                    continue;
                }

                UUID owner = UUID.fromString(owner_str);
                boolean isBanned = false;
                for (OfflinePlayer oplayer : Bukkit.getServer().getBannedPlayers()) {
                    if (oplayer.getUniqueId().equals(owner))
                        isBanned = true;
                }

                if (!Bukkit.getOfflinePlayer(owner).hasPlayedBefore() || isBanned) {
                    if (isBanned)
                        Cannons.getPlugin().logDebug("Owner of cannon " + cannon_id + " was banned");
                    else
                        Cannons.getPlugin().logDebug("Owner of cannon " + cannon_id + " does not exist");
                    invalid.add(cannon_id);
                    continue;
                }

                Vector offset = new Vector(rs.getInt("loc_x"), rs.getInt("loc_y"), rs.getInt("loc_z"));
                BlockFace cannonDirection = BlockFace.valueOf(rs.getString("cannon_direction"));

                //make a cannon
                Cannon cannon = new Cannon(design, world, offset, cannonDirection, owner);
                // cannon created - load properties
                cannon.setUID(cannon_id);
                cannon.setCannonName(rs.getString("name"));
                cannon.setSoot(rs.getDouble("soot"));
                cannon.setLoadedGunpowder(rs.getInt("gunpowder"));

                //load projectile
                cannon.setLoadedProjectile(ProjectileStorage.getProjectile(cannon, rs.getString("projectile_id")));

                cannon.setProjectilePushed(rs.getInt("projectile_pushed"));

                //angles
                cannon.setHorizontalAngle(rs.getDouble("horizontal_angle"));
                cannon.setVerticalAngle(rs.getDouble("vertical_angle"));

                //temperature
                cannon.setTemperature(rs.getDouble("cannon_temperature"));
                cannon.setTemperatureTimeStamp(rs.getLong("cannon_temperature_timestamp"));

                //amount of fired cannonballs
                cannon.setFiredCannonballs(rs.getLong("fired_cannonballs"));

                //load targets
                cannon.setTargetMob(rs.getBoolean("target_mob"));
                cannon.setTargetPlayer(rs.getBoolean("target_player"));
                cannon.setTargetCannon(rs.getBoolean("target_cannon"));
                cannon.setTargetOther(rs.getBoolean("target_other"));

                // cannon fee
                cannon.setPaid(rs.getBoolean("paid"));

                //add a cannon to the cannon list
                //BukkitTask task = new CreateCannon(Cannons.getPlugin(), cannon, false).runTask(Cannons.getPlugin());
                //plugin.createCannon(cannon);
                i++;

            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //delete invalid cannons
        try (Statement statement = Cannons.getPlugin().getConnection().createStatement()) {

            for (UUID inv : invalid) {
                statement.addBatch(String.format("DELETE FROM %s WHERE id='%s'", Cannons.getPlugin().getCannonDatabase(), inv.toString()));
                Cannons.getPlugin().logDebug("Delete cannon " + inv);
            }
            statement.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Cannons.getPlugin().logDebug(i + " cannons loaded from the database");

    }
}
