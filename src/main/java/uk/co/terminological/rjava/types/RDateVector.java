package uk.co.terminological.rjava.types;

import java.util.stream.Collectors;

import uk.co.terminological.rjava.RDataType;
import uk.co.terminological.rjava.RObjectVisitor;

/**
 * Java wrapper for R vector of dates. When transferred between R and Java this uses a string format for the date.
 * Factory methods are in {@link RVector}.
 * @author terminological
 *
 */
@RDataType(
		JavaToR = { 
				"function(jObj) as.Date(rJava::.jcall(jObj,returnSig='[Ljava/lang/String;',method='rPrimitive'),'%Y-%m-%d')",
		}, 
		RtoJava = { 
				"function(rObj) {", 
				"	if (is.null(rObj)) return(rJava::.new('~RDATEVECTOR~'))",
				//"	if (!is.integer(rObj)) stop('expected an integer')",
				"	tmp = as.character(rObj)",
				"	return(rJava::.jnew('~RDATEVECTOR~',rJava::.jarray(tmp)))", 
				"}"
		}
		//JNIType = "[I"
	)
public class RDateVector extends RVector<RDate> implements JNIPrimitive {
	
	public RDateVector(String[] primitives) {
		for (int i=0; i<primitives.length; i++) this.add(new RDate(primitives[i]));
	}
	public RDateVector() {super();}
	
	public String[] rPrimitive() {
		return this.stream().map(ri -> ri.rPrimitive()).collect(Collectors.toList()).toArray(new String[] {});
	}
	@Override
	public RDate na() {return new RDate();}
	@Override
	public Class<RDate> getType() {
		return RDate.class;
	}
	
	public String rCode() {
		return "as.Date(c("+this.stream().map(s -> s==null?"NA":("'"+s.toString()+"'")).collect(Collectors.joining(", "))+"),'%Y-%m-%d')";
	}
	
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {
		X out = visitor.visit(this);
		this.forEach(c -> c.accept(visitor));
		return out;
	}
}