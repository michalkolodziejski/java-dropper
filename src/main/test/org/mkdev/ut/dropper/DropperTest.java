package org.mkdev.ut.dropper;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michał Kołodziejski &lt;<I><A href="mailto:michal.kolodziejski@gmail.com">michal.kolodziejski@gmail.com</A></I>&gt;
 * @version 1.0
 * @license: GPLv3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 * @since: 2014-09-29
 */
public class DropperTest {

    Dropper dropper;

    @Before
    public void setUp() throws Exception {
        dropper = new Dropper();
    }

    @Test
    public void testCountWithinLimit() throws Exception {
        for(int i=0;i<5;i++) {
            spawnThread(i);

            Thread.sleep(500);
        }
    }

    @Test(expected = DropCountExceededException.class)
    public void testCountExceeded() throws Exception {
        for(int i=0;i<7;i++) {
            spawnThread(i);

            Thread.sleep(50);
        }
    }

    @Test
    public void testCountWithinLimitCallable() throws Exception {
        for(int i=0;i<5;i++) {
            spawnThreadCallable(i);

            Thread.sleep(500);
        }
    }

    @Test(expected = DropCountExceededException.class)
    public void testCountExceededCallable() throws Exception {
        for(int i=0;i<7;i++) {
            spawnThreadCallable(i);

            Thread.sleep(50);
        }
    }

    @Test
    public void testCountWithinLimitWithoutAnnotation() throws Exception {
        for(int i=0;i<5;i++) {
            spawnThreadWithoutAnnotation(i);

            Thread.sleep(500);
        }
    }

    @Test(expected = DropCountExceededException.class)
    public void testCountExceededWithoutAnnotation() throws Exception {
        for(int i=0;i<7;i++) {
            spawnThreadWithoutAnnotation(i);

            Thread.sleep(50);
        }
    }


    private void spawnThread(final int i) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doWork(i);
                } catch (DropCountExceededException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void spawnThreadCallable(final int i) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doWorkCallable(i);
                } catch (DropCountExceededException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void spawnThreadWithoutAnnotation(final int i) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doWorkWithoutAnnotation(i);
                } catch (DropCountExceededException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @LimitRate(value = 5)
    private void doWork(int i) throws DropCountExceededException {
        dropper.checkThread(null, null, null);
        System.out.println((i+1));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dropper.releaseThread();
    }

    @LimitRate(value = 5)
    private void doWorkCallable(final int i) throws DropCountExceededException {

        dropper.checkThread(new Callable() {
            @Override
            public void call() {
                System.out.println((i+1));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, null, null);

    }

    private void doWorkWithoutAnnotation(int i) throws DropCountExceededException {
        dropper.checkThread(null, null, 5);
        System.out.println((i+1));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dropper.releaseThread();
    }
}