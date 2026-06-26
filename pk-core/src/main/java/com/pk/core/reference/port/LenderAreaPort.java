package com.pk.core.reference.port;

import com.pk.core.reference.AreaReference;
import java.util.List;

public interface LenderAreaPort {
    List<AreaReference> listAreas(String parentCode);
}
