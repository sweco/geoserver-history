/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.ows.v1_1_0.validation;


/**
 * A sample validator interface for {@link net.opengis.ows.v1_1_0.AbstractReferenceBaseType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface AbstractReferenceBaseTypeValidator {
    boolean validate();

    boolean validateActuate(Object value);
    boolean validateArcrole(Object value);
    boolean validateHref(Object value);
    boolean validateRole(Object value);
    boolean validateShow(Object value);
    boolean validateTitle(Object value);
    boolean validateType(String value);
}
