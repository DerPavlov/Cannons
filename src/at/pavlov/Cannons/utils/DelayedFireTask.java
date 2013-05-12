package at.pavlov.Cannons.utils;

public abstract class DelayedFireTask implements Runnable{
	Object wrapper;
	
    public DelayedFireTask(Object wrapper) {
        this.wrapper = wrapper;
    }
   
    @Override
    public final void run() {
        run(wrapper);
    }
   
    public abstract void run(Object wrapper2);
}
