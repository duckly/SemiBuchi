package operation.inclusion;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import automata.BuchiWa;
import automata.IBuchiWa;
import automata.IState;
import automata.IStateWa;
import complement.BuchiWaComplement;
import complement.IBuchiComplement;
import complement.StateWaNCSB;
import main.TaskInclusion;
import util.IntIterator;


public abstract class BuchiInclusion implements IBuchiInclusion{
	
	protected final IBuchiWa mFstOperand;
	protected final IBuchiWa mSndOperand;
	protected final BuchiWaComplement mSndComplement;
	
	protected final IBuchiWa mResult;
	// use antichain to accelerate inclusion check
	protected final TaskInclusion mTask;
	
	protected BuchiInclusion(TaskInclusion task, IBuchiWa fstOp, IBuchiWa sndOp) {
		this.mTask = task;
		this.mFstOperand = fstOp;
		this.mSndOperand = sndOp;
		this.mSndComplement = new BuchiWaComplement(sndOp);
		this.mResult = new BuchiWa(fstOp.getAlphabetSize());
		this.mTask.setOperation(this);
		computeInitalStates();
	}
	
	private void computeInitalStates() {
		IntIterator fstIter = mFstOperand.getInitialStates().iterator();
		while(fstIter.hasNext()) {
		    int fst = fstIter.next();
		    IntIterator sndIter = mSndComplement.getInitialStates().iterator();
		    while(sndIter.hasNext()) {
		    	int snd = sndIter.next();
				StateWaNCSB sndState = (StateWaNCSB)mSndComplement.getState(snd);
				InclusionPairNCSB pair = new InclusionPairNCSB(fst, sndState);
				IStateWa state = getOrAddState(pair);
				mResult.setInitial(state);
			}
		}
	}

	protected final Map<InclusionPairNCSB, IStateWa> mPairStateMap = new HashMap<>();
	protected final BitSet mFstFinalStates = new BitSet();
	protected final BitSet mSndFinalStates = new BitSet();
	protected final List<InclusionPairNCSB> mPairNCSBArray = new ArrayList<>();
	
	protected IStateWa getOrAddState(InclusionPairNCSB pair) {
		IStateWa state = mPairStateMap.get(pair);
		if(state == null) {
			state = mResult.addState();
			mPairNCSBArray.add(pair);
			mPairStateMap.put(pair, state);
			if(mFstOperand.isFinal(pair.getFstElement())) mFstFinalStates.set(state.getId());
			if(mSndComplement.isFinal(pair.getSndElement())) mSndFinalStates.set(state.getId());
		}
		return state;
	}
	
//	protected abstract boolean isValidPair(InclusionPairNCSB pair);
	
	
	@Override
	public IBuchiWa getFstBuchi() {
		return mFstOperand;
	}
	
	@Override
	public IBuchiWa getSndBuchi() {
		return mSndOperand;
	}

	@Override
	public IBuchiComplement getSndBuchiComplement() {
		return mSndComplement;
	}
	
	@Override
	public IBuchiWa getBuchiDifference() {
		return mResult;
	}

}
