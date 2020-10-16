package uk.co.terminological.rjava.types;

import uk.co.terminological.rjava.RDataType;
import uk.co.terminological.rjava.RObjectVisitor;

/**
 * The vector of numerics is needed to ensure that NA values are correctly handled and
 * allow flexibility of a java List structure for easy manipulation. Factory methods are in {@link RVector}. 
 * @author terminological
 *
 */
@RDataType(
		JavaToR = { 
				"function(jObj) as.numeric(rJava::.jcall(jObj,returnSig='[D',method='rPrimitive'))",
		}, 
		RtoJava = { 
				"function(rObj) {", 
				"	if (is.null(rObj)) return(rJava::.jnew('~RNUMERICVECTOR~'))",
				"	if (!is.numeric(rObj)) stop('expected a numeric')",
				"	tmp = as.numeric(rObj)",
				"	return(rJava::.jnew('~RNUMERICVECTOR~',rJava::.jarray(tmp)))", 
				"}"
		}//,
		//JNIType = "[D"
	)
public class RNumericVector extends RVector<RNumeric> implements JNIPrimitive {
	
	public RNumericVector(double[] primitives) {
		for (int i=0; i<primitives.length; i++) this.add(new RNumeric(primitives[i]));
	}
	public RNumericVector() {super();}
	public double[] rPrimitive() {
		return this.stream().mapToDouble(ri -> ri.rPrimitive()).toArray();
	}
	@Override
	public RNumeric na() {return new RNumeric();}
	@Override
	public Class<RNumeric> getType() {
		return RNumeric.class;
	}
	
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {
		X out = visitor.visit(this);
		this.forEach(c -> c.accept(visitor));
		return out;
	}
}