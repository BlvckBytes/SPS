package at.sps.core.storage;

public enum ActionResult {

  // Inserting / updating was successful
  OK,

  // No data to write provided
  NO_DATA,

  // An internal error occured
  INTERNAL_ERROR,

  // Data with the key new values already exists
  ALREADY_EXISTENT,

  // This data didn't exist
  NON_EXISTENT

}
