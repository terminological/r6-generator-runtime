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
public class Convert2R {

	private static List<Class<?>> supportedLengthOneOutputs = Arrays.asList(
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
	
	/*private static List<Class<?>> supportedArrayOutputs = Arrays.asList(
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
	);*/
	
	/*private static Class<?>[] supportedInputs = {
			Double.class, Integer.class, String.class,
			Boolean.class, Byte.class, 
			double[].class, int[].class, String[].class, boolean[].class, byte[].class
	};*/
	
	@SuppressWarnings("unchecked")
	public static <X> MapRule<X>[] reflectionRules(Class<X> clazz) {
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
				out.add(mapping(method.getName(),method));
			}
		}
		return (MapRule<X>[]) out.toArray(new MapRule[out.size()]);
	}
	
	public static interface Convert<X,Y> {		
		default Y instance(X instance) {
			return stream(Stream.of(instance));
		};
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
	
	public static <K,V> Map<K,LinkedHashMap<String,Object>> convertMapValues(Map<K,V> input, @SuppressWarnings("unchecked") MapRule<V>... rules) {
		Map<K,LinkedHashMap<String,Object>> out = new LinkedHashMap<K,LinkedHashMap<String,Object>>();
		for (Entry<K,V> entry: input.entrySet()) {
			LinkedHashMap<String,Object> tmp = new LinkedHashMap<String,Object>();
			for(MapRule<V> rule: rules) {
				tmp.put(rule.label(), rule.rule().apply(entry.getValue()));
			}
			out.put(entry.getKey(), tmp);
		}
		return out;
	}
	
	public static <X> Convert<X,ArrayList<LinkedHashMap<String, Object>>> convertToRowMajor(Class<X> clazz) {
		return convertToRowMajor(reflectionRules(clazz));
	}
	
	public static <X> Convert<X,ArrayList<LinkedHashMap<String, Object>>> convertToRowMajor(@SuppressWarnings("unchecked") MapRule<X>... rules) {
		return new Convert<X,ArrayList<LinkedHashMap<String, Object>>>() {
			@Override
			public ArrayList<LinkedHashMap<String, Object>> stream(Stream<X> stream) {
				return stream.collect(toRowMajorDataframe(rules));
			}
		};
	}
	
	public static <X> Convert<X,LinkedHashMap<String, Object>> convertToDataframe(Class<X> clazz) {
		return convertToColMajor(clazz);
	}
	
	public static <X> Convert<X,LinkedHashMap<String, Object>> convertToDataframe(@SuppressWarnings("unchecked") MapRule<X>... rules) {
		return convertToColMajor(rules);
	}
	
	public static <X> Convert<X,LinkedHashMap<String, Object>> convertToColMajor(Class<X> clazz) {
		return convertToColMajor(reflectionRules(clazz));
	}
	
	public static <X> Convert<X,LinkedHashMap<String, Object>> convertToColMajor(@SuppressWarnings("unchecked") MapRule<X>... rules) {
		return new Convert<X,LinkedHashMap<String, Object>>() {
			@Override
			public LinkedHashMap<String, Object> stream(Stream<X> stream) {
				return stream.collect(toColMajorDataframe(rules));
			}
		};
	}
	
	public static interface MapRule<Y> {
		String label();
		Function<Y,Object> rule();
	}
	
	public static <Z> MapRule<Z> mapping(final String label, final Method method) {
		return mapping(label, 
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
	
	public static <Z> MapRule<Z> mapping(final Class<Z> clazz, final String label, final Function<Z,Object> rule) {
		return mapping(label,rule);
	}
	
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
	
	public static <X> Collector<X,?,LinkedHashMap<String, Object>> toColMajorDataframe(@SuppressWarnings("unchecked") final MapRule<X>... rules) {
		return new Collector<X,LinkedHashMap<String, List<Object>>,LinkedHashMap<String, Object>>() {

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
			public Function<LinkedHashMap<String, List<Object>>, LinkedHashMap<String, Object>> finisher() {
				return a -> {
					LinkedHashMap<String, Object> out = new LinkedHashMap<String, Object>();
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
	
	public static <X> Collector<X,?,ArrayList<LinkedHashMap<String, Object>>> toRowMajorDataframe(@SuppressWarnings("unchecked") final MapRule<X>... rules) {
		return new Collector<X,ArrayList<LinkedHashMap<String, Object>>,ArrayList<LinkedHashMap<String, Object>>>() {

			@Override
			public Supplier<ArrayList<LinkedHashMap<String, Object>>> supplier() {
				return () -> new ArrayList<LinkedHashMap<String, Object>>();
			}

			@Override
			public BiConsumer<ArrayList<LinkedHashMap<String, Object>>, X> accumulator() {
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
			public BinaryOperator<ArrayList<LinkedHashMap<String, Object>>> combiner() {
				return (lhm,rhm) -> {
					lhm.addAll(rhm);
					return lhm;
				};
			}

			@Override
			public Function<ArrayList<LinkedHashMap<String, Object>>, ArrayList<LinkedHashMap<String, Object>>> finisher() {
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
}
