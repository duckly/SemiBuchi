package automata;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import util.IntIterator;
import util.IntSet;
import util.UtilIntSet;

/**
 * Buchi nested word automata 
 * */
public interface IBuchiNwa {
	
	Acc getAcceptance();
	
	IntSet getAlphabetInternal();
	
	IntSet getAlphabetCall();
	
	IntSet getAlphabetReturn();
	
	// get nested alphabet size
	default public int getNwaAlphabetSize() {
		return getAlphabetInternal().cardinality() + getAlphabetCall().cardinality() + getAlphabetReturn().cardinality();
	}
	
	int getStateSize();
	
	IStateNwa addState();
	
	int addState(IStateNwa state);
	
	IStateNwa getState(int id);
	
	IStateNwa makeState(int id);
	
	IntSet getInitialStates();

	IntSet getFinalStates();
	
	
	default public boolean isInitial(IStateNwa s) {
		return isInitial(s.getId());
	}
	
	boolean isInitial(int id);
	
	default public boolean isFinal(IStateNwa s) {
		return isFinal(s.getId());
	}
	
	boolean isFinal(int id);
	
	default public void setInitial(IStateNwa s) {
		setInitial(s.getId());
	}
	
	void setInitial(int id);
	
	default public void setFinal(IStateNwa s) {
		setFinal(s.getId());
	}
	
	void setFinal(int id);
	
	// should use functional programming to following three 
	default public IntSet getSuccessorsInternal(IntSet states, int letter) {
		assert getAlphabetInternal().get(letter);
		IntSet result = UtilIntSet.newIntSet();
		IntIterator iter = states.iterator();
		while(iter.hasNext()) {
			int n = iter.next();
			result.or(getSuccessorsInternal(n, letter));
		}
		return result;
	}
	
	default public IntSet getSuccessorsCall(IntSet states, int letter) {
		assert getAlphabetCall().get(letter);
		IntSet result = UtilIntSet.newIntSet();
		IntIterator iter = states.iterator();
		while(iter.hasNext()) {
			int n = iter.next();
			result.or(getSuccessorsCall(n, letter));
		}
		return result;
	}
	
	default public IntSet getSuccessorsReturn(IntSet states, int letter) {
		assert getAlphabetReturn().get(letter);
		IntSet result = UtilIntSet.newIntSet();
		IntIterator iter = states.iterator();
		while(iter.hasNext()) {
			int n = iter.next();
			Set<Integer> enabledHiers = getState(n).getEnabledHiersReturn(letter);
			for(Integer hier : enabledHiers) {
				result.or(getState(n).getSuccessorsReturn(hier, letter));
			}
		}
		return result;
	}
	
	IntSet getSuccessorsInternal(int state, int letter);

	IntSet getSuccessorsCall(int state, int letter);

	IntSet getSuccessorsReturn(int state, int pred, int letter);

