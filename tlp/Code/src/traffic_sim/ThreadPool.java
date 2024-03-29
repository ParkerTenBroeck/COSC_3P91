package traffic_sim;


import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadPool {


    private final ArrayList<Runnable> next = new ArrayList<>();
    private final AtomicInteger count = new AtomicInteger();

    public ThreadPool(){
        for(int i = 0; i < Runtime.getRuntime().availableProcessors() - 1; i ++)
            new Thread(() -> {
                while(true){
                    Runnable run;
                    synchronized (next){
                        while(next.size() <= 1){
                            try{
                                next.wait();
                            }catch (Exception ignore){}
                        }
                        run = next.remove(next.size()-1);
                    }
                    run.run();
                    count.getAndDecrement();
                }
            }).start();
    }

    /**
     * @param run Something we want to run in the thread pool
     */
    public void add(Runnable run){
        count.getAndIncrement();
        synchronized (this.next){
            this.next.add(run);
            this.next.notifyAll();
        }
    }

    /**
     * Join on the currently running tasks on the thread pool and wait for them all to finish executing
     */
    public void join(){

        while(true){
            Runnable run;
            synchronized (this.next){
                if(this.next.isEmpty()){
                    break;
                }
                run = this.next.remove(this.next.size()-1);
            }
            run.run();
            count.getAndDecrement();
        }

        while(count.get() != 0){
            Thread.onSpinWait();
        }
    }
}
