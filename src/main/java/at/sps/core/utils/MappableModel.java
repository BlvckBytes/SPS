package at.sps.core.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public abstract class MappableModel< T > {
  
  public abstract List< T > read( ResultSet rs ) throws Exception;

  public abstract PreparedStatement write( Connection conn ) throws Exception;
}
