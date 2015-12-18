package org.nd4j.linalg.api.parallel.tasks.cpu;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.Op;
import org.nd4j.linalg.api.parallel.tasks.CPUTaskExecutor;
import org.nd4j.linalg.api.parallel.tasks.Task;
import org.nd4j.linalg.api.parallel.tasks.TaskExecutorProvider;
import org.nd4j.linalg.util.ArrayUtil;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseCPUAction implements Task<Void>, Future<Void> { //} extends RecursiveAction implements Task<Void> {

    //TODO: PADDING VARIABLES
    private long p0, p1, p2, p3, p4, p5, p6, p7;
    protected final AtomicInteger splitCount = new AtomicInteger(0);
    private long p8, p9, p10, p11, p12, p13, p14, p15;

    protected final int threshold;
    protected int maxSplits;
    protected int n;
    protected int offsetX;
    protected int offsetY;
    protected int offsetZ;
    protected int incrX;
    protected int incrY;
    protected int incrZ;

    protected CountDownLatch latch;

    public BaseCPUAction(int threshold, int n, int offsetX, int offsetY, int offsetZ, int incrX, int incrY, int incrZ) {
        this.threshold = threshold;
        this.n = n;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.incrX = incrX;
        this.incrY = incrY;
        this.incrZ = incrZ;

        if(n % threshold == 0) maxSplits = n / threshold;
        else maxSplits = n / threshold + 1;
        latch = new CountDownLatch(maxSplits);
    }

    public BaseCPUAction(Op op, int threshold) {
        this.threshold = threshold;
        this.n = op.x().length();
        this.offsetX = op.x().offset();
        this.offsetY = (op.y() != null ? op.y().offset() : 0);
        this.offsetZ = (op.z() != null ? op.z().offset() : 0);
        this.incrX = op.x().elementWiseStride();
        this.incrY = (op.y() != null ? op.y().elementWiseStride() : 0);
        this.incrZ = (op.z() != null ? op.z().elementWiseStride() : 0);

        if(n % threshold == 0) maxSplits = n / threshold;
        else maxSplits = n / threshold + 1;
        latch = new CountDownLatch(maxSplits);

        if (incrX == -1) {
            //Edge case: sometimes NDArray.elementWiseStride() returns -1, due to weird strides,
            //but every element is still separated by same amount in buffer
            //For example, a TransformOp with x.length() == x.data.length(), but x.stride() is not ascending/descending
            INDArray reshapeX = op.x().reshape(new int[]{1, ArrayUtil.prod(op.x().shape())});
            incrX = reshapeX.stride(1);
        }
        if (incrY == -1) {
            if (op.y() == op.x()) incrY = incrX;
            else {
                INDArray reshapeY = op.y().reshape(new int[]{1, ArrayUtil.prod(op.y().shape())});
                incrY = reshapeY.stride(1);
            }
        }
        if (incrZ == -1) {
            if (op.z() == op.x()) incrZ = incrX;
            else {
                INDArray reshapeZ = op.z().reshape(new int[]{1, ArrayUtil.prod(op.z().shape())});
                incrY = reshapeZ.stride(1);
            }
        }
    }

    @Override
    public void invokeAsync() {
        CPUTaskExecutor.getInstance().executeAsync(this);
    }

    @Override
    public Void blockUntilComplete() {
        try{
            return this.get();
        }catch(InterruptedException | ExecutionException e){
            throw new RuntimeException(e);
        }
    }


    @Override
    public Void invokeBlocking() {
        invokeAsync();
        return blockUntilComplete();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return latch.getCount() <= 0;
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        latch.await();
        return null;
    }

    @Override
    public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        latch.await(timeout, unit);
        return null;
    }
}