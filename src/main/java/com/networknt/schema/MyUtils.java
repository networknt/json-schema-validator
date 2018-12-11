package com.networknt.schema;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MyUtils {
    public static class LatencyTimer extends Thread {
        private static char noname = 'A';
        private final String name;
        private LatPrinter printer;
        public AtomicLong[] bins = new AtomicLong[4000];
        public AtomicLong maxNanos;
        public AtomicLong lastCount = new AtomicLong(0);

        double[] pTiles = new double[]{1, 50, 75, 90, 95, 99, 99.9};


        public LatencyTimer(String name) {
            this(new LatDefaultPrinter(), name);
        }


        public LatencyTimer(Class name) {
            this(new LatDefaultPrinter(), name.getName());
        }
        public LatencyTimer() {
            this(new LatDefaultPrinter(), ""+noname);
            noname += 1;
        }

        public LatencyTimer(LatPrinter p) {
            this(p, "noname");
        }

        public LatencyTimer(LatPrinter p, String name) {
            this.name = name;
            this.printer = p;
            reset();
            setDaemon(true);
            start();
        }

        public void setPrinter(LatPrinter printer) {
            this.printer = printer;
        }

        public void count(long latencyNanos) {
            int index = 0;
            maxNanos.set(Math.max(maxNanos.get(), latencyNanos));
            while(latencyNanos >= 1000) {
                latencyNanos /= 1000;
                index+=1000;
            }


            bins[(int) Math.min(index + latencyNanos, bins.length-1)].incrementAndGet();
        }

        public void count() {
            long now = System.nanoTime();
            count(Math.max(now-lastCount.get(), 0));
            lastCount.set(now);
        }

        public void reset() {
            for(int i=0; i<bins.length; i++) {
                bins[i] = new AtomicLong(0);
            }
            maxNanos = new AtomicLong(0);
        }

        AtomicBoolean die = new AtomicBoolean(false);
        public void die(){
            die.set(true);
        }
        @Override
        public void run() {
            while(true) {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(die.get()) {
                    return;
                }
                printer.log(name, snap());
            }
        }



        public interface LatPrinter {
            void log(String name, LatRet ret);
        }


        public void doLog() {
            printer.log(name, snap());
        }

        private LatRet snap() {
            long[] mybins = new long[bins.length];
            long mytotal = 0;
            for(int i=0; i<bins.length; i++) {
                mybins[i] = bins[i].get();
                mytotal+=mybins[i];
            }

            long myMaxNanos = maxNanos.get();

            double[] nanos = new double[pTiles.length];
            int index = 0;
            long cumulative = 0;
            for(int i=0; i<pTiles.length; i++) {
                long max = (long)((mytotal*pTiles[i])/100.0);
                while(index < mybins.length && mybins[index] + cumulative <  max) {
                    cumulative+=mybins[index];
                    index++;
                }

                long mul = 1;
                int temp = index;
                while(temp >= 1000) {
                    temp -= 1000;
                    mul *= 1000;
                }
                nanos[i] = (temp+1)*mul;
            }
            reset();
            return new LatRet(mytotal, myMaxNanos, nanos, pTiles);
        }

        public static class LatDefaultPrinter implements LatPrinter {
            @Override
            public void log(String name, LatRet ret) {
                System.out.println(name +","+ret);
            }
        }


        public static class LatRet implements Serializable {
            public double[] nanos;
            public double[] pTiles;
            public long total;
            public long maxNanos;
            public long snapTimeMillis;

            @Override
            public String toString() {
                if(total==0) {
                    return "No data points";
                }
                DecimalFormat df = new DecimalFormat("###.##");
                String s = String.format("max:%s", timeFormat(maxNanos, df));
                for(int i = nanos.length-1; i>=0; i--) {
                    s+=pTiles[i]+"%:"+timeFormat(nanos[i], df);
                }
                return s;
            }

            public static String timeFormat(double t, DecimalFormat df) {
                if(t<1000) {
                    return df.format(t)+"ns ";
                } else if (t<1000000) {
                    return df.format(t/1000)+"us ";
                } else if (t<1000000000){
                    return df.format(t/1000000)+"ms ";
                } else {
                    return df.format(t/1000000000)+"s ";
                }
            }
            //for objectmapper
            public LatRet(){}
            public LatRet(long total, long maxNanos, double[] nanos, double[] pTiles) {
                this.nanos = nanos;
                this.pTiles = pTiles;
                this.total = total;
                this.maxNanos = maxNanos;
                this.snapTimeMillis = System.currentTimeMillis();
            }
        }
    }

}
