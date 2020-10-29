package uk.co.terminological.rjava.types;

import uk.co.terminological.rjava.RDataType;
import uk.co.terminological.rjava.RObjectVisitor;

/**
 * The vector of logicals is needed to ensure that NA values are correctly handled and
 * allow flexibility of a java List structure for easy manipulation. Factory methods are in {@link RVector}. 
 * @author terminological
 *
 */
@RDataType(
		JavaToR = { 
				"function(jObj) as.logical(rJava::.jcall(jObj,returnSig='[I',method='rPrimitive'))",
		}, 
		RtoJava = { 
				"function(rObj) {", 
				"	if (is.null(rObj)) return(rJava::.jnew('~RLOGICALVECTOR~'))",
				"	if (!is.logical(rObj)) stop('expected a vector of logicals')",
				"	tmp = as.integer(rObj)",
				"	return(rJava::.jnew('~RLOGICALVECTOR~',rJava::.jarray(tmp)))", 
				"}"
		}//,
		//JNIType = "[I"
	)
public class RLogicalVector extends RVector<RLogical> implements JNIPrimitive {
	
	private static final long serialVersionUID = RObject.datatypeVersion;
	
	public RLogicalVector(int[] primitives) {
		for (int i=0; i<primitives.length; i++) this.add(new RLogical(primitives[i]));
	}
	public RLogicalVector() {super();}
	public RLogicalVector(boolean[] array) {
		super();
		for (int i=0;i<array.length;i++) this.add(new RLogical(array[i] ? 1 : 0));
	}
	public int[] rPrimitive() {
		return this.stream().mapToInt(ri -> ri.rPrimitive()).toArray();
	}
	@Override
	public RLogical na() {return new RLogical();}
	@Override
	public Class<RLogical> getType() {
		return RLogical.class;
	}
	
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {
		X out = visitor.visit(this);
		this.forEach(c -> c.accept(visitor));
		return out;
	}
}