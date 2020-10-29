package uk.co.terminological.rjava;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.rjava.types.RCharacter;
import uk.co.terminological.rjava.types.RCharacterVector;
import uk.co.terminological.rjava.types.RDataframe;
import uk.co.terminological.rjava.types.RDate;
import uk.co.terminological.rjava.types.RDateVector;
import uk.co.terminological.rjava.types.RFactor;
import uk.co.terminological.rjava.types.RFactorVector;
import uk.co.terminological.rjava.types.RInteger;
import uk.co.terminological.rjava.types.RIntegerVector;
import uk.co.terminological.rjava.types.RLogical;
import uk.co.terminological.rjava.types.RLogicalVector;
import uk.co.terminological.rjava.types.RNumeric;
import uk.co.terminological.rjava.types.RNumericVector;
import uk.co.terminological.rjava.types.RObject;
import uk.co.terminological.rjava.types.RPrimitive;
import uk.co.terminological.rjava.types.RVector;

public class RConverter {

	public static RIntegerVector convert(int[] array) {	return new RIntegerVector(array); }
	public static RNumericVector convert(double[] array) {	return new RNumericVector(array); }
	public static RLogicalVector convert(boolean[] array) {	return new RLogicalVector(array); }
	public static RCharacterVector convert(String[] array) {	return new RCharacterVector(array); }
	public static RFactorVector convert(int[] array, String[] labels) {	return new RFactorVector(array, labels); }
	
	
	public static RIntegerVector convert(Integer[] array) {	return Stream.of(array).collect(integerCollector()); }
	public static RNumericVector convert(Double[] array) {	return Stream.of(array).collect(doubleCollector()); }
	public static RNumericVector convert(Long[] array) {	return Stream.of(array).collect(longCollector()); }
	public static RNumericVector convert(Float[] array) {	return Stream.of(array).collect(floatCollector()); }
	public static RNumericVector convert(BigDecimal[] array) {	return Stream.of(array).collect(bigDecimalCollector()); }
	public static RLogicalVector convert(Boolean[] array) {	return Stream.of(array).collect(booleanCollector()); }
	public static RDateVector convert(LocalDate[] array) {	return Stream.of(array).collect(dateCollector()); }
	
	@SuppressWarnings("unchecked")
	public static <X extends Enum<?>> RFactorVector convert(X[] array) {
		Class<X> cls = (Class<X>) array[0].getClass();
		return (RFactorVector) Stream.of(array).collect(enumCollector(cls)); }
	
	
	public static RInteger convert(Integer boxed) {return new RInteger(boxed);}
	
	public static RNumeric convert(Long boxed) {return new RNumeric(boxed);}
	public static RNumeric convert(Double boxed) {return new RNumeric(boxed);}
	public static RNumeric convert(Float boxed) {return new RNumeric(boxed);}
	public static RNumeric convert(BigDecimal boxed) {return new RNumeric(boxed);}
	
	public static RLogical convert(Boolean boxed) {return new RLogical(boxed);}
	public static RCharacter convert(String boxed) {return new RCharacter(boxed);}
	public static RFactor convert(Enum<?> boxed) {return new RFactor(boxed);}
	public static RDate convert(LocalDate boxed) {return new RDate(boxed);}
	
	@SuppressWarnings("unchecked")
	public static <X extends RPrimitive> X convertObjectToPrimitive(Object o) throws UnconvertableTypeException {
		if (o instanceof Integer) return (X) convert((Integer) o);
		if (o instanceof Long) return (X) convert((Long) o);
		if (o instanceof Double) return (X) convert((Double) o);
		if (o instanceof Float) return (X) convert((Float) o);
		if (o instanceof BigDecimal) return (X) convert((BigDecimal) o);
		if (o instanceof Boolean) return (X) convert((Boolean) o);
		if (o instanceof Enum) return (X) convert((Enum<?>) o);
		if (o instanceof String) return (X) convert((String) o);
		if (o instanceof LocalDate) return (X) convert((LocalDate) o);
		throw new UnconvertableTypeException("Don't know how to convert a: "+o.getClass());
	}
	
