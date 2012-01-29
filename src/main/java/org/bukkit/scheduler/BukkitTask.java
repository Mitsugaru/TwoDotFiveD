package org.bukkit.scheduler;

/**
 * Represents a task being executed by the scheduler
 */

public interface BukkitTask {

    /**
     * Returns the taskId for the task
     *
     * @return Task id number
     */
    public int getTaskId();

    /**
     * Returns true if the Task is a sync task
     *
     * @return true if the task is run by main thread
     */
    public boolean isSync();
}
