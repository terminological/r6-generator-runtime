package uk.co.terminological.rjava.types;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.rjava.IncompatibleTypeException;
import uk.co.terminological.rjava.RConverter;

public abstract class RVector<X extends RPrimitive> extends ArrayList<X> implements RObject {

	private static final long serialVersionUID = RObject.datatypeVersion;
	
	public RVector<X> pad(int length) {
		return rep(na(),length);
	}
	
	public RVector<X> rep(X x,int length) {
		for (int i=0; i<length; i++) {
			this.add(x);
		}
		return this;
	}
	
	public abstract X na();
	public abstract Class<X> getType();
	
	@SuppressWarnings("unchecked")
	public static <Y extends RPrimitive> RVector<Y> create(Class<Y> clazz) {
		if (RCharacter.class.equals(clazz)) return (RVector<Y>) new RCharacterVector(); 
		if (RInteger.class.equals(clazz)) return (RVector<Y>) new RIntegerVector();
		if (RNumeric.class.equals(clazz)) return (RVector<Y>) new RNumericVector();
		if (RFactor.class.equals(clazz)) return (RVector<Y>) new RFactorVector();
		if (RLogical.class.equals(clazz)) return (RVector<Y>) new RLogicalVector();
		if (RDate.class.equals(clazz)) return (RVector<Y>) new RDateVector();
		throw new IncompatibleTypeException("No vector defined for: "+clazz.getCanonicalName());
	}
	
	@SuppressWarnings("unchecked")
	public static <Y extends RPrimitive> RVector<Y> singleton(Y primitive) {
		if (primitive instanceof RCharacter) return (RVector<Y>) new RCharacterVector().and((RCharacter) primitive); 
		if (primitive instanceof RInteger) return (RVector<Y>) new RIntegerVector().and((RInteger) primitive);
		if (primitive instanceof RNumeric) return (RVector<Y>) new RNumericVector().and((RNumeric) primitive);
		if (primitive instanceof RFactor) return (RVector<Y>) new RFactorVector().and((RFactor) primitive);
		if (primitive instanceof RLogical) return (RVector<Y>) new RLogicalVector().and((RLogical) primitive);
		if (primitive instanceof RDate) return (RVector<Y>) new RDateVector().and((RDate) primitive);
		throw new IncompatibleTypeException("No vector defined for: "+primitive.getClass().getCanonicalName());
	}
	
	@SuppressWarnings("unchecked")
	public static <Y extends RPrimitive> RVector<Y> padded(int length, Y primitive) {
		if (primitive instanceof RCharacter) return (RVector<Y>) new RCharacterVector().pad(length-1).and((RCharacter) primitive); 
		if (primitive instanceof RInteger) return (RVector<Y>) new RIntegerVector().pad(length-1).and((RInteger) primitive);
		if (primitive instanceof RNumeric) return (RVector<Y>) new RNumericVector().pad(length-1).and((RNumeric) primitive);
		if (primitive instanceof RFactor) return (RVector<Y>) new RFactorVector().pad(length-1).and((RFactor) primitive);
		if (primitive instanceof RLogical) return (RVector<Y>) new RLogicalVector().pad(length-1).and((RLogical) primitive);
		if (primitive instanceof RDate) return (RVector<Y>) new RDateVector().pad(length-1).and((RDate) primitive);
		throw new IncompatibleTypeException("No vector defined for: "+primitive.getClass().getCanonicalName());
	}
	
	@SuppressWarnings("unchecked")
	public static <Y extends RPrimitive> RVector<Y> padded(int length, Class<Y> clazz) {
		if (clazz.equals(RCharacter.class)) return (RVector<Y>) new RCharacterVector().pad(length); 
		if (clazz.equals(RInteger.class)) return (RVector<Y>) new RIntegerVector().pad(length);
		if (clazz.equals(RNumeric.class)) return (RVector<Y>) new RNumericVector().pad(length);
		if (clazz.equals(RFactor.class)) return (RVector<Y>) new RFactorVector().pad(length);
		if (clazz.equals(RLogical.class)) return (RVector<Y>) new RLogicalVector().pad(length);
		if (clazz.equals(RDate.class)) return (RVector<Y>) new RDateVector().pad(length);
		throw new IncompatibleTypeException("No vector defined for: "+clazz.getCanonicalName());
	}

	public void addAll(RVector<X> r1) {
		super.addAll(r1);
	}
	
	public boolean add(X r1) {
		return super.add(r1);
	}

	public String toString() {
		return "<"+this.getType().getSimpleName().toLowerCase()+"> "+
				this.stream().limit(10).map(v-> (v == null? "NULL": v.toString()))
						.collect(Collectors.joining(", "))+", ...";
	}
	
	public String rCode() {
		return "c("+this.stream().map(s -> s.rCode()).collect(Collectors.joining(", "))+")";
	}
	
	public static RCharacterVector with(String... o) {return RConverter.convert(o);}
	
	public static RNumericVector with(Double... o) {return RConverter.convert(o);}
	public static RNumericVector with(Long... o) {return RConverter.convert(o);}
	public static RNumericVector with(BigDecimal... o) {return RConverter.convert(o);}
	public static RNumericVector with(Float... o) {return RConverter.convert(o);}
	
	public static RIntegerVector with(Integer... o) {return RConverter.convert(o);}
	public static RLogicalVector with(Boolean... o) {return RConverter.convert(o);}
	public static RDateVector with(LocalDate... o) {return RConverter.convert(o);}
	
	public static RFactorVector with(Enum<?>... o) {return RConverter.convert(o);}
	
	
	public RVector<X> and(@SuppressWarnings("unchecked") X... o) {
		Stream.of(o).forEach(this::add);
		return this;
	}
	
}
