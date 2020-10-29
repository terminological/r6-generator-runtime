package uk.co.terminological.rjava;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import uk.co.terminological.rjava.types.RNull;
import uk.co.terminological.rjava.types.RObject;



/**
 * The MapRule interface allows a label to be associated with a data mapping
 * @author terminological
 *
 * @param <Y> - the input data type that will be mapped
 */
public interface MapRule<Y> {
	String label();
	Function<Y,Object> rule();
	@SuppressWarnings("unchecked")
	static <X> MapRule<X>[] reflectionRules(Class<X> clazz) {
		List<MapRule<X>> out = new ArrayList<>();
		for (Method method: clazz.getMethods()) {
			if (method.isAccessible() &&
					method.getParameterCount() == 0 
					&& (
							ConvertibleTypes.contains(method.getReturnType())
							|| RObject.class.isAssignableFrom(method.getReturnType()) 
//									&& RDataframe.supports(method.getGenericParameterTypes()[0])
//								)
//							|| (Map.class.isAssignableFrom(method.getReturnType()) 
//									&& RDataframe.supports(method.getGenericParameterTypes()[0])
//									&& RDataframe.supports(method.getGenericParameterTypes()[1])
//								)
						)
							// || supportedArrayOutputs.contains(method.getReturnType())
					) {
				out.add(MapRule.mapping(method));
			}
		}
		return (MapRule<X>[]) out.toArray(new MapRule[out.size()]);
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
	static <Z> MapRule<Z> mapping(final String label, final Function<Z,Object> rule) {
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
	static <Z> MapRule<Z> mapping(final Class<Z> clazz, final String label, final Function<Z,Object> rule) {
		return MapRule.mapping(label,rule);
	}
	/**
	 * generates a mapping using reflection from a method specification
	 * @param method - the java method
	 * @return - a mapping that associates the method name with the method. 
	 */
	static <Z> MapRule<Z> mapping(final Method method) {
		return MapRule.mapping(method.getName(), 
				new Function<Z,Object>() {
					@Override
					public RObject apply(Z t) {
						try {
							return RConverter.convertObject(method.invoke(t));
						} catch (IllegalAccessException | IllegalArgumentException |InvocationTargetException e) {
							throw new RuntimeException(e);
						} catch (UnconvertableTypeException e) {
							return new RNull();
						}
					}
		});
	}
	
	static List<Class<?>> ConvertibleTypes = Arrays.asList(
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
}