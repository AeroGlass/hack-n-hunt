package mil.nga.geopackage.test.attributes;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * Test Attributes from a created database
 *
 * @author osbornb
 */
public class AttributesCreateTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public AttributesCreateTest() {

    }

    /**
     * Test reading
     *
     * @throws SQLException
     */
    public void testRead() throws SQLException {

        AttributesUtils.testRead(geoPackage);

    }

    /**
     * Test updating
     *
     * @throws SQLException
     */
    public void testUpdate() throws SQLException {

        AttributesUtils.testUpdate(geoPackage);

    }

    /**
     * Test creating
     *
     * @throws SQLException
     */
    public void testCreate() throws SQLException {

        AttributesUtils.testCreate(geoPackage);

    }

    /**
     * Test deleting
     *
     * @throws SQLException
     */
    public void testDelete() throws SQLException {

        AttributesUtils.testDelete(geoPackage);

    }

}
