package me.quared.hubpvp.scheduler;

@FunctionalInterface
public interface CancellableTask {

	void cancel();

}
