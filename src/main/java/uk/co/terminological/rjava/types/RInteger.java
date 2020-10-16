package uk.co.terminological.rjava.types;

import java.util.Optional;

import uk.co.terminological.rjava.RDataType;
import uk.co.terminological.rjava.RObjectVisitor;

/**
 * The integer wrapper handles the translation of R integers to Java Integer.class while the value is passed 
 * through the JNI interface as an unboxed primitive int. This class is largely needed to handle 
 * R NA values, and map them to Java nulls.  If you are not
 * using a structure (such as a list or vector) 
 * and you know the value is not NA you can substitute a primitive int instead of this.
 * @author terminological
 *
 */
@RDataType(
	JavaToR = { 
			"function(jObj) as.integer(rJava::.jcall(jObj,returnSig='I',method='rPrimitive'))",
	}, 
	RtoJava = { 
			"function(rObj) {", 
			"	if (is.na(rObj)) return(rJava::.jnew('~RINTEGER~'))",
			"	if (length(rObj) > 1) stop('input too long')", 
			"	tmp = as.integer(rObj)[[1]]", 
			"	if (rObj[[1]]!=tmp) stop('cannot cast to integer: ',rObj)", 
			"	return(rJava::.jnew('~RINTEGER~',tmp))", 
			"}"
	}
	//JNIType = "I"
)
public class RInteger implements RPrimitive, JNIPrimitive {

	
	Integer self = null;
	
	static final int NA_INT = Integer.MIN_VALUE;
	
	public RInteger(Integer value) {
		self = value;
	}
	
	public RInteger(int value) {
		if ((int) value == NA_INT) {
			self = null;
		} else {
			self = Integer.valueOf((int) value);
		}
	}
	
	public RInteger() {
		this(NA_INT);
	}
	
	@Override
	public int hashCode() {return self.hashCode();}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RInteger other = (RInteger) obj;
		if (self == null) {
			if (other.self != null)
				return false;
		} else if (!self.equals(other.self))
			return false;
		return true;
	}

	public int rPrimitive() {
		return self==null ? NA_INT : self.intValue();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <X> Optional<X> as(Class<X> type) {
		if (type.isInstance(this)) return (Optional<X>) Optional.ofNullable((X) this);
		if (type.isInstance(self)) return (Optional<X>) Optional.ofNullable((X) self);
		return Optional.empty();
	}

	public String toString() {return self==null?"NA":self.toString();}
	
	public String rCode() {return self==null?"NA":this.toString()+"L";}
	
	@SuppressWarnings("unchecked")
	public Integer get() {return self;}
	
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {return visitor.visit(this);}
	
}
