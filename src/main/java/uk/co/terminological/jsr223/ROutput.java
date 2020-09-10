package uk.co.terminological.jsr223;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A set of utility classes for converting complex java structures to data types that are jdx friendly
 * @author terminological
 *
 */
public class ROutput {

	public static class Dataframe extends LinkedHashMap<String, Object[]> {
//		public Dataframe addRow(Map<String,Object> row) {
//			for (Entry<String,Object> ent: row.entrySet()) {
//				if (!this.containsKey(ent.getKey())) this.put(ent.getKey(), new ArrayList<Object>());
//				Object tmp = ent.getValue();
//				if (tmp == null || ROutput.supportedLengthOneOutputs.contains(tmp.getClass())) {
//					lhm.get(ent.getKey()).add(tmp);
//				} else {
//					//TODO: could do datatype conversion here
//					lhm.get(ent.getKey()).add(tmp.toString());
//				}
//				
//			}
//		}
	}
	public static class RowMajorDataframe extends ArrayList<LinkedHashMap<String, Object>> {}
	
	public static List<Class<?>> supportedLengthOneOutputs = Arrays.asList(
			Integer.class, int.class,
			String.class,
			Byte.class, byte.class,
			Character.class, char.class,
			Double.class, double.class,
			Float.class, float.class,
			BigDecimal.class,
			BigInteger.class,
			Short.class, short.class,
			Long.class, long.class
	);
	
	public static List<Class<?>> supportedArrayOutputs = Arrays.asList(
			Integer[].class, int[].class,
			String[].class,
			Byte[].class, byte[].class,
			Character[].class, char[].class,
			Double[].class, double[].class,
			Float[].class, float[].class,
			BigDecimal[].class,
			BigInteger[].class,
			Short[].class, short[].class,
			Long[].class, long[].class
	);
	
	/*private static Class<?>[] supportedInputs = {
			Double.class, Integer.class, String.class,
			Boolean.class, Byte.class, 
			double[].class, int[].class, String[].class, boolean[].class, byte[].class
	};*/
	
	public static <K> LinkedHashMap<K,Object> ensureSafe(Map<K,Object> input) {
		if (input == null) return null;
		LinkedHashMap<K,Object> out = new LinkedHashMap<K,Object>();
		boolean colMajor = input.values().stream().filter(o -> o != null).anyMatch(o -> o.getClass().isArray());
		if (colMajor) {
			for(Entry<K,Object> entry: input.entrySet()) {
				if (entry.getValue() == null || ROutput.supportedArrayOutputs.contains(entry.getValue().getClass())) {
					out.put(entry.getKey(), entry.getValue());
				} else {
					String[] tmp = Arrays.stream((Object[]) entry.getValue()).map(Object::toString).toArray(String[]::new);
					out.put(entry.getKey(), tmp);
					
				}
			}
		} else {
			for(Entry<K,Object> entry: input.entrySet()) {
				if (entry.getValue() == null || ROutput.supportedLengthOneOutputs.contains(entry.getValue().getClass())) {
					out.put(entry.getKey(), entry.getValue());
				} else {
					out.put(entry.getKey(), entry.getValue().toString());
				}
			}
		}
		return(out);
	}
	
	@SuppressWarnings("unchecked")
	private static <X> MapRule<X>[] reflectionRules(Class<X> clazz) {
		List<MapRule<X>> out = new ArrayList<>();
		for (Method method: clazz.getMethods()) {
			if (method.isAccessible() &&
					method.getParameterCount() == 0 
					&& (
							supportedLengthOneOutputs.contains(method.getReturnType())
							|| (Collection.class.isAssignableFrom(method.getReturnType()) 
									&& supportedLengthOneOutputs.contains(method.getGenericParameterTypes()[0])
								)
							|| (Map.class.isAssignableFrom(method.getReturnType()) 
									&& supportedLengthOneOutputs.contains(method.getGenericParameterTypes()[0])
									&& supportedLengthOneOutputs.contains(method.getGenericParameterTypes()[1])
								)
						)
							// || supportedArrayOutputs.contains(method.getReturnType())
					) {
				out.add(mapping(method));
			}
		}
		return (MapRule<X>[]) out.toArray(new MapRule[out.size()]);
	}
	
	/**
	 * The ConvertingCollector interface allows us to specify a set of rules for converting an X into a Y and
	 * then apply these to streams, iterators, arrays, collections, and iterables, as well as plain
	 * instances. By default all of these conversions are handled through streams. The target data type is essentially
	 * somethign that will convert to a R dataframe or vector.
	 * 
	 * @author terminological
	 *
	 * @param <X> - source datatype
	 * @param <Y> - target datatype
	 */
	public static interface ConvertingCollector<X,Y> {		
		/**
		 * Convert an instance of type X to Y
		 * @param instance
		 * @return
		 */
		default Y instance(X instance) {
			return stream(Stream.of(instance));
		};
		
