package operation.inclusion;

import java.util.BitSet;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import automata.BuchiWa;
import automata.IStateWa;
import automata.IBuchiWa;
import complement.StateWaNCSB;
import main.TaskInclusion;
import util.IPair;
import util.IntIterator;
import util.IntSet;
import util.IntStack;
import util.PairXY;
import util.Timer;

/**
 * ASCC algorithm with Antichain OPTOMIZATION
 * **/
public class BuchiInclusionASCCAntichain extends BuchiInclusion {
		
	public BuchiInclusionASCCAntichain(TaskInclusion task, IBuchiWa fstOp, IBuchiWa sndOp) {
		super(task, fstOp, sndOp);
	}
		
	/**
	 * try to compute the product of mFstOperand and mSndComplement
	 * by constructing the complement of mSndOperand
	 * */
	
	public Boolean isIncluded() {
		ASCCAntichain scc = new ASCCAntichain();
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
	public IPair<List<IStateWa>, List<IStateWa>> getCounterexampleRun() {
		return null;
	}
	
	// ---------------------------- part for SCC decomposition -------------
	/**
	 * SCC Decomposition
	 * */
	private class ASCCAntichain {
		
		private int mIndex=0;
		private final Stack<PairXY<Integer, BitSet>> mRootsStack ;
		private final Map<Integer, Integer> mDfsNum;
		private final IntStack mActiveStack ;
		private final BitSet mCurrent;
		
		private Boolean mIsEmpty = true;
		
		private final Timer mTimer;
		
		private final Antichain mAntichain;
		
		public ASCCAntichain() {
			
			this.mRootsStack = new Stack<>();
			this.mActiveStack = new IntStack();
			this.mDfsNum = new HashMap<>();
			this.mCurrent = new BitSet();
			this.mTimer = new Timer();
			this.mAntichain = new Antichain(mTask);
			
			mTimer.start();
			IntIterator iter = mResult.getInitialStates().iterator();
			while(iter.hasNext()) {
				int n = iter.next();

				if(! mDfsNum.containsKey(n)){
					dfs(n);
					if(mIsEmpty == null ||  ! mIsEmpty.booleanValue())
						break ;
				}
			}
			mTask.setNumPairInAntichain(mAntichain.size());
		}
		
		private boolean terminate() {
			if(mTimer.tick() > mTask.getTimeBound()) 
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
			
			if(pair.getSndElement().getNCSB().getBSet().isEmpty()) {
				flags.set(1);
			}
			
			mRootsStack.push(new PairXY<>(s, flags));
			
			//TODO only get enabled letters
			for(int letter = 0; letter < mFstOperand.getAlphabetSize(); letter ++) {
				// X states from first BA 
				IntSet fstSuccs = mFstOperand.getState(pair.getFstElement()).getSuccessors(letter);
				if(fstSuccs.isEmpty()) continue;
				// Y states from second BA
				IntSet sndSuccs = pair.getSndElement().getSuccessors(letter);
				IntIterator fstIter = fstSuccs.iterator();
				while(fstIter.hasNext()) {
					int fstSucc = fstIter.next();
					IntIterator sndIter = sndSuccs.iterator();
					while(sndIter.hasNext()) {
						int sndSucc = sndIter.next();				
						// pair (X, Y)
						StateWaNCSB yState = (StateWaNCSB) mSndComplement.getState(sndSucc);
						InclusionPairNCSB pairSucc = new InclusionPairNCSB(fstSucc, yState);
						IStateWa stateSucc = getOrAddState(pairSucc);
						mPairStateMap.get(pair).addSuccessor(letter, stateSucc.getId());
						//OPT1
						if(mAntichain.covers(pairSucc)) {
							mTask.increaseIngPairByAntichain();
							continue;
						}
						
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
					if(mActiveStack.isEmpty()) break;
					u = mActiveStack.pop();
					mCurrent.clear(u);
					//cache all nodes which has empty language
					mAntichain.addPair(mPairNCSBArray.get(u));
				}while(u != s);	
				
				// OPT2, backjump to depth i
				for(int i = 0; i < mActiveStack.size(); i ++) {
					int t = mActiveStack.get(i);
					InclusionPairNCSB p = mPairNCSBArray.get(t);
					if(t != s && p.coveredBy(pair)) {
						int e;
						do {
							if(mActiveStack.isEmpty()) break;
							e = mActiveStack.pop();
							mCurrent.clear(e);
							//cache all nodes which has empty language
							mAntichain.addPair(mPairNCSBArray.get(e));
						}while(t != e);
					}
				}
				
			}
		}
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ASCC+Antichain";
	}




}