	@SuppressWarnings("unchecked")
	public static <X extends RVector<?>> X convertObjectToVector(Object o) throws UnconvertableTypeException {
		
		if (o instanceof int[]) return (X) convert((int[]) o);
		if (o instanceof double[]) return (X) convert((double[]) o);
		if (o instanceof boolean[]) return (X) convert((boolean[]) o);
		if (o instanceof String[]) return (X) convert((String[]) o);
		
		if (o instanceof Integer[]) return (X) convert((Integer[]) o);
		if (o instanceof Double[]) return (X) convert((Double[]) o);
		if (o instanceof Boolean[]) return (X) convert((Boolean[]) o);
		if (o instanceof BigDecimal[]) return (X) convert((BigDecimal[]) o);
		if (o instanceof Long[]) return (X) convert((Long[]) o);
		if (o instanceof Float[]) return (X) convert((Float[]) o);
		if (o instanceof LocalDate[]) return (X) convert((LocalDate[]) o);
		if (o instanceof Enum<?>[]) return (X) convert((Enum<?>[]) o);
		
		throw new UnconvertableTypeException("Don't know how to convert a: "+o.getClass());
	}
	
	public static RObject convertObject(Object invoke) throws UnconvertableTypeException {
		if (invoke instanceof RObject) return (RObject) invoke;
		try {
			return convertObjectToPrimitive(invoke);
		} catch (UnconvertableTypeException e) {
			return convertObjectToVector(invoke);
		}
	} 
	
	// COLLECTOR BASED CONVERSION
	
	public static <X, Y extends RObject> CollectingConverter<X,Y> using(Collector<X,?,Y> collector) {
		return CollectingConverter.from(collector);
	}
	
	// VECTOR COLLECTORS
	
	public static interface VectorCollector<T,X extends RPrimitive, Y extends RVector<X>> extends Collector<T,Y,Y> {}
	
	private static <T,X extends RPrimitive, Y extends RVector<X>> VectorCollector<T,X,Y> vectorCollector(Supplier<Y> supplier, BiConsumer<Y, T> accumulator, Class<Y> type) {
		return new VectorCollector<T,X,Y>() {

			@Override
			public Supplier<Y> supplier() {
				return supplier;
			}

			@Override
			public BiConsumer<Y, T> accumulator() {
				return accumulator;
			}

			@Override
			public BinaryOperator<Y> combiner() {
				return (r1,r2) -> {
					Y out = supplier().get();
					out.addAll(r1);
					out.addAll(r2);
					return out;
				};
			}

			@Override
			public Function<Y, Y> finisher() {
				return r -> r;
			}

			@Override
			public Set<Characteristics> characteristics() {
				return new HashSet<>(
						Arrays.asList(
								Characteristics.UNORDERED,
								Characteristics.CONCURRENT,
								Characteristics.IDENTITY_FINISH
								));
			}
			
		};
	};
	
	public static VectorCollector<Integer,RInteger,RIntegerVector> integerCollector() {
		return vectorCollector(
			() -> new RIntegerVector(), 
			(r,i) -> r.add(RConverter.convert(i)), 
			RIntegerVector.class
		);
	}
	public static VectorCollector<Long,RNumeric,RNumericVector> longCollector() {
		return vectorCollector(
			() -> new RNumericVector(), 
			(r,i) -> r.add(RConverter.convert(i)),
			RNumericVector.class
		);}
	