		/**
		 * Convert a stream of 
		 * @param stream
		 * @return
		 */
		Y stream(Stream<X> stream);
		default Y collection(Collection<X> collection) {
			return stream(collection.stream());
		};
		default Y iterable(Iterable<X> iterable) {
			return stream(StreamSupport.stream(iterable.spliterator(), false));
		}
		default Y iterator(Iterator<X> iterator) {
			return iterable(() -> iterator);
		}
		default Y array(X[] array) {
			return collection(Arrays.asList(array));
		}
	}
	
	/**
	 * Converts a java map into a data frame in row major order with one data frame row per entry
	 * Keys in the java map are mapped to a keyColName
	 * @param input - a java Map<K,V> of primitive keys and arbitrarily complex value objects
	 * @param keyColName - the data frame column name for the java map keys
	 * @param rules - a set of mappings to apply to the map values to 
	 * @return
	 */
	@SafeVarargs
	public static <K,V> RowMajorDataframe convertMapValues(Map<K,V> input, String keyColName, MapRule<V>... rules) {
		RowMajorDataframe out = new RowMajorDataframe();
		for (Entry<K,V> entry: input.entrySet()) {
			LinkedHashMap<String,Object> tmp = new LinkedHashMap<String,Object>();
			tmp.put(keyColName, entry.getKey());
			for(MapRule<V> rule: rules) {
				tmp.put(rule.label(), rule.rule().apply(entry.getValue()));
			}
			out.add(tmp);
		}
		return out;
	}
	
	/**
	 * Generates a converting collector using reflection that maps public getter methods to entries in a R dataframe.
	 * Specifically targets a row major format. (see https://cran.r-project.org/web/packages/jsr223/vignettes/jsr223.pdf_
	 * Does not process fields at all unless they have a getter.
	 * @param clazz - the class that the converter will deal with 
	 * @return - a collectingConverter capable of dealing with inputs of type X 
	 */
	public static <X> ConvertingCollector<X,RowMajorDataframe> convertToRowMajor(Class<X> clazz) {
		return convertToRowMajor(reflectionRules(clazz));
	}
	
	/**
	 * Generates a converting collector using set of rules that maps public getter methods to entries in a R dataframe.
	 * Specifically targets a row major format. (see https://cran.r-project.org/web/packages/jsr223/vignettes/jsr223.pdf_
	 * Does not process fields at all unless they have a getter.
	 * @param clazz - the class that the converter will deal with 
	 * @return - a collectingConverter capable of dealing with inputs of type X 
	 */
	@SafeVarargs
	public static <X> ConvertingCollector<X,RowMajorDataframe> convertToRowMajor(MapRule<X>... rules) {
		return new ConvertingCollector<X,RowMajorDataframe>() {
			@Override
			public RowMajorDataframe stream(Stream<X> stream) {
				return stream.collect(toRowMajorDataframe(rules));
			}
		};
	}
	
	/**
	 * Generates a converting collector using reflection that maps public getter methods to entries in a R dataframe.
	 * Specifically targets a column major format. (see https://cran.r-project.org/web/packages/jsr223/vignettes/jsr223.pdf_
	 * Does not process fields at all unless they have a getter.
	 * @param clazz - the class that the converter will deal with 
	 * @return - a collectingConverter capable of dealing with inputs of type X 
	 */
	public static <X> ConvertingCollector<X,Dataframe> convertToDataframe(Class<X> clazz) {
		return convertToColMajor(clazz);
	}
	
	/**
	 * Generates a converting collector using set of rules that maps public getter methods to entries in a R dataframe.
	 * Specifically targets a column major format. (see https://cran.r-project.org/web/packages/jsr223/vignettes/jsr223.pdf_
	 * Does not process fields at all unless they have a getter.
	 * @param clazz - the class that the converter will deal with 
	 * @return - a collectingConverter capable of dealing with inputs of type X 
	 */
	@SafeVarargs
	public static <X> ConvertingCollector<X,Dataframe> convertToDataframe(MapRule<X>... rules) {
		return convertToColMajor(rules);
	}
	
	/**
	 * Generates a converting collector using reflection that maps public getter methods to entries in a R dataframe.
	 * Specifically targets a column major format. (see https://cran.r-project.org/web/packages/jsr223/vignettes/jsr223.pdf_
	 * Does not process fields at all unless they have a getter.
	 * @param clazz - the class that the converter will deal with 
	 * @return - a collectingConverter capable of dealing with inputs of type X 
	 */
	private static <X> ConvertingCollector<X,Dataframe> convertToColMajor(Class<X> clazz) {
		return convertToColMajor(reflectionRules(clazz));
	}
	
