package at.sps.core.utils;

public interface ParamFuncCB< I, O > {

  /**
   * Call this "virtual" method using call(), I is input O is output
   * @param val Input value I
   * @return Output value O
   */
  O call( I val );

}