	public static VectorCollector<Double,RNumeric,RNumericVector> doubleCollector() {
		return vectorCollector(
				() -> new RNumericVector(), 
				(r,i) -> r.add(RConverter.convert(i)),
				RNumericVector.class
		);}
	public static VectorCollector<BigDecimal,RNumeric,RNumericVector> bigDecimalCollector() {
		return vectorCollector(
				() -> new RNumericVector(), 
				(r,i) -> r.add(RConverter.convert(i)),
				RNumericVector.class
		);}
	public static VectorCollector<Float,RNumeric,RNumericVector> floatCollector() {
		return vectorCollector(
				() -> new RNumericVector(), 
				(r,i) -> r.add(RConverter.convert(i)),
				RNumericVector.class
		);}
	public static VectorCollector<Boolean,RLogical,RLogicalVector> booleanCollector() {return vectorCollector(
			() -> new RLogicalVector(), 
			(r,i) -> r.add(RConverter.convert(i)),
			RLogicalVector.class
	);}
	public static VectorCollector<String,RCharacter,RCharacterVector> stringCollector() {return vectorCollector(
			() -> new RCharacterVector(), 
			(r,i) -> r.add(RConverter.convert(i)),
			RCharacterVector.class
	);}
	public static VectorCollector<LocalDate,RDate,RDateVector> dateCollector() {
		return vectorCollector(
				() -> new RDateVector(), 
				(r,i) -> r.add(RConverter.convert(i)),
				RDateVector.class
		);}
	public static VectorCollector<String,RDate,RDateVector> dateFromStringCollector() {
		return vectorCollector(
				() -> new RDateVector(), 
				(r,i) -> r.add(RConverter.convert(i==null?(LocalDate) null:LocalDate.parse(i))),
				RDateVector.class
		);}
	public static <X extends Enum<?>> VectorCollector<X,RFactor,RFactorVector> enumCollector(Class<X> enumClass) {
		String[] labels = Stream.of(enumClass.getEnumConstants()).map(x -> x.toString()).collect(Collectors.toList()).toArray(new String[] {});
		return vectorCollector(
				() -> new RFactorVector(labels), 
				(r,i) -> r.add(RConverter.convert(i)),
				RFactorVector.class
				);}
	
	
	
	
	/**
	 * A stream collector that collects a stream of maps and assembles it into a col major dataframe
	 * @return A collector that works in a streamOfMaps.collect(RConvert.mapsToDataFrame())
	 */
	public static Collector<Map<String,Object>,?,RDataframe> dataframeCollector() {
		return new Collector<Map<String,Object>,RDataframe,RDataframe>() {
	
			@Override
			public Supplier<RDataframe> supplier() {
				return () -> new RDataframe();
			}
	
			@Override
			public BiConsumer<RDataframe, Map<String,Object>> accumulator() {
				return (lhm, o) -> lhm.addRow(o);
			}
	
			@Override
			public BinaryOperator<RDataframe> combiner() {
				return (lhm,rhm) -> {
					synchronized(this) {
						RDataframe out = new RDataframe();
						out.append(lhm);
						out.append(rhm);
						return out;
					}
				};
			}
	
			@Override
			public Function<RDataframe, RDataframe> finisher() {
				return a -> a;
				
			}
	
			@Override
			public Set<Characteristics> characteristics() {
				return new HashSet<>(
						Arrays.asList(
								Characteristics.UNORDERED,
								Characteristics.CONCURRENT,
								Characteristics.IDENTITY_FINISH
								));
			}
	
			
			
		};
	}
	
	/**
	 * A stream collector that applies mapping rules and coverts a stream of objects into a dataframe
	 * @param rules - an array of mapping(X.class, "colname", x -> x.getValue()) entries that define
	 * the data frame columns
	 * @return A collector that works in a stream.collect(RConvert.toDataFrame(mapping1, mapping2, ...))
	 */
	@SafeVarargs
	public static <X> Collector<X,?,RDataframe> dataframeCollector(final MapRule<X>... rules) {
		return new Collector<X,RDataframe,RDataframe>() {
	
			@Override
			public Supplier<RDataframe> supplier() {
				return () -> new RDataframe();
			}
	
			@Override
			public BiConsumer<RDataframe, X> accumulator() {
				return (lhm, o) -> {
					synchronized(lhm) {
						for (MapRule<X> rule: rules) {
							Map<String,Object> tmp = new HashMap<>();
							Object tmp2 = rule.rule().apply(o);
							tmp.put(rule.label(), tmp2);
							lhm.addRow(tmp);;
						}
					}
				};
			}
	
			@Override
			public BinaryOperator<RDataframe> combiner() {
				return (lhm,rhm) -> {
					RDataframe out = new RDataframe();
					out.append(lhm);
					out.append(rhm);
					return out;
				};
			}
	
			@Override
			public Function<RDataframe, RDataframe> finisher() {
				return a -> a;
			}
	
			@Override
			public Set<Characteristics> characteristics() {
				return new HashSet<>(
						Arrays.asList(
								Characteristics.UNORDERED,
								Characteristics.CONCURRENT,
								Characteristics.IDENTITY_FINISH
								));
			}
	
			
			
		};
	}

	public static String rQuote(String in,String quote) {
		String escaped = in;
	    escaped = escaped.replace("\\", "\\\\");
	    escaped = escaped.replace(quote, "\\"+quote);
	    escaped = escaped.replace("\b", "\\b");
	    escaped = escaped.replace("\f", "\\f");
	    escaped = escaped.replace("\n", "\\n");
	    escaped = escaped.replace("\r", "\\r");
	    escaped = escaped.replace("\t", "\\t");
	    // TODO: escape other non-printing characters using uXXXX notation
	    return quote+escaped+quote;
	}
	
	public static Object unconvert(RPrimitive rPrimitive) {
		return rPrimitive.get();
	}
	
	public static <X extends RPrimitive> Optional<X> tryConvertObjectToPrimitive(Object v) {
		try {
			X out = convertObjectToPrimitive(v);
			return Optional.ofNullable(out);
		} catch (UnconvertableTypeException e) {
			return Optional.empty();
		}
	}

	
}
