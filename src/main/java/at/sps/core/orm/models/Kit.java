package at.sps.core.orm.models;

import at.sps.core.orm.MappableModel;
import at.sps.core.orm.MapperColumn;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Kit extends MappableModel {

  @Getter @Setter
  @MapperColumn( key = true, length = "128" )
  private String title;

  @Getter @Setter
  @MapperColumn
  private String description;

  @Getter @Setter
  @MapperColumn
  private long cooldown;

  @Getter @Setter
  @MapperColumn
  private int stackCount;

  @Getter
  @MapperColumn
  private List< ItemStack > contents;

  /**
   * Create a new kit that players can use after the cooldown has been completed on
   * a regular basis, this contains a title, a description, the cooldown in milliseconds
   * and a list of items that will be given to the user on request
   * @param title Title of the kit
   * @param description Description of the kit
   * @param cooldown Cooldown between uses in milliseconds
   * @param contents List of items this kit contains
   */
  public Kit( String title, String description, long cooldown, List< ItemStack > contents ) {
    this.title = title;
    this.description = description;
    this.cooldown = cooldown;
    this.contents = contents;
    this.stackCount = contents.size();
  }

  /**
   * Used to set the contents (items) that this kit will provide to
   * the player, which also sets the stackCount property automatically
   * @param contents New list of items
   */
  public void setContents( List< ItemStack > contents ) {
    this.contents = contents;
    this.stackCount = contents.size();
  }
}
