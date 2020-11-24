package uk.co.terminological.rjava.types;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import uk.co.terminological.rjava.RDataType;
import uk.co.terminological.rjava.RObjectVisitor;

/**
 * A java representation of the R base Date class (will also be used for POSIXt classes)
 * @author terminological
 *
 */
@RDataType(
		JavaToR = { 
				"function(jObj) as.Date(rJava::.jcall(jObj,returnSig='Ljava/lang/String;',method='rPrimitive'),'%Y-%m-%d')",
		}, 
		RtoJava = { 
				"function(rObj) {", 
				"	if (is.na(rObj)) return(rJava::.jnew('~RDATE~'))",
				"	if (length(rObj) > 1) stop('input too long')",
				//"	if (!is.numeric(rObj)) stop('expected a numeric')",
				"	tmp = as.character(rObj)[[1]]",
				"	return(rJava::.jnew('~RDATE~',tmp))", 
				"}"
		}
		//JNIType = "D"
	)
public class RDate implements RPrimitive, JNIPrimitive  {

	private static final long serialVersionUID = RObject.datatypeVersion;
	
	static final String NA_VALUE = null;
	public static final RDate NA = new RDate(NA_VALUE);
	
	LocalDate self;
	
	static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
	@SuppressWarnings("unchecked")
	public LocalDate get() {
		return self;
	}
	
	public RDate(String value) {
		if (value == null) self=null;
		else self = LocalDate.parse(value);
	}
	
	public RDate() {
		self = null;
	}
	
	public RDate(LocalDate boxed) {
		self = boxed;
	}
	
	public static RDate from(String s) {
		return new RDate(s);
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
		RDate other = (RDate) obj;
		if (self == null) {
			if (other.self != null)
				return false;
		} else if (!self.equals(other.self))
			return false;
		return true;
	}
	
	public String rPrimitive() {return self == null ? "NA" : self.format(formatter);} 
	
	@SuppressWarnings("unchecked")
	@Override
	public <X> Optional<X> opt(Class<X> type) {
		if (type.isInstance(this)) return Optional.ofNullable((X) this);
		if (type.isInstance(self)) return Optional.ofNullable((X) self);
		return Optional.empty();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <X> X get(Class<X> type) throws ClassCastException {
		if (type.isInstance(this)) return (X) this;
		if (type.isInstance(self)) return (X) self;
		throw new ClassCastException("Can't convert to a "+type.getCanonicalName());
	}
	
	public String toString() {return self==null ? "NA" : self.format(formatter);}
	
	@Override
	public String rCode() {
		return this.isNa() ? "NA": "as.Date('"+self.format(formatter)+"','%Y-%m-%d')";
	}
	
	@Override
	public <X> X accept(RObjectVisitor<X> visitor) {return visitor.visit(this);}
	
	public boolean isNa() {return self == null;}
	
	public String asCsv() {
		return toString();
	}
}
