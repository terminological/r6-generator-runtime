package uk.co.terminological.rjava.types;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.rjava.IncompatibleTypeException;
import uk.co.terminological.rjava.RConverter;

public abstract class RVector<X extends RPrimitive> extends ArrayList<X> implements RObject {

	private static final long serialVersionUID = RObject.datatypeVersion;
	private static Logger log = LoggerFactory.getLogger(RVector.class);
	private static int INITIAL_SIZE = 100;
	
	public RVector() {
		super();
	}
	
	public RVector(int length) {
		super(length);
	}
	
	public static <Y extends RPrimitive> RVector<Y> pad(Class<Y> type, int length) {
		return rep(RPrimitive.na(type),length);
	}
	
	public static <Y extends RPrimitive> RVector<Y> rep(Y x, int length) {
		RVector<Y> out = RVector.create(x.getClass(), INITIAL_SIZE);
		for (int i=0; i<length; i++) {
			out.add(x);
		}
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private static <Y extends RPrimitive> RVector<Y> create(Class<? extends RPrimitive> clazz, int length) {
		if (RCharacter.class.equals(clazz)) return (RVector<Y>) new RCharacterVector(length); 
		if (RInteger.class.equals(clazz)) return (RVector<Y>) new RIntegerVector(length);
		if (RNumeric.class.equals(clazz)) return (RVector<Y>) new RNumericVector(length);
		if (RFactor.class.equals(clazz)) return (RVector<Y>) new RFactorVector(length);
		if (RLogical.class.equals(clazz)) return (RVector<Y>) new RLogicalVector(length);
		if (RDate.class.equals(clazz)) return (RVector<Y>) new RDateVector(length);
		throw new IncompatibleTypeException("No vector defined for: "+clazz.getCanonicalName());
	}

	public abstract Class<X> getType();
	
	@SuppressWarnings("unchecked")
	public static <Y extends RPrimitive> RVector<Y> create(Class<Y> clazz) {
		if (RCharacter.class.equals(clazz)) return (RVector<Y>) new RCharacterVector(INITIAL_SIZE); 
		if (RInteger.class.equals(clazz)) return (RVector<Y>) new RIntegerVector(INITIAL_SIZE);
		if (RNumeric.class.equals(clazz)) return (RVector<Y>) new RNumericVector(INITIAL_SIZE);
		if (RFactor.class.equals(clazz)) return (RVector<Y>) new RFactorVector(INITIAL_SIZE);
		if (RLogical.class.equals(clazz)) return (RVector<Y>) new RLogicalVector(INITIAL_SIZE);
		if (RDate.class.equals(clazz)) return (RVector<Y>) new RDateVector(INITIAL_SIZE);
		throw new IncompatibleTypeException("No vector defined for: "+clazz.getCanonicalName());
	}
	
	
	public static RCharacterVector singleton(RCharacter primitive) {return new RCharacterVector(INITIAL_SIZE).and(primitive);}
	public static RNumericVector singleton(RNumeric primitive) {return new RNumericVector(INITIAL_SIZE).and(primitive);}
	public static RIntegerVector singleton(RInteger primitive) {return new RIntegerVector(INITIAL_SIZE).and(primitive);}
	public static RFactorVector singleton(RFactor primitive) {return new RFactorVector(INITIAL_SIZE).and(primitive);}
	public static RLogicalVector singleton(RLogical primitive) {return new RLogicalVector(INITIAL_SIZE).and(primitive);}
	public static RDateVector singleton(RDate primitive) {return new RDateVector(100).and(primitive);}
	
	@SuppressWarnings("unchecked")
	public static <Y extends RPrimitive> RVector<Y> singleton(Y v) {
		if (v instanceof RCharacter) return (RVector<Y>) singleton((RCharacter) v);
		if (v instanceof RNumeric) return (RVector<Y>) singleton((RNumeric) v);
		if (v instanceof RInteger) return (RVector<Y>) singleton((RInteger) v);
		if (v instanceof RFactor) return (RVector<Y>) singleton((RFactor) v);
		if (v instanceof RLogical) return (RVector<Y>) singleton((RLogical) v);
		if (v instanceof RDate) return (RVector<Y>) singleton((RDate) v);
		throw new IncompatibleTypeException("No vector defined for: "+v.getClass().getCanonicalName());
	}
	
	public static RCharacterVector padded(int length, RCharacter primitive) { return RVector.pad(RCharacter.class,length-1).and(primitive);}
	public static RNumericVector padded(int length, RNumeric primitive) { return RVector.pad(RNumeric.class,length-1).and(primitive);}
	public static RIntegerVector padded(int length, RInteger primitive) { return RVector.pad(RInteger.class,length-1).and(primitive);}
	public static RFactorVector padded(int length, RFactor primitive) { return RVector.pad(RFactor.class,length-1).and(primitive);}
	public static RLogicalVector padded(int length, RLogical primitive) { return RVector.pad(RLogical.class,length-1).and(primitive);}
	public static RDateVector padded(int length, RDate primitive) { return RVector.pad(RDate.class,length-1).and(primitive);}
	
	@SuppressWarnings("unchecked")
	public static <Y extends RPrimitive> RVector<Y> padded(int length, Y v) {
		if (v instanceof RCharacter) return (RVector<Y>) padded(length,(RCharacter) v);
		if (v instanceof RNumeric) return (RVector<Y>) padded(length,(RNumeric) v);
		if (v instanceof RInteger) return (RVector<Y>) padded(length,(RInteger) v);
		if (v instanceof RFactor) return (RVector<Y>) padded(length,(RFactor) v);
		if (v instanceof RLogical) return (RVector<Y>) padded(length,(RLogical) v);
		if (v instanceof RDate) return (RVector<Y>) padded(length,(RDate) v);
		throw new IncompatibleTypeException("No vector defined for: "+v.getClass().getCanonicalName());
	}
		
	public static <Y extends RPrimitive> RVector<Y> padded(int length, Class<Y> clazz) {
		return rep(RPrimitive.na(clazz),length);
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
	
	
	public abstract <Y extends RVector<X>> Y and(@SuppressWarnings("unchecked") X... o);
	
	public Set<X> distinct() {
		return new LinkedHashSet<>(this);
	}
	
	public BitSet matches(RPrimitive value) {
		//TODO: optimise here.
		BitSet out = new BitSet(this.size());
		for (int i=0;i<this.size();i++) {
			out.set(i,value.equals(this.get(i)));
		}
		return out;
	}
	
	@SuppressWarnings("unchecked")
	public BitSet matches(Predicate<?> criteria) {
		BitSet out = new BitSet(this.size());
		try {
			for (int i=0;i<this.size();i++) {
				out.set(i,((Predicate<X>) criteria).test(this.get(i)));
			}
		} catch (Exception e) {
			log.debug("Vector filter did not complete correctly: "+criteria.toString());
		}
		return out;
	}
	
	public RVector<X> subset(BitSet filter) {
		if(filter.length() > this.size()) throw new IndexOutOfBoundsException("filter length greater than vector lenth");
		RVector<X> out = RVector.create(this.getType());
		for (int i = 0; i<this.size(); i++) {
			if (filter.get(i)) {
				out.add(this.get(i));
			}
		}
		return out;
	}

	public <Y extends RVector<?>> Y as(Class<Y> vectorClass) {
		try {
			return vectorClass.cast(this);
		} catch (ClassCastException e) {
			throw new IncompatibleTypeException("Requested a "+vectorClass.getSimpleName()+" but found a "+this.getClass().getSimpleName());
		}
	}
	
	public abstract <Y extends Object> Stream<Y> get();
	
	public abstract <Y extends Object> Stream<Optional<Y>> opt();

	@SuppressWarnings("unchecked")
	public void addUnsafe(RVector<? extends RPrimitive> rVector) throws IncompatibleTypeException {
		try {
			this.addAll((RVector<X>) rVector);
		} catch (ClassCastException e) {
			throw new IncompatibleTypeException("tried to append a "+rVector.getClass().getSimpleName()+" to a "+this.getClass().getSimpleName());
		}
	}

	@SuppressWarnings("unchecked")
	public void addUnsafe(RPrimitive v) {
		try {
			this.add((X) v);
		} catch (ClassCastException e) {
			throw new IncompatibleTypeException("tried to append a "+v.getClass().getSimpleName()+" to a "+this.getClass().getSimpleName());
		}
	}

	
	
}
