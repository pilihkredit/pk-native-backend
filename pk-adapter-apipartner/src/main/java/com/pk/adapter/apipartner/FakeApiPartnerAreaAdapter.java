package com.pk.adapter.apipartner;

import com.pk.core.reference.AreaReference;
import com.pk.core.reference.port.LenderAreaPort;
import java.util.List;

public class FakeApiPartnerAreaAdapter implements LenderAreaPort {
    @Override
    public List<AreaReference> listAreas(String parentCode) {
        if (parentCode == null || parentCode.isBlank()) {
            return List.of(
                    new AreaReference("110000", "Jakarta", "", 1),
                    new AreaReference("320000", "Jawa Barat", "", 1)
            );
        }
        return switch (parentCode.trim()) {
            case "110000" -> List.of(
                    new AreaReference("110100", "Jakarta Selatan", "110000", 2),
                    new AreaReference("110200", "Jakarta Pusat", "110000", 2)
            );
            case "110100" -> List.of(
                    new AreaReference("110101", "Kebayoran Baru", "110100", 3),
                    new AreaReference("110102", "Cilandak", "110100", 3)
            );
            case "320000" -> List.of(
                    new AreaReference("320100", "Bandung", "320000", 2)
            );
            case "320100" -> List.of(
                    new AreaReference("320101", "Coblong", "320100", 3)
            );
            default -> List.of();
        };
    }
}
