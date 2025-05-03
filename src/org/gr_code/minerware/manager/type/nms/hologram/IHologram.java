package org.gr_code.minerware.manager.type.nms.hologram;

import java.util.List;
import java.util.UUID;

public interface IHologram {

    void spawnAll();

    void destroyAll();

    void update();

    UUID owner();

    List<?> getStands();

    void setOwner(UUID uuid);

}
