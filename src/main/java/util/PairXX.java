package util;

public class PairXX<X> extends PairXY<X, X> {

	public PairXX(X x, X y) {
		super(x, y);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof IPair)) return false;
		IPair other = (IPair)o;
 		return getFstElement().equals(other.getFstElement())
			&& getSndElement().equals(other.getSndElement());
	}

}
