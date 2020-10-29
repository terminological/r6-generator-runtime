package uk.co.terminological.rjava.types;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import uk.co.terminological.rjava.RConverter;
import uk.co.terminological.rjava.RDataType;
import uk.co.terminological.rjava.RObjectVisitor;

/**
 * A Java wrapper for factors. R Factors can be mapped to java enumeration by the {@link RConverter} class.
 * Factory methods are in {@link RVector}. 
 * @author terminological
 *
 */
@RDataType(
		JavaToR = { 
				"function(jObj) ordered(",
				"	x = rJava::.jcall(jObj,returnSig='[I',method='rValues'),",
				"	labels = rJava::.jcall(jObj,returnSig='[Ljava/lang/String;',method='rLevels')",
				")"
		}, 
		RtoJava = { 
				"function(rObj) {", 
				"	if (is.null(rObj)) return(rJava::.jnew('~RFACTORVECTOR~'))",
				"	if (!is.factor(rObj)) stop('expected a vector of factors')",
				"	tmp = as.integer(rObj)",
				"	return(rJava::.jnew('~RFACTORVECTOR~', rJava::.jarray(tmp), rJava::.jarray(levels(rObj))))", 
				"}"
		}
		//JNIType = "[I"
	)
public class RFactorVector extends RVector<RFactor> {
	
	private static final long serialVersionUID = RObject.datatypeVersion;
	
	private String[] levels;
	public RFactorVector(int[] values, String[] levels) {
		for (int i=0; i<values.length; i++) this.add(new RFactor(values[i], levels[values[i]-1]));
		this.levels = levels;
		//factors are 1 indexed - java arrays zero indexed
	}
	public RFactorVector() {super();}
	public RFactorVector(String[] levels) {
		super();
		this.levels = levels;
	}
	public int[] rValues() {
		return this.stream().mapToInt(ri -> ri.rValue()).toArray();
	}
	public String[] rLevels() {
		if (levels != null) return levels;
		LinkedHashMap<Integer,String> tmp = new LinkedHashMap<>();
		this.stream().forEach(f -> tmp.put(f.rValue(), f.rLabel()));
		String[] obs = IntStream.range(0, tmp.size()).mapToObj(i -> tmp.getOrDefault(i, "unknown_"+i)).collect(Collectors.toList()).toArray(new String[] {});
		return obs;
	}
	@Override
	public RFactor na() {return new RFactor();}
	@Override
	public Class<RFactor> getType() {
		return RFactor.class;
	}
	
	public String rCode() {
		return "ordered(x=c("+
				this.stream().map(s -> s==null?"NA":RConverter.rQuote(s.rLabel(), "'")).collect(Collectors.joining(", "))+
				"),labels=c("+
				Stream.of(this.rLevels()).map(s -> RConverter.rQuote(s, "'")).collect(Collectors.joining(", "))
				+"))";
	}
	
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {
		X out = visitor.visit(this);
		this.forEach(c -> c.accept(visitor));
		return out;
	}
}