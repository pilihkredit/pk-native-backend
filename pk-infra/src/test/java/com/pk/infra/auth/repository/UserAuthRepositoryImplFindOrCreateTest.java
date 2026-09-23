package com.pk.infra.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.auth.UserProfileSummary;
import com.pk.infra.auth.mapper.UserAuthMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAuthRepositoryImplFindOrCreateTest {

    @Mock
    private UserAuthMapper mapper;

    private UserAuthRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new UserAuthRepositoryImpl(mapper);
    }

    @Test
    void returnsActiveWithoutTouchingClosedRow() {
        when(mapper.findByMobileNo("81234567815"))
                .thenReturn(new UserProfileSummary(10L, "UACTIVE", "81234567815", false));

        UserProfileSummary result = repository.findOrCreateActiveByMobileNo("81234567815");

        assertThat(result.userId()).isEqualTo(10L);
        assertThat(result.partnerUserId()).isEqualTo("UACTIVE");
        verify(mapper, never()).findLatestClosedByMobileNoForUpdate(anyString());
        verify(mapper, never()).insertProfile(anyString(), anyString());
    }

    @Test
    void createsFreshPartnerUserIdWhenOnlyClosedProfileExists() {
        when(mapper.findByMobileNo("81234567815"))
                .thenReturn(null, null, new UserProfileSummary(200L, "UNEW123456789", "81234567815", false));
        when(mapper.findLatestClosedByMobileNoForUpdate("81234567815"))
                .thenReturn(new UserProfileSummary(174L, "UABC", "81234567815", false));
        when(mapper.insertProfile(anyString(), org.mockito.ArgumentMatchers.eq("81234567815"))).thenReturn(1);

        UserProfileSummary result = repository.findOrCreateActiveByMobileNo("81234567815");

        assertThat(result.newlyCreated()).isTrue();
        assertThat(result.userId()).isEqualTo(200L);
        assertThat(result.partnerUserId()).isEqualTo("UNEW123456789");
        verify(mapper, never()).relinquishPartnerUserId(org.mockito.ArgumentMatchers.anyLong(), anyString());
        verify(mapper).insertProfile(anyString(), org.mockito.ArgumentMatchers.eq("81234567815"));
    }

    @Test
    void createsFreshPartnerUserIdWhenNoClosedProfile() {
        when(mapper.findByMobileNo("81234567815"))
                .thenReturn(null, null, new UserProfileSummary(300L, "UNEW123456789", "81234567815", false));
        when(mapper.findLatestClosedByMobileNoForUpdate("81234567815")).thenReturn(null);
        when(mapper.insertProfile(anyString(), anyString())).thenReturn(1);

        UserProfileSummary result = repository.findOrCreateActiveByMobileNo("81234567815");

        assertThat(result.newlyCreated()).isTrue();
        assertThat(result.userId()).isEqualTo(300L);
        assertThat(result.partnerUserId()).startsWith("U");
        verify(mapper, never()).relinquishPartnerUserId(org.mockito.ArgumentMatchers.anyLong(), anyString());
        verify(mapper).insertProfile(anyString(), org.mockito.ArgumentMatchers.eq("81234567815"));
    }
}
