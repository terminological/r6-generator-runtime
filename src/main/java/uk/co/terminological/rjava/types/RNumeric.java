package uk.co.terminological.rjava.types;

import java.math.BigDecimal;
import java.util.Optional;

import uk.co.terminological.rjava.RDataType;
import uk.co.terminological.rjava.RObjectVisitor;

/**
 * The numeric wrapper handles the translation of R numerics to Java Double.class while the value is passed 
 * through the JNI interface as an unboxed primitive double. This class is largely needed to handle 
 * R NA values, and map them to Java nulls.  If you are not
 * using a structure (such as a list or vector) 
 * and you know the value is not NA you can substitute a primitive double instead of this.
 * This is also the target datatype for java Long, Float, and BigDecimal, all of which will end up
 * as R numerics. Round tripping a java float is not supported at present, and Api classes that attempt
 * to use floats will not compile, but on the java side the conversion is handled by this class.
 * @author terminological
 *
 */
@RDataType(
		JavaToR = { 
				"function(jObj) as.numeric(rJava::.jcall(jObj,returnSig='D',method='rPrimitive'))",
		}, 
		RtoJava = { 
				"function(rObj) {", 
				"	if (length(rObj) > 1) stop('input too long')",
				"	if (!is.numeric(rObj)) stop('expected a numeric')",
				"	tmp = as.numeric(rObj)[[1]]",
				"	return(rJava::.jnew('~RNUMERIC~',tmp))", 
				"}"
		}//,
		//JNIType = "D"
	)
public class RNumeric implements RPrimitive, JNIPrimitive  {

	Double self;
	private static final long NA_DOUBLE_LONG = 0x7ff00000000007a2L;
	private static final double NA_DOUBLE = Double.longBitsToDouble(NA_DOUBLE_LONG);
	//NaN 7ff8000000000000
	//Infinity 7ff0000000000000
	//-Infinity fff0000000000000
	//private static final double NA_DOUBLE = Double.longBitsToDouble(9221120237041090560L);
	
	@SuppressWarnings("unchecked")
	public Double get() {
		return self;
	}
	
	public RNumeric(Double value) {
		self = value;
	}
	
	public RNumeric(Long value) {
		if (value == null) self=null;
		else self = Double.valueOf(value);
	}
	
	public RNumeric(Float value) {
		if (value == null) self=null;
		else self = Double.valueOf(value);
	}
	
	public RNumeric(BigDecimal value) {
		if (value == null) self=null;
		else self = value.doubleValue();
		
	}
	
	public RNumeric(double value) {
		if (Double.doubleToLongBits(value) == NA_DOUBLE_LONG) self = null;
		else self = (Double) value;
	}
	
	public RNumeric() {
		this(NA_DOUBLE);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((self == null) ? 0 : self.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RNumeric other = (RNumeric) obj;
		if (self == null) {
			if (other.self != null)
				return false;
		} else if (!self.equals(other.self))
			return false;
		return true;
	}
	
	public double rPrimitive() {return self == null ? NA_DOUBLE : self.doubleValue();} 
	
	@SuppressWarnings("unchecked")
	@Override
	public <X> Optional<X> as(Class<X> type) {
		if (type.isInstance(this)) return Optional.ofNullable((X) this);
		if (type.isInstance(self)) return Optional.ofNullable((X) self);
		if (type.equals(Long.class)) return Optional.ofNullable((X) (Long) self.longValue());
		if (type.equals(Double.class)) return Optional.ofNullable((X) (Double) self.doubleValue());
		if (type.equals(Float.class)) return Optional.ofNullable((X) (Float) self.floatValue());
		return Optional.empty();
	}
	
	public String toString() {return self==null?"NA":self.toString();}
	
	public String rCode() {
		if (self==null) return "NA";
		if (self==Double.POSITIVE_INFINITY) return "Inf";
		if (self==Double.NEGATIVE_INFINITY) return "-Inf";
		if (self==Double.NaN) return "NaN";
		return self.toString();
	}
	
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {return visitor.visit(this);}
	
}
