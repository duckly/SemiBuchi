package operation.inclusion;

import java.util.BitSet;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import automata.BuchiGeneral;
import automata.IState;
import automata.IBuchi;
import complement.StateNCSB;
import util.IPair;
import util.IntStack;
import util.PairXY;
import util.Timer;

/**
 * ASCC algorithm from the paper Comparison of Algorithms for Checking Emptiness on Buchi Automata
 *                by Andreas Gaiser and Stefan Schwoon
 * **/
public class BuchiInclusionASCC extends BuchiInclusion {
		
	public BuchiInclusionASCC(IBuchi fstOp, IBuchi sndOp) {
		super(fstOp, sndOp);
	}
		
	/**
	 * try to compute the product of mFstOperand and mSndComplement
	 * by constructing the complement of mSndOperand
	 * */
	
	public Boolean isIncluded(long timeLimit) {
		ASCC scc = new ASCC(timeLimit);
//		System.out.println(mResult.toString());
//		System.out.println(mFstFinalStates + ", " + mSndFinalStates);
//		System.out.println("acc:" + scc.getAcceptedSCC());
//		System.out.println(scc.getPrefix() + ", (" + scc.getLoop() + ")");
		return scc.mIsEmpty;
	}

	@Override
	public IPair<List<Integer>, List<Integer>> getCounterexampleWord() {
		return null;
	}

	@Override
	public IPair<List<IState>, List<IState>> getCounterexampleRun() {
		return null;
	}
	
	// ---------------------------- part for SCC decomposition -------------
	/**
	 * SCC Decomposition
	 * */
	private class ASCC {
		
		private int mIndex=0;
		private final Stack<PairXY<Integer, BitSet>> mRootsStack ;
		private final Map<Integer, Integer> mDfsNum;
		private final IntStack mActiveStack ;
		private final BitSet mCurrent;
		
		private Boolean mIsEmpty = true;
		
		private final long TIME_LIMIT;
		private final Timer mTimer;
		
		public ASCC(long timeLimit) {
			
			this.TIME_LIMIT = timeLimit;
			this.mRootsStack = new Stack<>();
			this.mActiveStack = new IntStack();
			this.mDfsNum = new HashMap<>();
			this.mCurrent = new BitSet();
			this.mTimer = new Timer();
			
			mTimer.start();
			for(int n = mResult.getInitialStates().nextSetBit(0);
					n >= 0;
					n = mResult.getInitialStates().nextSetBit(n + 1)) {

				if(! mDfsNum.containsKey(n)){
					dfs(n);
					if(mIsEmpty == null ||  ! mIsEmpty.booleanValue())
						return ;
				}
			}
		}
		
		private boolean terminate() {
			if(mTimer.tick() > TIME_LIMIT) 
				return true;
			return false;
		}
		
		
		// make use of tarjan algorithm
		void dfs(int s) {
			
			if(terminate()) {
				mIsEmpty = null;
				return ;
			}
			
			mIndex++;
			mDfsNum.put(s, mIndex);
			mActiveStack.push(s);
			mCurrent.set(s);
			
			InclusionPairNCSB pair = mPairNCSBArray.get(s); //v must in mPairArray
			// get flags
			BitSet flags = new BitSet();
			if(mFstOperand.isFinal(pair.getFstElement())) {
				flags.set(0);
			}
			
			if(pair.getSndElement().getBSet().isEmpty()) {
				flags.set(1);
			}
			
			mRootsStack.push(new PairXY<>(s, flags));
			
			//TODO only get enabled letters
			for(int letter = 0; letter < mFstOperand.getAlphabetSize(); letter ++) {
				// X states from first BA 
				BitSet fstSuccs = mFstOperand.getState(pair.getFstElement()).getSuccessors(letter);
				if(fstSuccs.isEmpty()) continue;
				// Y states from second BA
				BitSet sndSuccs = pair.getSndElement().getSuccessors(letter);
				for(int fstSucc = fstSuccs.nextSetBit(0); fstSucc >= 0; fstSucc = fstSuccs.nextSetBit(fstSucc + 1)) {
					for(int sndSucc = sndSuccs.nextSetBit(0); sndSucc >= 0; sndSucc = sndSuccs.nextSetBit(sndSucc + 1)) {
						// pair (X, Y)
						StateNCSB yState = (StateNCSB) mSndComplement.getState(sndSucc);
						InclusionPairNCSB pairSucc = new InclusionPairNCSB(fstSucc, yState);
						IState stateSucc = getOrAddState(pairSucc);
						mPairStateMap.get(pair).addSuccessor(letter, stateSucc.getId());
						if(! mDfsNum.containsKey(stateSucc.getId())){
							dfs(stateSucc.getId());
							if(mIsEmpty == null ||  ! mIsEmpty.booleanValue())
								return ;
						}else if(mCurrent.get(stateSucc.getId())){
							BitSet B = new BitSet();
							int u;
							do {
								PairXY<Integer, BitSet> p = mRootsStack.pop();
								B.or(p.getSndElement());
								if(B.cardinality() == 2) {
									mIsEmpty = false;
									return ;
								}
								u = p.getFstElement();
							}while(mDfsNum.get(u) > mDfsNum.get(stateSucc.getId()));
							mRootsStack.push(new PairXY<>(u, B));
						}
					}
				}
			}

			if(mRootsStack.peek().getFstElement().intValue() == s){
				
				mRootsStack.pop();
				int u;
				do {
					u = mActiveStack.pop();
					mCurrent.clear(u);
				}while(u != s);	
			}
		}
		
	}
	
