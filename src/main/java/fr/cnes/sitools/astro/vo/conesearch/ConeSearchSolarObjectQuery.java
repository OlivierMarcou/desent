 /*******************************************************************************
 * Copyright 2010-2014 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of SITools2.
 *
 * SITools2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SITools2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.cnes.sitools.astro.vo.conesearch;

import fr.cnes.sitools.astro.representation.GeoJsonRepresentation;
import fr.cnes.sitools.extensions.astro.application.opensearch.datamodel.FeatureDataModel;
import fr.cnes.sitools.extensions.astro.application.opensearch.datamodel.FeaturesDataModel;
import fr.cnes.sitools.extensions.common.AstroCoordinate;
import fr.cnes.sitools.extensions.common.AstroCoordinate.CoordinateSystem;
import fr.cnes.sitools.extensions.common.Utility;
import healpix.core.HealpixIndex;
import healpix.essentials.Pointing;
import healpix.essentials.Scheme;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.ivoa.xml.votable.v1.Field;

import org.restlet.data.Status;
 
import org.restlet.resource.ResourceException;

/**
 * This object contains methods to search solar objects in both Equatorial or galactic reference frame.
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class ConeSearchSolarObjectQuery implements ConeSearchQueryInterface {

  /**
   * Logger.
   */
  private static final Logger LOG = Logger.getLogger(ConeSearchSolarObjectQuery.class.getName());
  /**
   * Query.
   */
  private final transient ConeSearchQuery query;
  /**
   * Right ascension.
   */
  private transient double rightAscension;
  /**
   * Declination.
   */
  private transient double declination;
  /**
   * Radius in decimal degree.
   */
  private final transient double radius;
  /**
   * URL to query the WS.
   */
  private static final String URL = "http://vo.imcce.fr/webservices/skybot/skybotconesearch_query.php?from=SITools2&EPOCH=<EPOCH>&";
  /**
   * Max value in degree of declination axis.
   */
  private static final double MAX_DEC = 90.;
  /**
   * One degree.
   */
  private static final double ONE_DEG = 1.0;
  /**
   * One degree in arsec.
   */
  private static final double ONE_DEG_IN_ARSEC = 3600.;
  /**
   * Conversion arcsec to degree.
   */
  private static final double ARCSEC_2_DEG = ONE_DEG / ONE_DEG_IN_ARSEC;
  /**
   * Multiplation factor to embed the entire Healpix pixel in the ROI.
   */
  private static final double MULT_FACT = 1.5;
  /**
   * Healpix is not defined.
   */
  private static final int NOT_DEFINED = -1;
  /**
   * Pixel to check.
   */
  private final transient long pixelToCheck;
  /**
   * Healpix index.
   */
  private transient HealpixIndex healpixIndex = null;
  /**
   * Coordinate system in which the input is given and response is returned.
   */
  private final transient CoordinateSystem coordSystem;

  /**
   * keywords.
   */
  public enum ReservedWords {

    /**
     * RA.
     */
    POS_EQ_RA_MAIN("pos.eq.ra;meta.main"),
    /**
     * DEC.
     */
    POS_EQ_DEC_MAIN("pos.eq.dec;meta.main"),
    /**
     * ID.
     */
    IMAGE_TITLE("meta.id;meta.main"),
    /**
     * Type.
     */
    CLASS("meta.code.class;src.class"),
    /**
     * None.
     */
    NONE(null);
    /**
     * ucd name.
     */
    private final String name;

    /**
     * Constructor.
     *
     * @param nameVal ucd name
     */
    ReservedWords(final String nameVal) {
      this.name = nameVal;
    }

    /**
     * Returns the ucd name.
     *
     * @return the ucd name
     */
    public final String getName() {
      return this.name;
    }

    /**
     * Returns the enumeration from the ucd name.
     *
     * @param keyword ucd name.
     * @return the enumeration
     */
    public static ReservedWords find(final String keyword) {
      ReservedWords response = ReservedWords.NONE;
      final ReservedWords[] words = ReservedWords.values();
        for (ReservedWords word : words) {
            final String reservedName = word.getName();
            if (keyword.equals(reservedName)) {
                response = word;
                break;
            }
        }
      return response;
    }
  }

  /**
   * Constructs a query.
   *
   * When the reference frame is in galactic, the inputs are transformed in equatorial frame
   * to query the external web service that takes coordinates in equatorial frame.
   *
   * @param raVal right ascension
   * @param decVal declination
   * @param radiusVal radius in degree
   * @param timeVal time
   * @param coordSystemVal coordinates system of (raVal, decVal)
   */
  public ConeSearchSolarObjectQuery(final double raVal, final double decVal, final double radiusVal, final String timeVal, final CoordinateSystem coordSystemVal) {
    this.query = new ConeSearchQuery(URL.replace("<EPOCH>", timeVal));
    this.rightAscension = raVal;
    this.declination = decVal;
    this.radius = radiusVal;
    this.pixelToCheck = NOT_DEFINED;
    this.coordSystem = coordSystemVal;
    transformToCelestialCoordinates();
  }

  /**
   * Constructs a query.
   *
   * When the reference frame is in galactic, the inputs are transformed in equatorial frame
   * to query the external web service that takes coordinates in equatorial frame.
   *
   * @param healpix healpix
   * @param order helapix order
   * @param time time
   * @param coordSystemVal Coordinate system of the Healpix pixel.
   * @throws Exception Exception
   */
  public ConeSearchSolarObjectQuery(final long healpix, final int order, final String time, final CoordinateSystem coordSystemVal) throws Exception {
    final int nside = (int) Math.pow(2, order);
    final double pixRes = HealpixIndex.getPixRes(nside);
    this.healpixIndex = new HealpixIndex(nside, Scheme.NESTED);
    this.pixelToCheck = healpix;
    final Pointing pointing = healpixIndex.pix2ang(healpix);
    this.rightAscension = Math.toDegrees(pointing.phi);
    this.declination = MAX_DEC - Math.toDegrees(pointing.theta);
    this.radius = pixRes * ARCSEC_2_DEG * MULT_FACT;
    this.query = new ConeSearchQuery(URL.replace("<EPOCH>", time));
    this.coordSystem = coordSystemVal;
    transformToCelestialCoordinates();
  }
  /**
   * Transforms coordinates in Equatorial when the reference frame is in Galactic.
   */
  private void transformToCelestialCoordinates() {
      if (this.coordSystem == CoordinateSystem.GALACTIC) {
        final AstroCoordinate astroCoordinates = new AstroCoordinate(this.rightAscension, this.declination);
        astroCoordinates.transformTo(CoordinateSystem.EQUATORIAL);
        this.rightAscension = astroCoordinates.getRaAsDecimal();
        this.declination = astroCoordinates.getDecAsDecimal();
      }
  }

  @Override
  public final GeoJsonRepresentation getResponse() {
    final FeaturesDataModel features = new FeaturesDataModel();
    List<Map<Field, String>> response;
    try {
      response = query.getResponseAt(rightAscension, declination, radius);
    } catch (Exception ex) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No data found");
    }
    if (response == null || response.isEmpty()) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No data found");
    }

    for (Map<Field, String> record : response) {
      parseRecord(features, record);
    }

    return new GeoJsonRepresentation(features.getFeatures());
  }

  /**
   * Parses Record and writes the response in features.
   *
   * @param features the response
   * @param record the records to set
   */
  protected final void parseRecord(final FeaturesDataModel features, final Map<Field, String> record) {
    final FeatureDataModel feature = new FeatureDataModel();
    String coordinatesRa = null;
    String coordinatesDec = null;

    for (Map.Entry<Field, String> entryField : record.entrySet()) {
      final Field field = entryField.getKey();
      final String ucd = field.getUcd();
      final net.ivoa.xml.votable.v1.DataType dataType = field.getDatatype();
      final String value = entryField.getValue();
      if (Utility.isSet(ucd) && Utility.isSet(value) && !value.isEmpty()) {
        final Object response = Utility.getDataType(dataType, value);
        final ReservedWords ucdWord = ReservedWords.find(ucd);
        switch (ucdWord) {
          case POS_EQ_RA_MAIN:
            coordinatesRa = record.get(field);
            break;
          case POS_EQ_DEC_MAIN:
            coordinatesDec = record.get(field);
            break;
          case IMAGE_TITLE:
            final String identifier = record.get(field);
            feature.setIdentifier(identifier);
            feature.addProperty("seeAlso", String.format("http://vizier.u-strasbg.fr/cgi-bin/VizieR-5?-source=B/astorb/astorb&Name===%s", identifier));
            feature.addProperty("credits", "IMCCE");
            break;
          case CLASS:
            feature.addProperty("type", response);
            break;
          default:
            feature.addProperty(field.getName(), response);
            break;
        }
      } else {
        final Object response = Utility.getDataType(dataType, value);
        feature.addProperty(field.getName(), response);
      }
    }
    final AstroCoordinate astroCoordinate = new AstroCoordinate(coordinatesRa, coordinatesDec);
    // if we ask the calactic fram, we need to convert because the webservice returns coordinates
    // in equatorial frame.
    if (this.coordSystem == CoordinateSystem.GALACTIC) {
        astroCoordinate.transformTo(CoordinateSystem.GALACTIC);
    }
    feature.createGeometry(String.format("[%s,%s]", astroCoordinate.getRaAsDecimal(), astroCoordinate.getDecAsDecimal()), "Point");
    feature.createCrs(this.coordSystem.getCrs());
    if (isValid(feature)) {
      if (isPointIsInsidePixel(astroCoordinate.getRaAsDecimal(), astroCoordinate.getDecAsDecimal())) {
        features.addFeature(feature);
      } else {
        feature.clear();
      }
    } else {
      LOG.log(Level.WARNING, "{0} does not have an identifier. Also, this record is ignored.", feature.toString());
      feature.clear();
    }
  }

  /**
   * Returns true when the point (rightAscension,declination) is inside the pixel otherwise false.
   *
   * @param raSolarObj right ascension in degree
   * @param decSolarObj declination in degree
   * @return true when the point (rightAscension,declination) is inside the pixel otherwise false
   */
  private boolean isPointIsInsidePixel(final double raSolarObj, final double decSolarObj) {
    boolean result;
    try {
      final long healpixFromService = this.healpixIndex.ang2pix_nest(Math.PI / 2 - Math.toRadians(decSolarObj), Math.toRadians(raSolarObj));
      result = healpixFromService == this.pixelToCheck;
    } catch (Exception ex) {
      result = false;
      LOG.log(Level.WARNING, null, ex);
    }
    return result;
  }

  /**
   * Returns true when identifier is set.
   *
   * @param feature data model
   * @return true when identifier os set
   */
  private boolean isValid(final FeatureDataModel feature) {
    return feature.getProperties().containsKey("identifier");
  }
}
