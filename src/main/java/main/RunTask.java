package main;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class RunTask {
	
	private final long mTimeBound;
	private final ITask mTask;
	
	public RunTask(ITask task, long time) {
		assert task != null;
		mTask = task;
		mTimeBound = time;
	}
	
	public long getTimeBound() {
		return mTimeBound;
	}
	
	public void execute() {
		ResultValue resultValue = null;
        final ExecutorService service = Executors.newSingleThreadExecutor();
        Future<ResultValue> f = null;
        long duration = System.currentTimeMillis();
        try {
            f = service.submit(new Task(mTask));
            resultValue = f.get(mTimeBound, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
        	resultValue = ResultValue.EXE_TIMEOUT;
        } catch (final OutOfMemoryError e) {
        	resultValue = ResultValue.EXE_MEMOOUT;
        } catch (final ExecutionException | InterruptedException e) {
        	if(e.getCause() instanceof OutOfMemoryError
        	|| e.getCause() instanceof StackOverflowError) {
        		resultValue = ResultValue.EXE_MEMOOUT;
        	}else if(e.getCause() instanceof TimeoutException){
        		resultValue = ResultValue.EXE_TIMEOUT;
        	}else {
        		e.printStackTrace();
        		resultValue = ResultValue.EXE_UNKNOWN;
        	}
        } finally {
//        	while(!f.isCancelled()) f.cancel(true);
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
        duration = System.currentTimeMillis() - duration;
        // set result value
        mTask.setResultValue(resultValue);
        mTask.setRunningTime(duration);
	}
	
	private class Task implements Callable<ResultValue> {
		
		private ITask mTask;
		
		public Task(ITask task) {
			mTask = task;
		}

		@Override
		public ResultValue call() throws Exception,OutOfMemoryError, StackOverflowError {
			try {
				mTask.runTask();
			}catch (final OutOfMemoryError | StackOverflowError e) {
	            throw e;
	        }catch (final Exception e) {
	        	throw e;
	        }
			return mTask.getResultValue();
		}
		
	}

}