	private static BuchiGeneral getA() {
		
		BuchiGeneral buchi = new BuchiGeneral(2);
		IState aState = buchi.addState();
		IState bState = buchi.addState();
		
		aState.addSuccessor(0, aState.getId());	
		aState.addSuccessor(0, bState.getId());		

		bState.addSuccessor(0, bState.getId());
//		bState.addSuccessor(0, aState.getId());
		bState.addSuccessor(1, aState.getId());
		bState.addSuccessor(0, aState.getId());
		
		buchi.setFinal(bState);
		buchi.setInitial(aState);
		
		return buchi;
	}
	
	private static BuchiGeneral getB() {
		BuchiGeneral buchi = new BuchiGeneral(2);
		IState aState = buchi.addState();
		IState bState = buchi.addState();
		
		aState.addSuccessor(0, bState.getId());		

		bState.addSuccessor(0, bState.getId());
		bState.addSuccessor(1, aState.getId());
		
		buchi.setFinal(bState);
		buchi.setInitial(aState);
		
		return buchi;
	}
	
	private static BuchiGeneral getC() {
		BuchiGeneral buchi = new BuchiGeneral(2);
		IState aState = buchi.addState();
		IState bState = buchi.addState();
		
		aState.addSuccessor(0, bState.getId());		

		bState.addSuccessor(1, aState.getId());
		
		buchi.setFinal(bState);
		buchi.setInitial(aState);
		return buchi;
	}
	
	private static BuchiGeneral getD() {
		BuchiGeneral buchi = new BuchiGeneral(1);
		IState aState = buchi.addState();
		
		aState.addSuccessor(0, aState.getId());		
		
		buchi.setFinal(aState);
		buchi.setInitial(aState);
		return buchi;
	}
	
	private static BuchiGeneral getE() {
		BuchiGeneral buchi = new BuchiGeneral(1);
		IState aState = buchi.addState();
		
		aState.addSuccessor(0, aState.getId());	
		
		buchi.setInitial(aState);
		return buchi;
	}
	
	public static void main(String[] args) {
		
		BuchiGeneral A = getA();
		BuchiGeneral B = getB();
		BuchiGeneral C = getC();
		BuchiGeneral D = getD();
		BuchiGeneral E = getE();
		
		BuchiInclusionASCC inclusionChecker = new BuchiInclusionASCC(A, B);
//		System.out.println(inclusionChecker.isIncluded2());
		System.out.println(inclusionChecker.isIncluded(1000));
		
		inclusionChecker = new BuchiInclusionASCC(A, C);
//		System.out.println(inclusionChecker.isIncluded2());
		System.out.println(inclusionChecker.isIncluded(1000));
		
		inclusionChecker = new BuchiInclusionASCC(D, E);
//		System.out.println(inclusionChecker.isIncluded2());
		System.out.println(inclusionChecker.isIncluded(1000));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ASCC";
	}




}