	Collection<IStateNwa> getStates();
	
	
	default public void toATS(PrintStream out, List<String> alphabet) {
		final String PRE_BLANK = "   "; 
		final String ITEM_BLANK = " ";
		final String LINE_END = "},";
		final String BLOCK_END = "\n" + PRE_BLANK + "},";
		final String TRANS_PRE_BLANK = PRE_BLANK + "   "; 
		out.println("NestedWordAutomaton result = (");
		
        
        IntIterator iter = getAlphabetCall().iterator();
        out.print(PRE_BLANK + "callAlphabet = {");
        while(iter.hasNext()) {
        	int id = iter.next();
        	out.print(alphabet.get(id) + ITEM_BLANK);
        }
        out.println(LINE_END);
        
        iter = getAlphabetInternal().iterator();
        out.print(PRE_BLANK + "internalAlphabet = {");
        while(iter.hasNext()) {
        	int id = iter.next();
        	out.print(alphabet.get(id) + ITEM_BLANK);
        }
        out.println(LINE_END);
        
        iter = getAlphabetReturn().iterator();
        out.print(PRE_BLANK + "returnAlphabet = {");
        while(iter.hasNext()) {
        	int id = iter.next();
        	out.print(alphabet.get(id) + ITEM_BLANK);
        }
        out.println(LINE_END);
        
        // states
		Collection<IStateNwa> states = getStates();
		out.print(PRE_BLANK + "states = {");
		for(IStateNwa state : states) {
			out.print("s" + state.getId() + ITEM_BLANK);
        }	
        out.println(LINE_END);
        // initial states
        IntSet initialStates = getInitialStates();
        iter = initialStates.iterator();
        out.print(PRE_BLANK + "initialStates = {");
        while(iter.hasNext()) {
        	int id = iter.next();
        	out.print("s" + id + ITEM_BLANK);
        }
        out.println(LINE_END);
        
        // final states
        IntSet finalStates = getFinalStates();
        iter = finalStates.iterator();
        out.print(PRE_BLANK + "finalStates = {");
        while(iter.hasNext()) {
        	int id = iter.next();
        	out.print("s" + id + ITEM_BLANK);
        }
        out.println(LINE_END);
        
        // call transitions
        out.print(PRE_BLANK + "callTransitions = {");
		for(IStateNwa state : states) {
			iter = getAlphabetCall().iterator();
            while(iter.hasNext()) {
            	int letter = iter.next();
            	IntSet succs = state.getSuccessorsCall(letter);
            	IntIterator iterInner = succs.iterator();
            	while(iterInner.hasNext()) {
            		int succ = iterInner.next();
            		out.print("\n" + TRANS_PRE_BLANK + "(s" + state.getId() + " " + alphabet.get(letter) + " s" + succ + ")" );
            	}
            }
        }
		out.println(BLOCK_END);
        
        // internal transitions
        out.print(PRE_BLANK + "internalTransitions = {");
		for(IStateNwa state : states) {
			iter = getAlphabetInternal().iterator();
            while(iter.hasNext()) {
            	int letter = iter.next();
            	IntSet succs = state.getSuccessorsInternal(letter);
            	IntIterator iterInner = succs.iterator();
            	while(iterInner.hasNext()) {
            		int succ = iterInner.next();
            		out.print("\n" + TRANS_PRE_BLANK + "(s" + state.getId() + " " + alphabet.get(letter) + " s" + succ + ")" );
            	}
            }
        }
		out.println(BLOCK_END);
		
        // return transitions
        out.print(PRE_BLANK + "returnTransitions = {");
		for(IStateNwa state : states) {
			iter = getAlphabetReturn().iterator();
            while(iter.hasNext()) {
            	int letter = iter.next();
            	if(!state.getEnabledLettersReturn().contains(letter)) continue; 
            	Set<Integer> enabledHiers = state.getEnabledHiersReturn(letter);
            	for(Integer hier : enabledHiers) {
            		if(hier < 0) continue;
                	IntSet succs = state.getSuccessorsReturn(hier, letter);
                	IntIterator iterInner = succs.iterator();
                	while(iterInner.hasNext()) {
                		int succ = iterInner.next();
                		out.print("\n" + TRANS_PRE_BLANK + "(s" + state.getId() + " s" + hier + " " + alphabet.get(letter) + " s" + succ + ")" );
                	}
            	}

            }
        }
		out.println("\n" + PRE_BLANK + "}");
		
		out.println(");");
	}
	
	
	default int getNumTransition() {
		int num = 0;
		for(IStateNwa s : getStates()) {
			// call 
			for(Integer letter : s.getEnabledLettersCall()) {
				num += s.getSuccessorsCall(letter).cardinality();
			}
			// internal 
			for(Integer letter : s.getEnabledLettersInternal()) {
				num += s.getSuccessorsInternal(letter).cardinality();
			}
			// return 
			for(Integer letter : s.getEnabledLettersReturn()) {
				for(Integer hier : s.getEnabledHiersReturn(letter)) {
					num += s.getSuccessorsReturn(hier, letter).cardinality();	
				}
			}
		}
		return num;
	}
	
	

}
