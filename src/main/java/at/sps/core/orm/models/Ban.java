package at.sps.core.orm.models;

import at.sps.core.orm.MappableModel;
import at.sps.core.orm.MapperColumn;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ban extends MappableModel {

  @Getter @Setter
  @MapperColumn( length = "40", key = true )
  private UUID holder;

  @Getter @Setter
  @MapperColumn
  private UUID creator;

  @Getter @Setter
  @MapperColumn
  private boolean permanent;

  @Getter @Setter
  @MapperColumn( key = true )
  private long creationDate;

  @Getter @Setter
  @MapperColumn
  private long expireDate;

  @Getter @Setter
  @MapperColumn
  private List< String > ipAddresses;

  @Getter @Setter
  @MapperColumn
  private String reason;

  /**
   * Create a new permanent ban with a ban-reason for a player
   * @param holder Player to target this ban to
   * @param creator Player that executed this ban
   * @param reason Reason of the ban
   */
  public Ban( UUID holder, UUID creator, String reason ) {
    this.ipAddresses = new ArrayList<>();
    this.creationDate = System.currentTimeMillis();
    this.holder = holder;
    this.creator = creator;
    this.reason = reason;
    this.permanent = true;
  }

  /**
   * Create a new temporary ban with a ban-reason for a player
   * @param holder Player to target this ban to
   * @param creator Player that executed this ban
   * @param reason Reason of the ban
   * @param expireDate Timestamp of expiry
   */
  public Ban( UUID holder, UUID creator, String reason, long expireDate ) {
    this( holder, creator, reason );
    this.permanent = false;
    this.expireDate = expireDate;
  }

  /**
   * Create a new ban with all parameters (for loading from DB)
   * @param holder Player to target this ban to
   * @param creator Player that executed this ban
   * @param reason Reason of the ban
   * @param creationDate Timestamp of creation
   * @param expireDate Timestamp of expiry
   * @param permanent Whether or not this ban is permanent
   * @param ipAddresses List of ip addresses this ban includes
   */
  public Ban( UUID holder, UUID creator, String reason, long creationDate, long expireDate, boolean permanent, List< String > ipAddresses ) {
    this( holder, creator, reason, expireDate );
    this.creationDate = creationDate;
    this.expireDate = expireDate;
    this.permanent = permanent;
    this.ipAddresses = ipAddresses;
  }

  /**
   * Check whether or not this ban is still active
   * @return True if active, false otherwise
   */
  public boolean isActive() {
    if( this.permanent )
      return true;

    return expireDate - System.currentTimeMillis() > 0;
  }
}
