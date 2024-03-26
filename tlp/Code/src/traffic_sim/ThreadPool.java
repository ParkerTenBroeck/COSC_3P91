package traffic_sim;


import java.util.ArrayList;

public final class ThreadPool {

    private volatile int count;
    public final int max;
    private Runnable bruh;
    private final ArrayList<Runnable> next = new ArrayList<>();

    public ThreadPool(){
        var pool = this;
        this.max = Runtime.getRuntime().availableProcessors();
        for(int i = 0; i < this.max - 1; i ++)
            new Thread(() -> {
                while(true){
                    Runnable run;
                    synchronized (pool.next){
                        while(pool.next.isEmpty()){
                            try{
                                pool.next.wait();
                            }catch (Exception ignore){}
                        }
                        run = pool.next.remove(pool.next.size()-1);
                    }
//                    System.out.println("Thread started");
                    run.run();
                    synchronized (pool){
                        pool.count -= 1;
                        pool.notify();
                    }
                }
            }).start();
    }

    public void add(Runnable run){
        if(bruh != null){
            synchronized (this){
                this.count += 1;
            }
            synchronized (this.next){
                this.next.add(run);
                this.next.notifyAll();
            }
        }else{
            bruh = run;
        }
    }

    public void join(){

        while(true){
            Runnable run;
            synchronized (this.next){
                if(this.next.isEmpty()){
                    break ;
                }
                run = this.next.remove(this.next.size()-1);
            }
            run.run();
            synchronized (this){
                this.count -= 1;
            }
        }


        if(bruh != null)
            bruh.run();
        bruh = null;


        while(this.count != 0){
            Thread.onSpinWait();

        }
//        System.out.println("Finished\n\n\n");
    }

//    public void
}
