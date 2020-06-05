package at.sps.core.utils;

public interface ParamCall< T > {

  /**
   * Callback void with the possed arg
   * @param val Object to pass in callback
   */
  void call( T val );

}