	/**
	 * Generates a converting collector using set of rules that maps public getter methods to entries in a R dataframe.
	 * Specifically targets a column major format. (see https://cran.r-project.org/web/packages/jsr223/vignettes/jsr223.pdf_
	 * Does not process fields at all unless they have a getter.
	 * @param clazz - the class that the converter will deal with 
	 * @return - a collectingConverter capable of dealing with inputs of type X 
	 */
	@SafeVarargs
	private static <X> ConvertingCollector<X,Dataframe> convertToColMajor(MapRule<X>... rules) {
		return new ConvertingCollector<X,Dataframe>() {
			@Override
			public Dataframe stream(Stream<X> stream) {
				return stream.collect(toDataframe(rules));
			}
		};
	}
	
	/**
	 * The MapRule interface allows a label to be associated with a data mapping
	 * @author terminological
	 *
	 * @param <Y> - the input data type that will be mapped
	 */
	public static interface MapRule<Y> {
		String label();
		Function<Y,Object> rule();
	}
	
	/**
	 * generates a mapping using reflection from a method specification
	 * @param method - the java method
	 * @return - a mapping that associates the method name with the method. 
	 */
	private static <Z> MapRule<Z> mapping(final Method method) {
		return mapping(method.getName(), 
				new Function<Z,Object>() {
					@Override
					public Object apply(Z t) {
						try {
							return method.invoke(t);
						} catch (IllegalAccessException | IllegalArgumentException |InvocationTargetException e) {
							throw new RuntimeException(e);
						}
					}
		});
	}
	
	/**
	 * Create a mapping using a to allow us to extract data from an object of type defined by clazz and associate it
	 * with a label. This can be used to create a custom data mapping. e.g.
	 * 
	 * mapping(File.class, "absolutePath", f -> f.getAbsolutePath())
	 * 
	 * Typically this function will be imported statically:
	 * 
	 * import static uk.co.terminological.jsr223.RConvert.*; 
	 * @param clazz - a class maybe required to guide the compiler to use the correct lambda function
	 * @param label - the target column label in the R dataframe
	 * @param rule -  a lambda mapping an instance of clazz to the value of the column 
	 * @return a mapping rule
	 */
	public static <Z> MapRule<Z> mapping(final Class<Z> clazz, final String label, final Function<Z,Object> rule) {
		return mapping(label,rule);
	}
	
