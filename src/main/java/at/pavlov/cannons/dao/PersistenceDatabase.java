package at.pavlov.cannons.dao;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class PersistenceDatabase
{
	private Cannons plugin;
	private BukkitTask saveTask = null;

	public PersistenceDatabase(Cannons _plugin)
	{
		plugin = _plugin;
	}

	public void createTables(){
		CreateTableTask createTableTask = new CreateTableTask();
		createTableTask.run();
	}

    /**
     * loads all cannons from the database
     *
     * @return true if loading was successful
     */
	public void loadCannons()
	{
	    if (!plugin.hasConnection()) {
            plugin.logSevere("No connection to database");
            return;
        }
		plugin.getCannonManager().clearCannonList();

	    LoadCannonTask task = new LoadCannonTask();
	    task.runTaskAsynchronously(plugin);
	}

	public void saveAllCannons(boolean async){
		if (!plugin.hasConnection()) {
			plugin.logSevere("No connection to database");
			return;
		}
		SaveCannonTask saveCannonTask = new SaveCannonTask();
		if (async)
			saveTask = saveCannonTask.runTaskAsynchronously(plugin);
		else
			saveCannonTask.run();
    }

    public void saveCannon(Cannon cannon){
		if (!plugin.hasConnection()) {
			plugin.logSevere("No connection to database");
			return;
		}
		SaveCannonTask saveCannonTask = new SaveCannonTask(cannon.getUID());
		saveTask = saveCannonTask.runTaskAsynchronously(plugin);
	}

	public boolean isSaveTaskRunning() {
		return saveTask != null && Bukkit.getScheduler().isCurrentlyRunning(saveTask.getTaskId());
	}

    public void deleteCannon(UUID cannon_id){
		if (!plugin.hasConnection()) {
			plugin.logSevere("No connection to database");
			return;
		}
		DeleteCannonTask deleteCannonTask = new DeleteCannonTask(cannon_id);
		deleteCannonTask.runTaskAsynchronously(plugin);
	}

	public void deleteAllCannons(){
		if (!plugin.hasConnection()) {
			plugin.logSevere("No connection to database");
			return;
		}
		DeleteCannonTask deleteCannonTask = new DeleteCannonTask();
		deleteCannonTask.runTaskAsynchronously(plugin);
	}

	public void deleteCannons(UUID player_id){
		if (!plugin.hasConnection()) {
			plugin.logSevere("No connection to database");
			return;
		}
		DeleteCannonTask deleteCannonTask = new DeleteCannonTask(player_id, true);
		deleteCannonTask.runTaskAsynchronously(plugin);
	}
}
