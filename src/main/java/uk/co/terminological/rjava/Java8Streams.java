package uk.co.terminological.rjava;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.co.terminological.rjava.types.RPrimitive;
import uk.co.terminological.rjava.types.RVector;

public class Java8Streams {

	/**
	 * Create a stream from a potentially null singleton or array
	 * @param t a set of values
	 * @return a stream
	 */
	@SafeVarargs
	public static <T> Stream<T> maybe(T... t) {
	    return t == null ? Stream.empty() : Stream.of(t);
	}
	
	/**
	 * Create a stream from a potentially null collection
	 * @param t a collection of values
	 * @return a stream
	 */
	public static <T> Stream<T> maybe(Collection<T> list) {
	    return list == null ? Stream.empty() : list.stream();
	}
	
	/**
	 * Create a stream from a potentially null stream
	 * @param t a stream
	 * @return a stream
	 */
	public static <T> Stream<T> maybe(Stream<T> stream) {
	    return stream == null ? Stream.empty() : stream;
	}
	
	/**
	 * Create a stream from a potentially null optional
	 * @param t an optional value
	 * @return a stream
	 */
	public static <T> Stream<T> maybe(Optional<T> t) {
		return t.map(o -> Stream.of(o)).orElse(Stream.empty());
	}
	
	/**
	 * Create a stream from a potentially null iterable
	 * @param t an iterable of values
	 * @return a stream
	 */
	public static <T> Stream<T> maybe(Iterable<T> iter) {
		return iter == null ? Stream.empty() : StreamSupport.stream(iter.spliterator(), false );
	}
	
	/**
	 * Create a stream from a potentially null iterator
	 * @param t an iterator
	 * @return a stream
	 */
	public static <T> Stream<T> maybe(Iterator<T> iter) {
		return iter == null ? Stream.empty() : StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                    iter,
                    Spliterator.ORDERED)
            , false);
	}
	
	/**
	 * Create a stream from a potentially null or NA valued RPrimitive
	 * @param t an RPrimitive
	 * @return a stream of the underlying java object omitting NA values
	 */
	public static <T> Stream<T> maybe(RPrimitive t) {
	    return t == null ? Stream.empty() : maybe(t.opt());
	}
	
	/**
	 * Create a stream from a potentially null or NA valued RVector
	 * @param t an RVector
	 * @return a stream of the underlying java object omitting NA values
	 */
	public static <U extends RPrimitive, T> Stream<T> maybe(RVector<U> t) {
	    return t == null ? Stream.empty() : t.stream().flatMap(u -> maybe(u.opt()));
	}
}
