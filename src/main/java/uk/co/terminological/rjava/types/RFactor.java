package uk.co.terminological.rjava.types;

import java.util.Optional;

import uk.co.terminological.rjava.RConverter;
import uk.co.terminological.rjava.RDataType;
import uk.co.terminological.rjava.RObjectVisitor;

/**
 * A single value of a factor is largely the same as a String from most perpectives
 * but has an associated index value. This is largely expected to make sense in the context of a 
 * {@link RFactorVector}.
 * @author terminological
 *
 */
@RDataType(
		JavaToR = { 
				"function(jObj) as.character(rJava::.jcall(jObj,returnSig='Ljava/lang/String;',method='rLabel'))",
		}, 
		RtoJava = { 
				"function(rObj) {", 
				"	if (is.na(rObj)) return(rJava::.jnew('~RFACTOR~'))",
				"	if (length(rObj) > 1) stop('input too long')", 
				"	tmp = as.integer(rObj)[[1]]",
				"	tmpLabel = levels(rObj)[[tmp]]",
				"	return(rJava::.jnew('~RFACTOR~',tmp, tmpLabel))", 
				"}"
		}
		//JNIType = "I"
	)
public class RFactor implements RPrimitive {

	Integer self;
	String label;
	
	static final int NA_FACTOR = Integer.MIN_VALUE;
	
	public RFactor(Enum<?> e) {
		// convert zero based to 1 based for conversion to R.
		this(e==null ? NA_FACTOR : e.ordinal()+1, e==null ? "NA" : e.toString());
	}
	
	public RFactor(int value, String label) {
		if ((int) value == NA_FACTOR) {
			this.self = null;
			this.label = "NA";
		} else {
			this.self = Integer.valueOf((int) value);
			this.label = label;
		}
	}
	
	public RFactor() {
		this(NA_FACTOR, "NA");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		RFactor other = (RFactor) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (self == null) {
			if (other.self != null)
				return false;
		} else if (!self.equals(other.self))
			return false;
		return true;
	}
	
	public String rLabel() {return label;}

	public int rValue() {
		return self == null ? NA_FACTOR : self.intValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> Optional<X> as(Class<X> type) {
		if(type.isEnum()) return Optional.ofNullable(type.getEnumConstants()[this.self]);
		if(type.isInstance(this)) return Optional.ofNullable((X) this);
		if(type.equals(String.class)) return Optional.ofNullable((X) this.label);
		if(type.equals(Integer.class)) return Optional.ofNullable((X) this.self);
		return Optional.empty();
	}

	public String toString() {return self==null?"NA":label.toString();}
	
	public String rCode() {
		return RConverter.rQuote(this.toString(), "'");
	}
	
	@SuppressWarnings("unchecked")
	public String get() {return label;}
	
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {return visitor.visit(this);}
	
}
