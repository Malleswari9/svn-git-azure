package java.sql;

import java.util.Map;

public interface Struct {

  /**
   * Retrieves the SQL type name of the SQL structured type
   * that this <code>Struct</code> object represents.
   *
   * @return the fully-qualified type name of the SQL structured
   *         type for which this <code>Struct</code> object
   *         is the generic representation
   * @throws SQLException if a database access error occurs
   * @throws SQLFeatureNotSupportedException
   *                      if the JDBC driver does not support
   *                      this method
   * @since 1.2
   */
  String getSQLTypeName() throws SQLException;

  /**
   * Produces the ordered values of the attributes of the SQL
   * structured type that this <code>Struct</code> object represents.
   * As individual attributes are processed, this method uses the type map
   * associated with the
   * connection for customizations of the type mappings.
   * If there is no
   * entry in the connection's type map that matches the structured
   * type that an attribute represents,
   * the driver uses the standard mapping.
   * <p/>
   * Conceptually, this method calls the method
   * <code>getObject</code> on each attribute
   * of the structured type and returns a Java array containing
   * the result.
   *
   * @return an array containing the ordered attribute values
   * @throws SQLException if a database access error occurs
   * @throws SQLFeatureNotSupportedException
   *                      if the JDBC driver does not support
   *                      this method
   * @since 1.2
   */
  Object[] getAttributes() throws SQLException;

  /**
   * Produces the ordered values of the attributes of the SQL
   * structured type that this <code>Struct</code> object represents.
   * As individual attrbutes are proccessed, this method uses the given type map
   * for customizations of the type mappings.
   * If there is no
   * entry in the given type map that matches the structured
   * type that an attribute represents,
   * the driver uses the standard mapping. This method never
   * uses the type map associated with the connection.
   * <p/>
   * Conceptually, this method calls the method
   * <code>getObject</code> on each attribute
   * of the structured type and returns a Java array containing
   * the result.
   *
   * @param map a mapping of SQL type names to Java classes
   * @return an array containing the ordered attribute values
   * @throws SQLException if a database access error occurs
   * @throws SQLFeatureNotSupportedException
   *                      if the JDBC driver does not support
   *                      this method
   * @since 1.2
   */
  Object[] getAttributes(Map<String, Class<?>> map)
      throws SQLException;
}