	/**
	 * Create a mapping using a to allow us to extract data from an object of type defined by clazz and associate it
	 * with a label. This can be used to create a custom data mapping. e.g.
	 * 
	 * mapping("absolutePath", f -> f.getAbsolutePath())
	 * 
	 * If the compiler cannot work out the type from the context it may be necessary to use the 3 parameter version 
	 * of this method.
	 * 
	 * @param label - the target column label in the R dataframe
	 * @param rule -  a lambda mapping an instance of clazz to the value of the column 
	 * @return
	 */
	public static <Z> MapRule<Z> mapping(final String label, final Function<Z,Object> rule) {
		return new MapRule<Z>() {
			@Override
			public String label() {
				return label;
			}
			@Override
			public Function<Z, Object> rule() {
				return rule;
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
	public static <X> Collector<X,?,Dataframe> toDataframe(final MapRule<X>... rules) {
		return new Collector<X,LinkedHashMap<String, List<Object>>,Dataframe>() {

			@Override
			public Supplier<LinkedHashMap<String, List<Object>>> supplier() {
				return () -> new LinkedHashMap<String, List<Object>>();
			}

			@Override
			public BiConsumer<LinkedHashMap<String, List<Object>>, X> accumulator() {
				return (lhm, o) -> {
					synchronized(lhm) {
						for (MapRule<X> rule: rules) {
							List<Object> tmp = lhm.get(rule.label());
							if (tmp == null) tmp = new ArrayList<>();
							Object tmp2 = rule.rule().apply(o);
							tmp.add(tmp2);
							lhm.put(rule.label(), tmp);
						}
					}
				};
			}

			@Override
			public BinaryOperator<LinkedHashMap<String, List<Object>>> combiner() {
				return (lhm,rhm) -> {
					LinkedHashMap<String, List<Object>> out = new LinkedHashMap<String, List<Object>>();
					for (String key: lhm.keySet()) {
						List<Object> lhs = lhm.get(key);
						List<Object> rhs = rhm.get(key);
						lhs.addAll(rhs);
						out.put(key, lhs);
					}
					return out;
				};
			}

			@Override
			public Function<LinkedHashMap<String, List<Object>>, Dataframe> finisher() {
				return a -> {
					Dataframe out = new Dataframe();
					for (String key: a.keySet()) {
						out.put(key, a.get(key).toArray());
					}
					return out;
				};
			}

			@Override
			public Set<Characteristics> characteristics() {
				return new HashSet<>(
						Arrays.asList(
								Characteristics.UNORDERED,
								Characteristics.CONCURRENT
								));
			}

			
			
		};
	}
	
	/**
	 * A stream collector that applies mapping rules and coverts a stream of objects into a dataframe in row major format
	 * @param rules - an array of mapping(X.class, "colname", x -> x.getValue()) entries that define
	 * unless there is a strong reason you should consider using the column major version
	 * the data frame columns
	 * @return A collector that works in a stream.collect(RConvert.toRowMajorDataFrame(mapping1, mapping2, ...))
	 */
	@SafeVarargs
	public static <X> Collector<X,?,RowMajorDataframe> toRowMajorDataframe(final MapRule<X>... rules) {
		return new Collector<X,RowMajorDataframe,RowMajorDataframe>() {

			@Override
			public Supplier<RowMajorDataframe> supplier() {
				return () -> new RowMajorDataframe();
			}

			@Override
			public BiConsumer<RowMajorDataframe, X> accumulator() {
				return (lhm, o) -> {
					LinkedHashMap<String, Object> tmp = new LinkedHashMap<String, Object>(); 
					for (MapRule<X> rule: rules) {
						Object tmp2 = rule.rule().apply(o);
						tmp.put(rule.label(),tmp2);
					}
					lhm.add(tmp);
				};
			}

			@Override
			public BinaryOperator<RowMajorDataframe> combiner() {
				return (lhm,rhm) -> {
					lhm.addAll(rhm);
					return lhm;
				};
			}

			@Override
			public Function<RowMajorDataframe, RowMajorDataframe> finisher() {
				return a -> a;
			}

			@Override
			public Set<Characteristics> characteristics() {
				return new HashSet<>(
						Arrays.asList(
								Characteristics.UNORDERED,
								Characteristics.IDENTITY_FINISH,
								Characteristics.CONCURRENT
								));
			}
		};
	}
	
	/**
	 * A stream collector that collects a stream of maps and assembles it into a col major dataframe
	 * @return A collector that works in a streamOfMaps.collect(RConvert.mapsToDataFrame())
	 */
	public static Collector<Map<String,Object>,?,Dataframe> mapsToDataframe() {
		return new Collector<Map<String,Object>,LinkedHashMap<String, List<Object>>,Dataframe>() {

			@Override
			public Supplier<LinkedHashMap<String, List<Object>>> supplier() {
				return () -> new LinkedHashMap<String, List<Object>>();
			}

			@Override
			public BiConsumer<LinkedHashMap<String, List<Object>>, Map<String,Object>> accumulator() {
				return (lhm, o) -> {
					synchronized(lhm) {
						for (Entry<String,Object> ent: o.entrySet()) {
							//TODO: this wont work if the map is not complete.
							if (!lhm.containsKey(ent.getKey())) lhm.put(ent.getKey(), new ArrayList<Object>());
							Object tmp = ent.getValue();
							if (tmp == null || ROutput.supportedLengthOneOutputs.contains(tmp.getClass())) {
								lhm.get(ent.getKey()).add(tmp);
							} else {
								//TODO: could do datatype conversion here
								lhm.get(ent.getKey()).add(tmp.toString());
							}
							
						}
					}
				};
			}

			@Override
			public BinaryOperator<LinkedHashMap<String, List<Object>>> combiner() {
				return (lhm,rhm) -> {
					LinkedHashMap<String, List<Object>> out = new LinkedHashMap<String, List<Object>>();
					for (String key: lhm.keySet()) {
						List<Object> lhs = lhm.get(key);
						List<Object> rhs = rhm.get(key);
						lhs.addAll(rhs);
						out.put(key, lhs);
					}
					return out;
				};
			}

			@Override
			public Function<LinkedHashMap<String, List<Object>>, Dataframe> finisher() {
				return a -> {
					Dataframe out = new Dataframe();
					for (String key: a.keySet()) {
						out.put(key, a.get(key).toArray());
					}
					return out;
				};
			}

			@Override
			public Set<Characteristics> characteristics() {
				return new HashSet<>(
						Arrays.asList(
								Characteristics.UNORDERED,
								Characteristics.CONCURRENT
								));
			}

			
			
		};
	}
}
