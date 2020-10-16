package uk.co.terminological.rjava.types;

/**
 * Thing list R lists or named lists are collections in a loose sense of the term
 * as they can be iterated over. R has a odd (from a java perspective) approach to 
 * being able to name almost anything. RCollections are either Lists or Maps in the 
 * java implementations, but can contain any R derived structure.
 * 
 * @author terminological
 *
 * @param <X>
 */
public interface RCollection<X extends RObject> extends RObject,Iterable<X> {

}