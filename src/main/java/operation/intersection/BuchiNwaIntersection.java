package operation.intersection;

import automata.BuchiNwa;
import automata.IBuchiNwa;
import automata.StateNwa;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import operation.IBinaryOperation;
import util.IntSet;
import util.UtilIntSet;
/**
 * Compute Intersection of two Buchi automta and the result is Buchi automaton
 * */
public class BuchiNwaIntersection extends BuchiNwa implements IBinaryOperation<IBuchiNwa, IBuchiNwa> {

	private final IBuchiNwa mFstOperand;
	private final IBuchiNwa mSndOperand;
		
	private final TObjectIntMap<ProductState> mStateMap;
	
	public BuchiNwaIntersection(IBuchiNwa fstOp, IBuchiNwa sndOp) {
		super(    fstOp.getAlphabetCall()
				, fstOp.getAlphabetInternal()
				, fstOp.getAlphabetReturn()
			);
		
		this.mFstOperand = fstOp;
		this.mSndOperand = sndOp;
		
		assert checkAlphabetConsistency();
		
		mStateMap = new TObjectIntHashMap<>();
		
		computeInitialStates();
	}
	
	
	private boolean checkAlphabetConsistency() {
		return mFstOperand.getAlphabetCall().equals(mSndOperand.getAlphabetCall())
			&& mFstOperand.getAlphabetInternal().equals(mSndOperand.getAlphabetInternal())
			&& mFstOperand.getAlphabetReturn().equals(mSndOperand.getAlphabetReturn());
	}


	protected ProductState addState(int fst, int snd, TrackNumber track) {
		ProductState state = new ProductState(this, 0, fst, snd, track);
		if(mStateMap.containsKey(state)) {
			return (ProductState) getState(mStateMap.get(state));
		}
		// add new state
		ProductState newState = new ProductState(this, getStateSize(), fst, snd, track);
		int id = this.addState(newState);
		mStateMap.put(newState, id);
		// whether it is accepting state
		final boolean isFinal = mFstOperand.isFinal(fst) && (track == TrackNumber.TRACK_ONE);
		if(isFinal) setFinal(id);
		return newState;
	}

	private void computeInitialStates() {
		for(final Integer fst : mFstOperand.getInitialStates().iterable()) {
			for(final Integer snd : mSndOperand.getInitialStates().iterable()) {
				ProductState state = addState(fst, snd, TrackNumber.TRACK_ONE);		
				this.setInitial(state);
			}
		}
	}


	@Override
	public IBuchiNwa getResult() {
		return this;
	}

	@Override
	public IBuchiNwa getFirstOperand() {
		return mFstOperand;
	}


	@Override
	public IBuchiNwa getSecondOperand() {
		return mSndOperand;
	}
	
	private ProductState getProductState(int state) {
		return (ProductState) getState(state);
	}
	
	// -------------------------------------------------------------------------
	class ProductState extends StateNwa implements IProductState {
		private final int mFstState;
		private final int mSndState;
		private final TrackNumber mTrack;
		private TrackNumber mSuccTrack = null;
		
		public ProductState(BuchiNwaIntersection buchi,
				 int id, int fstState, int sndState, TrackNumber track) {
			super(buchi, id);
			mFstState = fstState;
			mSndState = sndState;
			mTrack = track;
		}

		@Override
		public int getFstState() {
			return mFstState;
		}

		@Override
		public int getSndState() {
			return mSndState;
		}

		@Override
		public TrackNumber getTrackNumber() {
			return mTrack;
		}

		@Override
		public TrackNumber getSuccStateTrack() {
			if(mSuccTrack == null) {
				mSuccTrack = this.getSuccStateTrack(
							   getFirstOperand().isFinal(mFstState)
							,  getSecondOperand().isFinal(mSndState)
							);
			}
			return mSuccTrack;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof ProductState)) {
				return false;
			}
			ProductState other = (ProductState)obj;
			return this.contentEq(other);
		}
		
		@Override
		public String toString() {
			return "(" + mFstState + "," + mSndState + "):" + mTrack;
		}
		
		int hashCode;
		boolean hasCode = false;
		@Override
		public int hashCode() {
			if(hasCode) return hashCode;
			else {
				hasCode = true;
				hashCode = mFstState * getFirstOperand().getStateSize() + mSndState;
				hashCode += mTrack == TrackNumber.TRACK_ONE ? 1 : 2;
			}
			return hashCode;
		}
		
		//------------------------ implementation for intersection
		
		private IntSet computeSuccessors(int letter, boolean isCall) {
			IntSet succs = UtilIntSet.newIntSet();
			IntSet fstSuccs = null, sndSuccs = null;
			if(isCall) {
				fstSuccs = mFstOperand.getState(mFstState).getSuccessorsCall(letter);
				sndSuccs = mSndOperand.getState(mSndState).getSuccessorsCall(letter);
			}else {
				fstSuccs = mFstOperand.getState(mFstState).getSuccessorsInternal(letter);
				sndSuccs = mSndOperand.getState(mSndState).getSuccessorsInternal(letter);
			}
			for(final Integer fstSucc : fstSuccs.iterable()) {
				for(final Integer sndSucc : sndSuccs.iterable()) {
					ProductState succ = addState(fstSucc, sndSucc, getSuccStateTrack());
					succs.set(succ.getId());
					if(isCall) {
						super.addSuccessorCall(letter, succ.getId());
					}else {
						super.addSuccessorInternal(letter, succ.getId());
					}
				}
			}
			return succs;	
		}
		
		@Override
		public IntSet getSuccessorsCall(int letter) {
			if(super.getEnabledLettersCall().contains(letter)) {
				return super.getSuccessorsCall(letter);
			}
			return computeSuccessors(letter, true);
		}
		
		@Override
		public IntSet getSuccessorsInternal(int letter) {
			if(super.getEnabledLettersInternal().contains(letter)) {
				return super.getSuccessorsInternal(letter);
			}
			return computeSuccessors(letter, false);
		}
		
		@Override
		public IntSet getSuccessorsReturn(int hier, int letter) {
			if(super.getEnabledLettersReturn().contains(letter)
			&& super.getEnabledHiersReturn(letter).contains(hier)) {
				return super.getSuccessorsReturn(hier, letter);
			}
			final ProductState prodHier = getProductState(hier);
			final int fstHier = prodHier.getFstState();
			final int sndHier = prodHier.getSndState();
			final IntSet succs = UtilIntSet.newIntSet();
			IntSet fstSuccs = mFstOperand.getState(mFstState).getSuccessorsReturn(fstHier, letter);
			IntSet sndSuccs = mSndOperand.getState(mSndState).getSuccessorsReturn(sndHier, letter);
			for(final Integer fstSucc : fstSuccs.iterable()) {
				for(final Integer sndSucc : sndSuccs.iterable()) {
					ProductState succ = addState(fstSucc, sndSucc, getSuccStateTrack());
					succs.set(succ.getId());
					super.addSuccessorReturn(hier, letter, succ.getId());
				}
			}
			return succs;
		}
		
	}

}
