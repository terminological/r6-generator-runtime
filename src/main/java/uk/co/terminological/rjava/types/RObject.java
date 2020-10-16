package uk.co.terminological.rjava.types;

import java.io.Serializable;

import uk.co.terminological.rjava.RObjectVisitor;

public interface RObject extends Serializable {

	
	/** Derives the R code representation of this object. This is used for some objects
	 * as a wire serialisation ({@link RList} and {@link RNamedList}) to copy them accross to R.
	 * Other data types tend to use the raw primitives to copy.
	 * @return
	 */
	String rCode();
		
	
	public <X> X accept(RObjectVisitor<X> visitor); 
		
}
