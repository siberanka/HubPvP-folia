package me.quared.hubpvp.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class TaskScheduler {

	private final JavaPlugin plugin;
	private final boolean foliaSupported;
	private final Method playerGetSchedulerMethod;
	private final Method schedulerRunAtFixedRateMethod;
	private final Method scheduledTaskCancelMethod;

	public TaskScheduler(JavaPlugin plugin) {
		this.plugin = plugin;

		Method getScheduler = null;
		Method runAtFixedRate = null;
		Method cancel = null;
		boolean folia = false;

		try {
			getScheduler = Player.class.getMethod("getScheduler");
			Class<?> entitySchedulerClass = getScheduler.getReturnType();
			runAtFixedRate = findRunAtFixedRateMethod(entitySchedulerClass);
			Class<?> scheduledTaskClass = runAtFixedRate.getReturnType();
			cancel = scheduledTaskClass.getMethod("cancel");
			folia = true;
		} catch (ReflectiveOperationException ignored) {
			// Paper API fallback uses Bukkit scheduler.
		}

		this.playerGetSchedulerMethod = getScheduler;
		this.schedulerRunAtFixedRateMethod = runAtFixedRate;
		this.scheduledTaskCancelMethod = cancel;
		this.foliaSupported = folia;
	}

	public CancellableTask runPlayerTimer(Player player, Runnable task, long delayTicks, long periodTicks) {
		if (foliaSupported) {
			try {
				Object scheduler = playerGetSchedulerMethod.invoke(player);
				Object scheduledTask = schedulerRunAtFixedRateMethod.invoke(
						scheduler,
						plugin,
						(Consumer<Object>) ignored -> task.run(),
						null,
						delayTicks,
						periodTicks
				);
				if (scheduledTask == null) {
					BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
					return bukkitTask::cancel;
				}
				return () -> {
					try {
						scheduledTaskCancelMethod.invoke(scheduledTask);
					} catch (ReflectiveOperationException ignored) {
					}
				};
			} catch (ReflectiveOperationException ignored) {
				// Fall through to Bukkit scheduler.
			}
		}

		BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
		return bukkitTask::cancel;
	}

	private Method findRunAtFixedRateMethod(Class<?> schedulerClass) throws ReflectiveOperationException {
		for (Method method : schedulerClass.getMethods()) {
			if (!method.getName().equals("runAtFixedRate")) continue;
			if (method.getParameterCount() != 5) continue;
			Class<?>[] params = method.getParameterTypes();
			if (Plugin.class.isAssignableFrom(params[0]) && Consumer.class.isAssignableFrom(params[1])
					&& Runnable.class.isAssignableFrom(params[2])) {
				return method;
			}
		}
		throw new NoSuchMethodException("Could not find runAtFixedRate method on scheduler.");
	}

}
