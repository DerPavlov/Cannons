package at.pavlov.cannons.utils;

public abstract class DelayedTask implements Runnable {
	private final Object wrapper;

    public DelayedTask(Object wrapper) {
        this.wrapper = wrapper;
    }
   
    @Override
    public final void run() {
        run(wrapper);
    }
   
    public abstract void run(Object wrapper2);
}
