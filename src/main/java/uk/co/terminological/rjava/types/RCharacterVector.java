package uk.co.terminological.rjava.types;

import java.util.stream.Collectors;

import uk.co.terminological.rjava.RDataType;
import uk.co.terminological.rjava.RObjectVisitor;

/**
 * A java representation of an R character vector. Factory methods are in {@link RVector}.
 * @author terminological
 *
 */
@RDataType(
		JavaToR = { 
				"function(jObj) as.character(rJava::.jcall(jObj,returnSig='[Ljava/lang/String;',method='rPrimitive'))",
		}, 
		RtoJava = { 
				"function(rObj) {", 
				"	if (is.null(rObj)) return(rJava::.jnew('~RCHARACTERVECTOR~'))",
				"	if (!is.character(rObj)) stop('expected a vector of characters')",
				"	tmp = as.character(rObj)",
				"	return(rJava::.jnew('~RCHARACTERVECTOR~',rJava::.jarray(tmp)))", 
				"}"
		}
		//JNIType = "[[C"
	)
public class RCharacterVector extends RVector<RCharacter> implements JNIPrimitive {
	
	public RCharacterVector(String[] primitives) {
		for (int i=0; i<primitives.length; i++) this.add(new RCharacter(primitives[i]));
	}
	public RCharacterVector() {super();}
	public String[] rPrimitive() {
		return this.stream().map(ri -> ri.rPrimitive()).collect(Collectors.toList()).toArray(new String[] {});
	}
	@Override
	public RCharacter na() {return new RCharacter();}
	@Override
	public Class<RCharacter> getType() {
		return RCharacter.class;
	}
	
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {
		X out = visitor.visit(this);
		this.forEach(c -> c.accept(visitor));
		return out;
	}
}