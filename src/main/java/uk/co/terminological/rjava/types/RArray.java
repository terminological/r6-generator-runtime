package uk.co.terminological.rjava.types;

import java.util.Arrays;
import java.util.stream.Collectors;

import uk.co.terminological.rjava.RObjectVisitor;

public abstract class RArray<X extends RPrimitive> implements RObject {

	private static final long serialVersionUID = RObject.datatypeVersion;
	
	public abstract RVector<X> getVector();
	int[] dimensions;
	
		
	@Override
	public String rCode() {
		String dims = Arrays.stream(dimensions).boxed().map(n -> n.toString()).collect(Collectors.joining(", "));
		return "matrix("+getVector().rCode()+", c("+dims+"))";
	}
	
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {
		X out = visitor.visit(this);
		this.getVector().forEach(c -> c.accept(visitor));
		return out;
	}

	public abstract Class<RNumeric> getType();
	
	
	//TODO: integerate this.
}
