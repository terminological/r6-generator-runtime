package uk.co.terminological.rjava.types;

import uk.co.terminological.rjava.RObjectVisitor;

public class RMatrix<X extends RPrimitive> implements RObject {

	private static final long serialVersionUID = RObject.datatypeVersion;
	
	RVector<X> vector;
	int[] dimensions;
	
	@Override
	public String rCode() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public <Y> Y accept(RObjectVisitor<Y> visitor) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//TODO: integerate this.
}
