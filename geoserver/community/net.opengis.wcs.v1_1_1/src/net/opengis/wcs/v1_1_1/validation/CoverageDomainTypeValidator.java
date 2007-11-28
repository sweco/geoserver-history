/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wcs.v1_1_1.validation;

import net.opengis.wcs.v1_1_1.SpatialDomainType;
import net.opengis.wcs.v1_1_1.TimeSequenceType;

/**
 * A sample validator interface for {@link net.opengis.wcs.v1_1_1.CoverageDomainType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface CoverageDomainTypeValidator {
    boolean validate();

    boolean validateSpatialDomain(SpatialDomainType value);
    boolean validateTemporalDomain(TimeSequenceType value);
}
