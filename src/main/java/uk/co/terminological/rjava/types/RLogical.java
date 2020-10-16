package uk.co.terminological.rjava.types;

import java.util.Optional;

import uk.co.terminological.rjava.RDataType;
import uk.co.terminological.rjava.RObjectVisitor;


/** The logical wrapper handles the translation of R logical to Java Boolean.class while the value is passed 
* through the JNI interface as an unboxed primitive int. This class is largely needed to handle 
* R NA values, and map them to Java nulls.  If you are not
* using a structure (such as a list or vector) 
* and you know the value is not NA you can substitute a primitive boolean instead of this.
*/
@RDataType(
		JavaToR = { 
				"function(jObj) as.logical(rJava::.jcall(jObj,returnSig='I',method='rPrimitive'))",
		}, 
		RtoJava = { 
				"function(rObj) {", 
				"	if (is.na(rObj)) return(rJava::.jnew('~RLOGICAL~'))",
				"	if (length(rObj) > 1) stop('input too long')",
				"	if (!is.logical(rObj)) stop('expected a logical')",
				"	tmp = as.integer(rObj)[[1]]",
				"	return(rJava::.jnew('~RLOGICAL~',tmp))", 
				"}"
		}//,
		//JNIType = "I"
	)
public class RLogical implements RPrimitive, JNIPrimitive {
	
	Boolean self;
	
	static final int NA_BOOL = Integer.MIN_VALUE;
	
	public RLogical(Boolean value) {
		self = value;
	}
	
	public RLogical(int value) {
		if (value == NA_BOOL) {
			self = null;
		} else {
			if(value != 0) {
				self = Boolean.TRUE;
			} else {
				self = Boolean.FALSE;
			}
		}
	}
	
	public RLogical() {
		this(NA_BOOL);
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
		RLogical other = (RLogical) obj;
		if (self == null) {
			if (other.self != null)
				return false;
		} else if (!self.equals(other.self))
			return false;
		return true;
	}
	
	public int rPrimitive() {
		return self==null ? NA_BOOL : (self.booleanValue() ? 1 : 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> Optional<X> as(Class<X> type) {
		if (type.isInstance(this)) return (Optional<X>) Optional.ofNullable((X) this);
		if (type.isInstance(self)) return (Optional<X>) Optional.ofNullable((X) self);
		return Optional.empty();
	}
	
	public String toString() {return self==null?"NA":self.toString();}
	
	public String rCode() { return this.toString().toUpperCase(); }
	
	@SuppressWarnings("unchecked")
	public Boolean get() {return self;}
	
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {return visitor.visit(this);}
}
