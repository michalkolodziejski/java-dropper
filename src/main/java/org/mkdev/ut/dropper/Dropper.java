package org.mkdev.ut.dropper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Michał Kołodziejski &lt;<I><A href="mailto:michal.kolodziejski@gmail.com">michal.kolodziejski@gmail.com</A></I>&gt;
 * @version 1.0
 * @license: GPLv3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 * @since: 2014-09-29
 */
public class Dropper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dropper.class);

    private ConcurrentHashMap<Method, VO> channelCount = new ConcurrentHashMap<>();

    public void releaseThread() throws DropCountExceededException {
        Method callingMethod = queryCallingMethod();

        if (channelCount.get(callingMethod).getThreadCount().get() == 0) {
            return;
        }

        channelCount.get(callingMethod).getThreadCount().decrementAndGet();

        LOGGER.debug("threads in queue: {}", channelCount.get(callingMethod).getThreadCount().get());
    }

    private Method queryCallingMethod() throws DropCountExceededException {
        Method callingMethod;
        try {
            callingMethod = getCallingMethod();
        } catch (ClassNotFoundException e) {
            throw new DropCountExceededException("Calling class not found!");
        }

        if (callingMethod == null) {
            throw new DropCountExceededException("Calling method has not been annotated!");
        }
        return callingMethod;
    }

    private Method getCallingMethod() throws ClassNotFoundException {
        final Thread t = Thread.currentThread();
        final StackTraceElement[] stackTrace = t.getStackTrace();

        String methodName = stackTrace[4].getMethodName();

        int i = 0;
        for (StackTraceElement stackTraceElement : stackTrace) {
            Class<?> clazz = Class.forName(stackTraceElement.getClassName());

            for (Method candidate : clazz.getDeclaredMethods()) {
                if (candidate.getName().equals(methodName)) {
                    LOGGER.debug("--> [{}] {}", i++, stackTraceElement.getMethodName());
                    return candidate;
                }
            }
        }

        return null;
    }

    public synchronized void checkThread(Callable workToDo, Callable callback, Integer limitRate) throws DropCountExceededException {

        if (workToDo != null) {
            try {
                this.checkThread(null, callback, null);

                workToDo.call();

                this.releaseThread();
            } catch (Exception e) {
                throw new DropCountExceededException(e);
            }
        } else {

            Method callingMethod = queryCallingMethod();

            LOGGER.debug("method = {} : {}", callingMethod, channelCount.containsKey(callingMethod));

            Integer localLimitRate;

            if (callingMethod.getAnnotation(LimitRate.class) != null) {
                localLimitRate = callingMethod.getAnnotation(LimitRate.class).value();
            } else {
                if (callingMethod.getAnnotation(LimitRateProperty.class) != null) {
                    String propertyName = callingMethod.getAnnotation(LimitRateProperty.class).name();

                    localLimitRate = Integer.parseInt(System.getProperty(propertyName));
                } else {
                    localLimitRate = limitRate;
                }
            }

            if (localLimitRate == null) {
                throw new DropCountExceededException("Could not evaluate LimitRate value!");
            }

            if (channelCount.containsKey(callingMethod)) {
                if (channelCount.get(callingMethod).getMaxCount() != localLimitRate) {
                    channelCount.remove(callingMethod);
                    channelCount.put(callingMethod, new VO(localLimitRate));
                }
            } else {
                channelCount.put(callingMethod, new VO(localLimitRate));
            }

            LOGGER.debug("[{}] maxCount:{}", callingMethod, channelCount.get(callingMethod).getMaxCount());
            LOGGER.debug("[{}] threadCount:{}", callingMethod, channelCount.get(callingMethod).getThreadCount());

            if (channelCount.get(callingMethod).getThreadCount().get() < channelCount.get(callingMethod).getMaxCount()) {
                channelCount.get(callingMethod).getThreadCount().incrementAndGet();
            } else {
                if (callback == null) {
                    throw new DropCountExceededException();
                } else {
                    callback.call();
                }
            }
        }
    }

    private class VO {
        private int maxCount;
        private AtomicInteger threadCount;

        public VO(int maxCount) {
            this.maxCount = maxCount;
            threadCount = new AtomicInteger();
        }

        public int getMaxCount() {
            return maxCount;
        }

        public AtomicInteger getThreadCount() {
            return threadCount;
        }
    }
}
