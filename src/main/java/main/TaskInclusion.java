package main;

import operation.inclusion.IBuchiInclusion;
import util.Timer;

public class TaskInclusion implements ITask {

	private static final String[] COLUMN_NAMES = {
			"FILE"
			//, "TIME_LIMIT"
			, "LHS_SEMIDETERMINISTIC"
			, "RHS_SEMIDETERMINISTIC"
			, "LHS_STATES"
			, "RHS_STATES"
			, "LHS_TRANS"
			, "RHS_TRANS"
			, "ALPHABET_SIZE" // shoud be the same as RHS_ALPHABET
//			, "RHS_ALPHABET"
			, "PAIR_REJ_ANTICHAIN"
			, "PAIR_DEL_ANTICHAIN"
			, "PAIR_LEFT_ANTICHAIN"
			, "TRANS_USED_SND_BUCHI"
			, "RESULT_STATES"
			, "ALGORITHM"
			, "RUNTIME(ms)"
			, "RESULT"
			};
	
	private String mFileName;
	
	private final long mTimeLimit;
	
	private boolean mIsLHSSemiDet;
	
	private boolean mIsRHSSemiDet;
	
	private int mLHSStateNum;
	
	private int mRHSStateNum;
	
	private int mLHSTransNum;
	
	private int mRHSTransNum;
	
	private String mOperation;
	
	private long mRunTime;
	
	private Boolean mResult;
	
	private IBuchiInclusion mChecker;
	
	
	/**
	 *  some runtime data
	 *  */
	private int mNumPairsRejectedByAntichain; // number of pairs covered by some current pair
	
	private int mNumPairsDeletedInAntichain;  // number of pairs deleted since it is covered by a new pair
	
	private int mNumPairsInAntichain;         // number of pairs left in Antichain
	
	private int mNumTransUsedInSndBuchi;      // number of transition used in the second Buchi (no duplicate)
	
	
	
	public TaskInclusion(String fileName, final long timeLimit) {
		this.mFileName = fileName;
		this.mTimeLimit = timeLimit;
	}
	
	
	public static String getColumns() {
		StringBuilder sb = new StringBuilder();
		sb.append(COLUMN_NAMES[0]);
		
		for(int i = 1; i < COLUMN_NAMES.length; i ++) {
			sb.append("," + COLUMN_NAMES[i]);
		}
		return sb.toString();
	}
	
	public String toString() {
		assert mChecker != null;
		return mFileName 
//		+ "," + mTimeLimit
		+ "," + mIsLHSSemiDet
		+ "," + mIsRHSSemiDet
		+ "," + mLHSStateNum
		+ "," + mRHSStateNum
		+ "," + mLHSTransNum
		+ "," + mRHSTransNum
		+ "," + mChecker.getFstBuchi().getAlphabetSize()
//		+ "," + mChecker.getSndBuchi().getAlphabetSize()
		+ "," + mNumPairsRejectedByAntichain
		+ "," + mNumPairsDeletedInAntichain
		+ "," + mNumPairsInAntichain
		+ "," + mNumTransUsedInSndBuchi
		+ "," + mChecker.getBuchiDifference().getStateSize()
		+ "," + mOperation
		+ "," + mRunTime
		+ "," + mResult;
		
	}
	
	@Override
	public void runTask() {
		Timer timer = new Timer();
		timer.start();
		mResult = mChecker.isIncluded();
		timer.stop();
		mRunTime = timer.getTimeElapsed();
		// get sizes
		mLHSStateNum = mChecker.getFstBuchi().getStateSize();
		mRHSStateNum = mChecker.getSndBuchi().getStateSize();
		mLHSTransNum = mChecker.getFstBuchi().getNumTransition();
		mRHSTransNum = mChecker.getSndBuchi().getNumTransition();
		mIsLHSSemiDet = mChecker.getFstBuchi().isSemiDeterministic();
		mIsRHSSemiDet = mChecker.getSndBuchi().isSemiDeterministic();
		if(mChecker.getSndBuchiComplement() != null)
			mNumTransUsedInSndBuchi = mChecker.getSndBuchiComplement().getNumUsedOpTransition();
	}
	
	public void setOperation(IBuchiInclusion checker) {
		this.mChecker = checker;
		this.mOperation = checker.getName();
	}
	
	public IBuchiInclusion getOperation() {
		return mChecker;
	}
	
	@Override
	public long getRuntime() {
		return mRunTime;
	}
	
	@Override
	public long getTimeBound() {
		return mTimeLimit;
	}
	
	@Override
	public Boolean getResult() {
		return mResult;
	}
	
	@Override
	public void increaseRejPairByAntichain() {
		mNumPairsRejectedByAntichain ++;
	}
	
	@Override
	public void increaseDelPairInAntichain() {
		mNumPairsDeletedInAntichain ++;
	}
	
	@Override
	public void setNumPairInAntichain(int num) {
		mNumPairsInAntichain = num;
	}
	
//	public void useTransition() {
//		mNumTransUsedInSndBuchi
//	}
	
}