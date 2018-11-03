package DataStructure;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by root on 18. 5. 20.
 */

public class TSafeQueue {

    public static BlockingQueue<AudioChunk> INbufferQueue = new ArrayBlockingQueue<AudioChunk>(300);

    public  static   void addToINbufferQueue(AudioChunk chunk) throws InterruptedException {
        TSafeQueue.INbufferQueue.put(chunk);
    }

    public static synchronized AudioChunk removeFromINbufferQueue() throws InterruptedException {
        AudioChunk chunk = TSafeQueue.INbufferQueue.take();
        return chunk;
    }
}
