package uk.co.terminological.rjava;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	
	
	public static RIntegerVector convert(Integer[] array) {	return (RIntegerVector) Stream.of(array).collect(integerCollector()); }
	public static RNumericVector convert(Double[] array) {	return (RNumericVector) Stream.of(array).collect(doubleCollector()); }
	public static RNumericVector convert(Long[] array) {	return (RNumericVector) Stream.of(array).collect(longCollector()); }
	public static RNumericVector convert(Float[] array) {	return (RNumericVector) Stream.of(array).collect(floatCollector()); }
	public static RNumericVector convert(BigDecimal[] array) {	return (RNumericVector) Stream.of(array).collect(bigDecimalCollector()); }
	public static RLogicalVector convert(Boolean[] array) {	return (RLogicalVector) Stream.of(array).collect(booleanCollector()); }
	public static RDateVector convert(LocalDate[] array) {	return (RDateVector) Stream.of(array).collect(dateCollector()); }
	
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
	public static <X extends RPrimitive> X convertObjectToPrimitive(Object o) {
		if (o instanceof Integer) return (X) convert((Integer) o);
		if (o instanceof Long) return (X) convert((Long) o);
		if (o instanceof Double) return (X) convert((Double) o);
		if (o instanceof Float) return (X) convert((Float) o);
		if (o instanceof BigDecimal) return (X) convert((BigDecimal) o);
		if (o instanceof Boolean) return (X) convert((Boolean) o);
		if (o instanceof Enum) return (X) convert((Enum<?>) o);
		if (o instanceof String) return (X) convert((String) o);
		if (o instanceof LocalDate) return (X) convert((LocalDate) o);
		throw new IncompatibleTypeException("Don't know how to convert a: "+o.getClass());
	}
	
	@SuppressWarnings("unchecked")
	public static <X extends RVector<?>> X convertObjectToVector(Object o) {
		
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
		
		throw new IncompatibleTypeException("Don't know how to convert a: "+o.getClass());
	}
	
	public static RObject convertObject(Object invoke) {
		if (invoke instanceof RObject) return (RObject) invoke;
		try {
			return convertObjectToPrimitive(invoke);
		} catch (IncompatibleTypeException e) {
			return convertObjectToVector(invoke);
		}
	} 
	
	public static <X, Y extends RPrimitive> CollectingConverter<X,Y> using(Collector<X,?,Y> collector) {
		return CollectingConverter.from(collector);
	}
	
	public static VectorCollector<Integer,RInteger> integerCollector() {return vectorCollector(() -> new RIntegerVector(), (r,i) -> r.add(RConverter.convert(i)));}
	public static VectorCollector<Long,RNumeric> longCollector() {return vectorCollector(() -> new RNumericVector(), (r,i) -> r.add(RConverter.convert(i)));}
	public static VectorCollector<Double,RNumeric> doubleCollector() {return vectorCollector(() -> new RNumericVector(), (r,i) -> r.add(RConverter.convert(i)));}
	public static VectorCollector<BigDecimal,RNumeric> bigDecimalCollector() {return vectorCollector(() -> new RNumericVector(), (r,i) -> r.add(RConverter.convert(i)));}
	public static VectorCollector<Float,RNumeric> floatCollector() {return vectorCollector(() -> new RNumericVector(), (r,i) -> r.add(RConverter.convert(i)));}
	public static VectorCollector<Boolean,RLogical> booleanCollector() {return vectorCollector(() -> new RLogicalVector(), (r,i) -> r.add(RConverter.convert(i)));}
	public static VectorCollector<String,RCharacter> stringCollector() {return vectorCollector(() -> new RCharacterVector(), (r,i) -> r.add(RConverter.convert(i)));}
	public static VectorCollector<LocalDate,RDate> dateCollector() {return vectorCollector(() -> new RDateVector(), (r,i) -> r.add(RConverter.convert(i)));}
	
	public static <X extends Enum<?>> VectorCollector<X,RFactor> enumCollector(Class<X> enumClass) {
		String[] labels = Stream.of(enumClass.getEnumConstants()).map(x -> x.toString()).collect(Collectors.toList()).toArray(new String[] {});
		return vectorCollector(() -> new RFactorVector(labels), (r,i) -> r.add(RConverter.convert(i)));}
	
	public static interface VectorCollector<T,X extends RPrimitive> extends Collector<T,RVector<X>,RVector<X>> {}
	
	private static <T,X extends RPrimitive> VectorCollector<T,X> vectorCollector(Supplier<RVector<X>> supplier, BiConsumer<RVector<X>, T> accumulator) {
		return new VectorCollector<T,X>() {

			@Override
			public Supplier<RVector<X>> supplier() {
				return supplier;
			}

			@Override
			public BiConsumer<RVector<X>, T> accumulator() {
				return accumulator;
			}

			@Override
			public BinaryOperator<RVector<X>> combiner() {
				return (r1,r2) -> {
					RVector<X> out = supplier().get();
					out.addAll(r1);
					out.addAll(r2);
					return out;
				};
			}

			@Override
			public Function<RVector<X>, RVector<X>> finisher() {
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
	
	public static List<Class<?>> ConvertibleTypes = Arrays.asList(
		String.class,
		Integer.class,
		Enum.class,
		BigDecimal.class,
		Float.class,
		Long.class,
		Double.class,
		Boolean.class,
		LocalDate.class,
		int[].class,
		double[].class,
		boolean[].class,
		String[].class
	);

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
	
}
