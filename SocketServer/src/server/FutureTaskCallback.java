package server;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

class FutureTaskCallback<T> extends FutureTask<T> {
    FutureTaskCallback(Callable<T> callable) {
        super(callable);
    }

    public void done() {
        String msg = "Result: ";
        if (isCancelled()) {
            msg += "Cancelled.";
        } else {
            try {
                msg += get();
            } catch (Exception exc) {
                msg += exc.toString();
            }
        }
        System.out.println("\n" + msg + "\n");
    }
}