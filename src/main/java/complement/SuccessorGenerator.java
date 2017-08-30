package complement;

import main.Options;
import util.IntSet;
import util.PowerSet;

class SuccessorGenerator {
	
	private boolean mIsCurrBEmpty;
	private final NCSB mSuccNCSB;
	
	private IntSet mMinusFSuccs;
	private IntSet mInterFSuccs;
	
	private IntSet mF;       // so far all final states
	
	private IntSet mNPrime;  // d(N)\F\B'\S'
	private IntSet mVPrime;  // d(C) \/ (d(N) /\ F)
	private IntSet mMustIn;  // must in states in C/B
	private IntSet mSPrime;  // d(S)
	private IntSet mBPrime;  // d(B)
	
//	private IntSet mNInterFSuccs;
	
	private PowerSet mPs;
	
	private boolean hasSuccessors = true;
	
		
	public SuccessorGenerator(boolean isBEmpty, NCSB succ, IntSet minusFSuccs, IntSet interFSuccs, IntSet f) {
		this.mIsCurrBEmpty = isBEmpty;
		this.mSuccNCSB = succ;
				
		this.mMinusFSuccs = minusFSuccs;
		this.mInterFSuccs = interFSuccs;
		this.mF = f;
		
		// initialization
		// N'
		mNPrime =  this.mSuccNCSB.copyNSet();
		mNPrime.andNot(mF);                    // remove final states
		mNPrime.andNot(mSuccNCSB.getCSet());   // remove successors of C, the final states of NSuccs are in CSuccs 
		mNPrime.andNot(mSuccNCSB.getSSet());   // remove successors of S
		
		// V' = d(C) \/ (d(N)/\F)
		mVPrime =  mSuccNCSB.copyCSet();
		IntSet nInterFSuccs =  mSuccNCSB.copyNSet();
		nInterFSuccs.and(mF);           // (d(N) /\ F)
		mVPrime.or(nInterFSuccs);       // d(C) \/ (d(N) /\ F)
		
		// S successors
		mSPrime =  mSuccNCSB.copySSet();
		
		// B successors
		mBPrime =  mSuccNCSB.copyBSet();
		
		// compute must in states
		if(Options.optNCSB) {
			// lazy NCSB initialization
			if(mIsCurrBEmpty) {
				mInterFSuccs = mSuccNCSB.copyCSet(); // set to d(C)
				// must in states computation
				mMustIn = mSuccNCSB.copyCSet();
				mMustIn.and(mF);                  // d(C) /\ F
				mMustIn.or(nInterFSuccs);         // d(C\/N) /\F
			}else {
				mMustIn = mInterFSuccs.clone(); // d(B/\F)
				mMustIn.and(mF);                // d(B/\F) /\F
				mMustIn.or(mMinusFSuccs);       // d(B\F) \/ (d(B/\F) /\F)
			}
		}else {
			// original NCSB
			mMustIn = mInterFSuccs.clone(); // d(C/\F)
			mMustIn.and(mF);                // d(C/\F) /\F
			mMustIn.or(mMinusFSuccs);       // d(C\F) \/ (d(C/\F) /\F)
			mMustIn.or(nInterFSuccs);       // d(C\F) \/ (d(C/\F) /\F) \/ (d(N)\/ F)
		}
		
		// compute nondeterministic states from mInterFSuccs
		mInterFSuccs.andNot(mMinusFSuccs);     // remove must-in C (B) states
		mInterFSuccs.andNot(mSPrime);          // remove must in S states
		mInterFSuccs.andNot(mF);               // remove final states 

		mPs = new PowerSet(mInterFSuccs);
		
		// d(C\F) /\ d(S) or d(B/\F) should be empty
		hasSuccessors = !mMinusFSuccs.overlap(mSPrime);
	}
	
	public boolean hasNext() {
		return hasSuccessors && mPs.hasNext();
	}
	
	public NCSB next() {
		IntSet statesToS = mPs.next(); // extra states to be added into S'
		IntSet left = mInterFSuccs.clone();
		left.andNot(statesToS);
		// this is implementation for NCSB 
		IntSet NP = mNPrime;
		IntSet CP =  null;
		IntSet SP =  mSPrime.clone();
		IntSet BP = null;
		
		if(Options.optNCSB) {
			if(mIsCurrBEmpty) {
				// as usual S and C
				CP = mMustIn.clone();
				CP.or(left); // C' get extra
				if(Options.optBeqC) {
					BP = CP;
				}else {
					// following is d(C) /\ C'
					BP = mSuccNCSB.copyCSet(); 
					BP.and(CP);   // B'= d(C) /\ C'
				}
				SP.or(statesToS); // S'=d(S)\/(V'\C')
			}else {
				// B is not empty
				SP.or(statesToS); // d(S) \/ M'
				BP = mBPrime.clone();
				BP.andNot(statesToS); // B'=d(B)\M'
				CP = mVPrime.clone();
				CP.andNot(SP); // C'= V'\S'
			}
			
			if(SP.overlap(mF) || BP.overlap(SP)) {
				return null;
			}

		}else {
			// original NCSB
			CP = mMustIn.clone();
			CP.or(left);
			SP.or(statesToS);
			if(mIsCurrBEmpty) {
				BP =  CP;
			}else {
				BP =  mBPrime.clone();
				BP.and(CP);
			}
			
			if(SP.overlap(mF) || CP.overlap(SP)) {
				return null;
			}
		}

		return new NCSB(NP, CP, SP, BP);
	}
	
	
	

}
